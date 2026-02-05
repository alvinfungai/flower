package com.alvinfungai.flower

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch


class ProjectDetailFragment : Fragment(R.layout.fragment_project_detail) {
    private val supabase = SupabaseClientProvider.client
    private val viewModel: ProjectDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = supabase.auth.currentUserOrNull()?.id
        val projectId = arguments?.getString("project_id") ?: return

        Log.d("ProjectDetailFragment", "onViewCreated: id: $projectId")

        viewModel.fetchProjectDetails(projectId)

        val tvTitle = view.findViewById<TextView>(R.id.tv_detail_title)
        val tvRepoUrl = view.findViewById<TextView>(R.id.tv_detail_repo)
        val tvDescription = view.findViewById<TextView>(R.id.tv_detail_desc)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_detail_tech)
        val fabEdit = view.findViewById<FloatingActionButton>(R.id.fab_edit_project)
        val btnDelete = view.findViewById<Button>(R.id.btn_delete_project)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pb_loading)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is UiState.Success -> {
                        val project = state.data
                        // Check if the current user is the owner
                        val isOwner = project.userId == currentUserId

                        Log.d("ProjectDetailFragment", "onViewCreated: $currentUserId: ${project.userId}")
                        // Toggle visibility based on ownership
                        if (isOwner) {
                            fabEdit.visibility = View.VISIBLE
                            btnDelete.visibility = View.VISIBLE
                            btnDelete.isEnabled = true
                        } else {
                            fabEdit.visibility = View.GONE
                            btnDelete.visibility = View.GONE
                        }
                        pbLoading.visibility = View.GONE

                        // ui bindings
                        tvTitle.text = project.title
                        tvRepoUrl.text = project.repoUrl
                        tvDescription.text = project.description

                        Log.d("ProjectDetailFragment", "onViewCreated: $project")

                        // clear and add chips
                        chipGroup.removeAllViews()
                        project.technologies.forEach { tech ->
                            val chip = Chip(context).apply { text = tech.name }
                            chipGroup.addView(chip)
                        }

                        // Setup edit nav
                        fabEdit.setOnClickListener {
                            // create a bundle and put the string ID inside
                            val bundle = Bundle().apply {
                                putString("project_id", project.id)
                            }
                            findNavController().navigate(
                                R.id.action_projectDetailsFragment_to_editProjectFragment,
                                bundle
                            )
                        }
                    }

                    is UiState.Error -> {
                        // show error message toast
                        pbLoading.visibility = View.GONE
                        btnDelete.isEnabled = true
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }

                    is UiState.Loading -> {
                        btnDelete.isEnabled = false  // Prevent double click
                        pbLoading.visibility = View.VISIBLE // show progress bar
                    }
                }
            }
        }

        btnDelete.setOnClickListener {
            Log.d("ProjectDetailFragment", "onViewCreated: CLICKED")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Project?")
                .setMessage("This will permanently remove the project. Are you sure?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteProject(projectId) {
                        // Navigate back to home and show toast message
                        Toast.makeText(context, "Project deleted", Toast.LENGTH_SHORT).show()

                        // Check if the fragment is still active before navigating
                        if (isAdded) {
                            // Navigate to previous destination -> Home
                            findNavController().navigateUp()
                        }
                    }
                }
                .show()
        }
    }
}