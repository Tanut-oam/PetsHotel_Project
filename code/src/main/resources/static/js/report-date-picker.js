(() => {
  "use strict";

  const monthFormatter = new Intl.DateTimeFormat("th-TH", {
    month: "long",
    year: "numeric"
  });

  function toIso(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  }

  function parseDate(value) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value || "")) return null;

    const [year, month, day] = value.split("-").map(Number);
    const date = new Date(year, month - 1, day);

    return toIso(date) === value ? date : null;
  }

  function displayDate(value) {
    if (!value) return "ยังไม่ได้เลือก";
    const [year, month, day] = value.split("-");
    return `${day}/${month}/${year}`;
  }

  function monthStart(date) {
    return new Date(date.getFullYear(), date.getMonth(), 1);
  }

  function initialise(picker) {
    const input = picker.querySelector("[data-date-input]");
    const openButton = picker.querySelector("[data-date-open]");
    const label = picker.querySelector("[data-date-label]");
    const error = picker.querySelector("[data-date-error]");
    const dialog = picker.querySelector("[data-date-dialog]");
    const previous = picker.querySelector("[data-date-previous]");
    const next = picker.querySelector("[data-date-next]");
    const monthLabel = picker.querySelector("[data-date-month]");
    const grid = picker.querySelector("[data-date-grid]");
    const summary = picker.querySelector("[data-date-summary]");
    const apply = picker.querySelector("[data-date-apply]");
    const form = picker.closest("form");

    const minimum = parseDate(picker.dataset.min);
    const maximum = parseDate(picker.dataset.max);
    const validRange = minimum && maximum && minimum <= maximum;

    function allowed(value) {
      return Boolean(
        validRange &&
        parseDate(value) &&
        value >= picker.dataset.min &&
        value <= picker.dataset.max
      );
    }

    if (!validRange) {
      openButton.disabled = true;
      error.textContent = "ไม่พบช่วงวันเข้าพักที่ถูกต้อง";
      error.hidden = false;
      form?.addEventListener("submit", event => event.preventDefault());
      return;
    }

    let draft = "";
    let month = monthStart(minimum);

    function render() {
      grid.replaceChildren();
      monthLabel.textContent = monthFormatter.format(month);

      for (let position = 0; position < month.getDay(); position++) {
        const empty = document.createElement("span");
        empty.className = "calendar-day-empty";
        empty.setAttribute("aria-hidden", "true");
        grid.append(empty);
      }

      const days = new Date(
        month.getFullYear(),
        month.getMonth() + 1,
        0
      ).getDate();

      for (let day = 1; day <= days; day++) {
        const value = toIso(
          new Date(month.getFullYear(), month.getMonth(), day)
        );
        const button = document.createElement("button");

        button.type = "button";
        button.className = "calendar-day";
        button.dataset.date = value;
        button.textContent = String(day);
        button.disabled = !allowed(value);
        button.setAttribute("aria-label", displayDate(value));
        button.setAttribute("aria-pressed", String(value === draft));

        if (button.disabled) {
          button.classList.add("is-outside-range");
        }

        if (value === draft) {
          button.classList.add("is-selected");
        }

        grid.append(button);
      }

      previous.disabled = month <= monthStart(minimum);
      next.disabled = month >= monthStart(maximum);
      summary.textContent = displayDate(draft);
      apply.disabled = !allowed(draft);
    }

    function openCalendar() {
      draft = allowed(input.value) ? input.value : "";
      month = monthStart(parseDate(draft) || minimum);
      render();

      if (!dialog.open) dialog.showModal();
    }

    openButton.addEventListener("click", openCalendar);

    picker.querySelector("[data-date-close]").addEventListener(
      "click",
      () => dialog.close()
    );

    dialog.addEventListener("click", event => {
      if (event.target === dialog) dialog.close();
    });

    dialog.addEventListener("close", () => {
      openButton.focus();
    });

    grid.addEventListener("click", event => {
      const button = event.target.closest("[data-date]");
      if (!button || button.disabled) return;

      draft = button.dataset.date;
      render();
      grid.querySelector(`[data-date="${draft}"]`)?.focus();
    });

    previous.addEventListener("click", () => {
      if (previous.disabled) return;

      month = new Date(month.getFullYear(), month.getMonth() - 1, 1);
      render();
    });

    next.addEventListener("click", () => {
      if (next.disabled) return;

      month = new Date(month.getFullYear(), month.getMonth() + 1, 1);
      render();
    });

    picker.querySelector("[data-date-clear]").addEventListener(
      "click",
      () => {
        draft = "";
        render();
      }
    );

    apply.addEventListener("click", () => {
      if (!allowed(draft)) return;

      input.value = draft;
      label.textContent = displayDate(draft);
      error.hidden = true;
      openButton.removeAttribute("aria-invalid");
      input.dispatchEvent(new Event("change", { bubbles: true }));
      dialog.close();
    });

    form?.addEventListener("submit", event => {
      if (allowed(input.value)) return;

      event.preventDefault();
      error.hidden = false;
      openButton.setAttribute("aria-invalid", "true");
      openCalendar();
    });

    form?.addEventListener("reset", () => {
      input.value = "";
      draft = "";
      label.textContent = "เลือกวันที่รายงาน";
      error.hidden = true;
      openButton.removeAttribute("aria-invalid");
      if (dialog.open) dialog.close();
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-report-date-picker]")
      .forEach(initialise);
  });
})();