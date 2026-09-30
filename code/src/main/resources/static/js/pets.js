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

    document.querySelectorAll(".pet-edit-button").forEach((editButton) => {
    const dialogId = editButton.dataset.dialogId;
    const editDialog = document.getElementById(dialogId);
    const closeEditButton = editDialog?.querySelector(".pet-edit-close");

    if (!editDialog || !closeEditButton) {
      return;
    }

    editButton.addEventListener("click", () => {
      editDialog.showModal();
      editDialog.querySelector('input[name="name"]')?.focus();
    });

    closeEditButton.addEventListener("click", () => {
      editDialog.close();
    });

    editDialog.addEventListener("close", () => {
      editButton.focus();
    });

    const editFeedback = editDialog.querySelector("[data-edit-feedback]");
    if (editFeedback) {
      editDialog.showModal();
      editFeedback.focus();
    }
     });
});