(() => {
  "use strict";

  const monthFormatter = new Intl.DateTimeFormat("th-TH", {
    month: "long",
    year: "numeric"
  });

  function initialisePicker(picker) {
    const toggle = picker.querySelector("[data-calendar-toggle]");
    const popup = picker.querySelector("[data-calendar-popup]");
    const previous = picker.querySelector("[data-calendar-previous]");
    const next = picker.querySelector("[data-calendar-next]");
    const monthLabel = picker.querySelector("[data-calendar-month]");
    const grid = picker.querySelector("[data-calendar-grid]");

    const reportLinks = new Map(
      Array.from(picker.querySelectorAll("[data-report-date]"))
        .map(link => [link.dataset.reportDate, link.href])
    );

    const months = Array.from(
      new Set(
        Array.from(reportLinks.keys())
          .map(date => date.slice(0, 7))
      )
    ).sort();

    if (months.length === 0) {
      toggle.disabled = true;
      return;
    }

    const selectedDate = picker.dataset.selectedDate;
    let monthIndex = months.indexOf(selectedDate.slice(0, 7));

    if (monthIndex < 0) {
      monthIndex = months.length - 1;
    }

    function renderCalendar() {
      const monthKey = months[monthIndex];
      const [year, month] = monthKey.split("-").map(Number);
      const firstDay = new Date(year, month - 1, 1);
      const daysInMonth = new Date(year, month, 0).getDate();

      monthLabel.textContent = monthFormatter.format(firstDay);
      grid.replaceChildren();

      for (let i = 0; i < firstDay.getDay(); i += 1) {
        const empty = document.createElement("span");
        empty.className = "calendar-day-empty";
        empty.setAttribute("aria-hidden", "true");
        grid.append(empty);
      }

      for (let day = 1; day <= daysInMonth; day += 1) {
        const date = `${monthKey}-${String(day).padStart(2, "0")}`;
        const href = reportLinks.get(date);
        const cell = document.createElement(href ? "a" : "button");

        cell.className = "calendar-day";
        cell.textContent = String(day);

        if (href) {
          cell.href = href;
          cell.classList.add("has-report");
          cell.setAttribute(
            "aria-label",
            `ดูรายงานวันที่ ${String(day).padStart(2, "0")}/`
              + `${String(month).padStart(2, "0")}/${year}`
          );

          if (date === selectedDate) {
            cell.classList.add("is-selected");
            cell.setAttribute("aria-current", "date");
          }
        } else {
          cell.type = "button";
          cell.disabled = true;
          cell.classList.add("is-outside-range");
        }

        grid.append(cell);
      }

      previous.disabled = monthIndex === 0;
      next.disabled = monthIndex === months.length - 1;
    }

    function closeCalendar() {
      popup.hidden = true;
      toggle.setAttribute("aria-expanded", "false");
    }

    toggle.addEventListener("click", () => {
      if (!popup.hidden) {
        closeCalendar();
        return;
      }

      monthIndex = months.indexOf(selectedDate.slice(0, 7));
      renderCalendar();
      popup.hidden = false;
      toggle.setAttribute("aria-expanded", "true");

      const selectedDay = grid.querySelector(
        '[aria-current="date"]'
      ) || grid.querySelector("a.calendar-day");

      selectedDay?.focus();
    });

    previous.addEventListener("click", () => {
      if (monthIndex === 0) return;
      monthIndex -= 1;
      renderCalendar();
    });

    next.addEventListener("click", () => {
      if (monthIndex === months.length - 1) return;
      monthIndex += 1;
      renderCalendar();
    });

    document.addEventListener("click", event => {
      if (!picker.contains(event.target)) {
        closeCalendar();
      }
    });

    picker.addEventListener("keydown", event => {
      if (event.key === "Escape") {
        closeCalendar();
        toggle.focus();
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(
      "[data-customer-report-date-picker]"
    ).forEach(initialisePicker);
  });
})();