package com.hotelperemaria.di

import com.hotelperemaria.data.repository.ReservationRepository
import com.hotelperemaria.data.repository.RoomRepository
import com.hotelperemaria.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 全局单例
object AppModule {

    @Provides
    @Singleton
    fun provideReservaRepository(): ReservationRepository {
        return ReservationRepository()
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(): UserRepository {
        return UserRepository()
    }

    @Provides
    @Singleton
    fun provideHabitacionRepository(): RoomRepository {
        return RoomRepository()
    }
}