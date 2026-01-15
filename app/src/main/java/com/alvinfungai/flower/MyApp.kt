package com.alvinfungai.flower

import android.app.Application


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.init(context = this)
    }
}