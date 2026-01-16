# QTI Driver App - Clean Architecture

## 📁 Cấu trúc dự án

```
com.qtifood.driver/
├── presentation/          # UI Layer (Activities, ViewModels, UiStates)
│   ├── login/
│   │   ├── LoginActivity.kt
│   │   ├── LoginViewModel.kt
│   │   └── LoginUiState.kt
│   ├── driverinfo/
│   │   ├── DriverInfoActivity.kt
│   │   ├── DriverInfoViewModel.kt
│   │   └── DriverInfoUiState.kt
│   └── main/
│       └── MainActivity.kt
│
├── domain/               # Business Logic Layer
│   ├── model/           # Domain Models (Pure Kotlin)
│   │   ├── Driver.kt
│   │   ├── Order.kt
│   │   ├── DriverLocation.kt
│   │   ├── VerificationStatus.kt
│   │   └── Result.kt
│   │
│   ├── repository/      # Repository Interfaces
│   │   └── DriverRepository.kt
│   │
│   └── usecase/        # Business Use Cases
│       ├── SignInWithPhoneUseCase.kt
│       ├── GetDriverByFirebaseUidUseCase.kt
│       ├── CreateDriverUseCase.kt
│       ├── GetDriverProfileUseCase.kt
│       ├── GetCurrentOrderUseCase.kt
│       ├── AcceptOrderUseCase.kt
│       └── UpdateDriverLocationUseCase.kt
│
├── data/                # Data Layer
│   ├── remote/
│   │   ├── api/
│   │   │   └── DriverApiService.kt
│   │   ├── dto/
│   │   │   ├── DriverDto.kt
│   │   │   └── OrderDto.kt
│   │   └── firebase/
│   │       └── DriverLocationRemoteDataSource.kt
│   │
│   ├── repository/      # Repository Implementation
│   │   └── DriverRepositoryImpl.kt
│   │
│   └── mapper/         # DTO <-> Domain Mappers
│       └── DriverMapper.kt
│
└── di/                 # Dependency Injection
    └── AppModule.kt
```

## 🏗️ Clean Architecture Layers

### 1. **Presentation Layer** (presentation/)
- **Trách nhiệm**: Hiển thị UI và xử lý user interactions
- **Components**:
  - `Activity`: Quản lý lifecycle và UI binding
  - `ViewModel`: Xử lý UI logic và state management
  - `UiState`: Data class chứa UI state (StateFlow)
- **Dependencies**: Phụ thuộc vào Domain layer (UseCases)

### 2. **Domain Layer** (domain/)
- **Trách nhiệm**: Business logic thuần túy, không phụ thuộc framework
- **Components**:
  - `Model`: Domain models (pure Kotlin)
  - `Repository Interface`: Định nghĩa contracts cho data operations
  - `UseCase`: Đóng gói business logic cụ thể
- **Dependencies**: Không phụ thuộc layer nào khác

### 3. **Data Layer** (data/)
- **Trách nhiệm**: Truy xuất và lưu trữ dữ liệu
- **Components**:
  - `DTO`: Data Transfer Objects (API responses)
  - `ApiService`: Retrofit API definitions
  - `RemoteDataSource`: Firebase operations
  - `RepositoryImpl`: Implementation của Repository interface
  - `Mapper`: Chuyển đổi giữa DTO và Domain models
- **Dependencies**: Implements Domain layer interfaces

### 4. **Dependency Injection** (di/)
- **Koin Module**: Cung cấp dependencies cho toàn app
- **Configuration**:
  - Network (Retrofit, OkHttp)
  - Firebase (Auth, Database)
  - Repository
  - UseCases
  - ViewModels

## 🔄 Data Flow

```
User Action 
   ↓
Activity/Fragment 
   ↓
ViewModel 
   ↓
UseCase 
   ↓
Repository Interface 
   ↓
Repository Implementation 
   ↓
API Service / Firebase 
   ↓
DTO → Mapper → Domain Model 
   ↓
StateFlow → UI Update
```

## 🌐 API Configuration

**Base URL**: `http://10.0.2.2:8080` (Android Emulator localhost)
- Cho thiết bị thật: thay bằng IP máy tính (ví dụ: `http://192.168.1.100:8080`)

### API Endpoints:

#### Driver APIs:
- `POST /api/drivers` - Tạo driver mới
- `GET /api/drivers/{id}` - Lấy thông tin driver theo Firebase UID
- `PUT /api/drivers/{id}` - Cập nhật thông tin driver

#### Order APIs:
- `GET /api/orders/current` - Lấy danh sách đơn hàng hiện tại
- `POST /api/orders/{id}/accept` - Chấp nhận đơn hàng
- `PUT /api/orders/{id}/status` - Cập nhật trạng thái đơn hàng

## 🔥 Firebase Configuration

### Firebase Authentication
- Phone Number Authentication
- OTP verification

### Firebase Realtime Database
- Driver location tracking: `driver_locations/{driverId}`

## 📱 Login Flow

1. **Nhập số điện thoại** → Gửi OTP qua Firebase Auth
2. **Xác thực OTP** → Đăng nhập Firebase
3. **Kiểm tra tài khoản**:
   - API: `GET /api/drivers/{firebaseUid}`
   - Nếu **404 (không tồn tại)** → Màn hình nhập thông tin
   - Nếu **200 (tồn tại)** → Màn hình chính
4. **Nhập thông tin** (nếu chưa có):
   - Họ tên, CCCD, Giấy phép lái xe
   - API: `POST /api/drivers`
   - Status: `PENDING` (chờ admin xác thực)
5. **Vào màn hình chính**

## 🎯 StateFlow Pattern

Mỗi ViewModel sử dụng `StateFlow` để quản lý UI state:

```kotlin
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val shouldNavigateToDriverInfo: Boolean = false,
    // ... other states
)

private val _uiState = MutableStateFlow(LoginUiState())
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
```

**Lợi ích**:
- Chỉ update những phần UI cần thiết
- Reactive và dễ test
- Type-safe state management

## 🛠️ Dependencies

- **Koin**: Dependency Injection
- **Retrofit**: REST API calls
- **OkHttp**: HTTP client
- **Coroutines**: Asynchronous programming
- **StateFlow**: State management
- **Firebase Auth**: Phone authentication
- **Firebase Realtime Database**: Location tracking
- **ViewBinding**: Type-safe view access

## 📝 Next Steps

1. Thêm Firebase Realtime Database configuration vào `google-services.json`
2. Chạy backend server tại `localhost:8080`
3. Test trên emulator hoặc thiết bị thật
4. Implement các màn hình còn lại (Home, Order, Profile)

## 🔐 Security Notes

- Firebase UID được dùng làm Driver ID trong database
- Verification status: PENDING → chờ admin approve
- Phone number được xác thực qua Firebase Auth OTP
