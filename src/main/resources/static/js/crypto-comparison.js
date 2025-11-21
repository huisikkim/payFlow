let wsUpbit, wsBithumb;
const upbitTickers = new Map();
const bithumbTickers = new Map();
const rsiData = new Map(); // RSI 데이터 캐시
let updateInterval;

function connectUpbit() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/crypto/upbit`;
    
    wsUpbit = new WebSocket(wsUrl);

    wsUpbit.onopen = () => {
        console.log('✅ Upbit Connected');
        const upbitDot = document.getElementById('upbitDot');
        if (upbitDot) {
            upbitDot.style.background = '#3b82f6';
        }
    };

    wsUpbit.onmessage = (event) => {
        try {
            const ticker = JSON.parse(event.data);
            upbitTickers.set(ticker.market, ticker);
            updateComparison();
        } catch (error) {
            console.error('Upbit parse error:', error);
        }
    };

    wsUpbit.onerror = (error) => {
        console.error('❌ Upbit WebSocket error:', error);
        const upbitDot = document.getElementById('upbitDot');
        if (upbitDot) {
            upbitDot.style.background = '#dc2626';
        }
    };

    wsUpbit.onclose = () => {
        const upbitDot = document.getElementById('upbitDot');
        if (upbitDot) {
            upbitDot.style.background = '#dc2626';
        }
        setTimeout(connectUpbit, 5000);
    };
}

function connectBithumb() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/crypto/bithumb`;
    
    wsBithumb = new WebSocket(wsUrl);

    wsBithumb.onopen = () => {
        console.log('✅ Bithumb Connected');
        const bithumbDot = document.getElementById('bithumbDot');
        if (bithumbDot) {
            bithumbDot.style.background = '#f59e0b';
        }
    };

    wsBithumb.onmessage = (event) => {
        try {
            const ticker = JSON.parse(event.data);
            bithumbTickers.set(ticker.market, ticker);
            updateComparison();
        } catch (error) {
            console.error('Bithumb parse error:', error);
        }
    };

    wsBithumb.onerror = (error) => {
        console.error('❌ Bithumb WebSocket error:', error);
        const bithumbDot = document.getElementById('bithumbDot');
        if (bithumbDot) {
            bithumbDot.style.background = '#dc2626';
        }
    };

    wsBithumb.onclose = () => {
        const bithumbDot = document.getElementById('bithumbDot');
        if (bithumbDot) {
            bithumbDot.style.background = '#dc2626';
        }
        setTimeout(connectBithumb, 5000);
    };
}

function updateComparison() {
    console.log('📊 updateComparison called - Upbit:', upbitTickers.size, 'Bithumb:', bithumbTickers.size);
    
    // 업비트 데이터만 있어도 표시
    if (upbitTickers.size === 0) {
        console.log('⚠️ No Upbit data yet');
        return;
    }

    console.log('✅ Showing comparison table');
    document.getElementById('loading').style.display = 'none';
    document.getElementById('comparisonTable').style.display = 'block';

    const comparisons = [];
    
    upbitTickers.forEach((upbitTicker, market) => {
        const bithumbTicker = bithumbTickers.get(market);
        if (bithumbTicker) {
            const comparison = createComparison(upbitTicker, bithumbTicker);
            comparisons.push(comparison);
        } else {
            // 빗썸 데이터가 없으면 업비트 데이터만 표시
            const comparison = {
                market: upbitTicker.market,
                koreanName: upbitTicker.koreanName,
                upbitPrice: parseFloat(upbitTicker.tradePrice),
                bithumbPrice: null,
                priceDiff: 0,
                diffPercent: 0,
                cheaperExchange: 'N/A',
                totalVolume: parseFloat(upbitTicker.accTradePrice24h),
                upbitVolume: parseFloat(upbitTicker.accTradePrice24h),
                bithumbVolume: 0
            };
            comparisons.push(comparison);
        }
    });

    // 가격 차이가 큰 순으로 정렬
    comparisons.sort((a, b) => Math.abs(b.diffPercent) - Math.abs(a.diffPercent));

    console.log('📈 Comparisons:', comparisons.length);
    if (comparisons.length > 0) {
        console.log('📊 Sample comparison:', comparisons[0]);
    }
    
    updateStats(comparisons);
    updateTable(comparisons);
    checkArbitrageOpportunity(comparisons);

    const now = new Date();
    const updateTimeEl = document.getElementById('updateTime');
    if (updateTimeEl) {
        updateTimeEl.textContent = now.toLocaleTimeString('ko-KR');
    }
}

function createComparison(upbitTicker, bithumbTicker) {
    const upbitPrice = parseFloat(upbitTicker.tradePrice);
    const bithumbPrice = parseFloat(bithumbTicker.tradePrice);
    
    const priceDiff = upbitPrice - bithumbPrice;
    const diffPercent = (priceDiff / upbitPrice) * 100;
    
    const upbitVolume = parseFloat(upbitTicker.accTradePrice24h);
    const bithumbVolume = parseFloat(bithumbTicker.accTradePrice24h);
    const totalVolume = upbitVolume + bithumbVolume;
    
    const cheaperExchange = priceDiff > 0 ? 'BITHUMB' : 'UPBIT';
    
    return {
        market: upbitTicker.market,
        koreanName: upbitTicker.koreanName,
        upbitPrice,
        bithumbPrice,
        priceDiff: Math.abs(priceDiff),
        diffPercent,
        cheaperExchange,
        totalVolume,
        upbitVolume,
        bithumbVolume
    };
}

function updateStats(comparisons) {
    if (comparisons.length === 0) return;
    
    // 평균 가격 차이
    const avgDiff = comparisons.reduce((sum, c) => sum + Math.abs(c.diffPercent), 0) / comparisons.length;
    const avgDiffEl = document.getElementById('avgDiff');
    if (avgDiffEl) {
        avgDiffEl.textContent = avgDiff.toFixed(2) + '%';
    }
    
    // 최대 차익거래 기회
    const maxDiff = Math.max(...comparisons.map(c => Math.abs(c.diffPercent)));
    const maxOpportunityEl = document.getElementById('maxOpportunity');
    if (maxOpportunityEl) {
        maxOpportunityEl.textContent = maxDiff.toFixed(2) + '%';
    }
    
    // 차익거래 기회 개수 (1% 이상)
    const arbitrageCount = comparisons.filter(c => Math.abs(c.diffPercent) >= 1.0).length;
    const arbitrageCountEl = document.getElementById('arbitrageCount');
    if (arbitrageCountEl) {
        arbitrageCountEl.textContent = arbitrageCount;
    }
    
    // 총 거래량
    const totalVolume = comparisons.reduce((sum, c) => sum + c.totalVolume, 0);
    const totalVolumeEl = document.getElementById('totalVolume');
    if (totalVolumeEl) {
        totalVolumeEl.textContent = formatVolume(totalVolume);
    }
}

function updateTable(comparisons) {
    const rows = document.getElementById('comparisonRows');
    if (!rows) {
        console.error('❌ comparisonRows element not found!');
        return;
    }
    
    console.log('📝 Updating table with', comparisons.length, 'rows');
    const html = comparisons.map(c => createComparisonRow(c)).join('');
    console.log('📝 Generated HTML length:', html.length);
    rows.innerHTML = html;
    
    // 테이블이 확실히 보이도록 강제 표시
    const table = document.getElementById('comparisonTable');
    if (table) {
        table.style.display = 'block';
        console.log('✅ Table display set to block');
    }
}

function createComparisonRow(comparison) {
    const isOpportunity = Math.abs(comparison.diffPercent) >= 1.0;
    const initial = comparison.koreanName.charAt(0);
    const hasBithumb = comparison.bithumbPrice !== null;
    
    // RSI 데이터 가져오기
    const marketRSI = rsiData.get(comparison.market) || { upbit: 0, bithumb: 0 };
    const upbitRSI = marketRSI.upbit || 0;
    const bithumbRSI = marketRSI.bithumb || 0;
    
    return `
        <div class="comparison-row ${isOpportunity ? 'opportunity' : ''}">
            <div class="market-cell">
                <div class="market-icon">${initial}</div>
                <div class="market-info">
                    <div class="market-korean">${comparison.koreanName}</div>
                    <div class="market-code">${comparison.market}</div>
                </div>
            </div>
            <div class="price-cell upbit">${formatPrice(comparison.upbitPrice)}</div>
            <div class="price-cell bithumb">${hasBithumb ? formatPrice(comparison.bithumbPrice) : '<span style="color: #666;">N/A</span>'}</div>
            <div class="diff-cell">
                ${hasBithumb ? `
                    <div class="diff-percent ${comparison.diffPercent > 0 ? 'positive' : 'negative'}">
                        ${comparison.diffPercent > 0 ? '+' : ''}${comparison.diffPercent.toFixed(2)}%
                    </div>
                    <div class="diff-amount">${formatPrice(comparison.priceDiff)}</div>
                ` : '<span style="color: #666;">-</span>'}
            </div>
            <div class="rsi-cell">
                ${formatRSI(upbitRSI)}
            </div>
            <div class="rsi-cell">
                ${formatRSI(bithumbRSI)}
            </div>
            <div class="volume-cell hide-mobile">${formatVolume(comparison.totalVolume)}</div>
        </div>
    `;
}

function formatRSI(rsi) {
    if (!rsi || rsi === 0) {
        return '<span style="color: #666;">-</span>';
    }
    
    const rsiValue = parseFloat(rsi);
    let rsiClass = 'neutral';
    let rsiLabel = '중립';
    
    if (rsiValue >= 70) {
        rsiClass = 'overbought';
        rsiLabel = '과매수';
    } else if (rsiValue <= 30) {
        rsiClass = 'oversold';
        rsiLabel = '과매도';
    }
    
    return `
        <div class="rsi-value ${rsiClass}">${rsiValue.toFixed(1)}</div>
        <div class="rsi-label">${rsiLabel}</div>
    `;
}

function checkArbitrageOpportunity(comparisons) {
    const opportunities = comparisons.filter(c => Math.abs(c.diffPercent) >= 1.5);
    
    const alertEl = document.getElementById('arbitrageAlert');
    const alertDescEl = document.getElementById('alertDesc');
    
    if (opportunities.length > 0 && alertEl && alertDescEl) {
        const best = opportunities[0];
        alertEl.style.display = 'flex';
        alertDescEl.textContent = 
            `${best.koreanName}: ${Math.abs(best.diffPercent).toFixed(2)}% difference - Buy on ${best.cheaperExchange}, Sell on ${best.cheaperExchange === 'UPBIT' ? 'BITHUMB' : 'UPBIT'}`;
    } else if (alertEl) {
        alertEl.style.display = 'none';
    }
}

function formatPrice(price) {
    if (price >= 1000) {
        return price.toLocaleString('ko-KR', {
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        });
    } else if (price >= 1) {
        return price.toLocaleString('ko-KR', {
            minimumFractionDigits: 0,
            maximumFractionDigits: 2
        });
    } else {
        return price.toLocaleString('ko-KR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 4
        });
    }
}

function formatVolume(volume) {
    if (volume >= 100000000) {
        return (volume / 100000000).toFixed(0) + '억';
    } else if (volume >= 10000) {
        return (volume / 10000).toFixed(0) + '만';
    } else {
        return volume.toLocaleString('ko-KR');
    }
}

// RSI 데이터 가져오기
async function fetchRSIData() {
    try {
        const response = await fetch('/api/crypto/rsi');
        if (response.ok) {
            const data = await response.json();
            
            // RSI 데이터 캐시에 저장
            Object.keys(data).forEach(market => {
                rsiData.set(market, data[market]);
            });
            
            console.log('📊 RSI 데이터 로드 완료:', rsiData.size);
            updateComparison(); // RSI 데이터 로드 후 화면 업데이트
        }
    } catch (error) {
        console.error('❌ RSI 데이터 로드 실패:', error);
    }
}

// 페이지 로드 시 연결
document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Page loaded, connecting to websockets...');
    console.log('Upbit WS URL:', `ws://${window.location.host}/ws/crypto/upbit`);
    console.log('Bithumb WS URL:', `ws://${window.location.host}/ws/crypto/bithumb`);
    
    connectUpbit();
    connectBithumb();
    
    // RSI 데이터 초기 로드
    fetchRSIData();
    
    // 1분마다 RSI 데이터 갱신
    setInterval(fetchRSIData, 60000);
    
    // 5초 후에도 데이터가 없으면 에러 표시
    setTimeout(() => {
        if (upbitTickers.size === 0) {
            console.error('❌ No data received from Upbit after 5 seconds');
            document.getElementById('loading').innerHTML = 
                'Failed to connect to Upbit WebSocket. Please check console for errors.';
        }
    }, 5000);
});
