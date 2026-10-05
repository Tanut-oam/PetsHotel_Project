(() => {
  const page = document.querySelector("[data-booking-page]");
  if (!page) return;

  const byId = id => document.getElementById(id);

  const statusLabels = {
    PENDING: "รอยืนยัน",
    CONFIRMED: "ยืนยันแล้ว",
    CHECKED_IN: "เข้าพักแล้ว",
    CHECKED_OUT: "เช็กเอาต์แล้ว",
    CANCELLED: "ยกเลิกแล้ว"
  };

  const paymentLabels = {
    UNPAID: "ยังไม่ชำระ",
    PAID: "ชำระแล้ว"
  };

  page.querySelectorAll("[data-status-label]").forEach(element => {
    const value = element.dataset.statusLabel;
    element.textContent = statusLabels[value] || value || "ไม่ระบุ";
  });

  page.querySelectorAll("[data-payment-label]").forEach(element => {
    const value = element.dataset.paymentLabel;
    element.textContent = paymentLabels[value] || value || "ไม่ระบุ";
  });

  const dialog = byId("booking-dialog");
  const dialogConfirm = byId("dialog-confirm");

  const actionForms = [...page.querySelectorAll("form[data-confirm]")];
  const sendingForms = new WeakSet();

  let pendingForm = null;
  let approvedForm = null;

  function bookingDetailCells(row) {
    if (!row) return [];

    return [
      ...row.querySelectorAll(
        '[data-detail-label]:not([data-detail-label="ดำเนินการ"])'
      )
    ];
  }

  function normalizeText(element) {
    return element.textContent
      .replace(/\s+/g, " ")
      .trim();
  }

  function describeRow(row) {
    return bookingDetailCells(row)
      .map(cell => {
        const label = cell.dataset.detailLabel;
        const value = normalizeText(cell);

        return `${label}: ${value}`;
      })
      .join("\n");
  }

  function openDialog(title, text, form = null) {
    const dialogText = byId("dialog-text");

    pendingForm = form;

    byId("dialog-title").textContent = title;

    dialogText.classList.remove("is-structured");
    dialogText.textContent = text;

    dialogConfirm.hidden = !form;
    dialog.showModal();
  }

  function openDetailsDialog(row) {
    const dialogText = byId("dialog-text");

    pendingForm = null;

    byId("dialog-title").textContent =
      "รายละเอียดการจอง";

    dialogText.replaceChildren();
    dialogText.classList.add("is-structured");

    bookingDetailCells(row).forEach(cell => {
      const detailRow =
        document.createElement("div");

      const label =
        document.createElement("span");

      const value =
        document.createElement("span");

      detailRow.className =
        "booking-dialog-detail-row";

      label.className =
        "booking-dialog-detail-label";

      value.className =
        "booking-dialog-detail-value";

      label.textContent =
        cell.dataset.detailLabel;

      value.textContent =
        normalizeText(cell);

      detailRow.append(label, value);
      dialogText.append(detailRow);
    });

    dialogConfirm.hidden = true;
    dialog.showModal();
  }

  byId("dialog-close").addEventListener("click", () => {
    dialog.close();
  });

  dialog.addEventListener("close", () => {
    // ป้องกัน close event เก่าล้างข้อมูลของ dialog ที่เปิดใหม่
    if (!dialog.open) pendingForm = null;
  });

  actionForms.forEach(form => {
    form.addEventListener("submit", event => {
      if (sendingForms.has(form)) {
        event.preventDefault();
        return;
      }

      if (approvedForm === form) {
        approvedForm = null;
        sendingForms.add(form);

        form.querySelectorAll('button[type="submit"]').forEach(button => {
          button.disabled = true;
        });

        // Browser ส่ง POST พร้อมข้อมูลและ CSRF ใน form
        return;
      }

      event.preventDefault();

      const details = describeRow(form.closest("[data-booking-row]"));
      const message = form.dataset.confirm;

      openDialog(
        "ยืนยันการดำเนินการ",
        details ? `${details}\n\n${message}` : message,
        form
      );
    });
  });

  dialogConfirm.addEventListener("click", () => {
    const form = pendingForm;
    if (!form || sendingForms.has(form)) return;

    dialog.close();
    pendingForm = null;
    approvedForm = form;

    form.requestSubmit();

    // ถ้า browser ไม่ส่งเพราะ validation ไม่ผ่าน
    approvedForm = null;
  });

  page.querySelectorAll("[data-show-details]").forEach(button => {
    button.hidden = false;

    button.addEventListener("click", () => {
      openDetailsDialog(
        button.closest("[data-booking-row]")
      );
    });
  });

  window.addEventListener("pageshow", () => {
    approvedForm = null;
    pendingForm = null;

    if (dialog.open) dialog.close();

    actionForms.forEach(form => {
      sendingForms.delete(form);

      form.querySelectorAll('button[type="submit"]').forEach(button => {
        button.disabled = false;
      });
    });
  });

    const search = byId("booking-search");
  const statusFilter = byId("booking-filter");

  const paymentFilter =
    byId("booking-payment-filter");

  const sortSelect =
    byId("booking-sort");

  const quickFilterButtons = [
    ...page.querySelectorAll(
      "[data-booking-quick-filter]"
    )
  ];

  /*
   * หน้ารายละเอียดไม่มีตาราง
   * จึงจบการทำงานของส่วนกรองตรงนี้
   */
  if (!search || !statusFilter) {
    return;
  }

  const rows = [
    ...page.querySelectorAll(
      "[data-booking-row]"
    )
  ];

  const rowsContainer =
    byId("booking-rows")
    || rows[0]?.parentElement;

  let activeQuickFilter = "ALL";

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

  function addDays(date, amount) {
    const result = new Date(date);

    result.setDate(
      result.getDate() + amount
    );

    return result;
  }

  const todayDate = new Date();
  const today = toIsoDate(todayDate);

  /*
   * นับวันนี้รวมเป็นวันแรก
   * จึงเพิ่มอีก 6 วัน
   */
  const upcomingEnd =
    toIsoDate(addDays(todayDate, 6));

  function isActionRequired(row) {
    const status = row.dataset.status;
    const checkIn = row.dataset.checkIn;
    const checkOut = row.dataset.checkOut;

    if (status === "PENDING") {
      return true;
    }

    if (
      status === "CONFIRMED"
      && checkIn
      && checkIn <= today
    ) {
      return true;
    }

    return (
      status === "CHECKED_IN"
      && checkOut
      && checkOut <= today
    );
  }

  function matchesQuickFilter(
    row,
    quickFilter
  ) {
    const status = row.dataset.status;
    const payment = row.dataset.payment;
    const checkIn = row.dataset.checkIn;
    const checkOut = row.dataset.checkOut;

    switch (quickFilter) {
      case "ACTION_REQUIRED":
        return isActionRequired(row);

      case "CHECK_IN_TODAY":
        return (
          status === "CONFIRMED"
          && checkIn === today
        );

      case "CHECK_OUT_TODAY":
        return (
          status === "CHECKED_IN"
          && checkOut === today
        );

      case "UNPAID":
        return (
          payment === "UNPAID"
          && status !== "CANCELLED"
        );

      case "UPCOMING_7_DAYS":
        return (
          status !== "CANCELLED"
          && status !== "CHECKED_OUT"
          && checkIn
          && checkIn >= today
          && checkIn <= upcomingEnd
        );

      default:
        return true;
    }
  }

  function actionPriority(row) {
    const status = row.dataset.status;
    const checkIn = row.dataset.checkIn;
    const checkOut = row.dataset.checkOut;
    const payment = row.dataset.payment;

    if (status === "PENDING") {
      return 0;
    }

    if (
      status === "CONFIRMED"
      && checkIn === today
    ) {
      return 1;
    }

    if (
      status === "CHECKED_IN"
      && checkOut === today
    ) {
      return 2;
    }

    if (
      status === "CONFIRMED"
      && checkIn < today
    ) {
      return 3;
    }

    if (
      status === "CHECKED_IN"
      && checkOut < today
    ) {
      return 4;
    }

    if (status === "CHECKED_IN") {
      return 5;
    }

    if (
      payment === "UNPAID"
      && status !== "CANCELLED"
    ) {
      return 6;
    }

    if (status === "CONFIRMED") {
      return 7;
    }

    if (status === "CHECKED_OUT") {
      return 8;
    }

    if (status === "CANCELLED") {
      return 9;
    }

    return 10;
  }

  function compareRows(first, second) {
    const selectedSort =
      sortSelect?.value || "";

    const firstId =
      Number(first.dataset.bookingId || 0);

    const secondId =
      Number(second.dataset.bookingId || 0);

    const firstTotal =
      Number(first.dataset.total || 0);

    const secondTotal =
      Number(second.dataset.total || 0);

    const firstCheckIn =
      first.dataset.checkIn || "9999-12-31";

    const secondCheckIn =
      second.dataset.checkIn || "9999-12-31";

    switch (selectedSort) {
      case "CHECK_IN_ASC":
        return (
          firstCheckIn.localeCompare(
            secondCheckIn
          )
          || secondId - firstId
        );

      case "NEWEST":
        return secondId - firstId;

      case "TOTAL_DESC":
        return (
          secondTotal - firstTotal
          || secondId - firstId
        );

      case "TOTAL_ASC":
        return (
          firstTotal - secondTotal
          || secondId - firstId
        );

      case "ACTION_FIRST":
        return (
          actionPriority(first)
          - actionPriority(second)
          || firstCheckIn.localeCompare(
            secondCheckIn
          )
          || secondId - firstId
        );

      default:
        return 0;
    }
  }

  function sortRows() {
    if (!sortSelect || !rowsContainer) {
      return;
    }

    const sortedRows =
      [...rows].sort(compareRows);

    sortedRows.forEach(row => {
      rowsContainer.append(row);
    });
  }

  function updateQuickCounts() {
    const filters = [
      "ALL",
      "ACTION_REQUIRED",
      "CHECK_IN_TODAY",
      "CHECK_OUT_TODAY",
      "UNPAID",
      "UPCOMING_7_DAYS"
    ];

    filters.forEach(filterName => {
      const count =
        filterName === "ALL"
          ? rows.length
          : rows.filter(row =>
              matchesQuickFilter(
                row,
                filterName
              )
            ).length;

      const output =
        page.querySelector(
          `[data-quick-count="${filterName}"]`
        );

      if (output) {
        output.textContent =
          String(count);
      }
    });
  }

  function applyFilter() {
    sortRows();

    const query =
      search.value
        .trim()
        .toLocaleLowerCase("th");

    let visible = 0;

    rows.forEach(row => {
      const searchable = bookingDetailCells(row)
        .map(cell =>
          cell.textContent.trim()
        )
        .join(" ")
        .toLocaleLowerCase("th");

      const matchesText =
        searchable.includes(query);

      const matchesStatus =
        !statusFilter.value
        || row.dataset.status
          === statusFilter.value;

      const matchesPayment =
        !paymentFilter?.value
        || row.dataset.payment
          === paymentFilter.value;

      const matchesQuick =
        matchesQuickFilter(
          row,
          activeQuickFilter
        );

      row.hidden = !(
        matchesText
        && matchesStatus
        && matchesPayment
        && matchesQuick
      );

      if (!row.hidden) {
        visible += 1;
      }
    });

    byId("booking-count").textContent =
      `แสดง ${visible} จาก ${rows.length} รายการ`;

    byId("booking-empty").hidden =
      visible > 0;
  }

  function selectQuickFilter(button) {
    activeQuickFilter =
      button.dataset.bookingQuickFilter
      || "ALL";

    quickFilterButtons.forEach(item => {
      const active = item === button;

      item.classList.toggle(
        "is-active",
        active
      );

      item.setAttribute(
        "aria-pressed",
        String(active)
      );
    });

    applyFilter();
  }

  search.addEventListener(
    "input",
    applyFilter
  );

  statusFilter.addEventListener(
    "change",
    applyFilter
  );

  paymentFilter?.addEventListener(
    "change",
    applyFilter
  );

  sortSelect?.addEventListener(
    "change",
    applyFilter
  );

  quickFilterButtons.forEach(button => {
    button.addEventListener(
      "click",
      () => selectQuickFilter(button)
    );
  });

  updateQuickCounts();
  applyFilter();
})();