# SmartSpend - Quản lý tài chính cá nhân

![SmartSpend Banner](app/src/main/res/drawable/ic_image.png)

**SmartSpend** là ứng dụng Android hỗ trợ người dùng quản lý tài chính cá nhân theo cách đơn giản và trực quan. Ứng dụng cho phép ghi lại các khoản thu chi, phân loại giao dịch, đặt ngân sách cho từng nhóm chi tiêu, theo dõi báo cáo thống kê và đồng bộ dữ liệu khi đăng nhập tài khoản.

Dự án được phát triển bằng **Kotlin**, sử dụng **SQLite** để lưu dữ liệu cục bộ trên thiết bị và kết hợp **Firebase Authentication** cùng **Cloud Firestore** để xử lý đăng nhập, lưu phiên và đồng bộ dữ liệu giữa các lần sử dụng.

## 🚀 Tính năng chính

### 1. Quản lý giao dịch

- ✅ Thêm, sửa, xóa giao dịch thu nhập và chi tiêu
- 📅 Chọn ngày giao dịch để lưu lịch sử theo thời gian thực tế
- 📝 Lưu thông tin cơ bản như tên giao dịch, số tiền, loại giao dịch và danh mục
- 🔄 Tự động cập nhật dữ liệu sau khi thay đổi giao dịch

### 2. Quản lý danh mục

- 🗂️ Hỗ trợ danh mục mặc định cho cả thu nhập và chi tiêu
- ➕ Cho phép người dùng tự tạo thêm danh mục mới
- 🌐 Đồng bộ tên danh mục mặc định theo ngôn ngữ hiện tại của ứng dụng
- ♻️ Khi xóa danh mục tự tạo, giao dịch cũ có thể được chuyển sang danh mục khác để tránh mất dữ liệu

### 3. Ngân sách và cảnh báo

- 💰 Thiết lập ngân sách cho từng danh mục chi tiêu
- 📈 Theo dõi số tiền đã dùng và phần còn lại của từng danh mục
- 🔔 Gửi thông báo khi mức chi tiêu chạm 80%, 90% hoặc vượt 100% ngân sách

### 4. Báo cáo và thống kê

- 📊 Hiển thị tổng thu, tổng chi và số dư hiện tại
- 📉 Biểu đồ xu hướng thu chi theo thời gian
- 📈 Biểu đồ theo dõi chi tiêu của từng danh mục
- 🏆 Thống kê các nhóm chi tiêu lớn nhất
- 📋 Xem chi tiết dữ liệu theo từng danh mục

### 5. Tài khoản và đồng bộ dữ liệu

- 🔐 Đăng ký tài khoản bằng email và mật khẩu
- 🔑 Đăng nhập bằng Firebase Authentication
- ☁️ Đồng bộ giao dịch, danh mục và một số dữ liệu thiết lập qua Firestore
- 📱 Khi đăng nhập lại trên thiết bị khác, ứng dụng có thể tải dữ liệu về máy cục bộ


## 🛠 Công nghệ sử dụng

- **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/)
- **Giao diện:** XML Layouts, Fragment, Navigation, RecyclerView, ConstraintLayout
- **Cơ sở dữ liệu cục bộ:** SQLite với `SQLiteOpenHelper`
- **Xác thực và cloud:** Firebase Authentication, Cloud Firestore
- **Biểu đồ:** [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **Bất đồng bộ:** Kotlin Coroutines
- **IDE:** Android Studio

## 🧱 Kiến trúc và tổ chức hiện tại

Repo hiện tại đang đi theo hướng tổ chức khá thực dụng, tập trung vào Activity/Fragment và lớp hỗ trợ dữ liệu:

- `Activity` dùng cho các màn hình cấp cao như bắt đầu, đăng nhập, đăng ký, thiết lập ban đầu và màn hình chính
- `Fragment` dùng cho các tab chức năng như giao dịch, danh mục, báo cáo, cài đặt
- `DatabaseHelper` chịu trách nhiệm thao tác với SQLite
- `CloudSyncManager` xử lý tải lên và tải xuống dữ liệu từ Firestore
- `adapters/` chứa adapter cho RecyclerView
- `models/` chứa model dữ liệu như `Transaction` và `Category`

## ⚙️ Yêu cầu môi trường

- Android Studio
- JDK 17
- Android SDK 35
- Min SDK 26
- Thiết bị thật hoặc emulator từ Android 8.0 trở lên
- Tài khoản Firebase nếu muốn dùng đầy đủ chức năng đăng nhập và đồng bộ

## ▶️ Cài đặt và chạy dự án

### 1. Clone dự án

```bash
git clone https://github.com/bengimixDoo/androidsmartspend.git
```

### 2. Mở bằng Android Studio

- Chọn `Open`
- Trỏ tới thư mục dự án vừa clone
- Chờ Android Studio index và sync project

### 3. Cấu hình Firebase

Để chạy đúng phần đăng nhập và đồng bộ dữ liệu, cần chuẩn bị Firebase:

- Tạo một Firebase project
- Bật **Authentication** với phương thức đăng nhập bằng Email/Password
- Bật **Cloud Firestore**
- Tải file `google-services.json`
- Đặt file này vào thư mục `app/`

Nếu thiếu `google-services.json`, project có thể vẫn mở được nhưng các tính năng liên quan đến Firebase sẽ không hoạt động đúng.

### 4. Sync Gradle

- Đợi Android Studio tải dependencies
- Nếu cần, kiểm tra lại phiên bản JDK và Android SDK trong phần cấu hình project

### 5. Chạy ứng dụng

- Kết nối thiết bị thật hoặc mở emulator
- Nhấn nút `Run` trong Android Studio

## 📌 Ghi chú sử dụng

- Dữ liệu giao dịch và danh mục được lưu trên SQLite ở thiết bị hiện tại
- Khi đăng nhập, dữ liệu có thể được đồng bộ với Firestore để hỗ trợ khôi phục hoặc dùng lại trên thiết bị khác
- Một số dữ liệu thiết lập ban đầu như ngân sách, tiền mặt và tiền trong thẻ được lưu trong `SharedPreferences`
- Ứng dụng có hỗ trợ thông báo chi tiêu, vì vậy thiết bị cần cho phép notification để trải nghiệm đầy đủ

*Dự án được thực hiện cho mục đích học tập.*
