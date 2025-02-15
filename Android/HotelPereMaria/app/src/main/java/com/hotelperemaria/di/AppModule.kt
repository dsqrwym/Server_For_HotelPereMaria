package com.hotelperemaria.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hotelperemaria.data.auth.SessionManager
import com.hotelperemaria.data.auth.TokenAuthenticator
import com.hotelperemaria.data.network.ApiAuthService
import com.hotelperemaria.data.network.ApiHabitacionService
import com.hotelperemaria.data.network.ApiReservaService
import com.hotelperemaria.data.repository.AuthRepository
import com.hotelperemaria.data.repository.ReservationRepository
import com.hotelperemaria.data.repository.RoomRepository
import com.hotelperemaria.data.repository.UserRepository
import com.hotelperemaria.network.RetrofitService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class) // 全局单例
object AppModule {
    @Provides
    @Singleton
    fun provideReservaRepository(apiReservaService: ApiReservaService, sessionManager: SessionManager): ReservationRepository {
        return ReservationRepository(apiReservaService, sessionManager)
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(): UserRepository {
        return UserRepository()
    }

    @Provides
    @Singleton
    fun provideHabitacionRepository(apiHabitacionService: ApiHabitacionService, sessionManager: SessionManager): RoomRepository {
        return RoomRepository(apiHabitacionService, sessionManager)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {


    @Provides
    @Singleton
    fun provideEncryptedPrefs(@ApplicationContext context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }


    @Provides
    @Singleton
    fun provideSessionManager(encryptedPrefs: EncryptedSharedPreferences): SessionManager {
        return SessionManager(encryptedPrefs)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        sessionManager: SessionManager,
        apiAuthService: Lazy<ApiAuthService>
    ): TokenAuthenticator {
        return TokenAuthenticator(sessionManager, apiAuthService.get())
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiAuthService: Lazy<ApiAuthService>,
        sessionManager: SessionManager
    ): AuthRepository {
        return AuthRepository(apiAuthService.get(), sessionManager)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authenticator: Lazy<TokenAuthenticator> // 使用 Lazy 延迟初始化
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .authenticator { route, response ->
                authenticator.get().authenticate(route, response)
            }
            .build()
    }


    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val urlServidor = "https://server-for-hotelperemaria.onrender.com/api/"
        val urlLocalPrueba = "http://10.0.2.2:3000/api/"
        val json = Json {
            ignoreUnknownKeys = true  // ✅ 忽略 JSON 中未声明的字段
            explicitNulls = false     // ✅ 允许字段完全缺失，而不仅仅是 `null`
        }

        return Retrofit
            .Builder().baseUrl(urlLocalPrueba)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiAuthService(retrofit: Retrofit): ApiAuthService {
        return retrofit.create(ApiAuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiReservationService(retrofit: Retrofit): ApiReservaService {
        return retrofit.create(ApiReservaService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiHabitacionService(okHttpClient: OkHttpClient): ApiHabitacionService {
        return makeRetrofitServiceTito(okHttpClient).create(ApiHabitacionService::class.java)
    }
    private fun makeRetrofitServiceTito(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/api/")
            .client(okHttpClient) // Usamos el cliente con el interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}