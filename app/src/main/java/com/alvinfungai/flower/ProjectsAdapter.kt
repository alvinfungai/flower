package com.alvinfungai.flower

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ProjectsAdapter(
    private val itemList: List<Project>,
    private val onItemClick: (Project) -> Unit
    ) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {
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
        val currentItem = itemList[position]
        holder.title.text = currentItem.title
        holder.description.text = currentItem.description

        // set OnClickListener
        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int = itemList.size

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val description: TextView = itemView.findViewById(R.id.tv_description)
    }
}