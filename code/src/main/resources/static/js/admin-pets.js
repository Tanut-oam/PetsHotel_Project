document.addEventListener("DOMContentLoaded", () => {
  const searchInput = document.getElementById("pet-search");
  const typeFilter = document.getElementById("pet-type-filter");
  const emptyMessage = document.getElementById("pet-filter-empty");
  const rows = Array.from(document.querySelectorAll("#pet-table tbody tr"));

  if (!searchInput || !typeFilter || !emptyMessage || rows.length === 0) {
    return;
  }

  const normalize = (value) => (value ?? "").trim().toLowerCase();

  const filterPets = () => {
    const search = normalize(searchInput.value);
    const type = typeFilter.value;
    let visibleCount = 0;

    for (const row of rows) {
      const name = normalize(row.querySelector(".pet-name")?.textContent);
      const owner = normalize(row.querySelector(".pet-owner")?.textContent);
      const matchesSearch =
        !search || name.includes(search) || owner.includes(search);
      const matchesType =
        type === "ALL" || row.dataset.petType === type;
      const visible = matchesSearch && matchesType;

      row.hidden = !visible;
      if (visible) {
        visibleCount += 1;
      }
    }

    emptyMessage.hidden = visibleCount !== 0;
  };

  searchInput.addEventListener("input", filterPets);
  typeFilter.addEventListener("change", filterPets);
  filterPets();
});