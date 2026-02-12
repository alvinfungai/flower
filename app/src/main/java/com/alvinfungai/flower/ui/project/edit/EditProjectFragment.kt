package com.alvinfungai.flower.ui.project.edit

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alvinfungai.flower.R
import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.ProjectTechJoin
import com.alvinfungai.flower.data.model.Technology
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.ui.common.UiState
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class EditProjectFragment : Fragment(R.layout.fragment_add_project) {
    private val supabase = SupabaseClientProvider.client
    private val viewModel: EditProjectViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val projectId = arguments?.getString("project_id") ?: return

        Log.d("EditProjectFragment", "onViewCreated: $projectId")

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val etTitle = view.findViewById<EditText>(R.id.et_title)
        val etRepoUrl = view.findViewById<EditText>(R.id.et_repo_url)
        val etDescription = view.findViewById<EditText>(R.id.et_description)
        val techChipGroup = view.findViewById<ChipGroup>(R.id.chip_group_tech)
        val btnSave = view.findViewById<Button>(R.id.btn_save_project)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(
                menu: Menu,
                menuInflater: MenuInflater
            ) {
                menuInflater.inflate(R.menu.edit_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        val techIds = (0 until techChipGroup.childCount)
                            .map { techChipGroup.getChildAt(it) as Chip }
                            .filter { it.isChecked }
                            .map { it.tag.toString() }

                        viewModel.updateProject(
                            projectId,
                            etTitle.text.toString(),
                            etDescription.text.toString(),
                            etRepoUrl.text.toString(),
                            techIds
                        )
                        true
                    }
                    else -> false
                }
            }

        },
            viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewModel.loadInitialData(projectId)

        // Dynamically change views since we are reusing add_project layout
        tvTitle.text = "Edit Project"
        btnSave.text = "Update Project"

        // Observe state
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state is UiState.Success) {
                    val project = state.data
                    etTitle.setText(project.title)
                    etDescription.setText(project.description)
                    etRepoUrl.setText(project.repoUrl)

                    // 2 Fetch tech stack and identify highlighted ones
                    val allTech = supabase.from("technologies").select().decodeList<Technology>()
                    val selectedTechIds = supabase.from("project_tech")
                        .select { filter { eq("project_id", projectId) } }
                        .decodeList<ProjectTechJoin>()
                        .map { it.techId }

                    Log.d("EditProjectFragment", "onViewCreated: $allTech")

                    allTech.forEach { tech ->
                        val chip = Chip(requireContext()).apply {
                            text = tech.name
                            isCheckable = true
                            tag = tech.id
                            isChecked = selectedTechIds.contains(tech.id)
                            setChipBackgroundColorResource(R.color.chip_background_selector)
                            setTextColor(
                                AppCompatResources.getColorStateList(
                                    context,
                                    R.color.chip_text_color
                                )
                            )
                        }
                        techChipGroup.addView(chip)
                    }
                }
            }
        }

        // Observe Save Status
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveStatus.collect { state ->
                when (state) {
                    is UiState.Loading -> btnSave.isEnabled = false
                    is UiState.Deleted -> findNavController().navigate(R.id.action_editProjectFragment_to_successFragment)
                    is UiState.Error -> {
                        btnSave.isEnabled = true
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        Log.d("EditProjectFragment", "onViewCreated: ${state.message}")
                    }
                    else -> {}
                }
            }
        }

        btnSave.setOnClickListener {
            val techIds = (0 until techChipGroup.childCount)
                .map { techChipGroup.getChildAt(it) as Chip }
                .filter { it.isChecked }
                .map { it.tag.toString() }

            viewModel.updateProject(
                projectId,
                etTitle.text.toString(),
                etDescription.text.toString(),
                etRepoUrl.text.toString(),
                techIds
            )
        }




        // 1. Fetch existing project
//        lifecycleScope.launch {
//            try {
//                viewModel.state.collect { state ->
//                    when (state) {
//                        is UiState.Loading -> btnSave.isEnabled = false
//                        is UiState.Success -> {
//                            Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show()
//                            findNavController().popBackStack()
//                        }
//                    }
//                }
//                val project = supabase.from("projects")
//                    .select { filter { Project::id eq projectId } }
//                    .decodeSingle<Project>()
//
//                // Pre-populate fields
//                etTitle.setText(project.title)
//                etRepoUrl.setText(project.repoUrl)
//                etDescription.setText(project.description)
//
//                // 2 Fetch tech stack and identify highlighted ones
//                val allTech = supabase.from("technologies").select().decodeList<Technology>()
//                val selectedTechIds = supabase.from("project_tech")
//                    .select { filter { eq("project_id", projectId) } }
//                    .decodeList<ProjectTechJoin>()
//                    .map { it.techId }
//
//                Log.d("EditProjectFragment", "onViewCreated: $allTech")
//
//                allTech.forEach { tech ->
//                    val chip = Chip(requireContext()).apply {
//                        text = tech.name
//                        isCheckable = true
//                        tag = tech.id
//                        isChecked = selectedTechIds.contains(tech.id)
//                        setChipBackgroundColorResource(R.color.chip_background_selector)
//                        setTextColor(
//                            AppCompatResources.getColorStateList(
//                                context,
//                                R.color.chip_text_color
//                            )
//                        )
//                    }
//                    techChipGroup.addView(chip)
//                }
//            } catch (e: Exception) {
//                Log.e("EditError", "Failed to load project", e)
//            }
//
//            // 3. Update logic
//            btnSave.setOnClickListener {
//                val updateTitle = etTitle.text.toString()
//                val updatedRepoUrl = etRepoUrl.text.toString()
//                val updatedDescription = etDescription.text.toString()
//
//                lifecycleScope.launch {
//                    try {
//                        // update projects table supabase
//                        supabase.from("projects").update({
//                            Project::title setTo updateTitle
//                            Project::repoUrl setTo updatedRepoUrl
//                            Project::description setTo updatedDescription
//                        }) { filter { Project::id eq projectId } }
//
//                        // Delete old joins and insert new ones
//                        supabase.from("project_tech").delete {
//                            filter { eq("project_id", projectId) }
//                        }
//
//                        val newSelections = mutableListOf<Map<String, String>>()
//                        for (i in 0 until techChipGroup.childCount) {
//                            val chip = techChipGroup.getChildAt(i) as Chip
//                            if (chip.isChecked) {
//                                newSelections.add(
//                                    mapOf(
//                                        "project_id" to projectId,
//                                        "tech_id" to chip.tag.toString()
//                                    )
//                                )
//                            }
//                        }
//                        supabase.from("project_tech").insert(newSelections)
//
//                        // Navigate to Success
//                        findNavController().navigate(R.id.action_editProjectFragment_to_successFragment)
//                    } catch (e: Exception) {
//                        Log.e("UpdateError", "Update failed", e)
//                    }
//                }
//            }
//        }
    }
}