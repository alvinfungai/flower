package com.alvinfungai.flower

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class AddProjectFragment : Fragment(R.layout.fragment_add_project) { // Standard way to provide layout
    private val supabase = SupabaseClientProvider.client

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        val etTitle = view.findViewById<EditText>(R.id.et_title)
        val etRepoUrl = view.findViewById<EditText>(R.id.et_repo_url)
        val etDescription = view.findViewById<EditText>(R.id.et_description)
        val techChipGroup = view.findViewById<ChipGroup>(R.id.chip_group_tech)
        val buttonSaveProject = view.findViewById<Button>(R.id.btn_save_project)

        // 1. Load technologies from Supabase
        lifecycleScope.launch {
            try {
                val techList = supabase.from("technologies").select().decodeList<Technology>()

                // Ensure fragment is still active before touching UI
                val context = context ?: return@launch

                techList.forEach { tech ->
                    val chip = Chip(context).apply {
                        text = tech.name
                        isCheckable = true
                        tag = tech.id
                        // 1. Set Chip background color when selected
                        setChipBackgroundColorResource(R.color.chip_background_selector)
                        // 2. Set the text color selector
                        setTextColor(androidx.appcompat.content.res.AppCompatResources.getColorStateList(context, R.color.chip_text_color))
                    }
                    techChipGroup.addView(chip)
                }
            } catch (e: Exception) {
                // Log the error so you can see it in Logcat!
                Log.e("SupabaseError", "Error fetching techs", e)
            }
        }

        buttonSaveProject.setOnClickListener {
            // 2. CAPTURE INPUT HERE (When button is clicked)
            val title = etTitle.text.toString().trim()
            val repoUrl = etRepoUrl.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Title required"
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
                        val draftProject = Project(
                            userId = user.id,
                            title = title,
                            repoUrl = repoUrl,
                            description = description,
                        )

                        val newProject = supabase.from("projects").insert(draftProject) {
                            select()
                        }.decodeSingle<Project>()

                        // Insert join table rows
                        if (selectedTechIds.isNotEmpty()) {
                            val joinRows = selectedTechIds.map { techId ->
                                mapOf("project_id" to newProject.id, "tech_id" to techId)
                            }
                            supabase.from("project_tech").insert(joinRows)
                        }

                        // Successfully saved! Maybe navigate back?
                         findNavController().navigate(R.id.action_addProjectFragment_to_successFragment)

                    } catch (e: Exception) {
                        Log.e("SupabaseError", "Error saving project", e)
                    }
                }
            }
        }
    }
}