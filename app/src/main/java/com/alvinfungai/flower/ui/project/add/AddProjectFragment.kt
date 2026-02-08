package com.alvinfungai.flower.ui.project.add

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alvinfungai.flower.R
import com.alvinfungai.flower.data.model.Project
import com.alvinfungai.flower.data.model.Technology
import com.alvinfungai.flower.data.remote.SupabaseClientProvider
import com.alvinfungai.flower.ui.common.UiState
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class AddProjectFragment : Fragment(R.layout.fragment_add_project) { // Standard way to provide layout

    private val supabase = SupabaseClientProvider.client
    private val viewModel: AddProjectViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        val etTitle = view.findViewById<EditText>(R.id.et_title)
        val etRepoUrl = view.findViewById<EditText>(R.id.et_repo_url)
        val etDescription = view.findViewById<EditText>(R.id.et_description)
        val techChipGroup = view.findViewById<ChipGroup>(R.id.chip_group_tech)
        val buttonSaveProject = view.findViewById<Button>(R.id.btn_save_project)

        // 1. Load technologies from Supabase
        viewModel.fetchProjectTechnologies()
        lifecycleScope.launch {
            try {
                viewModel.state.collect { technologies ->
                    techChipGroup.removeAllViews()
                    technologies.forEach { tech ->
                        val chip = Chip(context).apply {
                            text = tech.name
                            isCheckable = true
                            tag = tech.id
                            // 1. Set Chip background color when selected
                            setChipBackgroundColorResource(R.color.chip_background_selector)
                            // 2. Set the text color selector
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
            } catch (e: Exception) {
                // Log the error so you can see it in Logcat!
                Log.e("SupabaseError", "Error fetching techs", e)
            }
        }


         // 2. Observe sae status
        lifecycleScope.launch {
            viewModel.saveStatus.collect { state ->
                when (state) {
                    is UiState.Loading -> buttonSaveProject.isEnabled = false
                    is UiState.Success -> findNavController().navigate(R.id.action_addProjectFragment_to_successFragment)
                    is UiState.Error -> {
                        buttonSaveProject.isEnabled = true
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }


        buttonSaveProject.setOnClickListener {
            // 3. CAPTURE INPUT HERE FROM VIEWS (When button is clicked)
            val title = etTitle.text.toString().trim()
            val repoUrl = etRepoUrl.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Title required"
                return@setOnClickListener
            }

            if (repoUrl.isEmpty()) {
                etTitle.error = "Project URL required"
                return@setOnClickListener
            }

            // 3. Get Selected Tech IDs from the Chips
            val selectedTechIds = mutableListOf<String>()
            for (i in 0 until techChipGroup.childCount) {
                val chip = techChipGroup.getChildAt(i) as Chip
                if (chip.isChecked) {
                    // Use the TAG (ID) not the TEXT (Name) for database keys
                    selectedTechIds.add(chip.tag.toString())
                }
            }

            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                lifecycleScope.launch {
                    try {
                        viewModel.saveProject(
                            userId = user.id,
                            title = title,
                            description = description,
                            repoUrl = repoUrl,
                            selectedTechIds = selectedTechIds
                        )
                    } catch (e: Exception) {
                        Toast.makeText(context, "You must be logged in", Toast.LENGTH_SHORT).show()
                        Log.e("SupabaseError", "Error saving project", e)
                    }
                }
            }
        }
    }
}