package com.siowe.app

import android.app.Application
import com.clerk.api.Clerk

class SioweApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val key = BuildConfig.CLERK_PUBLISHABLE_KEY
        if (key.isNotBlank()) {
            Clerk.initialize(this, publishableKey = key)
        }
    }
}
