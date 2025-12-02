from flask import Flask, render_template_string, request, send_file, jsonify
import pyttsx3
import os
import subprocess
import wave
import re
import time
from PIL import Image, ImageDraw

app = Flask(__name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_DIR = os.path.join(BASE_DIR, "outputs")
os.makedirs(OUTPUT_DIR, exist_ok=True)

# 장면별 스타일 정의 (배경색, 표정)
SCENE_STYLES = [
    {"bg": (30, 30, 50), "mouth": "smile", "eyes": "normal"},      # 기본
    {"bg": (50, 30, 60), "mouth": "open", "eyes": "wide"},         # 놀람
    {"bg": (30, 50, 40), "mouth": "smile_big", "eyes": "happy"},   # 행복
    {"bg": (60, 40, 30), "mouth": "talk", "eyes": "normal"},       # 말하기
    {"bg": (40, 30, 60), "mouth": "think", "eyes": "look_up"},     # 생각
    {"bg": (30, 40, 60), "mouth": "excited", "eyes": "sparkle"},   # 신남
]

HTML_TEMPLATE = '''
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🎬 AI 숏폼 생성기 MVP</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
            min-height: 100vh; color: #fff; padding: 20px;
        }
        .container { max-width: 900px; margin: 0 auto; }
        h1 { text-align: center; margin-bottom: 30px; font-size: 2.5em; }
        .main-content { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
        .input-section, .output-section {
            background: rgba(255,255,255,0.1); border-radius: 20px;
            padding: 25px; backdrop-filter: blur(10px);
        }
        h2 { margin-bottom: 15px; font-size: 1.3em; }
        textarea {
            width: 100%; height: 200px; padding: 15px; border: none;
            border-radius: 12px; font-size: 16px; resize: vertical;
            background: rgba(255,255,255,0.9); color: #333;
        }
        button {
            width: 100%; padding: 15px 30px; margin-top: 15px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none; border-radius: 12px; color: #fff;
            font-size: 18px; font-weight: bold; cursor: pointer;
        }
        button:hover { transform: translateY(-2px); box-shadow: 0 10px 30px rgba(102,126,234,0.4); }
        button:disabled { opacity: 0.6; cursor: not-allowed; }
        .status { margin-top: 15px; padding: 12px; border-radius: 8px; background: rgba(255,255,255,0.1); text-align: center; }
        video { width: 100%; border-radius: 12px; background: #000; }
        .examples { margin-top: 20px; }
        .example-btn {
            display: block; width: 100%; padding: 10px; margin: 8px 0;
            background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2);
            border-radius: 8px; color: #fff; font-size: 14px; cursor: pointer; text-align: left;
        }
        .loading { display: none; text-align: center; padding: 40px; }
        .spinner { width: 50px; height: 50px; border: 4px solid rgba(255,255,255,0.3); border-top-color: #667eea; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 15px; }
        @keyframes spin { to { transform: rotate(360deg); } }
        .tip { margin-top: 15px; padding: 10px; background: rgba(102,126,234,0.2); border-radius: 8px; font-size: 13px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🎬 AI 애니메이션 숏폼 생성기</h1>
        <div class="main-content">
            <div class="input-section">
                <h2>📝 대본 입력</h2>
                <textarea id="script" placeholder="문장을 마침표(.)나 느낌표(!)로 구분하면 장면이 전환됩니다."></textarea>
                <button id="generateBtn" onclick="generateVideo()">🎥 영상 생성</button>
                <div class="status" id="status">대본을 입력하고 버튼을 눌러주세요</div>
                <div class="tip">💡 팁: 문장마다 다른 표정과 배경이 적용됩니다!</div>
                <div class="examples">
                    <h3 style="font-size:1em;margin-bottom:10px;opacity:0.8;">💡 예시 대본 (장면 전환)</h3>
                    <button class="example-btn" onclick="setExample(this)">안녕하세요! 오늘은 특별한 이야기를 들려드릴게요. 정말 재미있을 거예요!</button>
                    <button class="example-btn" onclick="setExample(this)">옛날 옛적에 작은 마을이 있었어요. 그 마을에는 용감한 소년이 살았습니다. 어느 날 신비한 일이 벌어졌죠!</button>
                </div>
            </div>
            <div class="output-section">
                <h2>🎬 생성된 영상</h2>
                <div class="loading" id="loading"><div class="spinner"></div><p>장면별 영상 생성 중...</p></div>
                <video id="videoPlayer" controls style="display:none;"></video>
                <div id="placeholder" style="height:300px;display:flex;align-items:center;justify-content:center;background:rgba(0,0,0,0.3);border-radius:12px;">
                    <p style="opacity:0.6;">영상이 여기에 표시됩니다</p>
                </div>
            </div>
        </div>
    </div>
    <script>
        function setExample(btn) { document.getElementById('script').value = btn.textContent; }
        async function generateVideo() {
            const script = document.getElementById('script').value.trim();
            if (!script) { alert('대본을 입력해주세요!'); return; }
            const btn = document.getElementById('generateBtn');
            const status = document.getElementById('status');
            const loading = document.getElementById('loading');
            const video = document.getElementById('videoPlayer');
            const placeholder = document.getElementById('placeholder');
            btn.disabled = true;
            status.textContent = '🔄 장면별 영상 생성 중...';
            loading.style.display = 'block';
            video.style.display = 'none';
            placeholder.style.display = 'none';
            try {
                const response = await fetch('/generate', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ script: script })
                });
                const data = await response.json();
                if (data.success) {
                    status.textContent = '✅ ' + data.message;
                    video.src = '/video?t=' + Date.now();
                    video.style.display = 'block';
                    video.load();
                } else {
                    status.textContent = '❌ ' + data.message;
                    placeholder.style.display = 'flex';
                }
            } catch (error) {
                status.textContent = '❌ 오류: ' + error.message;
                placeholder.style.display = 'flex';
            }
            btn.disabled = false;
            loading.style.display = 'none';
        }
    </script>
</body>
</html>
'''

def create_scene_image(scene_idx, text=""):
    """장면별 다른 스타일의 캐릭터 이미지 생성"""
    style = SCENE_STYLES[scene_idx % len(SCENE_STYLES)]
    img = Image.new('RGB', (720, 1280), color=style["bg"])
    draw = ImageDraw.Draw(img)
    cx, cy = 360, 500
    
    # 얼굴
    draw.ellipse([cx-150, cy-150, cx+150, cy+150], fill=(255, 220, 180), outline=(200, 170, 130), width=3)
    
    # 눈 스타일
    if style["eyes"] == "wide":
        draw.ellipse([cx-80, cy-60, cx-20, cy], fill=(50, 50, 50))
        draw.ellipse([cx+20, cy-60, cx+80, cy], fill=(50, 50, 50))
        draw.ellipse([cx-65, cy-50, cx-40, cy-25], fill=(255, 255, 255))
        draw.ellipse([cx+35, cy-50, cx+60, cy-25], fill=(255, 255, 255))
    elif style["eyes"] == "happy":
        draw.arc([cx-70, cy-50, cx-30, cy-10], 0, 180, fill=(50, 50, 50), width=4)
        draw.arc([cx+30, cy-50, cx+70, cy-10], 0, 180, fill=(50, 50, 50), width=4)
    elif style["eyes"] == "look_up":
        draw.ellipse([cx-70, cy-70, cx-30, cy-30], fill=(50, 50, 50))
        draw.ellipse([cx+30, cy-70, cx+70, cy-30], fill=(50, 50, 50))
        draw.ellipse([cx-60, cy-65, cx-45, cy-50], fill=(255, 255, 255))
        draw.ellipse([cx+40, cy-65, cx+55, cy-50], fill=(255, 255, 255))
    elif style["eyes"] == "sparkle":
        draw.ellipse([cx-70, cy-50, cx-30, cy-10], fill=(50, 50, 50))
        draw.ellipse([cx+30, cy-50, cx+70, cy-10], fill=(50, 50, 50))
        # 반짝이 효과
        for offset in [(-50, -30), (50, -30)]:
            draw.polygon([(cx+offset[0], cy+offset[1]-10), (cx+offset[0]-5, cy+offset[1]), 
                         (cx+offset[0], cy+offset[1]+10), (cx+offset[0]+5, cy+offset[1])], fill=(255, 255, 255))
    else:
        draw.ellipse([cx-70, cy-50, cx-30, cy-10], fill=(50, 50, 50))
        draw.ellipse([cx+30, cy-50, cx+70, cy-10], fill=(50, 50, 50))
        draw.ellipse([cx-60, cy-45, cx-45, cy-30], fill=(255, 255, 255))
        draw.ellipse([cx+40, cy-45, cx+55, cy-30], fill=(255, 255, 255))
    
    # 입 스타일
    if style["mouth"] == "open":
        draw.ellipse([cx-50, cy+20, cx+50, cy+90], fill=(200, 100, 100))
        draw.ellipse([cx-30, cy+25, cx+30, cy+50], fill=(255, 200, 200))
    elif style["mouth"] == "smile_big":
        draw.arc([cx-60, cy+10, cx+60, cy+80], 0, 180, fill=(200, 100, 100), width=8)
    elif style["mouth"] == "talk":
        draw.ellipse([cx-40, cy+30, cx+40, cy+70], fill=(200, 100, 100))
    elif style["mouth"] == "think":
        draw.arc([cx-30, cy+40, cx+30, cy+70], 0, 180, fill=(200, 100, 100), width=4)
    elif style["mouth"] == "excited":
        draw.ellipse([cx-45, cy+25, cx+45, cy+85], fill=(200, 100, 100))
        draw.chord([cx-35, cy+30, cx+35, cy+55], 0, 180, fill=(255, 255, 255))
    else:
        draw.arc([cx-40, cy+30, cx+40, cy+60], 0, 180, fill=(200, 100, 100), width=5)
    
    # 볼터치
    draw.ellipse([cx-130, cy+10, cx-90, cy+50], fill=(255, 180, 180))
    draw.ellipse([cx+90, cy+10, cx+130, cy+50], fill=(255, 180, 180))
    
    return img

def split_sentences(text):
    """텍스트를 문장 단위로 분리"""
    sentences = re.split(r'[.!?。！？]+', text)
    return [s.strip() for s in sentences if s.strip()]

def text_to_speech(text, output_path):
    """텍스트를 음성으로 변환"""
    engine = pyttsx3.init()
    voices = engine.getProperty('voices')
    for voice in voices:
        if 'korean' in voice.name.lower() or 'ko' in voice.id.lower():
            engine.setProperty('voice', voice.id)
            break
    engine.setProperty('rate', 150)
    engine.setProperty('volume', 0.9)
    engine.save_to_file(text, output_path)
    engine.runAndWait()

@app.route('/')
def index():
    return render_template_string(HTML_TEMPLATE)

@app.route('/generate', methods=['POST'])
def generate():
    try:
        data = request.get_json()
        script = data.get('script', '').strip()
        if not script:
            return jsonify({'success': False, 'message': '대본을 입력해주세요'})
        
        sentences = split_sentences(script)
        if not sentences:
            sentences = [script]
        
        scene_videos = []
        total_duration = 0
        
        for i, sentence in enumerate(sentences):
            # 장면별 파일 경로
            scene_audio_aiff = os.path.join(OUTPUT_DIR, f"scene_{i}_audio.aiff")
            scene_audio_wav = os.path.join(OUTPUT_DIR, f"scene_{i}_audio.wav")
            scene_image = os.path.join(OUTPUT_DIR, f"scene_{i}_image.png")
            scene_video = os.path.join(OUTPUT_DIR, f"scene_{i}_video.mp4")
            
            # 장면 이미지 생성
            img = create_scene_image(i, sentence)
            img.save(scene_image)
            
            # TTS 음성 생성
            text_to_speech(sentence, scene_audio_aiff)
            time.sleep(0.3)
            
            if not os.path.exists(scene_audio_aiff):
                continue
            
            # aiff -> wav 변환
            subprocess.run(f'ffmpeg -y -i "{scene_audio_aiff}" "{scene_audio_wav}"', 
                          shell=True, capture_output=True)
            
            if not os.path.exists(scene_audio_wav) or os.path.getsize(scene_audio_wav) < 100:
                continue
            
            # duration 계산
            with wave.open(scene_audio_wav, 'rb') as wf:
                duration = wf.getnframes() / float(wf.getframerate())
                total_duration += duration
            
            # 장면 영상 생성
            video_cmd = f'ffmpeg -y -loop 1 -i "{scene_image}" -i "{scene_audio_wav}" -c:v libx264 -tune stillimage -c:a aac -b:a 192k -pix_fmt yuv420p -shortest "{scene_video}"'
            subprocess.run(video_cmd, shell=True, capture_output=True)
            
            if os.path.exists(scene_video) and os.path.getsize(scene_video) > 1000:
                scene_videos.append(scene_video)
        
        if not scene_videos:
            return jsonify({'success': False, 'message': '장면 생성 실패'})
        
        # 모든 장면 합치기
        output_video = os.path.join(OUTPUT_DIR, "output_video.mp4")
        
        if len(scene_videos) == 1:
            subprocess.run(f'cp "{scene_videos[0]}" "{output_video}"', shell=True)
        else:
            # concat 파일 생성
            concat_file = os.path.join(OUTPUT_DIR, "concat.txt")
            with open(concat_file, 'w') as f:
                for v in scene_videos:
                    f.write(f"file '{v}'\n")
            
            concat_cmd = f'ffmpeg -y -f concat -safe 0 -i "{concat_file}" -c copy "{output_video}"'
            subprocess.run(concat_cmd, shell=True, capture_output=True)
        
        if not os.path.exists(output_video) or os.path.getsize(output_video) < 1000:
            return jsonify({'success': False, 'message': '영상 합성 실패'})
        
        return jsonify({'success': True, 'message': f'영상 생성 완료! ({len(scene_videos)}개 장면, {total_duration:.1f}초)'})
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)})

@app.route('/video')
def video():
    video_path = os.path.join(OUTPUT_DIR, "output_video.mp4")
    if os.path.exists(video_path):
        return send_file(video_path, mimetype='video/mp4')
    return "Video not found", 404

if __name__ == '__main__':
    print("\n🎬 AI 숏폼 생성기 MVP (장면 전환 버전)")
    print("👉 브라우저에서 http://localhost:8080 접속하세요\n")
    app.run(debug=False, port=8080)
