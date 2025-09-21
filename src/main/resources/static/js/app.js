// Custom JavaScript for Danish Electricity Price Collector

// HTMX event listeners
document.addEventListener('htmx:beforeRequest', function(event) {
    console.log('HTMX request starting:', event.detail.xhr.responseURL);
});

document.addEventListener('htmx:afterRequest', function(event) {
    console.log('HTMX request completed:', event.detail.xhr.status);
});

// Utility functions
function formatPrice(price) {
    return new Intl.NumberFormat('da-DK', {
        style: 'currency',
        currency: 'DKK',
        minimumFractionDigits: 2
    }).format(price);
}

function formatDateTime(dateTime) {
    return new Intl.DateTimeFormat('da-DK', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(dateTime));
}

// Auto-refresh functionality
function startAutoRefresh(intervalSeconds = 300) {
    setInterval(() => {
        const autoRefreshElements = document.querySelectorAll('[data-auto-refresh]');
        autoRefreshElements.forEach(element => {
            if (element.hasAttribute('hx-get')) {
                htmx.trigger(element, 'refresh');
            }
        });
    }, intervalSeconds * 1000);
}

// Initialize auto-refresh when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    startAutoRefresh(300); // Refresh every 5 minutes
});