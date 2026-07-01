document.addEventListener('DOMContentLoaded', () => {
    // 12-Month Trend Line Chart
    const trendCtx = document.getElementById('trendLineChart');
    if (trendCtx && typeof monthlyLabelsJSON !== 'undefined' && typeof monthlyValuesJSON !== 'undefined') {
        let gradient = trendCtx.getContext('2d').createLinearGradient(0, 0, 0, 400);
        gradient.addColorStop(0, 'rgba(99, 102, 241, 0.5)'); // Indigo
        gradient.addColorStop(1, 'rgba(99, 102, 241, 0.0)');
        
        new Chart(trendCtx, {
            type: 'line',
            data: {
                labels: monthlyLabelsJSON,
                datasets: [{
                    label: 'Monthly Spend (₹)',
                    data: monthlyValuesJSON,
                    borderColor: '#6366F1',
                    backgroundColor: gradient,
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#FFFFFF',
                    pointBorderColor: '#6366F1',
                    pointBorderWidth: 2,
                    pointRadius: 4,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: '#E2E8F0', drawBorder: false },
                        ticks: { color: '#475569' }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { color: '#475569' }
                    }
                },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#FFFFFF',
                        titleColor: '#0F172A',
                        bodyColor: '#10B981',
                        borderColor: '#E2E8F0',
                        borderWidth: 1,
                        padding: 10
                    }
                }
            }
        });
    }

    // Doughnut Chart
    const pieCtx = document.getElementById('premiumCategoryPieChart');
    if (pieCtx && typeof categoryLabelsJSON !== 'undefined' && typeof categoryValuesJSON !== 'undefined') {
        const total = categoryValuesJSON.reduce((a, b) => a + b, 0);

        const centerTextPlugin = {
            id: 'centerText',
            beforeDraw: function(chart) {
                if (chart.config.type !== 'doughnut') return;
                var width = chart.width,
                    height = chart.height,
                    ctx = chart.ctx;
                
                ctx.restore();
                var fontSize = (height / 114).toFixed(2);
                ctx.font = "bold " + fontSize + "em Inter, sans-serif";
                ctx.textBaseline = "middle";
                ctx.fillStyle = "#0F172A";
                
                var text = "₹" + total.toLocaleString('en-IN', {maximumFractionDigits: 0}),
                    textX = Math.round((width - ctx.measureText(text).width) / 2),
                    textY = height / 2;
                
                ctx.fillText(text, textX, textY);
                ctx.save();
            }
        };
        
        const colors = ['#6366F1', '#10B981', '#F59E0B', '#F43F5E', '#06B6D4', '#8B5CF6'];

        new Chart(pieCtx, {
            type: 'doughnut',
            data: {
                labels: categoryLabelsJSON,
                datasets: [{
                    data: categoryValuesJSON,
                    backgroundColor: colors,
                    borderWidth: 0,
                    hoverOffset: 4
                }]
            },
            plugins: [centerTextPlugin],
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
        
        const legendContainer = document.getElementById('custom-legend');
        if (legendContainer) {
            let legendHTML = '';
            categoryLabelsJSON.forEach((label, i) => {
                const color = colors[i % colors.length];
                const value = categoryValuesJSON[i];
                legendHTML += `
                    <div class="category-legend-item">
                        <div class="legend-dot" style="background-color: ${color};"></div>
                        <div style="display: flex; justify-content: space-between; width: 100%;">
                            <span>${label.charAt(0).toUpperCase() + label.slice(1)}</span>
                            <span style="font-weight: bold; margin-left: 1rem;">₹${value.toLocaleString('en-IN')}</span>
                        </div>
                    </div>
                `;
            });
            legendContainer.innerHTML = legendHTML;
        }
    }

    // Fix 2: Compact Calendar Injection
    const calendarContainer = document.getElementById('calendar-container');
    if (calendarContainer && typeof calendarDataJSON !== 'undefined') {
        const date = new Date();
        const year = date.getFullYear();
        const month = date.getMonth();
        const today = date.getDate();
        
        const firstDay = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const currentMonthName = date.toLocaleString('default', { month: 'long', year: 'numeric' });
        
        let html = `
            <div class="compact-calendar">
                <div class="calendar-header">
                    <span>${currentMonthName}</span>
                </div>
                <div class="calendar-grid">
                    <div class="cal-day-header">Su</div>
                    <div class="cal-day-header">Mo</div>
                    <div class="cal-day-header">Tu</div>
                    <div class="cal-day-header">We</div>
                    <div class="cal-day-header">Th</div>
                    <div class="cal-day-header">Fr</div>
                    <div class="cal-day-header">Sa</div>
        `;
        
        for (let i = 0; i < firstDay; i++) {
            html += '<div class="cal-day empty">-</div>';
        }
        
        for (let i = 1; i <= daysInMonth; i++) {
            const hasRenewal = calendarDataJSON[i] && calendarDataJSON[i].length > 0;
            const isToday = i === today;
            
            let classes = 'cal-day';
            if (isToday) classes += ' today';
            if (hasRenewal) classes += ' has-renewal';
            
            html += `<div class="${classes}">${i}`;
            
            if (hasRenewal) {
                const subsLength = calendarDataJSON[i].length;
                const subsNames = calendarDataJSON[i].join(', ');
                html += `<span class="cal-tooltip">${subsNames}</span>`;
            }
            
            html += `</div>`;
        }
        
        const totalCellsSoFar = firstDay + daysInMonth;
        const remainder = totalCellsSoFar % 7;
        if (remainder !== 0) {
            const emptyCellsNeeded = 7 - remainder;
            for (let i = 0; i < emptyCellsNeeded; i++) {
                html += '<div class="cal-day empty">-</div>';
            }
        }
        
        html += `</div></div>`;
        calendarContainer.innerHTML = html;
    }
});
