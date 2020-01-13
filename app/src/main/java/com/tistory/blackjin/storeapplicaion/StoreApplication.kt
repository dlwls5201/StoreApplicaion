package com.tistory.blackjin.storeapplicaion

import android.app.Application
import timber.log.Timber

class StoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}