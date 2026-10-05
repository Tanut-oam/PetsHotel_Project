document.addEventListener("DOMContentLoaded", () => {
    const forms = document.querySelectorAll(".deactivate-extra-service-form");

    for (const form of forms) {
        form.addEventListener("submit", (event) => {
            const name = form.dataset.name;
            const confirmed = window.confirm(
                `ต้องการปิดใช้บริการ "${name}" หรือไม่?`
            );

            if (!confirmed) {
                event.preventDefault();
            }
        });
    }
});