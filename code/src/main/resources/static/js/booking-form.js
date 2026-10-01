(() => {
  const form = document.getElementById("booking-form");
  if (!form) return;

  const byId = id => document.getElementById(id);

  const steps = [
    ...form.querySelectorAll("[data-booking-step]")
  ];

  const indicators = [
    ...document.querySelectorAll("[data-step-indicator]")
  ];

  const groups = [
    ...form.querySelectorAll("[data-service-group]")
  ];

  const room = byId("room-id");
  const checkIn = byId("check-in");
  const checkOut = byId("check-out");
  const promotion = byId("promotion-id");

  const previous = byId("booking-prev");
  const next = byId("booking-next");
  const submit = byId("booking-confirm");

  const error = byId("booking-error");
  const dialog = byId("create-dialog");

  const csrfToken =
    document.querySelector('meta[name="_csrf"]')?.content;

  const csrfHeader =
    document.querySelector('meta[name="_csrf_header"]')?.content;

  let currentStep = 0;
  let approved = false;
  let sending = false;

  let timer;
  let pendingRequest;
  let revision = 0;
  let quoteKey = null;
  let quoteTotal = null;

  const pets = () => [
    ...form.querySelectorAll('input[name="petIds"]:checked')
  ];

  const recipients = group => [
    ...group.querySelectorAll("[data-service-pet]")
  ].filter(input => input.checked && !input.disabled);

  const money = value =>
    Number(value).toLocaleString("th-TH", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }) + " บาท";

  const roomName = () =>
    room.selectedOptions[0]?.dataset.name || "ยังไม่ได้เลือก";

  const promotionName = () =>
    promotion.selectedOptions[0]?.textContent.trim()
      || "ไม่ใช้โปรโมชัน";

  function nights() {
    if (!checkIn.value || !checkOut.value) return 0;

    return Math.max(
      0,
      Math.round(
        (
          Date.parse(checkOut.value + "T00:00:00Z")
          - Date.parse(checkIn.value + "T00:00:00Z")
        ) / 86400000
      )
    ) || 0;
  }

  function syncServices() {
    const selected = new Set(
      pets().map(input => input.value)
    );

    groups.forEach(group => {
      const toggle = group.querySelector("[data-service-toggle]");

      group.querySelector("[data-service-recipients]").hidden =
        !toggle.checked;

      group.querySelectorAll("[data-recipient-row]").forEach(row => {
        const input = row.querySelector("[data-service-pet]");
        const included = selected.has(input.value);

        row.hidden = !included;
        input.disabled = !included || !toggle.checked;

        if (input.disabled) {
          input.checked = false;
        }
      });

      group.querySelector("[data-no-recipients]").hidden =
        selected.size > 0;
    });
  }

  function payload() {
    const servicePetIds = {};

    groups.forEach(group => {
      const ids = recipients(group).map(input =>
        Number(input.value)
      );

      if (ids.length) {
        servicePetIds[group.dataset.serviceId] = ids;
      }
    });

    return {
      roomId: Number(room.value),
      petIds: pets().map(input => Number(input.value)),
      checkInDate: checkIn.value,
      checkOutDate: checkOut.value,
      servicePetIds,
      promotionId: promotion.value
        ? Number(promotion.value)
        : null
    };
  }

  function problem() {
    if (!room.value || !checkIn.value || !checkOut.value) {
      return "เลือกห้องและวันที่เพื่อคำนวณราคา";
    }

    if (nights() < 1) {
      return "วันเช็กเอาต์ต้องอยู่หลังวันเช็กอิน";
    }

    if (!pets().length) {
      return "เลือกสัตว์เลี้ยงอย่างน้อย 1 ตัว";
    }

    const capacity = Number(
      room.selectedOptions[0]?.dataset.capacity
    );

    if (!Number.isFinite(capacity) || pets().length > capacity) {
      return "จำนวนสัตว์เลี้ยงเกินความจุห้อง";
    }

    for (const group of groups) {
      const toggle = group.querySelector("[data-service-toggle]");

      if (toggle.checked && !recipients(group).length) {
        return "เลือกสัตว์ที่รับบริการ " + group.dataset.serviceName;
      }
    }

    return null;
  }

  function updateSummary() {
    const services = groups.flatMap(group => {
      const selected = recipients(group);

      return selected.length
        ? [
            group.dataset.serviceName + ": "
            + selected.map(input => input.dataset.petName).join(", ")
          ]
        : [];
    }).join("\n") || "ไม่เลือกบริการเสริม";

    byId("summary-room").textContent = roomName();

    byId("summary-nights").textContent =
      nights() ? nights() + " คืน" : "—";

    byId("summary-pets").textContent =
      pets().length + " ตัว";

    byId("summary-services").textContent = services;

    byId("pet-selection-count").textContent =
      "เลือกแล้ว " + pets().length + " ตัว";

    byId("review-room").textContent = roomName();

    byId("review-dates").textContent =
      checkIn.value && checkOut.value
        ? checkIn.value + " – " + checkOut.value
        : "—";

    byId("review-nights").textContent =
      nights() ? nights() + " คืน" : "—";

    byId("review-pets").textContent =
      pets().map(input => input.dataset.name).join(", ")
        || "ยังไม่ได้เลือก";

    byId("review-services").textContent = services;
    byId("review-promotion").textContent = promotionName();
  }

  function clearPrice() {
    // เก็บตัวเลขล่าสุดบนหน้าจอ แต่ยกเลิกผลราคาสำหรับการยืนยัน
    quoteKey = null;
    quoteTotal = null;
  }

  function hasDisplayedPrice() {
    return byId("price-total").textContent.trim() !== "—";
  }

  function refreshPrice(delay = 350) {
    clearTimeout(timer);

    pendingRequest?.abort();
    pendingRequest = null;

    const version = ++revision;

    approved = false;
    clearPrice();

    byId("price-retry").hidden = true;

    const issue = problem();

    const priceNote = hasDisplayedPrice()
      ? "ราคาที่แสดงเป็นผลคำนวณล่าสุด ยังไม่อัปเดตตามตัวเลือกใหม่ — "
      : "";

    byId("price-status").textContent =
      priceNote + (issue || "กำลังคำนวณราคา…");

    if (issue) return;

    const data = payload();
    const key = JSON.stringify(data);

    timer = setTimeout(
      () => fetchPrice(data, key, version),
      delay
    );
  }

  async function fetchPrice(data, key, version) {
    const controller = new AbortController();

    pendingRequest = controller;

    let timedOut = false;

    const timeout = setTimeout(() => {
      timedOut = true;
      controller.abort();
    }, 15000);

    try {
      if (!csrfToken || !csrfHeader) {
        throw new Error("กรุณาโหลดหน้าใหม่เพื่อยืนยันคำขอ");
      }

      const response = await fetch(form.dataset.previewUrl, {
        method: "POST",
        credentials: "same-origin",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
          [csrfHeader]: csrfToken
        },
        body: JSON.stringify(data),
        signal: controller.signal
      });

      if (version !== revision) return;

      if (response.status === 401) {
        throw new Error("กรุณาเข้าสู่ระบบใหม่");
      }

      if (response.status === 403) {
        throw new Error("กรุณาโหลดหน้าใหม่หรือตรวจสิทธิ์บัญชี");
      }

      if (
        !response.headers.get("content-type")
          ?.includes("application/json")
      ) {
        throw new Error("ไม่ได้รับผลราคา กรุณาโหลดหน้าใหม่");
      }

      const result = await response.json();

      if (version !== revision) return;

      if (!response.ok) {
        throw new Error(
          result.message || "ไม่สามารถคำนวณราคาได้"
        );
      }

      const fields = [
        "basePrice",
        "extraServicesPrice",
        "holidaySurcharge",
        "discountAmount",
        "totalPrice"
      ];

      if (
        fields.some(field =>
          result[field] == null
          || !Number.isFinite(Number(result[field]))
        )
      ) {
        throw new Error("ข้อมูลราคาไม่ครบ");
      }

      if (key !== JSON.stringify(payload())) return;

      byId("price-room").textContent =
        money(result.basePrice);

      byId("price-services").textContent =
        money(result.extraServicesPrice);

      byId("price-surcharge").textContent =
        money(result.holidaySurcharge);

      byId("price-discount").textContent =
        (Number(result.discountAmount) > 0 ? "−" : "")
        + money(result.discountAmount);

      byId("price-total").textContent =
        money(result.totalPrice);

      byId("review-total").textContent =
        money(result.totalPrice);

      quoteKey = key;
      quoteTotal = result.totalPrice;

      byId("price-status").textContent =
        "คำนวณจากตัวเลือกปัจจุบันแล้ว ยังไม่ได้จองหรือรับชำระเงิน";
    } catch (failure) {
      if (version !== revision) return;

      if (failure.name === "AbortError" && !timedOut) return;

      clearPrice();

      const message = timedOut
        ? "คำนวณราคาใช้เวลานาน กรุณาลองอีกครั้ง"
        : failure.message || "เชื่อมต่อไม่สำเร็จ";

      byId("price-status").textContent =
        (
          hasDisplayedPrice()
            ? "ราคาที่แสดงเป็นผลคำนวณล่าสุด ยังไม่อัปเดตตามตัวเลือกใหม่ — "
            : ""
        ) + message;

      byId("price-retry").hidden = false;
    } finally {
      clearTimeout(timeout);

      if (pendingRequest === controller) {
        pendingRequest = null;
      }
    }
  }

  function showStep(index, focus = true) {
    currentStep = index;

    steps.forEach((section, position) => {
      section.hidden = position !== index;
    });

    indicators.forEach((indicator, position) => {
      indicator.classList.toggle("active", position === index);
      indicator.classList.toggle("done", position < index);

      if (position === index) {
        indicator.setAttribute("aria-current", "step");
      } else {
        indicator.removeAttribute("aria-current");
      }
    });

    previous.hidden = false;
    previous.disabled = sending || index === 0;

    next.hidden = index === steps.length - 1;
    next.disabled = sending;

    submit.hidden = index !== steps.length - 1;
    submit.disabled = sending;

    updateSummary();

    if (focus) {
      steps[index].querySelector("h2")?.focus();
    }
  }

  function fail(index, message, input) {
    showStep(index, false);

    error.textContent = message;
    error.hidden = false;

    (input || error).focus();

    return false;
  }

  function validateStep(index) {
    for (const input of steps[index].querySelectorAll("input, select")) {
      if (!input.disabled && !input.checkValidity()) {
        return fail(index, input.validationMessage, input);
      }
    }

    if (index === 0 && nights() < 1) {
      return fail(
        index,
        "วันเช็กเอาต์ต้องอยู่หลังวันเช็กอิน",
        checkOut
      );
    }

    if (index === 1) {
      if (!pets().length) {
        return fail(index, "เลือกสัตว์เลี้ยงอย่างน้อย 1 ตัว");
      }

      const capacity = Number(
        room.selectedOptions[0]?.dataset.capacity
      );

      if (!Number.isFinite(capacity) || pets().length > capacity) {
        return fail(index, "จำนวนสัตว์เลี้ยงเกินความจุห้อง");
      }
    }

    if (index === 2) {
      for (const group of groups) {
        const toggle = group.querySelector("[data-service-toggle]");

        if (toggle.checked && !recipients(group).length) {
          return fail(
            index,
            "เลือกสัตว์ที่รับบริการ " + group.dataset.serviceName,
            toggle
          );
        }
      }
    }

    return true;
  }

  previous.addEventListener("click", () => {
    error.hidden = true;

    if (currentStep > 0) {
      showStep(currentStep - 1);
    }
  });

  next.addEventListener("click", () => {
    error.hidden = true;

    if (validateStep(currentStep)) {
      showStep(currentStep + 1);
    }
  });

  form.addEventListener("submit", event => {
    if (sending) {
      event.preventDefault();
      return;
    }

    if (currentStep < steps.length - 1) {
      event.preventDefault();

      if (validateStep(currentStep)) {
        showStep(currentStep + 1);
      }

      return;
    }

    error.hidden = true;

    for (let index = 0; index < steps.length; index++) {
      if (!validateStep(index)) {
        event.preventDefault();
        approved = false;
        return;
      }
    }

    if (!quoteKey || quoteKey !== JSON.stringify(payload())) {
      event.preventDefault();
      approved = false;

      fail(
        3,
        "กรุณารอผลราคา หรือกดคำนวณอีกครั้งก่อนยืนยัน"
      );

      return;
    }

    if (!approved) {
      event.preventDefault();

      byId("create-dialog-text").textContent = [
        "ห้อง: " + roomName(),
        "วันเข้า–วันออก: " + checkIn.value + " – " + checkOut.value,
        "สัตว์เลี้ยง: " + byId("review-pets").textContent,
        "บริการ:\n" + byId("review-services").textContent,
        "ยอดรวมประมาณการ: " + money(quoteTotal),
        "",
        "ระบบตรวจสอบราคาและห้องว่างอีกครั้ง ต้องการสร้างการจองหรือไม่?"
      ].join("\n");

      dialog.showModal();
      return;
    }

    approved = false;
    sending = true;

    previous.disabled = true;
    next.disabled = true;
    submit.disabled = true;

    submit.textContent = "กำลังสร้างการจอง…";
  });

  byId("create-dialog-close").addEventListener("click", () => {
    dialog.close();
  });

  byId("create-dialog-confirm").addEventListener("click", () => {
    if (sending) return;

    dialog.close();
    approved = true;

    form.requestSubmit(submit);
  });

  function changed() {
    approved = false;
    error.hidden = true;

    syncServices();
    updateSummary();
    refreshPrice();
  }

  form.addEventListener("input", changed);
  form.addEventListener("change", changed);

  byId("price-retry").addEventListener("click", () => {
    refreshPrice(0);
  });

  groups.forEach(group => {
    const toggle = group.querySelector("[data-service-toggle]");

    toggle.hidden = false;

    toggle.checked = [
      ...group.querySelectorAll("[data-service-pet]")
    ].some(input => input.checked);
  });

  form.noValidate = true;

  syncServices();

  const errorStep = steps.findIndex(section =>
    section.querySelector("[data-field-error]")
  );

  showStep(errorStep >= 0 ? errorStep : 0, false);
  refreshPrice();

  byId("server-errors")?.focus();

  window.addEventListener("pageshow", () => {
    approved = false;
    sending = false;

    if (dialog.open) {
      dialog.close();
    }

    submit.textContent = "สร้างการจอง";

    syncServices();
    showStep(currentStep, false);
    refreshPrice();
  });
})();