package com.alvinfungai.flower

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val supabase get() = SupabaseClientProvider.client
    private lateinit var textViewGreeting: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val user = supabase.auth.currentUserOrNull()

        textViewGreeting = findViewById<TextView>(R.id.textViewGreeting)
        textViewGreeting.text = "Welcome, ${user?.email}"
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            lifecycleScope.launch {
                supabase.auth.signOut()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}