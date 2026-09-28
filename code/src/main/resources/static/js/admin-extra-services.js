document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("extra-service-form");
    const message = document.getElementById("extra-service-message");

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

        const extraService = {
            name: fields.namedItem("name").value.trim(),
            description: fields.namedItem("description").value.trim(),
            price: Number(fields.namedItem("price").value),
            active: fields.namedItem("active").checked
        };

        message.textContent = "";
        submitButton.disabled = true;
        const editingId = form.dataset.editingId;

        const url = editingId
            ? `/api/extra-services/${encodeURIComponent(editingId)}`
            : "/api/extra-services";

        const method = editingId ? "PUT" : "POST";

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(extraService)
            });

            if (!response.ok) {
                const error = await response.json().catch(() => null);

                if (error?.fieldErrors
                        && Object.keys(error.fieldErrors).length > 0) {
                    message.textContent =
                        Object.values(error.fieldErrors).join(", ");
                } else {
                    message.textContent =
                        error?.message || "บันทึกบริการไม่สำเร็จ";
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

    const addButton = document.getElementById("add-extra-service");

addButton.addEventListener("click", () => {
    form.reset();
    delete form.dataset.editingId;
    submitButton.textContent = "บันทึกบริการ";
    message.textContent = "";
});

    const editButtons = document.querySelectorAll(".edit-extra-service");

    for (const button of editButtons) {
        button.addEventListener("click", async () => {
            const serviceId = button.dataset.serviceId;

            if (!serviceId) {
                message.textContent = "ไม่พบรหัสบริการ";
                message.scrollIntoView({ block: "center" });
                return;
            }

            button.disabled = true;
            message.textContent = "";

            try {
                const response = await fetch(
                    `/api/extra-services/${encodeURIComponent(serviceId)}`
                );

                if (!response.ok) {
                    const error = await response.json().catch(() => null);
                    message.textContent =
                        error?.message || "โหลดข้อมูลบริการไม่สำเร็จ";
                    message.scrollIntoView({ block: "center" });
                    return;
                }

                const service = await response.json();
                const fields = form.elements;

                fields.namedItem("name").value = service.name;
                fields.namedItem("description").value =
                    service.description ?? "";
                fields.namedItem("price").value = service.price;
                fields.namedItem("active").checked = service.active === true;

                form.dataset.editingId = serviceId;
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

    const deactivateButtons = document.querySelectorAll(".deactivate-extra-service");

    for (const button of deactivateButtons) {
        button.addEventListener("click", async () => {
            const serviceId = button.dataset.serviceId;

            if (!serviceId) {
                message.textContent = "ไม่พบรหัสบริการ";
                message.scrollIntoView({ block: "center" });
                return;
            }

            message.textContent = "";
            button.disabled = true;

            try {
                const response = await fetch(
                    `/api/extra-services/${encodeURIComponent(serviceId)}`,
                    { method: "DELETE" }
                );

                if (!response.ok) {
                    const error = await response.json().catch(() => null);
                    message.textContent =
                        error?.message || "ปิดใช้บริการไม่สำเร็จ";
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