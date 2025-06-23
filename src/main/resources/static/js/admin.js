document.addEventListener('DOMContentLoaded', function() {
    console.log("Admin JS loaded.");
    const toggleUserStatusForms = document.querySelectorAll('form[action*="toggle-user-status"]');
    toggleUserStatusForms.forEach(form => {
        form.addEventListener('submit', function(event) {
            const button = this.querySelector('button[type="submit"]');
            const actionText = button.textContent.trim();
            const confirmMessage = `Anda yakin ingin ${actionText.toLowerCase()} pengguna ini?`;
            if (!confirm(confirmMessage)) {
                event.preventDefault();
            }
        });
    });
});