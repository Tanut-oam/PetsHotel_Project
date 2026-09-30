(() => {
  const page = document.querySelector("[data-booking-page]");
  if (!page) return;

  const mode = page.dataset.bookingPage;
  const byId = id => document.getElementById(id);

  const labels = {
    PENDING: "รอยืนยัน",
    CONFIRMED: "ยืนยันแล้ว",
    CHECKED_IN: "เข้าพักแล้ว",
    CHECKED_OUT: "เช็กเอาต์แล้ว",
    CANCELLED: "ยกเลิกแล้ว"
  };

  // ข้อมูลสำหรับทดลอง UX เท่านั้น ไม่ใช่ข้อมูลจากระบบ
  const examples = [
    {
      id: 1025, customer: "ผู้ใช้ตัวอย่าง A", pets: ["Mochi"],
      room: "Deluxe 101", checkIn: "2026-10-10", checkOut: "2026-10-15",
      status: "CONFIRMED", paid: false, total: 2500
    },
    {
      id: 1024, customer: "ผู้ใช้ตัวอย่าง A", pets: ["Lily"],
      room: "Standard 201", checkIn: "2026-10-12", checkOut: "2026-10-14",
      status: "PENDING", paid: false, total: 1000
    },
    {
      id: 1023, customer: "ผู้ใช้ตัวอย่าง B", pets: ["Coco"],
      room: "Deluxe 102", checkIn: "2026-10-01", checkOut: "2026-10-04",
      status: "CHECKED_IN", paid: true, total: 1500
    },
    {
      id: 1022, customer: "ผู้ใช้ตัวอย่าง A", pets: ["Mochi", "Lily"],
      room: "Family 301", checkIn: "2026-09-20", checkOut: "2026-09-23",
      status: "CHECKED_OUT", paid: true, total: 3000
    },
    {
      id: 1021, customer: "ผู้ใช้ตัวอย่าง A", pets: ["Lily"],
      room: "Standard 201", checkIn: "2026-09-15", checkOut: "2026-09-18",
      status: "CANCELLED", paid: false, total: 1500
    },
    {
      id: 1020, customer: "ผู้ใช้ตัวอย่าง A", pets: ["Mochi"],
      room: "Deluxe 101", checkIn: "2026-10-20", checkOut: "2026-10-22",
      status: "CONFIRMED", paid: true, total: 1000
    }
  ];

  // การกรองนี้มีไว้จัดข้อมูลสาธิต ไม่ใช่ระบบตรวจสิทธิ์
  const rows = mode === "staff"
    ? examples
    : examples.filter(item => item.customer === "ผู้ใช้ตัวอย่าง A");

  const money = value => new Intl.NumberFormat("th-TH", {
    style: "currency", currency: "THB"
  }).format(value);

  const dialog = byId("booking-dialog");
  const dialogConfirm = byId("dialog-confirm");
  let pendingAction = null;

  function canCancel(item) {
    return !item.paid &&
      ["PENDING", "CONFIRMED"].includes(item.status);
  }

  function describe(item) {
    return [
      `เลขที่: #${item.id}`,
      `ผู้จอง: ${item.customer}`,
      `สัตว์: ${item.pets.join(", ")}`,
      `ห้อง: ${item.room}`,
      `วันเข้า–วันออก: ${item.checkIn} – ${item.checkOut}`,
      `สถานะ: ${labels[item.status]}`,
      `การชำระเงิน: ${item.paid ? "ชำระแล้ว" : "ยังไม่ชำระ"}`,
      `ยอดตัวอย่าง: ${money(item.total)}`
    ].join("\n");
  }

  function showDialog(title, text, action = null) {
    pendingAction = action;
    byId("dialog-title").textContent = title;
    byId("dialog-text").textContent = text;
    dialogConfirm.hidden = !action;
    dialogConfirm.style.display = action ? "" : "none";
    dialog.showModal();
  }

  function previewAction(item, action) {
    showDialog(
      `${action} #${item.id}`,
      `${describe(item)}\n\nนี่เป็นการทดลอง UX เท่านั้น ไม่เปลี่ยนข้อมูลจริง`,
      `${action} #${item.id}`
    );
  }

  byId("dialog-close").addEventListener("click", () => dialog.close());

  dialogConfirm.addEventListener("click", () => {
    if (!pendingAction) return;

    const message = byId("booking-message");
    message.textContent =
      `ทดลองคำสั่ง “${pendingAction}” แล้ว — ยังไม่ได้ส่งคำขอหรือเปลี่ยนสถานะ`;
    message.hidden = false;

    dialog.close();
    message.scrollIntoView({ behavior: "smooth", block: "center" });
  });

  dialog.addEventListener("close", () => {
    pendingAction = null;
  });

  function button(text, action) {
    const element = document.createElement("button");
    element.type = "button";
    element.className = "btn btn-small";
    element.textContent = text;
    element.addEventListener("click", action);
    return element;
  }

  function addActions(container, item, includeDetails = true) {
    if (includeDetails) {
      container.append(button("รายละเอียด", () =>
        showDialog(`การจองตัวอย่าง #${item.id}`, describe(item))
      ));
    }

    if (mode === "staff") {
      const action = {
        PENDING: "ยืนยันการจอง",
        CONFIRMED: "เช็กอิน",
        CHECKED_IN: "เช็กเอาต์"
      }[item.status];

      if (action) {
        container.append(button(action, () => previewAction(item, action)));
      }
    }

    if (canCancel(item)) {
      container.append(button("ยกเลิก", () =>
        previewAction(item, "ยกเลิกการจอง")
      ));
    }
  }

  function renderTable() {
    const query = byId("booking-search").value.trim().toLocaleLowerCase("th");
    const status = byId("booking-filter").value;

    const filtered = rows.filter(item => {
      const searchable = [
        item.id, item.customer, item.room, ...item.pets
      ].join(" ").toLocaleLowerCase("th");

      return (!status || item.status === status) &&
        searchable.includes(query);
    });

    const tbody = byId("booking-rows");
    tbody.replaceChildren();

    for (const item of filtered) {
      const row = document.createElement("tr");

      [
        `#${item.id}`,
        item.pets.join(", "),
        item.room,
        `${item.checkIn} – ${item.checkOut}`,
        labels[item.status]
      ].forEach(value => {
        const cell = document.createElement("td");
        cell.textContent = value;
        row.append(cell);
      });

      const actions = document.createElement("td");
      const group = document.createElement("div");
      group.className = "row";
      addActions(group, item);
      actions.append(group);
      row.append(actions);
      tbody.append(row);
    }

    byId("booking-count").textContent =
      `แสดง ${filtered.length} จาก ${rows.length} รายการตัวอย่าง`;
    byId("booking-empty").hidden = filtered.length > 0;
  }

  function renderDetail() {
    const id = Number(byId("demo-booking-select").value);
    const item = rows.find(entry => entry.id === id);
    if (!item) return;

    const container = byId("booking-detail");
    container.replaceChildren();

    const title = document.createElement("h2");
    title.textContent = `การจอง #${item.id}`;

    const details = document.createElement("p");
    details.style.whiteSpace = "pre-line";
    details.textContent = describe(item);

    const actions = document.createElement("div");
    actions.className = "row";
    addActions(actions, item, false);

    const note = document.createElement("p");
    note.className = "muted";
    note.textContent = canCancel(item)
      ? "สามารถทดลองกล่องยืนยันยกเลิกได้"
      : "ตัวอย่างนี้ไม่แสดงปุ่มยกเลิก เพราะจ่ายแล้วหรือสถานะไม่อนุญาต";

    container.append(title, details, actions, note);
  }

  if (mode === "detail") {
    const select = byId("demo-booking-select");

    rows.forEach(item => {
      const option = document.createElement("option");
      option.value = String(item.id);
      option.textContent = `#${item.id} · ${labels[item.status]}`;
      select.append(option);
    });

    select.addEventListener("change", () => {
      byId("booking-message").hidden = true;
      renderDetail();
    });

    renderDetail();
  } else {
    byId("booking-search").addEventListener("input", renderTable);
    byId("booking-filter").addEventListener("change", renderTable);
    renderTable();
  }
})();
