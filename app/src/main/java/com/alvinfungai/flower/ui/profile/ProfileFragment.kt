package com.alvinfungai.flower.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.alvinfungai.flower.R
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.ui.common.ProjectsAdapter
import de.hdodenhof.circleimageview.CircleImageView
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    val supabase = SupabaseClientProvider.client
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var profileImage: CircleImageView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Attempt to get profile ID being viewed from Fragment args
        // (these are passed when user list item click event)
        val viewedUserId = arguments?.getString("user_id")

        // If user argument is null, default to logged in user ID
        val targetUserId = viewedUserId ?: supabase.auth.currentUserOrNull()?.id

        profileImage = view.findViewById<CircleImageView>(R.id.img_profile)
        val textViewUsername = view.findViewById<TextView>(R.id.tv_username)
        val textViewBio = view.findViewById<TextView>(R.id.tv_user_bio)
        val projectsRecyclerView =
            view.findViewById<RecyclerView>(R.id.recycler_view_profile_projects)
        val editProfileButton = view.findViewById<Button>(R.id.btn_edit_profile)
        val logoutButton = view.findViewById<Button>(R.id.btn_logout)
        val progressBar = view.findViewById<ProgressBar>(R.id.pb_loading)
        val profileGroup = view.findViewById<Group>(R.id.profile_content_group)

        // Edit profile
        editProfileButton.setOnClickListener {
            lifecycleScope.launch {
                // navigate to edit profile fragment
            }
        }

        // Logout action
        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.signOut()
            }
        }

        if (viewedUserId == targetUserId) {
            // Own profile
            editProfileButton.visibility = View.VISIBLE
            logoutButton.visibility = View.VISIBLE
        } else {
            // Other profile
            editProfileButton.visibility = View.VISIBLE
            logoutButton.visibility = View.VISIBLE
        }

        if (targetUserId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is ProfileUiState.Loading -> {
                                progressBar.visibility = View.GONE
                                profileGroup.visibility = View.GONE
                            }
                            is ProfileUiState.Success -> {
                                progressBar.visibility = View.GONE
                                profileGroup.visibility = View.VISIBLE

                                profileImage.load(state.profile.avatarUrl)
                                textViewUsername.text = state.profile.fullName
                                textViewBio.text = state.profile.bio
                                val projects = state.projects
                                projectsRecyclerView.layoutManager =
                                    LinearLayoutManager(requireContext())
                                val adapter = ProjectsAdapter(projects) { project ->
                                    // create a bundle and put the string inside
                                    val bundle = Bundle().apply {
                                        putString("project_id", project.id)
                                    }
                                    // nav
                                    findNavController().navigate(
                                        R.id.action_profileFragment_to_projectDetailFragment,
                                        bundle
                                    )
                                }
                                projectsRecyclerView.adapter = adapter
                            }
                            is ProfileUiState.Error -> {
                                progressBar.visibility = View.GONE
                                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            targetUserId.let { id ->
                viewModel.loadProfileData(id)
            }
        }
    }
}