(() => {
  "use strict";

  const widget =
    document.getElementById("room-booking-widget");

  if (!widget) {
    return;
  }

  const roomId = String(widget.dataset.roomId);
  const calendarUrl = widget.dataset.calendarUrl;
  const bookingUrl = widget.dataset.bookingUrl;

  const openButton =
    document.getElementById("open-room-calendar");

  const dialog =
    document.getElementById("room-calendar-dialog");

  const closeButton =
    document.getElementById("close-room-calendar");

  const previousMonthButton =
    document.getElementById("previous-calendar-month");

  const nextMonthButton =
    document.getElementById("next-calendar-month");

  const monthLabel =
    document.getElementById("calendar-month-label");

  const calendarGrid =
    document.getElementById("room-calendar-grid");

  const loading =
    document.getElementById("calendar-loading");

  const calendarError =
    document.getElementById("calendar-error");

  const selectionHelp =
    document.getElementById("calendar-selection-help");

  const checkInSummary =
    document.getElementById("calendar-check-in-summary");

  const checkOutSummary =
    document.getElementById("calendar-check-out-summary");

  const clearButton =
    document.getElementById("clear-calendar-selection");

  const applyButton =
    document.getElementById("apply-calendar-selection");

  const selectedDateRange =
    document.getElementById("selected-date-range");

  const availabilityStatus =
    document.getElementById("room-availability-status");

  const bookingLink =
    document.getElementById("continue-booking");

  const petCount =
    document.getElementById("room-pet-count");

  const thaiDateFormatter =
    new Intl.DateTimeFormat("th-TH", {
      day: "numeric",
      month: "short",
      year: "2-digit"
    });

  const thaiMonthFormatter =
    new Intl.DateTimeFormat("th-TH", {
      month: "long",
      year: "numeric"
    });

  const today = startOfDay(new Date());
  const maximumDate = addDays(today, 365);
  const calendarToExclusive = addDays(maximumDate, 1);

  let displayedMonth = startOfMonth(today);

  let unavailableRanges = [];

  let selectedCheckIn = null;
  let selectedCheckOut = null;

  let draftCheckIn = null;
  let draftCheckOut = null;

  let loadingCalendar = false;

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
    const [year, month, day] =
      value.split("-").map(Number);

    return new Date(
      year,
      month - 1,
      day
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
    if (!value) {
      return "ยังไม่ได้เลือก";
    }

    return thaiDateFormatter.format(
      parseIsoDate(value)
    );
  }

  function sameMonth(first, second) {
    return (
      first.getFullYear() === second.getFullYear()
      && first.getMonth() === second.getMonth()
    );
  }

  function isBefore(date, otherDate) {
    return date.getTime() < otherDate.getTime();
  }

  function isAfter(date, otherDate) {
    return date.getTime() > otherDate.getTime();
  }

  function isOccupied(dateValue) {
    return unavailableRanges.some(range =>
      dateValue >= range.unavailableFrom
      && dateValue < range.availableAgainOn
    );
  }

  /*
   * checkOut ไม่นับเป็นคืนที่เข้าพัก
   * จึงตรวจตั้งแต่ checkIn จนถึงวันก่อน checkOut
   */
  function stayRangeIsAvailable(
    checkInValue,
    checkOutValue
  ) {
    let cursor = parseIsoDate(checkInValue);
    const checkOutDate =
      parseIsoDate(checkOutValue);

    while (isBefore(cursor, checkOutDate)) {
      if (isOccupied(toIsoDate(cursor))) {
        return false;
      }

      cursor = addDays(cursor, 1);
    }

    return true;
  }

  function canSelectAsCheckOut(dateValue) {
    if (!draftCheckIn) {
      return false;
    }

    if (dateValue <= draftCheckIn) {
      return false;
    }

    return stayRangeIsAvailable(
      draftCheckIn,
      dateValue
    );
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

  function updateCalendarSummary() {
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
      "ตรวจสอบช่วงวันที่แล้ว กดยืนยันเพื่อดำเนินการต่อ";

    applyButton.disabled = false;
  }

  function createEmptyCalendarCell() {
    const cell = document.createElement("span");

    cell.className = "calendar-day-empty";
    cell.setAttribute("aria-hidden", "true");

    return cell;
  }

  function createCalendarDay(date) {
    const dateValue = toIsoDate(date);
    const occupied = isOccupied(dateValue);
    const past = isBefore(date, today);
    const beyondMaximum =
      isAfter(date, maximumDate);

    const choosingNewCheckIn =
      !draftCheckIn || draftCheckOut;

    const validCheckOut =
      !choosingNewCheckIn
      && canSelectAsCheckOut(dateValue);

    const selectable = !past
      && !beyondMaximum
      && (
        choosingNewCheckIn
          ? !occupied
          : validCheckOut
      );

    const button =
      document.createElement("button");

    button.type = "button";
    button.className = "calendar-day";
    button.dataset.date = dateValue;
    button.textContent = String(date.getDate());

    button.setAttribute(
      "role",
      "gridcell"
    );

    button.setAttribute(
      "aria-label",
      thaiDateFormatter.format(date)
    );

    if (past || beyondMaximum) {
      button.classList.add("is-outside-range");
      button.disabled = true;
    } else if (occupied) {
      button.classList.add("is-unavailable");

      /*
       * วันเริ่มการจองถัดไปสามารถเป็นวัน
       * checkOut ของลูกค้าคนก่อนหน้าได้
       */
      if (validCheckOut) {
        button.classList.add(
          "is-checkout-boundary"
        );

        button.title =
          "เลือกเป็นวันเช็กเอาต์ได้";
      } else {
        button.disabled = true;
        button.title = "ห้องไม่ว่าง";
      }
    }

    if (!selectable) {
      button.setAttribute(
        "aria-disabled",
        "true"
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
        createEmptyCalendarCell()
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

    const currentMonth =
      startOfMonth(today);

    const finalMonth =
      startOfMonth(maximumDate);

    previousMonthButton.disabled =
      displayedMonth.getTime()
      <= currentMonth.getTime();

    nextMonthButton.disabled =
      displayedMonth.getTime()
      >= finalMonth.getTime();

    updateCalendarSummary();
  }

  function selectDate(dateValue) {
    const choosingNewCheckIn =
      !draftCheckIn || draftCheckOut;

    if (choosingNewCheckIn) {
      if (isOccupied(dateValue)) {
        return;
      }

      draftCheckIn = dateValue;
      draftCheckOut = null;

      renderCalendar();
      return;
    }

    if (!canSelectAsCheckOut(dateValue)) {
      return;
    }

    draftCheckOut = dateValue;

    renderCalendar();
  }

  function setStatus(message, type) {
    availabilityStatus.textContent = message;

    availabilityStatus.className =
      "room-availability-status";

    availabilityStatus.classList.add(
      `is-${type}`
    );
  }

  function disableBookingLink() {
    bookingLink.removeAttribute("href");

    bookingLink.setAttribute(
      "aria-disabled",
      "true"
    );

    bookingLink.setAttribute(
      "tabindex",
      "-1"
    );

    bookingLink.textContent =
      "เลือกวันเข้าพักก่อน";
  }

  function enableBookingLink() {
    const url = new URL(
      bookingUrl,
      window.location.origin
    );

    url.searchParams.set(
      "roomId",
      roomId
    );

    url.searchParams.set(
      "checkIn",
      selectedCheckIn
    );

    url.searchParams.set(
      "checkOut",
      selectedCheckOut
    );

    bookingLink.href = url.toString();

    bookingLink.setAttribute(
      "aria-disabled",
      "false"
    );

    bookingLink.removeAttribute("tabindex");

    bookingLink.textContent =
      "จองห้องนี้";
  }

  function updateMainSelection() {
    if (!selectedCheckIn || !selectedCheckOut) {
      selectedDateRange.textContent =
        "เลือกเช็กอินและเช็กเอาต์";

      setStatus(
        "กรุณาเลือกช่วงวันเข้าพัก",
        "neutral"
      );

      disableBookingLink();
      return;
    }

    selectedDateRange.textContent =
      `${formatDate(selectedCheckIn)} – `
      + `${formatDate(selectedCheckOut)}`;

    setStatus(
      "ห้องว่างในช่วงวันที่เลือก",
      "success"
    );

    enableBookingLink();
  }

  function clearSelection() {
    draftCheckIn = null;
    draftCheckOut = null;

    selectedCheckIn = null;
    selectedCheckOut = null;

    updateMainSelection();
    renderCalendar();
  }

  function responseErrorMessage(body) {
    if (body?.message) {
      return body.message;
    }

    return "โหลดปฏิทินไม่สำเร็จ กรุณาลองอีกครั้ง";
  }

  async function loadAvailabilityCalendar() {
    if (loadingCalendar) {
      return;
    }

    loadingCalendar = true;

    loading.hidden = false;
    calendarError.hidden = true;
    calendarGrid.hidden = true;

    setStatus(
      "กำลังตรวจสอบวันที่ว่าง...",
      "loading"
    );

    const url = new URL(
      calendarUrl,
      window.location.origin
    );

    url.searchParams.set(
      "fromDate",
      toIsoDate(today)
    );

    url.searchParams.set(
      "toDate",
      toIsoDate(calendarToExclusive)
    );

    try {
      const response = await fetch(url, {
        method: "GET",
        credentials: "same-origin",
        headers: {
          Accept: "application/json"
        }
      });

      const body = await response
        .json()
        .catch(() => null);

      if (!response.ok) {
        throw new Error(
          responseErrorMessage(body)
        );
      }

      unavailableRanges =
        Array.isArray(body.unavailableRanges)
          ? body.unavailableRanges
          : [];

      if (
        selectedCheckIn
        && selectedCheckOut
        && !stayRangeIsAvailable(
          selectedCheckIn,
          selectedCheckOut
        )
      ) {
        selectedCheckIn = null;
        selectedCheckOut = null;

        draftCheckIn = null;
        draftCheckOut = null;

        updateMainSelection();
      }

      calendarGrid.hidden = false;
      renderCalendar();

      if (selectedCheckIn && selectedCheckOut) {
        setStatus(
          "ห้องว่างในช่วงวันที่เลือก",
          "success"
        );
      } else {
        setStatus(
          "เลือกช่วงวันจากปฏิทิน",
          "neutral"
        );
      }
    } catch (error) {
      calendarError.textContent =
        error.message
        || "โหลดปฏิทินไม่สำเร็จ กรุณาลองอีกครั้ง";

      calendarError.hidden = false;

      setStatus(
        "ยังโหลดวันที่ว่างไม่ได้",
        "error"
      );
    } finally {
      loading.hidden = true;
      loadingCalendar = false;
    }
  }

  function openCalendar() {
    draftCheckIn = selectedCheckIn;
    draftCheckOut = selectedCheckOut;

    displayedMonth = startOfMonth(
      selectedCheckIn
        ? parseIsoDate(selectedCheckIn)
        : today
    );

    renderCalendar();
    dialog.showModal();

    loadAvailabilityCalendar();
  }

  openButton.addEventListener(
    "click",
    openCalendar
  );

  closeButton.addEventListener(
    "click",
    () => dialog.close()
  );

  previousMonthButton.addEventListener(
    "click",
    () => {
      displayedMonth = addMonths(
        displayedMonth,
        -1
      );

      renderCalendar();
    }
  );

  nextMonthButton.addEventListener(
    "click",
    () => {
      displayedMonth = addMonths(
        displayedMonth,
        1
      );

      renderCalendar();
    }
  );

  calendarGrid.addEventListener(
    "click",
    event => {
      const dayButton =
        event.target.closest(
          "button[data-date]"
        );

      if (!dayButton || dayButton.disabled) {
        return;
      }

      selectDate(dayButton.dataset.date);
    }
  );

  clearButton.addEventListener(
    "click",
    clearSelection
  );

  applyButton.addEventListener(
    "click",
    () => {
      if (!draftCheckIn || !draftCheckOut) {
        return;
      }

      if (!stayRangeIsAvailable(
        draftCheckIn,
        draftCheckOut
      )) {
        calendarError.textContent =
          "ช่วงวันที่เลือกมีวันที่ห้องไม่ว่าง";

        calendarError.hidden = false;
        return;
      }

      selectedCheckIn = draftCheckIn;
      selectedCheckOut = draftCheckOut;

      updateMainSelection();
      dialog.close();

      openButton.focus();
    }
  );

  dialog.addEventListener(
    "click",
    event => {
      if (event.target === dialog) {
        dialog.close();
      }
    }
  );

  dialog.addEventListener(
    "cancel",
    event => {
      event.preventDefault();
      dialog.close();
    }
  );

  petCount.addEventListener(
    "change",
    () => {
      if (selectedCheckIn && selectedCheckOut) {
        enableBookingLink();
      }
    }
  );

  updateMainSelection();
})();