# QTI Driver App

QTI Driver là ứng dụng Android dành cho tài xế giao hàng của hệ thống QTI Food Delivery. Ứng dụng cung cấp các tính năng quản lý đơn hàng, theo dõi vị trí thời gian thực, và xác nhận giao hàng.

## 🚀 Tính năng chính

### 📱 Quản lý tài khoản
- Đăng nhập bằng số điện thoại với Firebase Authentication
- Quản lý thông tin cá nhân và tài liệu tài xế
- Upload hình ảnh: CCCD, GPLX, đăng ký xe, biển số xe
- Xác thực tài khoản tài xế

### 🗺️ Theo dõi vị trí
- **Background Location Tracking**: Tự động cập nhật vị trí lên Firebase khi app ở background
- Foreground Service đảm bảo tracking liên tục (cập nhật mỗi 5-10 giây)
- Hiển thị vị trí tài xế trên bản đồ OpenStreetMap
- Tích hợp Google Maps để chỉ đường

### 📦 Quản lý đơn hàng
- Nhận thông báo đơn hàng mới qua Firebase Cloud Messaging (FCM)
- Popup notification khi app ở foreground
- Xem chi tiết đơn hàng: thông tin khách hàng, địa chỉ, món ăn
- Cập nhật trạng thái đơn hàng
- **Xác nhận vị trí giao hàng**: Kiểm tra tài xế có đúng vị trí giao hàng không (trong bán kính 300m)

### 💰 Ví điện tử
- Xem số dư ví
- Lịch sử giao dịch
- Nạp tiền qua SePay

### 📊 Thống kê
- Lịch sử giao hàng
- Thống kê thu nhập theo ngày/tuần/tháng
- Chi tiết từng đơn hàng đã giao

### 🔔 Thông báo
- FCM push notification cho đơn hàng mới
- Overlay popup notification khi app foreground
- Âm thanh thông báo
- Deep link tới chi tiết đơn hàng

## 🏗️ Kiến trúc

Dự án sử dụng **Clean Architecture** với 3 layer:

```
app/
├── data/                          # Data Layer
│   ├── remote/                    # API và DTO
│   │   ├── api/                   # Retrofit Services
│   │   ├── dto/                   # Data Transfer Objects
│   │   ├── util/                  # Error Handler
│   │   └── interceptor/           # UTF-8 Interceptor
│   ├── firebase/                  # Firebase Services
│   ├── repository/                # Repository Implementations
│   └── mapper/                    # DTO ↔ Domain Model Mappers
│
├── domain/                        # Domain Layer
│   ├── model/                     # Business Models
│   ├── repository/                # Repository Interfaces
│   └── usecase/                   # Use Cases
│
├── presentation/                  # Presentation Layer
│   ├── login/                     # Login Screen
│   ├── home/                      # Home Screen
│   ├── order/                     # Order Detail Screen
│   ├── profile/                   # Profile Management
│   ├── documents/                 # Document Upload
│   ├── wallet/                    # Wallet & Transactions
│   ├── history/                   # Delivery History
│   └── income/                    # Income Statistics
│
├── service/                       # Services
│   └── LocationTrackingService.kt # Background Location Service
│
├── di/                            # Dependency Injection
│   └── AppModule.kt               # Koin Modules
│
└── core/                          # Core Utilities
    └── Constants.kt               # App Constants
```

## 🛠️ Tech Stack

### Core
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

### Architecture & Patterns
- Clean Architecture
- MVVM Pattern
- Repository Pattern
- Use Cases
- Dependency Injection (Koin)

### Networking
- Retrofit 2 - REST API client
- OkHttp 4 - HTTP client & interceptors
- Gson - JSON serialization

### Firebase
- Firebase Authentication - Phone Auth
- Firebase Realtime Database - Real-time location & order tracking
- Firebase Cloud Messaging - Push notifications

### UI/UX
- ViewBinding
- Material Design 3
- Glide - Image loading
- OSMDroid - OpenStreetMap integration
- Google Play Services Location - GPS tracking

### Background Processing
- Kotlin Coroutines - Async operations
- Foreground Service - Background location tracking
- WorkManager ready (if needed)

## 📋 Yêu cầu

- Android Studio Hedgehog (2023.1.1) trở lên
- JDK 17 hoặc cao hơn
- Android SDK 35
- Gradle 8.13

## 🚀 Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/thanh123kte/QtiDriver.git
cd QtiDriver
```

### 2. Cấu hình Firebase

1. Tạo project trên [Firebase Console](https://console.firebase.google.com/)
2. Thêm Android app với package name: `com.qtifood.driver`
3. Download `google-services.json` và đặt vào `app/`
4. Enable các services:
   - Authentication (Phone)
   - Realtime Database
   - Cloud Messaging

### 3. Cấu hình Backend URL

Mở `app/src/main/java/com/qtifood/driver/core/Constants.kt`:

```kotlin
object Constants {
    const val BASE_URL = "https://your-backend-url.com"
    const val FIREBASE_DB_URL = "https://your-firebase-db.firebaseio.com"
    
    fun getImageUrl(path: String?): String {
        // ...
    }
}
```

### 4. Build & Run

```bash
./gradlew assembleDebug
```

hoặc chạy từ Android Studio: **Run > Run 'app'**

## 🔑 Permissions

Ứng dụng yêu cầu các quyền sau:

- `ACCESS_FINE_LOCATION` - GPS chính xác
- `ACCESS_COARSE_LOCATION` - GPS gần đúng
- `ACCESS_BACKGROUND_LOCATION` - Tracking khi background
- `FOREGROUND_SERVICE` - Chạy service nền
- `FOREGROUND_SERVICE_LOCATION` - Location service
- `POST_NOTIFICATIONS` - Thông báo push (Android 13+)
- `SYSTEM_ALERT_WINDOW` - Popup overlay
- `CAMERA` - Chụp ảnh tài liệu
- `INTERNET` - Kết nối mạng

## 📡 API Endpoints

### Driver Management
- `POST /api/drivers` - Tạo tài xế mới
- `GET /api/drivers/{id}` - Lấy thông tin tài xế
- `PUT /api/drivers/{id}` - Cập nhật thông tin
- `PATCH /api/drivers/{id}/status` - Cập nhật trạng thái (ONLINE/OFFLINE)
- `POST /api/drivers/{id}/upload-image` - Upload hình ảnh

### Orders
- `GET /api/orders/{id}` - Chi tiết đơn hàng
- `GET /api/order-items/order/{orderId}` - Danh sách món ăn
- `PATCH /api/orders/{id}/status` - Cập nhật trạng thái đơn
- `POST /api/orders/{orderId}/driver-location-confirm` - Xác nhận vị trí giao hàng

### Wallet
- `GET /api/wallets/{userId}` - Thông tin ví
- `GET /api/wallets/{userId}/transactions` - Lịch sử giao dịch
- `POST /api/sepay/topup/{userId}` - Nạp tiền

### Delivery History
- `GET /api/deliveries/driver/{driverId}` - Lịch sử giao hàng
- `GET /api/deliveries/driver/{driverId}/income/{period}` - Thống kê thu nhập

## 🔥 Firebase Structure

### Realtime Database

```
qtifood-realtime-db/
├── driver_locations/              # Vị trí tài xế realtime
│   └── {driverId}/
│       ├── latitude: Double
│       ├── longitude: Double
│       ├── timestamp: Long
│       └── isOnline: Boolean
│
└── order_tracking/                # Theo dõi đơn hàng
    └── {orderId}/
        ├── orderId: Long
        ├── driverId: String
        ├── customerId: String
        ├── status: String
        ├── shippingAddress: String
        ├── storeAddress: String
        ├── customerName: String
        ├── customerPhone: String
        ├── driverLocation/
        │   ├── latitude: Double
        │   ├── longitude: Double
        │   └── updatedAt: Long
        └── assignedAt: String
```

## 🎯 Workflow

### 1. Đăng nhập
```
User nhập SĐT → Firebase Auth → OTP → Xác thực → 
Check user exists → Tạo/Load profile → Home Screen
```

### 2. Online/Offline
```
Toggle Online → Update API status → Start LocationTrackingService →
Service cập nhật vị trí lên Firebase mỗi 5-10s (chạy ngay cả khi background)
```

### 3. Nhận đơn hàng
```
Backend assign order → FCM notification → 
App foreground: Popup overlay
App background: System notification
→ Click notification → OrderDetailActivity
```

### 4. Giao hàng
```
Load order from Firebase tracking + API →
Start location updates (realtime) →
Navigate to customer →
Confirm delivery (check vị trí trong 300m) →
Update status → Home
```

## 🐛 Error Handling

- **HTTP 404**: "Mất kết nối"
- **HTTP 500**: "Vui lòng đến đúng vị trí giao hàng"
- **Other errors**: "Có lỗi xảy ra. Vui lòng thử lại"
- UTF-8 encoding được xử lý bởi `Utf8ResponseInterceptor`

## 📸 Screenshots

_Screenshots sẽ được thêm vào đây_

## 🤝 Contributing

Contributions, issues và feature requests đều được chào đón!

## 📝 License

Copyright © 2026 QTI Food Delivery

## 👨‍💻 Author

**Thanh Nguyen**
- GitHub: [@thanh123kte](https://github.com/thanh123kte)

---

Made with ❤️ by QTI Team
