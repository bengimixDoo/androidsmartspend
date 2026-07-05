# SmartSpend - Quản lý tài chính cá nhân

![SmartSpend Banner](app/src/main/res/drawable/ic_image.png)

**SmartSpend** là ứng dụng Android giúp theo dõi thu chi, quản lý danh mục, lập ngân sách và xem thống kê tài chính cá nhân. Dự án được viết bằng **Kotlin**, dùng **SQLite** để lưu dữ liệu cục bộ và **Firebase** để đăng nhập, lưu phiên và đồng bộ dữ liệu.

## 🚀 Tính năng chính

- ✅ Thêm, sửa, xóa giao dịch thu nhập và chi tiêu
- 🗂️ Quản lý danh mục mặc định và tự tạo danh mục mới
- 💰 Thiết lập ngân sách cho từng danh mục
- 🔔 Cảnh báo khi chi tiêu đạt 80%, 90% hoặc vượt 100% ngân sách
- 📊 Xem tổng thu, tổng chi, số dư và các biểu đồ thống kê
- ☁️ Đăng nhập/đăng ký tài khoản và đồng bộ dữ liệu qua Firebase

## 🛠 Công nghệ sử dụng

- **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/)
- **UI:** XML Layouts, RecyclerView, ConstraintLayout
- **Local database:** SQLite (`SQLiteOpenHelper`)
- **Cloud & Auth:** Firebase Authentication, Cloud Firestore
- **Biểu đồ:** [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **IDE:** Android Studio

## ⚙️ Yêu cầu môi trường

- Android Studio
- JDK 17
- Android SDK 35
- Thiết bị hoặc emulator từ Android 8.0 (API 26) trở lên

## ▶️ Cài đặt và chạy dự án

1. Clone dự án:
   ```bash
   git clone https://github.com/bengimixDoo/androidsmartspend.git
   ```
2. Mở thư mục dự án bằng Android Studio.
3. Đảm bảo file `app/google-services.json` đã được cấu hình đúng cho Firebase project của bạn.
4. Sync Gradle để tải dependencies.
5. Chạy ứng dụng trên thiết bị thật hoặc emulator.

## 📝 Ghi chú

- Dữ liệu giao dịch và danh mục được lưu cục bộ bằng SQLite.
- Khi người dùng đăng nhập, ứng dụng có thể đồng bộ dữ liệu với Firestore.
- Dự án được thực hiện cho mục đích học tập.
