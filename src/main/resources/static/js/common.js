document.addEventListener('DOMContentLoaded', function() {
    const logoutForms = document.querySelectorAll('.logout-form');
    logoutForms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!confirm('Anda yakin ingin keluar?')) {
                event.preventDefault();
            }
        });
    });

    const dropdownToggles = document.querySelectorAll('.sidebar .dropdown-toggle');
    dropdownToggles.forEach(toggle => {
        toggle.addEventListener('click', function(event) {
            event.preventDefault();
            const dropdownMenu = this.nextElementSibling;
            if (dropdownMenu && dropdownMenu.classList.contains('dropdown-menu')) {
                dropdownMenu.classList.toggle('show');
                this.classList.toggle('active');
            }
        });
    });

    document.addEventListener('click', function(event) {
        if (!event.target.closest('.sidebar .dropdown')) {
            dropdownToggles.forEach(toggle => {
                const dropdownMenu = toggle.nextElementSibling;
                if (dropdownMenu && dropdownMenu.classList.contains('show')) {
                    dropdownMenu.classList.remove('show');
                    toggle.classList.remove('active');
                }
            });
        }
    });

    const currentPath = window.location.pathname;
    const sidebarLinks = document.querySelectorAll('.sidebar a');

    document.querySelectorAll('.sidebar a.active, .sidebar .dropdown-toggle.active, .sidebar .dropdown-menu.show').forEach(el => {
        el.classList.remove('active', 'show');
    });

    sidebarLinks.forEach(link => {
        const linkHrefAttr = link.getAttribute('th:href');
        if (linkHrefAttr) {
            const cleanLinkHref = linkHrefAttr.replace(/@{|}/g, ''); 
            if (cleanLinkHref === '/') {
                if (currentPath === cleanLinkHref) {
                    link.classList.add('active');
                }
            } else if (currentPath.startsWith(cleanLinkHref)) {
                link.classList.add('active');
                const parentDropdown = link.closest('.dropdown');
                if (parentDropdown) {
                    const parentToggle = parentDropdown.querySelector('.dropdown-toggle');
                    const parentMenu = parentDropdown.querySelector('.dropdown-menu');
                    if (parentToggle) parentToggle.classList.add('active');
                    if (parentMenu) parentMenu.classList.add('show'); 
                }
            }
        }
    });
});