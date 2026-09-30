// จำกัดวันเช็กเอาต์ให้อยู่หลังวันเช็กอินอย่างน้อย 1 วัน
// ใช้กับ <form data-date-range> ที่มี input [data-check-in] และ [data-check-out]
document.addEventListener("DOMContentLoaded", () => {
    const toIso = (date) => date.toISOString().slice(0, 10);
    const nextDay = (iso) => {
        const d = new Date(iso + "T00:00:00Z");
        d.setUTCDate(d.getUTCDate() + 1);
        return toIso(d);
    };
    const today = () => {
        const now = new Date();
        return toIso(new Date(Date.UTC(now.getFullYear(), now.getMonth(), now.getDate())));
    };

    document.querySelectorAll("form[data-date-range]").forEach((form) => {
        const checkIn = form.querySelector("[data-check-in]");
        const checkOut = form.querySelector("[data-check-out]");
        if (!checkIn || !checkOut) {
            return;
        }

        checkIn.min = today();
        const sync = () => {
            if (!checkIn.value) {
                return;
            }
            const min = nextDay(checkIn.value);
            checkOut.min = min;
            if (checkOut.value && checkOut.value < min) {
                checkOut.value = min;
            }
        };
        checkIn.addEventListener("change", sync);
        sync();
    });
});