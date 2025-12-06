/**
 * ReviewKit Widget SDK
 * 고객 웹사이트에 리뷰를 자동으로 표시하는 위젯
 */

(function() {
    'use strict';
    
    const WIDGET_VERSION = '1.0.0';
    const API_BASE_URL = window.location.origin;
    
    class ReviewKitWidget {
        constructor(element, widgetId, options = {}) {
            this.element = element;
            this.widgetId = widgetId;
            this.options = {
                theme: options.theme || element.dataset.theme || 'light',
                layout: options.layout || element.dataset.layout || 'grid',
                limit: parseInt(options.limit || element.dataset.limit || 6),
                lang: options.lang || element.dataset.lang || 'ko',
                ...options
            };
            
            this.reviews = [];
            this.init();
        }
        
        async init() {
            try {
                await this.fetchReviews();
                this.render();
            } catch (error) {
                console.error('[ReviewKit] Failed to initialize widget:', error);
                this.renderError();
            }
        }
        
        async fetchReviews() {
            const url = `${API_BASE_URL}/api/widgets/${this.widgetId}/reviews?limit=${this.options.limit}`;
            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            this.reviews = await response.json();
        }
        
        render() {
            // Create shadow DOM for style isolation
            const shadow = this.element.attachShadow({ mode: 'open' });
            
            // Inject styles
            const style = document.createElement('style');
            style.textContent = this.getStyles();
            shadow.appendChild(style);
            
            // Create container
            const container = document.createElement('div');
            container.className = `reviewkit-container reviewkit-${this.options.theme} reviewkit-${this.options.layout}`;
            
            if (this.reviews.length === 0) {
                container.innerHTML = this.getEmptyState();
            } else {
                container.innerHTML = this.getReviewsHTML();
            }
            
            shadow.appendChild(container);
        }
        
        getStyles() {
            return `
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                
                .reviewkit-container {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                    padding: 24px;
                    border-radius: 12px;
                }
                
                /* Light Theme */
                .reviewkit-light {
                    background: #ffffff;
                    color: #2d3748;
                }
                
                .reviewkit-light .review-card {
                    background: #f7fafc;
                    border: 1px solid #e2e8f0;
                }
                
                .reviewkit-light .reviewer-name {
                    color: #2d3748;
                }
                
                .reviewkit-light .review-content {
                    color: #4a5568;
                }
                
                .reviewkit-light .review-date {
                    color: #a0aec0;
                }
                
                /* Dark Theme */
                .reviewkit-dark {
                    background: #1a202c;
                    color: #e2e8f0;
                }
                
                .reviewkit-dark .review-card {
                    background: #2d3748;
                    border: 1px solid #4a5568;
                }
                
                .reviewkit-dark .reviewer-name {
                    color: #e2e8f0;
                }
                
                .reviewkit-dark .review-content {
                    color: #cbd5e0;
                }
                
                .reviewkit-dark .review-date {
                    color: #718096;
                }
                
                /* Grid Layout */
                .reviewkit-grid .reviews-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
                    gap: 20px;
                }
                
                /* List Layout */
                .reviewkit-list .reviews-grid {
                    display: flex;
                    flex-direction: column;
                    gap: 16px;
                }
                
                /* Review Card */
                .review-card {
                    padding: 20px;
                    border-radius: 12px;
                    transition: all 0.3s ease;
                }
                
                .review-card:hover {
                    transform: translateY(-4px);
                    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
                }
                
                .review-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: start;
                    margin-bottom: 12px;
                }
                
                .reviewer-info {
                    flex: 1;
                }
                
                .reviewer-name {
                    font-size: 16px;
                    font-weight: 600;
                    margin-bottom: 4px;
                }
                
                .reviewer-company {
                    font-size: 13px;
                    opacity: 0.7;
                    margin-bottom: 4px;
                }
                
                .review-rating {
                    font-size: 18px;
                    color: #fbbf24;
                    white-space: nowrap;
                }
                
                .review-content {
                    font-size: 14px;
                    line-height: 1.6;
                    margin-bottom: 12px;
                }
                
                .review-date {
                    font-size: 12px;
                }
                
                /* Empty State */
                .empty-state {
                    text-align: center;
                    padding: 60px 20px;
                    opacity: 0.6;
                }
                
                .empty-state-icon {
                    font-size: 48px;
                    margin-bottom: 16px;
                }
                
                .empty-state-text {
                    font-size: 16px;
                }
                
                /* Error State */
                .error-state {
                    text-align: center;
                    padding: 40px 20px;
                    color: #e53e3e;
                }
                
                /* Responsive */
                @media (max-width: 768px) {
                    .reviewkit-container {
                        padding: 16px;
                    }
                    
                    .reviewkit-grid .reviews-grid {
                        grid-template-columns: 1fr;
                    }
                    
                    .review-card {
                        padding: 16px;
                    }
                }
                
                /* Powered by badge */
                .powered-by {
                    text-align: center;
                    margin-top: 24px;
                    padding-top: 16px;
                    border-top: 1px solid rgba(0, 0, 0, 0.1);
                    font-size: 12px;
                    opacity: 0.5;
                }
                
                .powered-by a {
                    color: inherit;
                    text-decoration: none;
                    font-weight: 600;
                }
                
                .powered-by a:hover {
                    opacity: 0.8;
                }
            `;
        }
        
        getReviewsHTML() {
            const reviewsHTML = this.reviews.map(review => this.getReviewCardHTML(review)).join('');
            
            return `
                <div class="reviews-grid">
                    ${reviewsHTML}
                </div>
                <div class="powered-by">
                    Powered by <a href="${API_BASE_URL}/reviewkit" target="_blank">ReviewKit</a>
                </div>
            `;
        }
        
        getReviewCardHTML(review) {
            const stars = '⭐'.repeat(review.rating);
            const date = this.formatDate(review.createdAt);
            const companyHTML = review.reviewerCompany 
                ? `<div class="reviewer-company">${this.escapeHtml(review.reviewerCompany)}</div>` 
                : '';
            
            return `
                <div class="review-card">
                    <div class="review-header">
                        <div class="reviewer-info">
                            <div class="reviewer-name">${this.escapeHtml(review.reviewerName)}</div>
                            ${companyHTML}
                        </div>
                        <div class="review-rating">${stars}</div>
                    </div>
                    ${review.content ? `<div class="review-content">${this.escapeHtml(review.content)}</div>` : ''}
                    <div class="review-date">${date}</div>
                </div>
            `;
        }
        
        getEmptyState() {
            const text = this.options.lang === 'ko' 
                ? '아직 리뷰가 없습니다' 
                : 'No reviews yet';
            
            return `
                <div class="empty-state">
                    <div class="empty-state-icon">📝</div>
                    <div class="empty-state-text">${text}</div>
                </div>
            `;
        }
        
        renderError() {
            const text = this.options.lang === 'ko' 
                ? '리뷰를 불러올 수 없습니다' 
                : 'Failed to load reviews';
            
            this.element.innerHTML = `
                <div class="error-state">
                    <div>⚠️</div>
                    <div>${text}</div>
                </div>
            `;
        }
        
        formatDate(dateString) {
            const date = new Date(dateString);
            const now = new Date();
            const diffTime = Math.abs(now - date);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            
            if (this.options.lang === 'ko') {
                if (diffDays === 0) return '오늘';
                if (diffDays === 1) return '어제';
                if (diffDays < 7) return `${diffDays}일 전`;
                if (diffDays < 30) return `${Math.floor(diffDays / 7)}주 전`;
                if (diffDays < 365) return `${Math.floor(diffDays / 30)}개월 전`;
                return `${Math.floor(diffDays / 365)}년 전`;
            } else {
                if (diffDays === 0) return 'Today';
                if (diffDays === 1) return 'Yesterday';
                if (diffDays < 7) return `${diffDays} days ago`;
                if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
                if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`;
                return `${Math.floor(diffDays / 365)} years ago`;
            }
        }
        
        escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
    }
    
    // Auto-initialize widgets on page load
    function initializeWidgets() {
        const widgets = document.querySelectorAll('[data-reviewkit-widget]');
        
        widgets.forEach(element => {
            const widgetId = element.dataset.reviewkitWidget;
            
            if (!widgetId) {
                console.error('[ReviewKit] Widget ID is required');
                return;
            }
            
            new ReviewKitWidget(element, widgetId);
        });
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeWidgets);
    } else {
        initializeWidgets();
    }
    
    // Expose API for manual initialization
    window.ReviewKit = {
        version: WIDGET_VERSION,
        init: function(element, widgetId, options) {
            return new ReviewKitWidget(element, widgetId, options);
        }
    };
    
    console.log(`[ReviewKit] Widget SDK v${WIDGET_VERSION} loaded`);
})();
