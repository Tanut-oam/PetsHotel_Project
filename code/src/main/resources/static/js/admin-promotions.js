document.addEventListener("DOMContentLoaded", () => {
    const forms = document.querySelectorAll(".deactivate-promotion-form");

    for (const form of forms) {
        form.addEventListener("submit", (event) => {
            const name = form.dataset.name;
            const confirmed = window.confirm(
                `ต้องการปิดใช้โปรโมชัน "${name}" หรือไม่?`
            );

            if (!confirmed) {
                event.preventDefault();
            }
        });
    }
});