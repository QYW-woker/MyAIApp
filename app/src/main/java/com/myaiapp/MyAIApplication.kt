package com.myaiapp

import android.app.Application

class MyAIApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MyAIApplication
            private set
    }
}
