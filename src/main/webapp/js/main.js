document.addEventListener('DOMContentLoaded', () => {
    // Sticky Navbar
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 50) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        });
    }

    // Smooth Scroll
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            if (targetId.startsWith('#') && targetId.length > 1) {
                const target = document.querySelector(targetId);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth'
                    });
                }
            }
        });
    });

    // Mobile Menu Toggle
    const menuToggle = document.querySelector('.menu-toggle');
    const navLinks = document.querySelector('.nav-links');
    const navActions = document.querySelector('.nav-actions');

    if (menuToggle && navLinks) {
        menuToggle.addEventListener('click', () => {
            const isHidden = window.getComputedStyle(navLinks).display === 'none';
            if (isHidden) {
                navLinks.style.display = 'flex';
                navLinks.style.flexDirection = 'column';
                navLinks.style.position = 'absolute';
                navLinks.style.top = '100%';
                navLinks.style.left = '0';
                navLinks.style.width = '100%';
                navLinks.style.backgroundColor = 'var(--bg-navy)';
                navLinks.style.padding = '1rem';
                if(navActions) {
                   navActions.style.display = 'flex';
                   navActions.style.flexDirection = 'column';
                   navActions.style.position = 'absolute';
                   navActions.style.top = 'calc(100% + 150px)';
                   navActions.style.left = '0';
                   navActions.style.width = '100%';
                   navActions.style.backgroundColor = 'var(--bg-navy)';
                   navActions.style.padding = '1rem';
                }
            } else {
                navLinks.style.display = 'none';
                if(navActions) navActions.style.display = 'none';
            }
        });
    }

    // Form Validation (Login)
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', (e) => {
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value.trim();
            if (!email || !password) {
                e.preventDefault();
                alert('Please fill in all fields.');
            }
        });
    }

    // Form Validation (Signup)
    const signupForm = document.getElementById('signupForm');
    if (signupForm) {
        signupForm.addEventListener('submit', (e) => {
            const name = document.getElementById('name').value.trim();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value.trim();
            const confirmPassword = document.getElementById('confirmPassword').value.trim();

            if (!name || !email || !password || !confirmPassword) {
                e.preventDefault();
                alert('Please fill in all fields.');
                return;
            }

            if (password !== confirmPassword) {
                e.preventDefault();
                alert('Passwords do not match!');
            }
        });
    }
});
