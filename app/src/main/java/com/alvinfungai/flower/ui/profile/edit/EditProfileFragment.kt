package com.alvinfungai.flower.ui.profile.edit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.alvinfungai.flower.data.repository.SupabaseProfileRepository
import com.alvinfungai.flower.data.repository.SupabaseProjectRepository
import com.alvinfungai.flower.ui.common.ImageCompressor
import com.alvinfungai.flower.ui.profile.ProfileUiState
import com.alvinfungai.flower.ui.profile.ProfileViewModel
import com.alvinfungai.flower.ui.profile.ProfileViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import de.hdodenhof.circleimageview.CircleImageView
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    private val viewModel: ProfileViewModel by viewModels {
        // 1. Create the dependencies manually
        val profileRepository = SupabaseProfileRepository(SupabaseClientProvider.client)
        val projectRepository = SupabaseProjectRepository(SupabaseClientProvider.client)
        val imageCompressor = ImageCompressor(requireContext().applicationContext)

        // 2. Pass them into factory
        ProfileViewModelFactory(profileRepository, projectRepository, imageCompressor)
    }

    // register photo picker
    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // send to viewmodel
            val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
            if (userId != null) {
                viewModel.uploadProfileImage(userId, requireContext().contentResolver, uri)
            }
        } else {
            Toast.makeText(requireContext(), "No media selected", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUserId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
        currentUserId?.let { viewModel.loadProfileData(it) }

        val btnSaveProfile = view.findViewById<Button>(R.id.btn_save_profile)
        val tilFullName = view.findViewById<TextInputLayout>(R.id.til_full_name)
        val etFullName = view.findViewById<EditText>(R.id.et_full_name)
        val tilBio = view.findViewById<TextInputLayout>(R.id.til_bio)
        val etBio = view.findViewById<EditText>(R.id.et_bio)
        val imgProfile = view.findViewById<CircleImageView>(R.id.img_edit_profile)
        val fabChangePhoto = view.findViewById<FloatingActionButton>(R.id.fab_change_photo)
        val loadingOverlay = view.findViewById<View>(R.id.loading_overlay)

        val loadingOverlayTextView = view.findViewById<TextView>(R.id.overlay_message)

        loadingOverlayTextView.text = "Loading profile ..."

        fabChangePhoto.setOnClickListener {
            // Launch image picker
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            loadingOverlayTextView.text = "Saving profile image ..."
        }

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
                        else -> {}
                    }
                }
            }
        }

        btnSaveProfile.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val bio = etBio.text.toString().trim()

            if (name.isEmpty()) {
                tilFullName.error = "Name is required"
                return@setOnClickListener
            }
            viewModel.updateUserProfile(name, bio)
        }
    }
}