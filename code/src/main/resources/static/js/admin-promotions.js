document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("promotion-form");
    const message = document.getElementById("promotion-message");

    if (!form || !message) {
        return;
    }

    const submitButton = form.querySelector('button[type="submit"]');

    if (!submitButton) {
        return;
    }

    submitButton.disabled = false;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const fields = form.elements;

        const promotion = {
            name: fields.namedItem("name").value.trim(),
            type: fields.namedItem("type").value,
            discountValue: Number(fields.namedItem("discountValue").value),
            startDate: fields.namedItem("startDate").value,
            endDate: fields.namedItem("endDate").value,
            active: fields.namedItem("active").checked
        };

        message.textContent = "";
        submitButton.disabled = true;
        const editingId = form.dataset.editingId;

        const url = editingId
            ? `/api/promotions/${encodeURIComponent(editingId)}`
            : "/api/promotions";

        const method = editingId ? "PUT" : "POST";

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(promotion)
            });

            if (!response.ok) {
                const error = await response.json();

                if (error.fieldErrors
                        && Object.keys(error.fieldErrors).length > 0) {
                    message.textContent =
                        Object.values(error.fieldErrors).join(", ");
                } else {
                    message.textContent =
                        error.message || "บันทึกโปรโมชันไม่สำเร็จ";
                }

                return;
            }

            window.location.reload();
        } catch (error) {
            message.textContent =
                "ติดต่อระบบไม่ได้ กรุณาลองใหม่อีกครั้ง";
        } finally {
            submitButton.disabled = false;
        }
    });

    const addButton = document.getElementById("add-promotion");

addButton.addEventListener("click", () => {
    form.reset();
    delete form.dataset.editingId;
    submitButton.textContent = "บันทึกโปรโมชัน";
    message.textContent = "";
});

const editButtons = document.querySelectorAll(".edit-promotion");

for (const button of editButtons) {
    button.addEventListener("click", async () => {
        const promotionId = button.dataset.promotionId;

        if (!promotionId) {
            message.textContent = "ไม่พบรหัสโปรโมชัน";
            message.scrollIntoView({ block: "center" });
            return;
        }

        button.disabled = true;
        message.textContent = "";

        try {
            const response = await fetch(
                `/api/promotions/${encodeURIComponent(promotionId)}`
            );

            if (!response.ok) {
                const error = await response.json().catch(() => null);
                message.textContent =
                    error?.message || "โหลดข้อมูลโปรโมชันไม่สำเร็จ";
                message.scrollIntoView({ block: "center" });
                return;
            }

            const promotion = await response.json();
            const fields = form.elements;

            fields.namedItem("name").value = promotion.name;
            fields.namedItem("type").value = promotion.type;
            fields.namedItem("discountValue").value = promotion.value;
            fields.namedItem("startDate").value = promotion.startDate;
            fields.namedItem("endDate").value = promotion.endDate;
            fields.namedItem("active").checked = promotion.active;

            form.dataset.editingId = promotionId;
            submitButton.textContent = "บันทึกการแก้ไข";
            form.scrollIntoView({ block: "center" });
        } catch (error) {
            message.textContent =
                "ติดต่อระบบไม่ได้ กรุณาลองใหม่อีกครั้ง";
            message.scrollIntoView({ block: "center" });
        } finally {
            button.disabled = false;
        }
    });
}

    const deactivateButtons = document.querySelectorAll(".deactivate-promotion");

    for (const button of deactivateButtons) {
        button.addEventListener("click", async () => {
            const promotionId = button.dataset.promotionId;

            if (!promotionId) {
                message.textContent = "ไม่พบรหัสโปรโมชัน";
                return;
            }

            message.textContent = "";
            button.disabled = true;

            try {
                const response = await fetch(
                    `/api/promotions/${encodeURIComponent(promotionId)}`,
                    { method: "DELETE" }
                );

                if (!response.ok) {
                    const error = await response.json().catch(() => null);
                    message.textContent =
                        error?.message || "ปิดใช้โปรโมชันไม่สำเร็จ";
                    message.scrollIntoView({ block: "center" });
                    return;
                }

                window.location.reload();
            } catch (error) {
                message.textContent =
                    "ติดต่อระบบไม่ได้ กรุณาลองใหม่อีกครั้ง";
                message.scrollIntoView({ block: "center" });
            } finally {
                button.disabled = false;
            }
        });
    }

});