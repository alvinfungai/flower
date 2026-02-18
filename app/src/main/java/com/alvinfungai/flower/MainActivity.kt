package com.alvinfungai.flower

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupWithNavController
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.ui.auth.AuthState
import com.alvinfungai.flower.ui.auth.MainViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val supabase = SupabaseClientProvider.client
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Install the splash screen before super.onCreate() lifecycle hook
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 2. Keep the splash screen visible until session check is complete
        splashScreen.setKeepOnScreenCondition {
            viewModel.state.value is AuthState.Loading
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val toolbar = findViewById<MaterialToolbar>(R.id.top_app_bar)

        // Config top-level destination for no "back button"
        val appBarConfig = AppBarConfiguration(setOf(R.id.loginFragment, R.id.homeFragment))

        // setup with navController
        setupWithNavController(toolbar, navController, appBarConfig)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        val navGraph = navController.navInflater.inflate(R.navigation.main_nav_graph)

        // Toggle BottomNav Visibility
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.profileFragment -> {
                    bottomNav.visibility = View.VISIBLE
                }
                else -> {
                    bottomNav.visibility = View.GONE
                }
            }
        }

        // 3. Once loading is done, setup navigation
        lifecycleScope.launch {
            // Only collect when activity started and visible
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is AuthState.Authenticated -> {
                            bottomNav.visibility = View.VISIBLE
                            if (navController.currentDestination?.id == R.id.loginFragment) {
                                navGraph.setStartDestination(R.id.homeFragment)
                                val navOptions = NavOptions.Builder()
                                    .setPopUpTo(R.id.loginFragment, true)
                                    .setLaunchSingleTop(true)
                                    .build()
                                navController.navigate(R.id.action_loginFragment_to_homeFragment, null, navOptions)
                            }
                        }
                        is AuthState.Unauthenticated -> {
                            navGraph.setStartDestination(R.id.loginFragment)
                            if (navController.currentDestination?.id != R.id.loginFragment) {
                                // clear backstack to remove "back button" into the app
                                navController.navigate(R.id.loginFragment, null, NavOptions.Builder()
                                    .setPopUpTo(navController.graph.startDestinationId, true)
                                    .build())
                            }
                            bottomNav.visibility = View.GONE
                        }
                        else -> {
                            // Handle Loading or Error states
                        }
                    }
                    navController.graph = navGraph
                    bottomNav.setupWithNavController(navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Set the new intent so handleDeeplinks finds the data
        setIntent(intent)

        // Parse the URL and complete login
        supabase.handleDeeplinks(intent)
    }
}