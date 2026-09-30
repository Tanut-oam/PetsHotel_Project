document.addEventListener("DOMContentLoaded", () => {
    const dialog = document.querySelector(".admin-form-dialog");
    const form = dialog.querySelector("form");
    const addButton = document.querySelector("[data-open-form]");
    const cancelButton = dialog.querySelector("[data-close-form]");
    const parameters = new URLSearchParams(window.location.search);

    const hasEditingOrErrors = form.dataset.autoOpen === "true";
    const requestedCreate = parameters.get("create") === "true";

    if (hasEditingOrErrors || requestedCreate) {
        dialog.showModal();
    }

    addButton.addEventListener("click", (event) => {
        if (hasEditingOrErrors) {
            return;
        }

        event.preventDefault();
        form.reset();
        dialog.showModal();
    });

    cancelButton.addEventListener("click", () => {
        dialog.close();
    });
});