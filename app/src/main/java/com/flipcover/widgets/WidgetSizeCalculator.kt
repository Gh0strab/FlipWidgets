package com.flipcover.widgets

import android.appwidget.AppWidgetProviderInfo
import kotlin.math.ceil

object WidgetSizeCalculator {
    
    private const val CELL_SIZE_DP = 70
    private const val CELL_GAP_DP = 8
    const val MAX_GRID_SPAN = 4
    
    data class GridSize(
        val columnSpan: Int,
        val rowSpan: Int,
        val isClamped: Boolean = false
    )
    
    fun calculateGridSize(widgetInfo: AppWidgetProviderInfo): GridSize {
        val widthResult = calculateCellsForDimension(widgetInfo.minWidth)
        val heightResult = calculateCellsForDimension(widgetInfo.minHeight)
        
        val isClamped = !widthResult.fitsInGrid || !heightResult.fitsInGrid
        
        return GridSize(
            columnSpan = widthResult.cells,
            rowSpan = heightResult.cells,
            isClamped = isClamped
        )
    }
    
    private data class CellCalculation(
        val cells: Int,
        val fitsInGrid: Boolean
    )
    
    private fun calculateCellsForDimension(requiredDp: Int): CellCalculation {
        if (requiredDp <= 0) return CellCalculation(1, true)
        
        var cells = 1
        while (cells <= MAX_GRID_SPAN) {
            val allocatedDp = CELL_SIZE_DP * cells + CELL_GAP_DP * (cells - 1)
            if (allocatedDp >= requiredDp) {
                return CellCalculation(cells, true)
            }
            cells++
        }
        
        val maxAllocatedDp = CELL_SIZE_DP * MAX_GRID_SPAN + CELL_GAP_DP * (MAX_GRID_SPAN - 1)
        val fitsInGrid = maxAllocatedDp >= requiredDp
        
        return CellCalculation(MAX_GRID_SPAN, fitsInGrid)
    }
    
    fun calculateWidgetSizePx(gridSpan: Int, density: Float): Int {
        val cellSizePx = (CELL_SIZE_DP * density).toInt()
        val gapSizePx = (CELL_GAP_DP * density).toInt()
        return (cellSizePx * gridSpan) + (gapSizePx * (gridSpan - 1))
    }
    
    fun getMaxGridDimensions(widgetInfoList: List<AppWidgetProviderInfo>): Pair<Int, Int> {
        var maxColumns = 4
        var maxRows = 4
        
        for (info in widgetInfoList) {
            val gridSize = calculateGridSize(info)
            maxColumns = maxOf(maxColumns, gridSize.columnSpan)
            maxRows = maxOf(maxRows, gridSize.rowSpan)
        }
        
        return Pair(maxColumns, maxRows)
    }
}
