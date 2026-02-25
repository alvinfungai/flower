package com.alvinfungai.flower.ui.common

import android.graphics.Color
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alvinfungai.flower.R
import com.alvinfungai.flower.data.model.Project

class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
    override fun areItemsTheSame(
        oldItem: Project,
        newItem: Project
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: Project,
        newItem: Project
    ): Boolean = oldItem == newItem

}
class ProjectsAdapter(
    private val onItemClick: (Project) -> Unit,
    private val onVoteClick: (String, Boolean) -> Unit
    ) : ListAdapter<Project, ProjectsAdapter.ProjectViewHolder>(ProjectDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.project_item_layout, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProjectViewHolder,
        position: Int
    ) {

        // bind data
        val currentItem = getItem(position)

        holder.title.text = currentItem.title
        holder.description.text = currentItem.description
        holder.voteScore.text = currentItem.voteScore.toString()

        // Apply Activated state based on userVote
        // This triggers the ColorStateList tint
        holder.btnUpvote.isActivated = currentItem.userVote == true
        holder.btnDownvote.isActivated = currentItem.userVote == false

        holder.btnUpvote.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onVoteClick(currentItem.id!!, true)
        }

        holder.btnDownvote.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onVoteClick(currentItem.id!!, false)
        }

        // Color the score text based on state
        val scoreColor = when(currentItem.userVote) {
            true -> "#4CAF50".toColorInt()
            false -> "#F44336".toColorInt()
            else -> Color.GRAY
        }
        holder.voteScore.setTextColor(scoreColor)

        // set OnClickListener
        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val description: TextView = itemView.findViewById(R.id.tv_description)
        val voteScore: TextView = itemView.findViewById(R.id.tv_vote_score)
        val btnUpvote: AppCompatImageButton = itemView.findViewById(R.id.btn_upvote)
        val btnDownvote: AppCompatImageButton = itemView.findViewById(R.id.btn_downvote)
    }
}