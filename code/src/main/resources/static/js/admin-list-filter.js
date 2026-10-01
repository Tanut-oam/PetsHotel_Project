    document.addEventListener("DOMContentLoaded", function () {
    const filters = document.getElementById("list-filters");

    if (!filters) {
        return;
    }

    const searchInput = document.getElementById("list-search");
    const statusSelect = document.getElementById("list-status");
    const clearButton = document.getElementById("clear-filters");
    const emptyMessage = document.getElementById("filter-empty");
    const rows = document.querySelectorAll("[data-filter-row]");

    function filterRows() {
        const keyword = searchInput.value.trim().toLowerCase();
        const selectedStatus = statusSelect.value;
        let visibleCount = 0;

        for (const row of rows) {
        const name = (row.dataset.name || "").toLowerCase();

        const matchesName = name.includes(keyword);
        const matchesStatus =
            selectedStatus === "ALL" ||
            row.dataset.status === selectedStatus;

        const visible = matchesName && matchesStatus;

        row.hidden = !visible;

        if (visible) {
            visibleCount++;
        }
        }

        emptyMessage.hidden = rows.length === 0 || visibleCount > 0;
    }

    function clearFilters() {
        searchInput.value = "";
        statusSelect.value = "ALL";

        filterRows();
        searchInput.focus();
    }

    searchInput.addEventListener("input", filterRows);
    statusSelect.addEventListener("change", filterRows);
    clearButton.addEventListener("click", clearFilters);

    filters.hidden = false;
    filterRows();
    });