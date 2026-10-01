// เปิดกล่องยืนยันก่อนออกจากระบบ (หน้าบัญชีของฉัน)
(() => {
  const dialog = document.getElementById("logout-dialog");
  const openButton = document.querySelector("[data-open-logout]");
  const closeButton = document.querySelector("[data-close-logout]");

  if (!dialog || !openButton || !closeButton) return;

  openButton.addEventListener("click", () => dialog.showModal());
  closeButton.addEventListener("click", () => dialog.close());
})();