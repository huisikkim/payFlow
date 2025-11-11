/**
 * Session Replay SDK
 * 사용자 상호작용 이벤트를 캡처하고 백엔드로 전송
 */
class SessionReplaySDK {
    constructor(config = {}) {
        this.sessionId = this.generateSessionId();
        this.eventQueue = [];
        this.config = {
            apiEndpoint: config.apiEndpoint || '/api/session-replay/events',
            batchSize: config.batchSize || 10,
            flushInterval: config.flushInterval || 5000,
            maxRetries: config.maxRetries || 3,
            maxEventsPerSecond: config.maxEventsPerSecond || 100
        };
        
        this.eventCount = 0;
        this.lastResetTime = Date.now();
        this.flushTimer = null;
        this.isStarted = false;
    }
    
    generateSessionId() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }
    
    start() {
        if (this.isStarted) {
            console.warn('SessionReplaySDK is already started');
            return;
        }
        
        this.isStarted = true;
        this.attachEventListeners();
        this.startFlushTimer();
        this.attachUnloadListener();
        
        console.log('SessionReplaySDK started with sessionId:', this.sessionId);
    }
    
    attachEventListeners() {
        document.addEventListener('click', this.captureClick.bind(this), true);
        document.addEventListener('scroll', this.captureScroll.bind(this), true);
        document.addEventListener('input', this.captureInput.bind(this), true);
        window.addEventListener('popstate', this.captureNavigation.bind(this));
    }
    
    startFlushTimer() {
        this.flushTimer = setInterval(() => {
            if (this.eventQueue.length > 0) {
                this.flushEvents();
            }
        }, this.config.flushInterval);
    }
    
    attachUnloadListener() {
        window.addEventListener('beforeunload', () => {
            if (this.eventQueue.length > 0) {
                this.flushEventsSync();
            }
        });
    }
    
    checkRateLimit() {
        const now = Date.now();
        if (now - this.lastResetTime >= 1000) {
            this.eventCount = 0;
            this.lastResetTime = now;
        }
        
        if (this.eventCount >= this.config.maxEventsPerSecond) {
            return false;
        }
        
        this.eventCount++;
        return true;
    }
    
    addEvent(eventType, payload) {
        if (!this.checkRateLimit()) {
            return;
        }
        
        const event = {
            sessionId: this.sessionId,
            eventType: eventType,
            timestamp: Date.now(),
            payload: payload
        };
        
        this.eventQueue.push(event);
        
        if (this.eventQueue.length >= this.config.batchSize) {
            this.flushEvents();
        }
    }
    
    captureClick(event) {
        const target = event.target;
        
        if (target.classList && target.classList.contains('replay-exclude')) {
            return;
        }
        
        const payload = {
            tagName: target.tagName,
            id: target.id || null,
            className: target.className || null,
            text: target.textContent ? target.textContent.substring(0, 100) : null,
            x: event.clientX,
            y: event.clientY,
            pageX: event.pageX,
            pageY: event.pageY
        };
        
        this.addEvent('CLICK', payload);
    }
    
    captureScroll(event) {
        const payload = {
            scrollX: window.scrollX || window.pageXOffset,
            scrollY: window.scrollY || window.pageYOffset,
            viewportWidth: window.innerWidth,
            viewportHeight: window.innerHeight,
            documentHeight: document.documentElement.scrollHeight
        };
        
        this.addEvent('SCROLL', payload);
    }
    
    captureInput(event) {
        const target = event.target;
        
        if (target.classList && target.classList.contains('replay-exclude')) {
            return;
        }
        
        const fieldType = target.type || 'text';
        const fieldName = target.name || target.id || 'unknown';
        let value = target.value;
        
        if (this.shouldMaskField(target, fieldType, fieldName)) {
            value = this.maskSensitiveData(value, fieldType);
        }
        
        const payload = {
            tagName: target.tagName,
            fieldType: fieldType,
            fieldName: fieldName,
            fieldId: target.id || null,
            value: value
        };
        
        this.addEvent('INPUT', payload);
    }
    
    captureNavigation(event) {
        const payload = {
            from: document.referrer || null,
            to: window.location.href,
            title: document.title
        };
        
        this.addEvent('NAVIGATION', payload);
    }
    
    shouldMaskField(element, fieldType, fieldName) {
        if (fieldType === 'password') {
            return true;
        }
        
        const sensitiveKeywords = ['password', 'pwd', 'pass', 'credit', 'card', 'cvv', 'ssn'];
        const lowerFieldName = fieldName.toLowerCase();
        if (sensitiveKeywords.some(keyword => lowerFieldName.includes(keyword))) {
            return true;
        }
        
        if (element.hasAttribute('data-replay-mask')) {
            return true;
        }
        
        return false;
    }
    
    maskSensitiveData(value, fieldType) {
        if (!value) {
            return '';
        }
        
        if (fieldType === 'password') {
            return '********';
        }
        
        if (value.length >= 4) {
            const last4 = value.slice(-4);
            return '*'.repeat(value.length - 4) + last4;
        }
        
        return '****';
    }
    
    async flushEvents() {
        if (this.eventQueue.length === 0) {
            return;
        }
        
        const eventsToSend = [...this.eventQueue];
        this.eventQueue = [];
        
        let retries = 0;
        while (retries < this.config.maxRetries) {
            try {
                await this.sendEvents(eventsToSend);
                return;
            } catch (error) {
                retries++;
                if (retries >= this.config.maxRetries) {
                    console.error('Failed to send events after retries', error);
                    this.saveToLocalStorage(eventsToSend);
                    return;
                }
                await this.sleep(Math.pow(2, retries) * 1000);
            }
        }
    }
    
    async sendEvents(events) {
        const payload = this.compressIfNeeded(events);
        
        const response = await fetch(this.config.apiEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    }
    
    flushEventsSync() {
        if (this.eventQueue.length === 0) {
            return;
        }
        
        const eventsToSend = [...this.eventQueue];
        this.eventQueue = [];
        
        const payload = JSON.stringify(eventsToSend);
        
        if (navigator.sendBeacon) {
            const blob = new Blob([payload], { type: 'application/json' });
            navigator.sendBeacon(this.config.apiEndpoint, blob);
        }
    }
    
    compressIfNeeded(events) {
        const jsonString = JSON.stringify(events);
        
        if (jsonString.length > 1024) {
            console.log('Payload size:', jsonString.length, 'bytes (compression recommended)');
        }
        
        return events;
    }
    
    saveToLocalStorage(events) {
        try {
            const stored = localStorage.getItem('session-replay-failed-events') || '[]';
            const failedEvents = JSON.parse(stored);
            failedEvents.push(...events);
            localStorage.setItem('session-replay-failed-events', JSON.stringify(failedEvents));
            console.log('Events saved to localStorage for retry');
        } catch (error) {
            console.error('Failed to save events to localStorage', error);
        }
    }
    
    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
    
    stop() {
        if (this.flushTimer) {
            clearInterval(this.flushTimer);
        }
        if (this.eventQueue.length > 0) {
            this.flushEvents();
        }
        this.isStarted = false;
    }
}

window.SessionReplaySDK = SessionReplaySDK;
