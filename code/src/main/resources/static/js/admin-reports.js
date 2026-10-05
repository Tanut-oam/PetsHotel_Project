document.addEventListener("DOMContentLoaded", () => {
  const page = document.querySelector(".report-detail");
  if (!page) return;

  const historyTab = page.querySelector('[data-report-tab="history"]');
  const writeTab = page.querySelector('[data-report-tab="write"]');
  const historyPanel = page.querySelector('[data-report-panel="history"]');
  const writePanel = page.querySelector('[data-report-panel="write"]');

  function showTab(name) {
    const writing = name === "write";
    historyTab.setAttribute("aria-selected", String(!writing));
    writeTab.setAttribute("aria-selected", String(writing));
    historyPanel.hidden = writing;
    writePanel.hidden = !writing;
  }

  page.querySelector("[data-show-write]").addEventListener("click", () => {
    writeTab.hidden = false;
    showTab("write");
    writeTab.focus();
  });

  historyTab.addEventListener("click", () => {
    showTab("history");
    writeTab.hidden = true;
  });

  writeTab.addEventListener("click", () => showTab("write"));

  page.querySelectorAll(".care-report-entry").forEach((entry) => {
    const editButton = entry.querySelector("[data-edit-report]");
    const details = entry.querySelector(".care-report-more");

    const toggleEdit = (editing) => {
    if (editing && details) details.open = true;
    entry.classList.toggle("is-editing", editing);
    editButton.setAttribute("aria-expanded", String(editing));
    };

    editButton.addEventListener("click", () => {
      toggleEdit(!entry.classList.contains("is-editing"));
      if (entry.classList.contains("is-editing")) {
        entry.querySelector(".care-report-input")?.focus();
      }
    });

    entry.querySelector("[data-cancel-edit]").addEventListener("click", () => {
      entry.querySelector("form").reset();
      toggleEdit(false);
      editButton.focus();
    });

    if (entry.dataset.openEdit === "true") toggleEdit(true);
  });

  if (page.dataset.openWrite === "true") {
    writeTab.hidden = false;
    showTab("write");
  }
});