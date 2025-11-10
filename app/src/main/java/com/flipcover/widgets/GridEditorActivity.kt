package com.flipcover.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GridEditorActivity : AppCompatActivity() {

    private lateinit var gridView: RecyclerView
    private lateinit var addWidgetButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var widgetDataManager: WidgetDataManager
    private lateinit var appWidgetManager: AppWidgetManager
    private var containerId: Int = 0
    private val gridSize = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid_editor)

        widgetDataManager = WidgetDataManager(this)
        appWidgetManager = AppWidgetManager.getInstance(this)
        containerId = intent.getIntExtra("container_id", 0)

        setupViews()
        loadGrid()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.edit_grid)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        gridView = findViewById(R.id.gridRecyclerView)
        gridView.layoutManager = GridLayoutManager(this, gridSize)

        addWidgetButton = findViewById(R.id.addWidgetToGridButton)
        addWidgetButton.setOnClickListener {
            showWidgetPicker()
        }

        saveButton = findViewById(R.id.saveGridButton)
        saveButton.setOnClickListener {
            saveGrid()
        }
    }

    private fun loadGrid() {
        val widgets = widgetDataManager.getWidgetsForContainer(containerId)
        val gridItems = createGridItems(widgets)
        
        val adapter = GridEditorAdapter(gridItems) { position ->
            handleGridItemClick(position)
        }
        gridView.adapter = adapter
    }

    private fun createGridItems(widgets: List<ChildWidgetData>): List<GridItem> {
        val items = mutableListOf<GridItem>()
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                val widget = widgets.find { it.gridX == x && it.gridY == y }
                items.add(GridItem(x, y, widget))
            }
        }
        return items
    }

    private fun handleGridItemClick(position: Int) {
        val x = position % gridSize
        val y = position / gridSize
        showWidgetPickerForPosition(x, y)
    }

    private fun showWidgetPicker() {
        showWidgetPickerForPosition(0, 0)
    }

    private fun showWidgetPickerForPosition(x: Int, y: Int) {
        val installedProviders = appWidgetManager.installedProviders
        val widgetNames = installedProviders.map { it.loadLabel(packageManager) }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Widget")
            .setItems(widgetNames) { _, which ->
                val provider = installedProviders[which]
                addWidgetToGrid(provider, x, y)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addWidgetToGrid(providerInfo: AppWidgetProviderInfo, x: Int, y: Int) {
        val widgetData = ChildWidgetData(
            id = java.util.UUID.randomUUID().toString(),
            provider = providerInfo.provider,
            label = providerInfo.loadLabel(packageManager),
            icon = null,
            gridX = x,
            gridY = y,
            gridWidth = 1,
            gridHeight = 1,
            containerId = containerId
        )

        widgetDataManager.saveWidget(widgetData)
        loadGrid()
        Toast.makeText(this, "Widget added at ($x, $y)", Toast.LENGTH_SHORT).show()
    }

    private fun saveGrid() {
        Toast.makeText(this, "Grid saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

data class GridItem(
    val x: Int,
    val y: Int,
    val widget: ChildWidgetData?
)
