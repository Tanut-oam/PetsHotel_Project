document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".report-pet-button").forEach((button) => {
    const dialog = document.getElementById(button.dataset.dialogId);
    const closeButton = dialog?.querySelector(".care-pet-close");

    if (!dialog || !closeButton) {
      return;
    }

    button.addEventListener("click", () => {
      dialog.showModal();
    });

    closeButton.addEventListener("click", () => {
      dialog.close();
    });

    dialog.addEventListener("close", () => {
      button.focus();
    });

    if (dialog.dataset.autoOpen === "true") {
      dialog.showModal();
      dialog.querySelector(".care-pet-feedback")?.focus();
    }
  });
});