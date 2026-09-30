document.addEventListener("DOMContentLoaded", () => {
  const addButton = document.getElementById("add-pet-button");
  const petDialog = document.getElementById("pet-create-dialog");
  const closeButton = document.getElementById("close-pet-dialog");

  if (!addButton || !petDialog || !closeButton) {
    return;
  }

  const feedback = petDialog.querySelector("[data-pet-feedback]");

  function openDialog() {
    if (!petDialog.open) {
      petDialog.showModal();
    }
  }

  addButton.addEventListener("click", () => {
    openDialog();
    petDialog.querySelector('input[name="name"]')?.focus();
  });

  closeButton.addEventListener("click", () => {
    petDialog.close();
  });

  petDialog.addEventListener("close", () => {
    addButton.focus();
  });

  if (feedback) {
    openDialog();
    feedback.setAttribute("tabindex", "-1");
    feedback.focus();
  }
});