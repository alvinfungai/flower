package com.alvinfungai.flower

import android.app.Application
import com.alvinfungai.flower.data.remote.SupabaseClientProvider


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.init(context = this)
    }
}