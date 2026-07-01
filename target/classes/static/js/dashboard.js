document.addEventListener('DOMContentLoaded', () => {
    // Chart initialization points
    const barChartCtx = document.getElementById('monthlySpendChart');
    const pieChartCtx = document.getElementById('categoryPieChart');

    if (barChartCtx && typeof monthlyData !== 'undefined') {
        const labels = Object.keys(monthlyData);
        const data = Object.values(monthlyData);
        
        new Chart(barChartCtx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Monthly Spend (₹)',
                    data: data,
                    backgroundColor: '#6366F1',
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: '#334155' },
                        ticks: { color: '#94A3B8' }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { color: '#94A3B8' }
                    }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
    }

    if (pieChartCtx && typeof categoryData !== 'undefined') {
        const labels = Object.keys(categoryData);
        const data = Object.values(categoryData);
        
        new Chart(pieChartCtx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: ['#6366F1', '#10B981', '#F59E0B', '#F43F5E', '#06B6D4', '#8B5CF6'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                        labels: { color: '#94A3B8' }
                    }
                }
            }
        });
    }
    
    // Income edit toggle
    const editIncomeBtn = document.getElementById('editIncomeBtn');
    const incomeDisplay = document.getElementById('incomeDisplay');
    const incomeForm = document.getElementById('incomeForm');
    
    if (editIncomeBtn) {
        editIncomeBtn.addEventListener('click', () => {
            incomeDisplay.style.display = 'none';
            incomeForm.style.display = 'block';
        });
    }
});
