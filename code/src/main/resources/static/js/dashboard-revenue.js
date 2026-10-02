    let lastRevenueButton = null;

    document.addEventListener("submit", handleRevenueYearSubmit);
    document.addEventListener("click", handleRevenueMonthClick);
    document.addEventListener("close", handleRevenueDialogClose, true);

    async function handleRevenueYearSubmit(event) {
    const form = event.target;

    if (!form.matches("[data-revenue-year-form]")) {
        return;
    }

    event.preventDefault();

    const report = document.getElementById("monthly-revenue");

    if (report.getAttribute("aria-busy") === "true") {
        return;
    }

    const submitButton = form.querySelector('button[type="submit"]');
    const errorMessage = report.querySelector("[data-revenue-error]");
    const originalButtonText = submitButton.textContent;

    const formData = new FormData(form);
    const url = new URL(form.action);
    url.searchParams.set("year", formData.get("year"));

    errorMessage.hidden = true;
    errorMessage.textContent = "";

    report.setAttribute("aria-busy", "true");
    submitButton.disabled = true;
    submitButton.textContent = "กำลังโหลด...";

    try {
        const response = await fetch(url, {
        method: "GET",
        credentials: "same-origin"
        });

        if (!response.ok) {
        throw new Error("Cannot load revenue report");
        }

        const html = await response.text();
        const parser = new DOMParser();
        const newDocument = parser.parseFromString(html, "text/html");
        const updatedReport =
        newDocument.getElementById("monthly-revenue");

        if (updatedReport === null) {
        throw new Error("Revenue report was not found");
        }

        const pageScrollX = window.scrollX;
        const pageScrollY = window.scrollY;

        const oldTableScroll = report.querySelector(".revenue-scroll");
        const tableScrollTop = oldTableScroll.scrollTop;

        const newReport = document.importNode(updatedReport, true);
        report.replaceWith(newReport);

        const newTableScroll =
        newReport.querySelector(".revenue-scroll");

        newTableScroll.scrollTop = tableScrollTop;

        history.replaceState(
        history.state,
        "",
        url.pathname + url.search
        );

        window.scrollTo({
        left: pageScrollX,
        top: pageScrollY,
        behavior: "instant"
        });
    } catch (error) {
        errorMessage.textContent =
        "โหลดรายงานไม่สำเร็จ กรุณาลองอีกครั้ง หากหมดเวลาเข้าสู่ระบบให้เข้าสู่ระบบใหม่";

        errorMessage.hidden = false;
    } finally {
        report.removeAttribute("aria-busy");
        submitButton.disabled = false;
        submitButton.textContent = originalButtonText;
    }
    }

    function handleRevenueMonthClick(event) {
    const button = event.target.closest("[data-revenue-open]");

    if (button === null) {
        return;
    }

    const dialogId = button.getAttribute("data-revenue-open");
    const dialog = document.getElementById(dialogId);

    if (dialog === null || dialog.open) {
        return;
    }

    lastRevenueButton = button;
    dialog.showModal();
    }

    function handleRevenueDialogClose(event) {
    if (!event.target.matches(".revenue-dialog")) {
        return;
    }

    if (lastRevenueButton !== null && lastRevenueButton.isConnected) {
        lastRevenueButton.focus({
        preventScroll: true
        });
    }

    lastRevenueButton = null;
    }

    document.addEventListener("click", handleRevenueDetailClick);

    function handleRevenueDetailClick(event) {
    const button = event.target.closest("[data-revenue-detail]");

    if (button === null) {
        return;
    }

    const detailId = button.getAttribute("data-revenue-detail");
    const detailRow = document.getElementById(detailId);

    if (detailRow === null) {
        return;
    }

    const isExpanded =
        button.getAttribute("aria-expanded") === "true";

    if (isExpanded) {
        detailRow.hidden = true;
        button.setAttribute("aria-expanded", "false");
        button.textContent = "ดูรายละเอียด";
    } else {
        detailRow.hidden = false;
        button.setAttribute("aria-expanded", "true");
        button.textContent = "ย่อรายละเอียด";
    }
    }