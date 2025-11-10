package com.flipcover.widgets

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class GridEditorAdapter(
    private val gridItems: List<GridItem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<GridEditorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.gridItemCard)
        val iconView: ImageView = view.findViewById(R.id.gridItemIcon)
        val labelView: TextView = view.findViewById(R.id.gridItemLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grid_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = gridItems[position]
        
        if (item.widget != null) {
            holder.labelView.text = item.widget.label
            holder.iconView.setImageBitmap(item.widget.icon)
            holder.iconView.visibility = View.VISIBLE
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
        } else {
            holder.labelView.text = ""
            holder.iconView.visibility = View.GONE
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = gridItems.size
}
