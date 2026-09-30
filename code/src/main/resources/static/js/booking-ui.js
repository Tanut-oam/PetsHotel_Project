(() => {
  // ยืนยันก่อนส่งฟอร์มจริง
  document.querySelectorAll("form[data-confirm]").forEach(form => {
    form.addEventListener("submit", event => {
      if (!window.confirm(form.dataset.confirm)) {
        event.preventDefault();
      }
    });
  });

  // กรองแถวที่ backend แสดงมาแล้ว
  const search = document.getElementById("booking-search");
  const filter = document.getElementById("booking-filter");

  if (!search || !filter) return;

  const rows = [...document.querySelectorAll("[data-booking-row]")];
  const count = document.getElementById("booking-count");
  const empty = document.getElementById("booking-empty");

  function applyFilter() {
    const query = search.value.trim().toLocaleLowerCase("th");
    let visible = 0;

    rows.forEach(row => {
      const matchesText =
        row.textContent.toLocaleLowerCase("th").includes(query);

      const matchesStatus =
        !filter.value || row.dataset.status === filter.value;

      row.hidden = !(matchesText && matchesStatus);

      if (!row.hidden) visible++;
    });

    count.textContent = `แสดง ${visible} จาก ${rows.length} รายการ`;
    empty.hidden = visible > 0;
  }

  search.addEventListener("input", applyFilter);
  filter.addEventListener("change", applyFilter);

  applyFilter();
})();