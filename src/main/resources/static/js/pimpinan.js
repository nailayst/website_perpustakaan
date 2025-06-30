document.addEventListener('DOMContentLoaded', function() {
    console.log("Pimpinan JS loaded.");

    let peminjamanBulanChartInstance; 
    let peminjamanStatusChartInstance;
    let bukuKategoriChartInstance;

    window.initializePimpinanCharts = function(peminjamanStatusLabels, peminjamanStatusData,
                                              bukuKategoriLabels, bukuKategoriData,
                                              peminjamanBulanLabels, peminjamanBulanData) {

        const peminjamanStatusCtx = document.getElementById('peminjamanStatusChart');
        if (peminjamanStatusCtx) {
            if (peminjamanStatusChartInstance) {
                peminjamanStatusChartInstance.destroy();
            }
            peminjamanStatusChartInstance = new Chart(peminjamanStatusCtx.getContext('2d'), {
                type: 'pie',
                data: {
                    labels: peminjamanStatusLabels,
                    datasets: [{
                        data: peminjamanStatusData,
                        backgroundColor: [
                            'rgba(40, 167, 69, 0.8)', 
                            'rgba(26, 60, 94, 0.8)', 
                            'rgba(220, 53, 69, 0.8)' 
                        ],
                        borderColor: [
                            '#fff', '#fff', '#fff'
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'top',
                        },
                        title: {
                            display: false,
                            text: 'Statistik Status Peminjaman'
                        }
                    }
                }
            });
        }

        const bukuKategoriCtx = document.getElementById('bukuKategoriChart');
        if (bukuKategoriCtx) {
            if (bukuKategoriChartInstance) {
                bukuKategoriChartInstance.destroy();
            }
            bukuKategoriChartInstance = new Chart(bukuKategoriCtx.getContext('2d'), {
                type: 'bar',
                data: {
                    labels: bukuKategoriLabels,
                    datasets: [{
                        label: 'Jumlah Peminjaman',
                        data: bukuKategoriData,
                        backgroundColor: 'rgba(26, 60, 94, 0.8)', 
                        borderColor: 'rgba(26, 60, 94, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            display: false,
                        },
                        title: {
                            display: false,
                            text: 'Peminjaman per Kategori Buku'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                precision: 0
                            }
                        }
                    }
                }
            });
        }

        const peminjamanBulanCtx = document.getElementById('peminjamanBulanChart');
        if (peminjamanBulanCtx) {
            if (peminjamanBulanChartInstance) {
                peminjamanBulanChartInstance.destroy();
            }
            peminjamanBulanChartInstance = new Chart(peminjamanBulanCtx.getContext('2d'), {
                type: 'line',
                data: {
                    labels: peminjamanBulanLabels,
                    datasets: [{
                        label: 'Jumlah Peminjaman',
                        data: peminjamanBulanData,
                        borderColor: 'rgba(26, 60, 94, 0.8)',
                        backgroundColor: 'rgba(26, 60, 94, 0.2)',
                        fill: true,
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            display: false,
                        },
                        title: {
                            display: false,
                            text: 'Tren Peminjaman per Periode'
                        }
                    },
                    scales: {
                        x: { 
                            ticks: {
                                autoSkip: false, 
                                maxRotation: 45, 
                                minRotation: 45 
                            }
                        },
                        y: {
                            beginAtZero: true,
                            ticks: {
                                precision: 0
                            }
                        }
                    }
                }
            });
        }
    }

    const trendPeriodSelect = document.getElementById('trendPeriodSelect');
    const trendCountInput = document.getElementById('trendCountInput');
    const updateTrendButton = document.getElementById('updateTrendButton');

    if (trendPeriodSelect && trendCountInput && updateTrendButton) {
        updateTrendButton.addEventListener('click', function() {
            const selectedPeriod = trendPeriodSelect.value;
            const selectedCount = trendCountInput.value;

            fetch(`/pimpinan/dashboard/trend-data?trendPeriod=${selectedPeriod}&trendCount=${selectedCount}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (peminjamanBulanChartInstance) {
                        peminjamanBulanChartInstance.data.labels = data.labels;
                        peminjamanBulanChartInstance.data.datasets[0].data = data.data;
                        peminjamanBulanChartInstance.update(); 
                        console.log("Chart tren berhasil diperbarui:", data);
                    }
                })
                .catch(error => {
                    console.error('Error fetching trend data:', error);
                    alert('Gagal mengambil data tren: ' + error.message + '. Pastikan backend berfungsi dan endpoint /pimpinan/dashboard/trend-data mengembalikan JSON.');
                });
        });

        const urlParams = new URLSearchParams(window.location.search);
        const currentPeriod = urlParams.get('trendPeriod') || 'monthly';
        const currentCount = urlParams.get('trendCount') || '6';
        
        trendPeriodSelect.value = currentPeriod;
        trendCountInput.value = currentCount;
    }
});