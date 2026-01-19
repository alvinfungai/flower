package com.alvinfungai.flower

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private val supabase get() = SupabaseClientProvider.client
    private var handledRedirect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.btnGithub).setOnClickListener {
            Log.d("AUTH", "onCreate: clicked")
            // Observe session changes
             observeAuthState()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("AUTH", "Intent data: ${intent.data}")
        Log.d("AUTH", "Intent extras: ${intent.extras}")
        handleRedirect(intent)
    }

    private fun handleRedirect(intent: Intent?) {
        if (handledRedirect) return
        handledRedirect = true

        val data = intent?.data ?: return
        if (data.scheme != "myapp") return

        Log.d("AUTH", "Intent data: $data")

        // Extract access token from fragment
        val fragment = data.fragment ?: return
        val params = fragment.split("&").associate {
            val (key, value) = it.split("=")
            key to value
        }

        val accessToken = params["access_token"] ?: run {
            Log.e("AUTH", "No access token in redirect")
            return
        }

        val refreshToken = params["refresh_token"] ?: ""
        val expiresIn = params["expires_in"]?.toLong() ?: 3600
        val tokenType = params["token_type"] ?: "bearer"

        // Now fetch the user info from Supabase API
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${BuildConfig.SUPABASE_URL}/auth/v1/user")
                    .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val response = client.newCall(request).execute()
                val userJson = JSONObject(response.body.string())
                Log.d("AUTH", "User info: $userJson")

                val userInfo = UserInfo(
                    id = userJson.getString("id"),
                    email = userJson.optString("email"),
                    role = userJson.optString("role", "authenticated"),
                    aud = userJson.optString("aud")
                )

                val session = UserSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn,
                    tokenType = tokenType,
                    user = userInfo
                )

                Log.d("AUTH >>", "maybeHandleRedirect: $session")

                withContext(Dispatchers.Main) {
                    supabase.auth.importSession(
                        session = session,
                        autoRefresh = true,
                        source = SessionSource.SignIn(Github)
                    )
                    goHome()
                }

            } catch (e: Exception) {
                Log.e("AUTH", "Failed to import session", e)
            }
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            supabase.auth.signInWith(provider = Github, redirectUrl = "myapp://auth")
            supabase.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    goHome()
                }
            }
        }
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}