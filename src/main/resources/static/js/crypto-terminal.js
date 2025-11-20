let ws;
const tickers = new Map();
let lastUpdate = new Map();

function connect() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/crypto`;
    
    ws = new WebSocket(wsUrl);

    ws.onopen = () => {
        console.log('✅ Connected');
        document.getElementById('statusText').textContent = 'Connected';
        document.getElementById('statusBadge').textContent = 'LIVE';
        document.getElementById('statusDot').style.background = '#16a34a';
        document.getElementById('loading').style.display = 'none';
        document.getElementById('error').style.display = 'none';
        document.getElementById('tickerTable').style.display = 'block';
    };

    ws.onmessage = (event) => {
        try {
            const ticker = JSON.parse(event.data);
            const prevTicker = tickers.get(ticker.market);
            tickers.set(ticker.market, ticker);
            
            // 가격 변동 체크
            if (prevTicker && prevTicker.tradePrice !== ticker.tradePrice) {
                lastUpdate.set(ticker.market, Date.now());
            }
            
            updateUI();
        } catch (error) {
            console.error('Parse error:', error);
        }
    };

    ws.onerror = (error) => {
        console.error('❌ Connection error:', error);
        document.getElementById('statusText').textContent = 'Error';
        document.getElementById('statusBadge').textContent = 'ERROR';
        document.getElementById('statusDot').style.background = '#dc2626';
        document.getElementById('error').style.display = 'block';
    };

    ws.onclose = () => {
        console.log('❌ Disconnected');
        document.getElementById('statusText').textContent = 'Disconnected';
        document.getElementById('statusBadge').textContent = 'OFFLINE';
        document.getElementById('statusDot').style.background = '#dc2626';
        
        setTimeout(() => {
            console.log('🔄 Reconnecting...');
            connect();
        }, 5000);
    };
}

function updateUI() {
    const sortedTickers = Array.from(tickers.values())
        .sort((a, b) => b.accTradePrice24h - a.accTradePrice24h);

    updateOverview(sortedTickers);
    updateTable(sortedTickers);
    
    const now = new Date();
    document.getElementById('updateTime').textContent = now.toLocaleTimeString('ko-KR');
}

function updateOverview(sortedTickers) {
    const totalVolume = sortedTickers.reduce((sum, t) => sum + parseFloat(t.accTradePrice24h), 0);
    const gainers = sortedTickers.filter(t => t.change === 'RISE').length;
    const losers = sortedTickers.filter(t => t.change === 'FALL').length;

    document.getElementById('totalMarkets').textContent = sortedTickers.length;
    document.getElementById('totalVolume').textContent = formatVolume(totalVolume);
    document.getElementById('gainers').textContent = gainers;
    document.getElementById('losers').textContent = losers;
}

function updateTable(sortedTickers) {
    const rows = document.getElementById('tickerRows');
    rows.innerHTML = sortedTickers.map(ticker => createTickerRow(ticker)).join('');
}

function createTickerRow(ticker) {
    const changeClass = ticker.change === 'RISE' ? 'rise' : 
                       ticker.change === 'FALL' ? 'fall' : 'even';
    
    const changeSymbol = ticker.change === 'RISE' ? '▲' : 
                        ticker.change === 'FALL' ? '▼' : '—';
    
    const changeRate = Math.abs(ticker.signedChangeRate).toFixed(2);
    const isUpdated = lastUpdate.has(ticker.market) && 
                    (Date.now() - lastUpdate.get(ticker.market)) < 1000;

    const initial = ticker.koreanName.charAt(0);

    return `
        <div class="ticker-row ${isUpdated ? 'updated' : ''}">
            <div class="ticker-name-cell">
                <div class="ticker-icon">${initial}</div>
                <div class="ticker-info">
                    <div class="ticker-korean">${ticker.koreanName}</div>
                    <div class="ticker-market">${ticker.market}</div>
                </div>
            </div>
            <div class="ticker-price">${formatPrice(ticker.tradePrice)}</div>
            <div class="ticker-change ${changeClass}">
                <span class="change-badge ${changeClass}">
                    ${changeSymbol} ${changeRate}%
                </span>
            </div>
            <div class="ticker-stat hide-mobile">${formatPrice(ticker.highPrice)}</div>
            <div class="ticker-stat hide-mobile">${formatPrice(ticker.lowPrice)}</div>
            <div class="ticker-volume">${formatVolume(ticker.accTradePrice24h)}</div>
        </div>
    `;
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

// 페이지 로드 시 연결
document.addEventListener('DOMContentLoaded', () => {
    connect();
});
