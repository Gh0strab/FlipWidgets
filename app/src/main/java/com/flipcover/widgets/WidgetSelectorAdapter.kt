package com.flipcover.widgets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WidgetSelectorAdapter(
    private val widgets: List<WidgetInfo>,
    private val onSelect: (WidgetInfo) -> Unit
) : RecyclerView.Adapter<WidgetSelectorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.widgetIcon)
        val labelView: TextView = view.findViewById(R.id.widgetLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_widget_selector, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val widget = widgets[position]
        holder.labelView.text = widget.label
        holder.iconView.setImageDrawable(widget.icon)
        holder.itemView.setOnClickListener {
            onSelect(widget)
        }
    }

    override fun getItemCount() = widgets.size
}
