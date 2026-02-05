package com.alvinfungai.flower

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import de.hdodenhof.circleimageview.CircleImageView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    val supabase = SupabaseClientProvider.client
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
        val projectsRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_profile_projects)
        val editProfileButton = view.findViewById<Button>(R.id.btn_edit_profile)
        val logoutButton = view.findViewById<Button>(R.id.btn_logout)

        // Logout action
        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                attemptLogout()
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
            lifecycleScope.launch {
                val profile = fetchUserProfile(targetUserId)
                if (profile != null) {
                    // Update UI
                    Log.d("ProfileFragment", "onViewCreated: $profile")
                    profileImage.load(profile.avatarUrl)
                    textViewUsername.text = profile.fullName
                    textViewBio.text = profile.bio
                    val projects = fetchUserProjects(targetUserId)
                    projectsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    val adapter = ProjectsAdapter(projects) { project ->
                        // create a bundle and put the string inside
                        val bundle = Bundle().apply {
                            putString("project_id", project.id)
                        }
                        // nav
                        findNavController().navigate(R.id.action_profileFragment_to_projectDetailFragment, bundle)
                    }
                    projectsRecyclerView.adapter = adapter
                }
            }
        }
    }

    private suspend fun fetchUserProfile(viewedUserId: String): Profile? {
            try {
                // Fetch single profile where id matches current user
                return supabase.from("profiles")
                    .select {
                        filter {
                            eq("id", viewedUserId)
                        }
                    }.decodeSingleOrNull<Profile>()
            } catch (e: Exception) {
                Log.e("AUTH_DEBUG", "Error fetching profile: ${e.message}")
                Toast.makeText(view?.context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        return null
    }

    private suspend fun fetchUserProjects(userId: String): List<Project> {
        try {
            return supabase.from("projects")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList()
        } catch (e: Exception) {
            Log.e("PROFILE", "Error fetching projects: ${e.message}")
            Toast.makeText(view?.context, "Failed to load profile projects", Toast.LENGTH_SHORT).show()
        }
        return emptyList()
    }

    private suspend fun attemptLogout() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            Log.e("AUTH_DEBUG", "Logout error: ${e.message}")
            Toast.makeText(view?.context, "Failed to logout", Toast.LENGTH_SHORT).show()
        }
    }
}