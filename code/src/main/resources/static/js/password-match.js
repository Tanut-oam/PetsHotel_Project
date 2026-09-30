// เตือนทันทีถ้ารหัสยืนยันไม่ตรง (server ยังตรวจซ้ำใน AuthController)
document.addEventListener("DOMContentLoaded", () => {
    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirmPassword");
    if (!password || !confirmPassword) {
        return;
    }

    const check = () => {
        const mismatch = confirmPassword.value && confirmPassword.value !== password.value;
        confirmPassword.setCustomValidity(mismatch ? "รหัสผ่านไม่ตรงกัน" : "");
    };
    password.addEventListener("input", check);
    confirmPassword.addEventListener("input", check);
});