package com.qtifood.driver.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.qtifood.driver.core.Constants
import com.qtifood.driver.data.firebase.OrderTrackingDataSource
import com.qtifood.driver.data.remote.api.DeviceTokenApiService
import com.qtifood.driver.data.remote.api.DriverApiService
import com.qtifood.driver.data.remote.api.OrderApiService
import com.qtifood.driver.data.remote.api.WalletApiService
import com.qtifood.driver.data.remote.api.DeliveryApiService
import com.qtifood.driver.data.remote.firebase.DriverLocationRemoteDataSource
import com.qtifood.driver.data.remote.interceptor.Utf8ResponseInterceptor
import com.qtifood.driver.data.repository.DriverRepositoryImpl
import com.qtifood.driver.data.repository.OrderRepositoryImpl
import com.qtifood.driver.data.repository.WalletRepositoryImpl
import com.qtifood.driver.data.repository.DeliveryRepositoryImpl
import com.qtifood.driver.domain.repository.DriverRepository
import com.qtifood.driver.domain.repository.OrderRepository
import com.qtifood.driver.domain.repository.WalletRepository
import com.qtifood.driver.domain.repository.DeliveryRepository
import com.qtifood.driver.domain.usecase.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.qtifood.driver.presentation.login.LoginViewModel
import com.qtifood.driver.presentation.driverinfo.DriverInfoViewModel
import com.qtifood.driver.presentation.home.DriverHomeViewModel
import com.qtifood.driver.presentation.profile.DriverProfileViewModel
import com.qtifood.driver.presentation.documents.DriverDocumentsViewModel
import com.qtifood.driver.presentation.wallet.WalletViewModel

val appModule = module {
    
    // Network
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    single {
        OkHttpClient.Builder()
            .addInterceptor(Utf8ResponseInterceptor())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    single {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    single {
        get<Retrofit>().create(DriverApiService::class.java)
    }
    
    single {
        get<Retrofit>().create(DriverApiService::class.java)
    }
    
    single {
        get<Retrofit>().create(DeviceTokenApiService::class.java)
    }
    
    single {
        get<Retrofit>().create(WalletApiService::class.java)
    }
    
    single {
        get<Retrofit>().create(OrderApiService::class.java)
    }

    single {
        get<Retrofit>().create(DeliveryApiService::class.java)
    }
    
    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseDatabase.getInstance(Constants.FIREBASE_DB_URL) }
    single { DriverLocationRemoteDataSource() }
    single { OrderTrackingDataSource() }
    
    // Repository
    single<DriverRepository> {
        DriverRepositoryImpl(
            apiService = get(),
            deviceTokenApiService = get(),
            locationDataSource = get(),
            firebaseAuth = get(),
            context = get()
        )
    }
    
    single<WalletRepository> {
        WalletRepositoryImpl(
            apiService = get()
        )
    }

    single<DeliveryRepository> {
        DeliveryRepositoryImpl(
            apiService = get()
        )
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            apiService = get()
        )
    }
    
    // Use Cases
    single { SignInWithPhoneUseCase(get()) }
    single { GetDriverByFirebaseUidUseCase(get()) }
    single { CreateDriverUseCase(get()) }
    single { GetDriverProfileUseCase(get()) }
    single { UpdateDriverStatusUseCase(get()) }
    single { UpdateDriverLocationUseCase(get()) }
    
    // ViewModels
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { DriverInfoViewModel(get()) }
    viewModel { DriverHomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { DriverProfileViewModel(get()) }
    viewModel { DriverDocumentsViewModel(get()) }
    viewModel { (userId: String) -> WalletViewModel(get(), userId) }
}
