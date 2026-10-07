package com.example.simplecalc

import android.app.Application
import com.example.simplecalc.di.AppContainer
import com.example.simplecalc.di.DefaultAppContainer

class GymApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}