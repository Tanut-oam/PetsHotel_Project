    (function () {
    "use strict";

    const monthFormatter = new Intl.DateTimeFormat(
        "th-TH-u-ca-gregory",
        {
        month: "long",
        year: "numeric"
        }
    );

    function createDate(year, monthIndex, day) {
        const date = new Date(0);

        date.setHours(0, 0, 0, 0);
        date.setFullYear(year, monthIndex, day);

        return date;
    }

    function parseDate(value) {
        if (!value) {
        return null;
        }

        const parts = value.split("-");

        if (parts.length !== 3) {
        return null;
        }

        const year = Number(parts[0]);
        const month = Number(parts[1]);
        const day = Number(parts[2]);

        if (
        !Number.isInteger(year)
        || !Number.isInteger(month)
        || !Number.isInteger(day)
        || year < 1
        || year > 9999
        || month < 1
        || month > 12
        || day < 1
        || day > 31
        ) {
        return null;
        }

        const date = createDate(year, month - 1, day);

        if (
        date.getFullYear() !== year
        || date.getMonth() !== month - 1
        || date.getDate() !== day
        ) {
        return null;
        }

        return date;
    }

    function toIsoDate(date) {
        const year = String(date.getFullYear()).padStart(4, "0");
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");

        return year + "-" + month + "-" + day;
    }

    function formatDate(value) {
        const date = parseDate(value);

        if (date === null) {
        return "ยังไม่ได้เลือก";
        }

        const year = String(date.getFullYear()).padStart(4, "0");
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");

        return day + "/" + month + "/" + year;
    }

    function initialisePromotionPicker(picker) {
        const form = picker.closest("form");

        if (form === null) {
        return;
        }

        const startInput = picker.querySelector("[data-promotion-start]");
        const endInput = picker.querySelector("[data-promotion-end]");
        const openButton = picker.querySelector("[data-promotion-open]");
        const triggerLabel = picker.querySelector("[data-promotion-label]");

        const calendar =
        picker.querySelector("[data-promotion-calendar]");

        const closeButton =
        picker.querySelector("[data-promotion-close]");

        const previousButton =
        picker.querySelector("[data-promotion-previous]");

        const nextButton =
        picker.querySelector("[data-promotion-next]");

        const monthLabel =
        picker.querySelector("[data-promotion-month]");

        const calendarGrid =
        picker.querySelector("[data-promotion-grid]");

        const selectionHelp =
        picker.querySelector("[data-promotion-help]");

        const startSummary =
        picker.querySelector("[data-promotion-start-summary]");

        const endSummary =
        picker.querySelector("[data-promotion-end-summary]");

        const clearButton =
        picker.querySelector("[data-promotion-clear]");

        const applyButton =
        picker.querySelector("[data-promotion-apply]");

        const dateError =
        picker.querySelector("[data-promotion-date-error]");

        let draftStart = "";
        let draftEnd = "";

        const today = new Date();

        let displayedMonth = createDate(
        today.getFullYear(),
        today.getMonth(),
        1
        );

        function hasValidRange() {
        const startDate = parseDate(startInput.value);
        const endDate = parseDate(endInput.value);

        return startDate !== null
            && endDate !== null
            && endDate.getTime() >= startDate.getTime();
        }

        function updateTrigger() {
        if (!hasValidRange()) {
            triggerLabel.textContent =
            "เลือกวันเริ่มต้นและวันสิ้นสุด";

            return;
        }

        triggerLabel.textContent =
            formatDate(startInput.value)
            + " – "
            + formatDate(endInput.value);
        }

        function updateSummary() {
        startSummary.textContent = formatDate(draftStart);
        endSummary.textContent = formatDate(draftEnd);

        applyButton.disabled = !draftStart || !draftEnd;

        if (!draftStart) {
            selectionHelp.textContent = "เลือกวันเริ่มต้นก่อน";
        } else if (!draftEnd) {
            selectionHelp.textContent =
            "เลือกวันสิ้นสุด สามารถเลือกวันเดียวกับวันเริ่มต้นได้";
        } else {
            selectionHelp.textContent =
            "เลือกช่วงวันที่แล้ว กดยืนยันเพื่อใช้งาน";
        }
        }

        function selectDate(dateValue) {
        dateError.hidden = true;

        if (!draftStart || draftEnd) {
            draftStart = dateValue;
            draftEnd = "";
        } else {
            if (dateValue < draftStart) {
            return;
            }

            draftEnd = dateValue;
        }

        renderCalendar();
        }

        function createDayButton(date) {
        const dateValue = toIsoDate(date);
        const button = document.createElement("button");

        button.type = "button";
        button.className = "calendar-day";
        button.textContent = String(date.getDate());
        button.setAttribute("aria-label", formatDate(dateValue));

        const choosingEnd = draftStart && !draftEnd;

        if (choosingEnd && dateValue < draftStart) {
            button.disabled = true;
            button.classList.add("is-outside-range");
        }

        const isSelected = Boolean(
            draftStart
            && dateValue >= draftStart
            && dateValue <= (draftEnd || draftStart)
        );

        button.setAttribute("aria-pressed", String(isSelected));

        if (isSelected) {
            button.classList.add("is-selected");
        }

        if (dateValue === draftStart) {
            button.classList.add("is-check-in");
        }

        if (dateValue === draftEnd) {
            button.classList.add("is-check-out");
        }

        button.addEventListener("click", function () {
            selectDate(dateValue);
        });

        return button;
        }

        function renderCalendar() {
        calendarGrid.replaceChildren();

        monthLabel.textContent =
            monthFormatter.format(displayedMonth);

        const year = displayedMonth.getFullYear();
        const monthIndex = displayedMonth.getMonth();

        const firstWeekday = displayedMonth.getDay();

        const daysInMonth =
            createDate(year, monthIndex + 1, 0).getDate();

        for (let index = 0; index < firstWeekday; index++) {
            const emptyCell = document.createElement("span");

            emptyCell.className = "calendar-day-empty";
            emptyCell.setAttribute("aria-hidden", "true");

            calendarGrid.append(emptyCell);
        }

        for (let day = 1; day <= daysInMonth; day++) {
            calendarGrid.append(
            createDayButton(createDate(year, monthIndex, day))
            );
        }

        previousButton.disabled =
            year === 1 && monthIndex === 0;

        nextButton.disabled =
            year === 9999 && monthIndex === 11;

        updateSummary();
        }

        function openCalendar() {
        const startDate = parseDate(startInput.value);
        const endDate = parseDate(endInput.value);

        draftStart =
            startDate === null ? "" : toIsoDate(startDate);

        draftEnd =
            endDate === null ? "" : toIsoDate(endDate);

        if (!draftStart || draftEnd < draftStart) {
            draftEnd = "";
        }

        const initialDate = startDate || new Date();

        displayedMonth = createDate(
            initialDate.getFullYear(),
            initialDate.getMonth(),
            1
        );

        dateError.hidden = true;

        renderCalendar();

        if (!calendar.open) {
            calendar.showModal();
        }
        }

        openButton.addEventListener("click", openCalendar);

        closeButton.addEventListener("click", function () {
        calendar.close();
        });

        calendar.addEventListener("close", function () {
        openButton.focus({
            preventScroll: true
        });
        });

        previousButton.addEventListener("click", function () {
        displayedMonth = createDate(
            displayedMonth.getFullYear(),
            displayedMonth.getMonth() - 1,
            1
        );

        renderCalendar();
        });

        nextButton.addEventListener("click", function () {
        displayedMonth = createDate(
            displayedMonth.getFullYear(),
            displayedMonth.getMonth() + 1,
            1
        );

        renderCalendar();
        });

        clearButton.addEventListener("click", function () {
        draftStart = "";
        draftEnd = "";
        dateError.hidden = true;

        renderCalendar();
        });

        applyButton.addEventListener("click", function () {
        if (!draftStart || !draftEnd || draftEnd < draftStart) {
            return;
        }

        startInput.value = draftStart;
        endInput.value = draftEnd;

        updateTrigger();

        startInput.dispatchEvent(
            new Event("change", { bubbles: true })
        );

        endInput.dispatchEvent(
            new Event("change", { bubbles: true })
        );

        calendar.close();
        });

        form.addEventListener("reset", function () {
        // อ่านค่าอีกครั้งหลังเบราว์เซอร์รีเซ็ต input แล้ว
        setTimeout(function () {
            updateTrigger();
        }, 0);
        });

        form.addEventListener("submit", function (event) {
        if (hasValidRange()) {
            return;
        }

        event.preventDefault();

        openCalendar();

        dateError.textContent =
            "กรุณาเลือกวันเริ่มต้นและวันสิ้นสุดให้ครบ";

        dateError.hidden = false;
        });

        updateTrigger();
    }

    document.addEventListener("DOMContentLoaded", function () {
        const pickers =
        document.querySelectorAll("[data-promotion-date-picker]");

        for (const picker of pickers) {
        initialisePromotionPicker(picker);
        }
    });
    })();