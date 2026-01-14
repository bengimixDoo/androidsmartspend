# SmartSpend - Quản lý Tài chính Cá nhân Thông minh

![SmartSpend Banner](app/src/main/res/drawable/ic_image.png)
**SmartSpend** là ứng dụng Android giúp người dùng theo dõi thu chi, lập ngân sách và quản lý tài chính cá nhân một cách hiệu quả và trực quan. Ứng dụng được phát triển bằng ngôn ngữ **Kotlin**, sử dụng kiến trúc **MVVM** (cơ bản) và **SQLite** để lưu trữ dữ liệu offline.

## 🚀 Tính năng nổi bật

### 1. Quản lý Giao dịch (Transactions)
* ✅ **Thêm/Sửa/Xóa giao dịch:** Ghi chép nhanh chóng các khoản thu nhập và chi tiêu.
* ✅ **Phân loại đa dạng:** Hỗ trợ các danh mục mặc định (Ăn uống, Di chuyển, Lương...) và cho phép người dùng tự tạo danh mục mới.
* ✅ **Chọn ngày linh hoạt:** Lựa chọn ngày tháng giao dịch dễ dàng.

### 2. Báo cáo & Thống kê (Reports & Analytics)
* 📊 **Tổng quan:** Hiển thị Tổng thu, Tổng chi và Số dư hiện tại ngay trên màn hình chính.
* 📈 **Biểu đồ Xu hướng (Trend Chart):** Biểu đồ cột so sánh Thu/Chi trong 6 tháng gần nhất.
* 📉 **Xu hướng Danh mục (Category Trend):** Biểu đồ đường theo dõi biến động chi tiêu của từng danh mục cụ thể.
* 🏆 **Top Chi tiêu:** Tự động liệt kê 5 danh mục tiêu tốn nhiều tiền nhất.
* 📋 **Chi tiết Danh mục:** Xem lịch sử giao dịch chi tiết của từng nhóm chi tiêu.

### 3. Quản lý Ngân sách & Cảnh báo (Budget & Notifications)
* 💰 **Thiết lập ngân sách:** Đặt hạn mức chi tiêu cho từng danh mục.
* 🔔 **Cảnh báo thông minh:** Hệ thống tự động gửi thông báo (Notification) khi bạn tiêu vượt quá 80%, 90% hoặc 100% ngân sách đã đặt.

### 4. Giao diện thân thiện
* Hỗ trợ hiển thị Tiếng Việt.
* Giao diện Material Design hiện đại, dễ sử dụng.

---

## 🛠 Công nghệ sử dụng

* **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/)
* **IDE:** Android Studio
* **Cơ sở dữ liệu:** SQLite (sử dụng `SQLiteOpenHelper`)
* **Biểu đồ:** [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Thư viện vẽ biểu đồ mạnh mẽ.
* **Giao diện:** XML Layouts, RecyclerView, ConstraintLayout.

---

## ⚙️ Cài đặt & Chạy ứng dụng

Để chạy dự án này trên máy local của bạn:

1.  **Clone dự án:**
    ```bash
    git clone [https://github.com/bengimixDoo/androidsmartspend.git](https://github.com/bengimixDoo/androidsmartspend.git)
    ```
2.  **Mở trong Android Studio:**
   * Khởi động Android Studio -> Open -> Chọn thư mục vừa clone.
3.  **Đồng bộ Gradle:**
   * Đợi Android Studio tải các thư viện cần thiết.
4.  **Chạy ứng dụng:**
   * Kết nối thiết bị thật hoặc mở máy ảo (Emulator).
   * Nhấn nút **Run** (biểu tượng tam giác xanh).
---

*Dự án được thực hiện cho mục đích học tập.*