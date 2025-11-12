const chatWelcome = document.getElementById('chatWelcome');
const chatContent = document.getElementById('chatContent');
const chatMessages = document.getElementById('chatMessages');
const messageInput = document.getElementById('messageInput');
const sendButton = document.getElementById('sendButton');

const userId = 'user_' + Math.random().toString(36).substr(2, 9);
let conversationId = null;

function startChat() {
    chatWelcome.classList.add('hidden');
    chatContent.classList.add('active');
    
    // 초기 메시지와 버튼 추가
    addMessage('안녕하세요! 👋\n\n궁금하신 내용을 선택하거나 직접 질문해주세요!', false);
    addQuestionButtons();
    
    setTimeout(() => {
        messageInput.focus();
    }, 100);
}

function addQuestionButtons() {
    const buttonsDiv = document.createElement('div');
    buttonsDiv.className = 'message bot';
    
    const buttonsContainer = document.createElement('div');
    buttonsContainer.className = 'question-buttons';
    buttonsContainer.style.maxWidth = '75%';
    
    const questions = [
        { icon: '💼', text: '자기소개', keyword: '소개' },
        { icon: '🏗️', text: '아키텍처 경험', keyword: '아키텍처' },
        { icon: '🔧', text: '레거시 현대화', keyword: '레거시' },
        { icon: '💻', text: '기술 스택', keyword: '기술' },
        { icon: '📂', text: '프로젝트 경험', keyword: '프로젝트' },
        { icon: '🏢', text: '회사 문화', keyword: '회사문화' }
    ];
    
    questions.forEach(q => {
        const btn = document.createElement('button');
        btn.className = 'question-btn';
        btn.innerHTML = `<span class="question-btn-icon">${q.icon}</span>${q.text}`;
        btn.onclick = () => sendQuestionFromButton(q.keyword);
        buttonsContainer.appendChild(btn);
    });
    
    buttonsDiv.appendChild(buttonsContainer);
    chatMessages.appendChild(buttonsDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function sendQuestionFromButton(keyword) {
    messageInput.value = keyword;
    sendMessage();
}

function addMessage(content, isUser) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${isUser ? 'user' : 'bot'}`;
    
    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    contentDiv.textContent = content;
    
    messageDiv.appendChild(contentDiv);
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function showTypingIndicator() {
    const typingDiv = document.createElement('div');
    typingDiv.className = 'message bot';
    typingDiv.id = 'typingIndicator';
    
    const indicator = document.createElement('div');
    indicator.className = 'typing-indicator';
    indicator.style.display = 'block';
    indicator.innerHTML = '<span></span><span></span><span></span>';
    
    typingDiv.appendChild(indicator);
    chatMessages.appendChild(typingDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function hideTypingIndicator() {
    const typingIndicator = document.getElementById('typingIndicator');
    if (typingIndicator) {
        typingIndicator.remove();
    }
}

async function sendMessage() {
    const message = messageInput.value.trim();
    if (!message) return;

    addMessage(message, true);
    messageInput.value = '';
    showTypingIndicator();

    try {
        const response = await fetch('/api/chatbot/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId: userId,
                message: message,
                conversationId: conversationId
            })
        });

        const data = await response.json();
        conversationId = data.conversationId;
        
        hideTypingIndicator();
        addMessage(data.message, false);
    } catch (error) {
        hideTypingIndicator();
        addMessage('죄송합니다. 오류가 발생했습니다. 다시 시도해주세요.', false);
        console.error('Error:', error);
    }
}

sendButton.addEventListener('click', sendMessage);
messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        sendMessage();
    }
});
