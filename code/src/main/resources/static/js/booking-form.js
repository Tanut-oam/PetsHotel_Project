(() => {
  const root = document.getElementById("booking-form");
  if (!root) return;

  const byId = id => document.getElementById(id);
  const steps = [...root.querySelectorAll("[data-booking-step]")];
  const indicators = [...document.querySelectorAll("[data-step-indicator]")];
  const room = byId("room-id");
  const checkIn = byId("check-in");
  const checkOut = byId("check-out");
  const previous = byId("booking-prev");
  const next = byId("booking-next");
  const confirm = byId("booking-confirm");
  const error = byId("booking-error");
  const result = byId("booking-result");
  let currentStep = 0;

  function pets() {
    return [...root.querySelectorAll('input[name="petIds"]:checked')];
  }

  function services() {
    return [...root.querySelectorAll("[data-service-id]")];
  }

  function nights() {
    if (!checkIn.value || !checkOut.value) return 0;

    const start = Date.parse(`${checkIn.value}T00:00:00Z`);
    const end = Date.parse(`${checkOut.value}T00:00:00Z`);

    return Math.max(0, Math.round((end - start) / 86400000)) || 0;
  }

  function selectedRoom() {
    return room.selectedOptions[0];
  }

  function updateSummary() {
    const roomName = selectedRoom()?.dataset.name || "ยังไม่ได้เลือก";
    const selectedPets = pets();
    const count = nights();

    const selectedServices = services()
      .filter(input => Number(input.value) > 0)
      .map(input => `${input.dataset.name} ${input.value} ครั้ง`);

    byId("summary-room").textContent = roomName;
    byId("summary-nights").textContent = count > 0 ? `${count} คืน` : "—";
    byId("summary-pets").textContent = `${selectedPets.length} ตัว`;

    byId("pet-selection-count").textContent =
      `เลือกแล้ว ${selectedPets.length} ตัว`;

    byId("review-room").textContent = roomName;
    byId("review-dates").textContent =
      checkIn.value && checkOut.value
        ? `${checkIn.value} – ${checkOut.value}`
        : "—";
    byId("review-nights").textContent = count > 0 ? `${count} คืน` : "—";
    byId("review-pets").textContent =
      selectedPets.map(input => input.dataset.name).join(", ") || "ยังไม่ได้เลือก";
    byId("review-services").textContent =
      selectedServices.join(", ") || "ไม่เลือกบริการเสริม";
  }

  function showStep(index, focus = true) {
    currentStep = index;

    steps.forEach((section, position) => {
      section.hidden = position !== index;
      section.style.display = position === index ? "" : "none";
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

    previous.disabled = index === 0;
    next.disabled = false;
    next.hidden = index === steps.length - 1;
    next.style.display = next.hidden ? "none" : "";

    confirm.disabled = false;
    confirm.hidden = index !== steps.length - 1;
    confirm.style.display = confirm.hidden ? "none" : "";

    updateSummary();

    if (focus) {
      steps[index].querySelector("h2")?.focus();
    }
  }

  function fail(message, input) {
    error.textContent = message;
    error.hidden = false;
    input?.focus();
    return false;
  }

  function validateStep(index) {
    error.hidden = true;

    for (const input of steps[index].querySelectorAll("input, select")) {
      if (!input.checkValidity()) {
        return fail(input.validationMessage || "กรุณาตรวจสอบข้อมูล", input);
      }
    }

    if (index === 0 && nights() < 1) {
      return fail("วันเช็กเอาต์ต้องอยู่หลังวันเช็กอิน", checkOut);
    }

    if (index === 1) {
      const selectedPets = pets();
      const firstPet = root.querySelector('input[name="petIds"]');

      if (selectedPets.length === 0) {
        return fail("กรุณาเลือกสัตว์เลี้ยงอย่างน้อย 1 ตัว", firstPet);
      }

      const capacity = Number(selectedRoom()?.dataset.capacity || 0);

      if (selectedPets.length > capacity) {
        return fail(`ห้องนี้รับสัตว์เลี้ยงได้สูงสุด ${capacity} ตัว`, firstPet);
      }
    }

    if (index === 2) {
      for (const input of services()) {
        const quantity = Number(input.value);

        if (!Number.isSafeInteger(quantity) || quantity < 0) {
          return fail("จำนวนบริการต้องเป็นจำนวนเต็มตั้งแต่ 0 ขึ้นไป", input);
        }
      }
    }

    return true;
  }

  previous.addEventListener("click", () => {
    error.hidden = true;
    if (currentStep > 0) showStep(currentStep - 1);
  });

  next.addEventListener("click", () => {
    if (validateStep(currentStep)) showStep(currentStep + 1);
  });

  confirm.addEventListener("click", () => {
    for (let index = 0; index < steps.length - 1; index++) {
      showStep(index, false);
      if (!validateStep(index)) return;
    }

    showStep(steps.length - 1);
    result.textContent =
      "ตรวจสอบข้อมูลตัวอย่างเรียบร้อย ยังไม่ได้สร้างการจองหรือชำระเงิน";
    result.hidden = false;
    result.scrollIntoView({ behavior: "smooth", block: "center" });
  });

  root.addEventListener("input", () => {
    result.hidden = true;
    error.hidden = true;
    updateSummary();
  });

  root.addEventListener("change", updateSummary);

  showStep(0, false);
})();