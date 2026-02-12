package com.alvinfungai.flower.ui.project.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvinfungai.flower.ui.common.ProjectsAdapter
import com.alvinfungai.flower.R
import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.ui.common.dp
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val supabase = SupabaseClientProvider.client
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_projects)
        val fabAddProject = view.findViewById<FloatingActionButton>(R.id.fab_add_project)

        ViewCompat.setOnApplyWindowInsetsListener(fabAddProject) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val params = view.layoutParams as ViewGroup.MarginLayoutParams

            val bottomNavHeight = 80.dp // Standard Material 3 Navigation Bar height
            params.bottomMargin = insets.bottom + bottomNavHeight + 16.dp
            view.layoutParams = params

            windowInsets
        }

        // 1. Set the LayoutManager
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            try {
                // 2. API call to fetch projects list
                val projects = supabase.from("projects")
                    .select()
                    .decodeList<Project>()

                // 3. Set the Adapter to the recycler view
                recyclerView.adapter = ProjectsAdapter(projects) { project ->
                    // create a bundle and put the string inside
                    val bundle = Bundle().apply {
                        putString("project_id", project.id)
                    }

                    // Navigate using the Action ID and the bundle
                    findNavController().navigate(
                        R.id.action_homeFragment_to_projectDetailFragment,
                        bundle
                    )
                }
                Log.d("PROJECTS", "fetchUserPosts: $projects")
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Fetch failed: ${e.message}")
            }
        }

        fabAddProject.setOnClickListener {
            Log.d("NAV", "Attempting to navigate to Add Project")
            // Use the action ID you defined in the XML
            findNavController().navigate(R.id.action_homeFragment_to_addProjectFragment)
        }
    }
}