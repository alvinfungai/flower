package com.alvinfungai.flower.ui.project.home

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvinfungai.flower.ui.common.ProjectsAdapter
import com.alvinfungai.flower.R
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.data.repository.SupabaseProjectRepository
import com.alvinfungai.flower.ui.common.dp
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(SupabaseProjectRepository(SupabaseClientProvider.client))
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_home_feed)
        val fabAddProject = view.findViewById<FloatingActionButton>(R.id.fab_add_project)
        val progressBar = view.findViewById<ProgressBar>(R.id.pb_home_loading)

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

        // Setup shared adapter
        val adapter = ProjectsAdapter(
            onItemClick = { project ->
                val bundle = Bundle().apply { putString("project_id", project.id) }
                findNavController().navigate(R.id.action_homeFragment_to_projectDetailFragment, bundle)
            },
            onVoteClick = { projectId, isUpVote ->
                viewModel.onVoteInHome(projectId, isUpVote)
            }
        )

        // Set the LayoutManager
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadProjects()
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is HomeUiState.Loading -> progressBar.visibility = View.VISIBLE
                        is HomeUiState.Success -> {
                            progressBar.visibility = View.GONE
                            adapter.submitList(state.projects)
                        }
                        is HomeUiState.Error -> {
                            progressBar.visibility = View.GONE
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        fabAddProject.setOnClickListener {
            // Use the action ID you defined in the XML
            findNavController().navigate(R.id.action_homeFragment_to_addProjectFragment)
        }
    }
}