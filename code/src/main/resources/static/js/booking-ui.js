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

  function describeRow(row) {
    if (!row) return "";

    return [...row.querySelectorAll("[data-detail-label]")]
      .map(cell =>
        `${cell.dataset.detailLabel}: ${cell.textContent.trim()}`
      )
      .join("\n");
  }

  function openDialog(title, text, form = null) {
    pendingForm = form;
    byId("dialog-title").textContent = title;
    byId("dialog-text").textContent = text;
    dialogConfirm.hidden = !form;
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
      openDialog(
        "รายละเอียดการจอง",
        describeRow(button.closest("[data-booking-row]"))
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
  const filter = byId("booking-filter");

  // หน้ารายละเอียดไม่มีตาราง จบการทำงานส่วนกรองตรงนี้
  if (!search || !filter) return;

  const rows = [...page.querySelectorAll("[data-booking-row]")];

  function applyFilter() {
    const query = search.value.trim().toLocaleLowerCase("th");
    let visible = 0;

    rows.forEach(row => {
      const searchable = [...row.querySelectorAll("[data-detail-label]")]
        .map(cell => cell.textContent.trim())
        .join(" ")
        .toLocaleLowerCase("th");

      const matchesText = searchable.includes(query);
      const matchesStatus =
        !filter.value || row.dataset.status === filter.value;

      row.hidden = !(matchesText && matchesStatus);

      if (!row.hidden) visible++;
    });

    byId("booking-count").textContent =
      `แสดง ${visible} จาก ${rows.length} รายการ`;

    byId("booking-empty").hidden = visible > 0;
  }

  search.addEventListener("input", applyFilter);
  filter.addEventListener("change", applyFilter);

  applyFilter();
})();