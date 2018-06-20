package org.geekhaven.ishare

import android.app.Application
import timber.log.Timber

class IShare : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}