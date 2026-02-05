package com.alvinfungai.flower

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {
    private var _supabase: SupabaseClient? = null
    val client get() = _supabase!!

    fun init(context: Context) {
        if (_supabase != null) return
        _supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "myapp"
                host = "auth"
            }
            install(Postgrest)
        }
    }
}