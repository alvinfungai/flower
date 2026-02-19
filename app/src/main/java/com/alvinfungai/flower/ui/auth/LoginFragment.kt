package com.alvinfungai.flower.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.alvinfungai.flower.R
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Github
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
//    private val supabase = SupabaseClientProvider.client
    private val viewModel: MainViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_login_with_github).setOnClickListener {
            viewModel.loginWithGithub()
        }
        view.findViewById<Button>(R.id.btn_signin_with_google).setOnClickListener {
            viewModel.loginWithGoogle(requireContext())
        }
    }
}