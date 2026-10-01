(() => {
  const printButton = document.getElementById("print-receipt");

  if (!printButton) return;

  printButton.hidden = false;

  printButton.addEventListener("click", () => {
    window.print();
  });
})();