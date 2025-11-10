package com.flipcover.widgets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CoverWidgetAdapter(
    private val widgets: List<CoverWidget>,
    private val onRemove: (CoverWidget) -> Unit
) : RecyclerView.Adapter<CoverWidgetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val labelText: TextView = view.findViewById(R.id.widgetLabelText)
        val removeButton: ImageButton = view.findViewById(R.id.removeWidgetButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cover_widget, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val widget = widgets[position]
        holder.labelText.text = widget.label
        holder.removeButton.setOnClickListener {
            onRemove(widget)
        }
    }

    override fun getItemCount() = widgets.size
}
