package com.dietapp

import android.app.Application

class DietApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: DietApp
            private set
    }
}
