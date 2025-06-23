document.addEventListener('DOMContentLoaded', function() {
    const changePasswordForm = document.querySelector('form[action="/member/change-password"]');
    if (changePasswordForm) {
        changePasswordForm.addEventListener('submit', function(e) {
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const existingError = this.closest('.card').querySelector('.alert-danger');
            if (existingError) {
                existingError.remove();
            }

            if (newPassword !== confirmPassword) {
                e.preventDefault();
                const errorDiv = document.createElement('div');
                errorDiv.className = 'alert alert-danger alert-dismissible fade show mt-3';
                errorDiv.setAttribute('role', 'alert');
                errorDiv.innerHTML = `Konfirmasi password tidak cocok!
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>`;
                
                this.closest('.card').insertBefore(errorDiv, this); 

                setTimeout(() => {
                    const alertElement = this.closest('.card').querySelector('.alert-danger');
                    if(alertElement) alertElement.remove();
                }, 5000); 
            }
        });
    }
});