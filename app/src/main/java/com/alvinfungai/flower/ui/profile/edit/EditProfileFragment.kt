package com.alvinfungai.flower.ui.profile.edit

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import coil3.request.placeholder
import com.alvinfungai.flower.R
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.ui.profile.ProfileUiState
import com.alvinfungai.flower.ui.profile.ProfileViewModel
import com.google.android.material.textfield.TextInputLayout
import de.hdodenhof.circleimageview.CircleImageView
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    private val viewModel: ProfileViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUserId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
        currentUserId?.let { viewModel.loadProfileData(it) }

        val btnSaveProfile = view.findViewById<Button>(R.id.btn_save_profile)
        val tilFullname = view.findViewById<TextInputLayout>(R.id.til_full_name)
        val etFullName = view.findViewById<EditText>(R.id.et_full_name)
        val tilBio = view.findViewById<TextInputLayout>(R.id.til_bio)
        val etBio = view.findViewById<EditText>(R.id.et_bio)
        val imgProfile = view.findViewById<CircleImageView>(R.id.img_edit_profile)
        val loadingOverlay = view.findViewById<View>(R.id.loading_overlay)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ProfileUiState.Loading -> {
                            loadingOverlay.visibility = View.VISIBLE
                        }
                        is ProfileUiState.Success -> {
                            loadingOverlay.visibility = if (state.isSaving) View.VISIBLE else View.GONE
                            // pre-populate fields only if empty
                            if (etFullName.text.isNullOrEmpty()) {
                                etFullName.setText(state.profile.fullName)
                                etBio.setText(state.profile.bio)
                                imgProfile.load(state.profile.avatarUrl) {
                                    placeholder(R.drawable.ic_profile_placeholder)
                                }
                            }
                            // done saving, navigate back
                            if (state.isDoneSaving) {
                                // Reset flag so it doesn't trigger again on rotation
                                viewModel.resetSaveFlag()

                                Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()

                                // Navigate back
                                findNavController().popBackStack()
                            }
                        }
                        is ProfileUiState.Error -> {
                            loadingOverlay.visibility = View.GONE
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        btnSaveProfile.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val bio = etBio.text.toString().trim()

            if (name.isEmpty()) {
                tilFullname.error = "Name is required"
                return@setOnClickListener
            }
            viewModel.updateUserProfile(name, bio)
        }
    }
}