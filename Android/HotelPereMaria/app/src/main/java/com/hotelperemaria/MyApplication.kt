package com.hotelperemaria

import android.app.Application
import com.hotelperemaria.utils.DateUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DateUtils.initialize(this)
    }
}