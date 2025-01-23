package com.danielurbina.booknest

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ReadingGoalsAdapter(
    private val goals: MutableList<ReadingGoal>,
    private val onDelete: (ReadingGoal) -> Unit,
    private val onStatusChange: (ReadingGoal, Boolean) -> Unit
) : RecyclerView.Adapter<ReadingGoalsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val goalDescription: TextView = view.findViewById(R.id.goal_description)
        val goalCheckbox: CheckBox = view.findViewById(R.id.goal_checkbox)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_goal_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reading_goal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]
        holder.goalDescription.text = goal.description
        holder.goalCheckbox.isChecked = goal.isCompleted
        val color = ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark)
        holder.goalCheckbox.buttonTintList = ColorStateList.valueOf(color)
        holder.goalCheckbox.setOnCheckedChangeListener { _, isChecked ->
            onStatusChange(goal, isChecked)
        }
        holder.deleteButton.setOnClickListener {
            onDelete(goal)
        }
    }

    override fun getItemCount(): Int = goals.size
}