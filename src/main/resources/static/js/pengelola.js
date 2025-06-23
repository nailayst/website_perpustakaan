document.addEventListener('DOMContentLoaded', function() {
    console.log("Pengelola JS loaded.");
    const bookForms = document.querySelectorAll('form[th:action*="tambah-buku"], form[th:action*="edit-buku"]');

    bookForms.forEach(form => {
        form.addEventListener('submit', function(event) {
            const gambarBukuInput = form.querySelector('#gambarBuku');
            const filePdfInput = form.querySelector('#filePdf');

            if (gambarBukuInput && gambarBukuInput.files.length > 0) {
                const file = gambarBukuInput.files[0];
                if (file.type !== 'image/jpeg' && file.type !== 'image/png') {
                    alert('File cover harus JPG atau PNG.');
                    event.preventDefault();
                    return;
                }
                if (file.size > 10 * 1024 * 1024) {
                    alert('Ukuran file cover maksimal 10MB.');
                    event.preventDefault();
                    return;
                }
            }

            if (filePdfInput && filePdfInput.files.length > 0) {
                const file = filePdfInput.files[0];
                if (file.type !== 'application/pdf') {
                    alert('File PDF harus PDF.');
                    event.preventDefault();
                    return;
                }
                if (file.size > 10 * 1024 * 1024) { 
                    alert('Ukuran file PDF maksimal 10MB.');
                    event.preventDefault();
                    return;
                }
            }

            const bulanTerbitSelect = form.querySelector('select[name="bulanTerbit"]');
            const tahunTerbitSelect = form.querySelector('select[name="tahunTerbit"]');

            if (bulanTerbitSelect && tahunTerbitSelect) {
                if (!bulanTerbitSelect.value || !tahunTerbitSelect.value) {
                    alert('Bulan dan Tahun Terbit harus dipilih.');
                    event.preventDefault();
                    return;
                }
            }
        });
    });
});