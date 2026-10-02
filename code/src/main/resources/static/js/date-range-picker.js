(() => {
  "use strict";

  const thaiDateFormatter =
    new Intl.DateTimeFormat("th-TH", {
      day: "numeric",
      month: "short",
      year: "numeric"
    });

  const thaiMonthFormatter =
    new Intl.DateTimeFormat("th-TH", {
      month: "long",
      year: "numeric"
    });

  function startOfDay(date) {
    return new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate()
    );
  }

  function startOfMonth(date) {
    return new Date(
      date.getFullYear(),
      date.getMonth(),
      1
    );
  }

  function addDays(date, amount) {
    const result = new Date(date);

    result.setDate(result.getDate() + amount);

    return startOfDay(result);
  }

  function addMonths(date, amount) {
    return new Date(
      date.getFullYear(),
      date.getMonth() + amount,
      1
    );
  }

  function parseIsoDate(value) {
    if (!value) {
      return null;
    }

    const parts = value
      .split("-")
      .map(Number);

    if (
      parts.length !== 3
      || parts.some(Number.isNaN)
    ) {
      return null;
    }

    return new Date(
      parts[0],
      parts[1] - 1,
      parts[2]
    );
  }

  function toIsoDate(date) {
    const year = date.getFullYear();

    const month = String(
      date.getMonth() + 1
    ).padStart(2, "0");

    const day = String(
      date.getDate()
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
  }

  function formatDate(value) {
    const date = parseIsoDate(value);

    if (!date) {
      return "ยังไม่ได้เลือก";
    }

    return thaiDateFormatter.format(date);
  }

  function sameMonth(first, second) {
    return (
      first.getFullYear() === second.getFullYear()
      && first.getMonth() === second.getMonth()
    );
  }

  function initialisePicker(picker) {
    const openButton =
      picker.querySelector("[data-range-open]");

    const dialog =
      picker.querySelector("[data-range-dialog]");

    const closeButton =
      picker.querySelector("[data-range-close]");

    const previousMonthButton =
      picker.querySelector("[data-range-previous]");

    const nextMonthButton =
      picker.querySelector("[data-range-next]");

    const monthLabel =
      picker.querySelector("[data-range-month]");

    const calendarGrid =
      picker.querySelector("[data-range-grid]");

    const selectionHelp =
      picker.querySelector("[data-range-help]");

    const checkInSummary =
      picker.querySelector("[data-range-check-in-summary]");

    const checkOutSummary =
      picker.querySelector("[data-range-check-out-summary]");

    const clearButton =
      picker.querySelector("[data-range-clear]");

    const applyButton =
      picker.querySelector("[data-range-apply]");

    const triggerLabel =
      picker.querySelector("[data-range-label]");

    const checkInInput =
      picker.querySelector("[data-range-check-in]");

    const checkOutInput =
      picker.querySelector("[data-range-check-out]");

    if (
      !openButton
      || !dialog
      || !closeButton
      || !previousMonthButton
      || !nextMonthButton
      || !monthLabel
      || !calendarGrid
      || !selectionHelp
      || !checkInSummary
      || !checkOutSummary
      || !clearButton
      || !applyButton
      || !triggerLabel
      || !checkInInput
      || !checkOutInput
    ) {
      return;
    }

    const today = startOfDay(new Date());
    const maximumDate = addDays(today, 365);

    let selectedCheckIn =
      normaliseSelectedDate(checkInInput.value);

    let selectedCheckOut =
      normaliseSelectedDate(checkOutInput.value);

    if (
      !selectedCheckIn
      || !selectedCheckOut
      || selectedCheckOut <= selectedCheckIn
    ) {
      selectedCheckIn = "";
      selectedCheckOut = "";
    }

    let draftCheckIn = selectedCheckIn;
    let draftCheckOut = selectedCheckOut;

    let displayedMonth = startOfMonth(
      parseIsoDate(selectedCheckIn) || today
    );

    function normaliseSelectedDate(value) {
      const date = parseIsoDate(value);

      if (!date) {
        return "";
      }

      if (
        date.getTime() < today.getTime()
        || date.getTime() > maximumDate.getTime()
      ) {
        return "";
      }

      return toIsoDate(date);
    }

    function isDateInDraftRange(dateValue) {
      if (!draftCheckIn) {
        return false;
      }

      if (!draftCheckOut) {
        return dateValue === draftCheckIn;
      }

      return (
        dateValue >= draftCheckIn
        && dateValue <= draftCheckOut
      );
    }

    function updateSummary() {
      checkInSummary.textContent =
        formatDate(draftCheckIn);

      checkOutSummary.textContent =
        formatDate(draftCheckOut);

      if (!draftCheckIn) {
        selectionHelp.textContent =
          "เลือกวันเช็กอินก่อน";

        applyButton.disabled = true;
        return;
      }

      if (!draftCheckOut) {
        selectionHelp.textContent =
          "เลือกวันเช็กเอาต์";

        applyButton.disabled = true;
        return;
      }

      selectionHelp.textContent =
        "เลือกช่วงวันที่แล้ว กดยืนยันเพื่อใช้งาน";

      applyButton.disabled = false;
    }

    function updateTrigger() {
      checkInInput.value = selectedCheckIn;
      checkOutInput.value = selectedCheckOut;

      if (!selectedCheckIn || !selectedCheckOut) {
        triggerLabel.textContent =
          "เลือกเช็กอินและเช็กเอาต์";

        return;
      }

      triggerLabel.textContent =
        `${formatDate(selectedCheckIn)} – `
        + `${formatDate(selectedCheckOut)}`;
    }

    function createEmptyCell() {
      const cell =
        document.createElement("span");

      cell.className = "calendar-day-empty";
      cell.setAttribute("aria-hidden", "true");

      return cell;
    }

    function createCalendarDay(date) {
      const dateValue = toIsoDate(date);

      const past =
        date.getTime() < today.getTime();

      const beyondMaximum =
        date.getTime() > maximumDate.getTime();

      const choosingNewCheckIn =
        !draftCheckIn || draftCheckOut;

      const invalidCheckOut =
        !choosingNewCheckIn
        && dateValue <= draftCheckIn;

      const button =
        document.createElement("button");

      button.type = "button";
      button.className = "calendar-day";
      button.dataset.date = dateValue;
      button.textContent =
        String(date.getDate());

      button.setAttribute(
        "role",
        "gridcell"
      );

      button.setAttribute(
        "aria-label",
        thaiDateFormatter.format(date)
      );

      if (
        past
        || beyondMaximum
        || invalidCheckOut
      ) {
        button.disabled = true;

        button.setAttribute(
          "aria-disabled",
          "true"
        );
      }

      if (past || beyondMaximum) {
        button.classList.add(
          "is-outside-range"
        );
      }

      if (isDateInDraftRange(dateValue)) {
        button.classList.add("is-selected");
      }

      if (dateValue === draftCheckIn) {
        button.classList.add("is-check-in");
      }

      if (dateValue === draftCheckOut) {
        button.classList.add("is-check-out");
      }

      return button;
    }

    function renderCalendar() {
      calendarGrid.replaceChildren();

      monthLabel.textContent =
        thaiMonthFormatter.format(
          displayedMonth
        );

      const firstWeekday =
        displayedMonth.getDay();

      const daysInMonth =
        new Date(
          displayedMonth.getFullYear(),
          displayedMonth.getMonth() + 1,
          0
        ).getDate();

      for (
        let position = 0;
        position < firstWeekday;
        position += 1
      ) {
        calendarGrid.append(
          createEmptyCell()
        );
      }

      for (
        let day = 1;
        day <= daysInMonth;
        day += 1
      ) {
        calendarGrid.append(
          createCalendarDay(
            new Date(
              displayedMonth.getFullYear(),
              displayedMonth.getMonth(),
              day
            )
          )
        );
      }

      const firstMonth =
        startOfMonth(today);

      const finalMonth =
        startOfMonth(maximumDate);

      previousMonthButton.disabled =
        displayedMonth.getTime()
        <= firstMonth.getTime();

      nextMonthButton.disabled =
        displayedMonth.getTime()
        >= finalMonth.getTime();

      updateSummary();
    }

    function selectDate(dateValue) {
      const choosingNewCheckIn =
        !draftCheckIn || draftCheckOut;

      if (choosingNewCheckIn) {
        draftCheckIn = dateValue;
        draftCheckOut = "";

        renderCalendar();
        return;
      }

      if (dateValue <= draftCheckIn) {
        return;
      }

      draftCheckOut = dateValue;

      renderCalendar();
    }

    function openCalendar() {
      draftCheckIn = selectedCheckIn;
      draftCheckOut = selectedCheckOut;

      displayedMonth = startOfMonth(
        parseIsoDate(draftCheckIn) || today
      );

      renderCalendar();
      dialog.showModal();
    }

    function closeCalendar() {
      dialog.close();
    }

    calendarGrid.addEventListener(
      "click",
      (calendarEvent) => {
        const dayButton =
          calendarEvent.target.closest(
            ".calendar-day"
          );

        if (
          !dayButton
          || dayButton.disabled
        ) {
          return;
        }

        selectDate(dayButton.dataset.date);
      }
    );

    previousMonthButton.addEventListener(
      "click",
      () => {
        displayedMonth =
          addMonths(displayedMonth, -1);

        renderCalendar();
      }
    );

    nextMonthButton.addEventListener(
      "click",
      () => {
        displayedMonth =
          addMonths(displayedMonth, 1);

        renderCalendar();
      }
    );

    clearButton.addEventListener(
      "click",
      () => {
        draftCheckIn = "";
        draftCheckOut = "";

        renderCalendar();
      }
    );

    applyButton.addEventListener(
      "click",
      () => {
        if (!draftCheckIn || !draftCheckOut) {
          return;
        }

        selectedCheckIn = draftCheckIn;
        selectedCheckOut = draftCheckOut;

        updateTrigger();

        checkInInput.dispatchEvent(
          new Event("change", {
            bubbles: true
          })
        );

        checkOutInput.dispatchEvent(
          new Event("change", {
            bubbles: true
          })
        );

        closeCalendar();
      }
    );

    openButton.addEventListener(
      "click",
      openCalendar
    );

    closeButton.addEventListener(
      "click",
      closeCalendar
    );

    dialog.addEventListener(
      "click",
      (dialogEvent) => {
        if (dialogEvent.target === dialog) {
          closeCalendar();
        }
      }
    );

    const form = picker.closest("form");

    if (form) {
      form.addEventListener(
        "submit",
        (submitEvent) => {
          if (
            selectedCheckIn
            && selectedCheckOut
          ) {
            return;
          }

          submitEvent.preventDefault();

          selectionHelp.textContent =
            "กรุณาเลือกวันเช็กอินและเช็กเอาต์";

          openCalendar();
        }
      );
    }

    updateTrigger();
  }

  document.addEventListener(
    "DOMContentLoaded",
    () => {
      document
        .querySelectorAll(
          "[data-date-range-picker]"
        )
        .forEach(initialisePicker);
    }
  );
})();