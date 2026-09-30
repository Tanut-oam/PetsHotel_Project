(() => {
  const form = document.getElementById("booking-form");
  if (!form) return;

  const byId = id => document.getElementById(id);
  const steps = [...form.querySelectorAll("[data-booking-step]")];
  const indicators = [...document.querySelectorAll("[data-step-indicator]")];

  const room = byId("room-id");
  const checkIn = byId("check-in");
  const checkOut = byId("check-out");
  const promotion = byId("promotion-id");
  const previous = byId("booking-prev");
  const next = byId("booking-next");
  const submit = byId("booking-confirm");
  const error = byId("booking-error");

  const dialog = byId("create-dialog");
  const dialogConfirm = byId("create-dialog-confirm");

  let currentStep = 0;
  let approved = false;
  let sending = false;

  const pets = () =>
    [...form.querySelectorAll('input[name="petIds"]:checked')];

  const services = () =>
    [...form.querySelectorAll("[data-service-id]")];

  function nights() {
    if (!checkIn.value || !checkOut.value) return 0;

    const start = Date.parse(`${checkIn.value}T00:00:00Z`);
    const end = Date.parse(`${checkOut.value}T00:00:00Z`);

    return Math.max(0, Math.round((end - start) / 86400000)) || 0;
  }

  function roomName() {
    return room.selectedOptions[0]?.dataset.name || "ยังไม่ได้เลือก";
  }

  function promotionName() {
    return promotion.selectedOptions[0]?.textContent.trim()
      || "ไม่ใช้โปรโมชั่น";
  }

  function updateSummary() {
    const selectedPets = pets();
    const count = nights();

    const selectedServices = services()
      .filter(input => Number(input.value) > 0)
      .map(input => `${input.dataset.name} × ${input.value}`);

    byId("summary-room").textContent = roomName();
    byId("summary-nights").textContent = count > 0 ? `${count} คืน` : "—";
    byId("summary-pets").textContent = `${selectedPets.length} ตัว`;

    byId("pet-selection-count").textContent =
      `เลือกแล้ว ${selectedPets.length} ตัว`;

    byId("review-room").textContent = roomName();
    byId("review-dates").textContent =
      checkIn.value && checkOut.value
        ? `${checkIn.value} – ${checkOut.value}`
        : "—";

    byId("review-nights").textContent = count > 0 ? `${count} คืน` : "—";
    byId("review-pets").textContent =
      selectedPets.map(input => input.dataset.name).join(", ")
      || "ยังไม่ได้เลือก";

    byId("review-services").textContent =
      selectedServices.join(", ") || "ไม่เลือกบริการเสริม";

    byId("review-promotion").textContent = promotionName();
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
      if (!input.checkValidity()) {
        return fail(
          index,
          input.validationMessage || "กรุณาตรวจสอบข้อมูล",
          input
        );
      }
    }

    if (index === 0 && nights() < 1) {
      return fail(index, "วันเช็กเอาต์ต้องอยู่หลังวันเช็กอิน", checkOut);
    }

    if (index === 1) {
      const selectedPets = pets();
      const firstPet = form.querySelector('input[name="petIds"]');

      if (selectedPets.length === 0) {
        return fail(index, "กรุณาเลือกสัตว์เลี้ยงอย่างน้อย 1 ตัว", firstPet);
      }

      const capacity = Number(room.selectedOptions[0]?.dataset.capacity);

      if (!Number.isFinite(capacity) || selectedPets.length > capacity) {
        return fail(index, "จำนวนสัตว์เลี้ยงเกินความจุห้องที่เลือก", firstPet);
      }
    }

    if (index === 2) {
      for (const input of services()) {
        const quantity = Number(input.value);

        if (!Number.isSafeInteger(quantity) || quantity < 0) {
          return fail(
            index,
            "จำนวนบริการต้องเป็นจำนวนเต็มตั้งแต่ 0 ขึ้นไป",
            input
          );
        }
      }
    }

    return true;
  }

  function validateAll() {
    error.hidden = true;

    for (let index = 0; index < steps.length; index++) {
      if (!validateStep(index)) return false;
    }

    return true;
  }

  previous.addEventListener("click", () => {
    error.hidden = true;
    if (currentStep > 0) showStep(currentStep - 1);
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

    // กด Enter ในขั้นก่อนหน้าให้ไปขั้นถัดไปก่อน
    if (currentStep < steps.length - 1) {
      event.preventDefault();
      error.hidden = true;

      if (validateStep(currentStep)) {
        showStep(currentStep + 1);
      }
      return;
    }

    if (!validateAll()) {
      event.preventDefault();
      approved = false;
      return;
    }

    if (!approved) {
      event.preventDefault();
      updateSummary();

      byId("create-dialog-text").textContent = [
        `ห้อง: ${roomName()}`,
        `วันเข้า–วันออก: ${checkIn.value} – ${checkOut.value}`,
        `จำนวนคืน: ${nights()}`,
        `สัตว์เลี้ยง: ${byId("review-pets").textContent}`,
        `บริการ: ${byId("review-services").textContent}`,
        `โปรโมชั่น: ${promotionName()}`,
        "",
        "ต้องการสร้างการจองตามข้อมูลนี้หรือไม่?"
      ].join("\n");

      dialog.showModal();
      return;
    }

    // ปล่อยให้ browser ส่ง POST ตาม action ของ form
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

  dialogConfirm.addEventListener("click", () => {
    if (sending) return;

    dialog.close();
    approved = true;
    form.requestSubmit(submit);
  });

  form.addEventListener("input", () => {
    approved = false;
    error.hidden = true;
    updateSummary();
  });

  form.addEventListener("change", updateSummary);

  window.addEventListener("pageshow", () => {
    approved = false;
    sending = false;
    submit.textContent = "สร้างการจอง";
    showStep(currentStep, false);
  });

  // JavaScript จะตรวจ validity แล้วเปิดขั้นที่ผิดก่อนโฟกัส
  // หากไม่มี JavaScript form ยังตรวจและส่งได้ตามปกติ
  form.noValidate = true;

  const errorStep = steps.findIndex(section =>
    section.querySelector("[data-field-error]")
  );

  showStep(errorStep >= 0 ? errorStep : 0, false);

  if (byId("server-errors")) {
    byId("server-errors").focus();
  }
})();