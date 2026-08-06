package com.hsbc.portfoliomanager

import android.app.Application
import android.content.Context

class PortfolioApplication : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}

