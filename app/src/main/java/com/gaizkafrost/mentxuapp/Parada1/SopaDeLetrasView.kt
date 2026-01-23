package com.gaizkafrost.mentxuapp.Parada1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.gaizkafrost.mentxuapp.R
import kotlin.math.abs
import kotlin.math.min

class SopaDeLetrasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        isClickable = true
        isFocusable = true
        setWillNotDraw(false) // Asegurarse de que onDraw() se llame
    }

    private val grid = arrayOf(
        charArrayOf('F', 'S', 'G', 'G', 'Z', 'V', 'W', 'F', 'S', 'U', 'K', 'T', 'C', 'T'),
        charArrayOf('D', 'X', 'I', 'D', 'V', 'D', 'P', 'X', 'N', 'B', 'M', 'G', 'Q', 'F'),
        charArrayOf('U', 'R', 'M', 'O', 'K', 'O', 'A', 'R', 'I', 'P', 'R', 'M', 'P', 'D'),
        charArrayOf('S', 'V', 'S', 'H', 'X', 'E', 'P', 'W', 'D', 'W', 'Q', 'O', 'Z', 'X'),
        charArrayOf('Y', 'V', 'Y', 'F', 'L', 'U', 'M', 'A', 'Q', 'D', 'W', 'R', 'S', 'H'),
        charArrayOf('J', 'G', 'X', 'Z', 'Z', 'B', 'N', 'U', 'D', 'F', 'J', 'E', 'T', 'E'),
        charArrayOf('D', 'B', 'I', 'Z', 'I', 'D', 'U', 'N', 'A', 'J', 'X', 'A', 'Z', 'G'),
        charArrayOf('H', 'O', 'U', 'C', 'Y', 'E', 'C', 'V', 'M', 'Z', 'L', 'X', 'P', 'A'),
        charArrayOf('E', 'V', 'D', 'A', 'R', 'R', 'A', 'U', 'T', 'Z', 'A', 'Y', 'L', 'Z'),
        charArrayOf('F', 'Y', 'A', 'V', 'J', 'N', 'K', 'S', 'G', 'I', 'Y', 'P', 'U', 'T'),
        charArrayOf('Y', 'E', 'L', 'M', 'L', 'C', 'W', 'X', 'G', 'L', 'T', 'S', 'U', 'I'),
        charArrayOf('R', 'L', 'A', 'D', 'B', 'E', 'O', 'B', 'I', 'P', 'A', 'R', 'O', 'A'),
        charArrayOf('Y', 'U', 'R', 'J', 'C', 'A', 'K', 'A', 'I', 'O', 'A', 'Y', 'C', 'J'),
        charArrayOf('C', 'F', 'J', 'P', 'K', 'V', 'H', 'E', 'G', 'O', 'A', 'T', 'W', 'S')
    )

    private val rows = grid.size
    private val cols = grid[0].size

    // Definición de palabras y sus posiciones
    private val palabras = mapOf(
        "ARRAUTZA" to WordLocation(8, 3, 8, 10, Direction.HORIZONTAL),  // 8 letras
        "BIZIDUNA" to WordLocation(6, 1, 6, 8, Direction.HORIZONTAL),   // 8 letras
        "HEGOA" to WordLocation(13, 6, 13, 10, Direction.HORIZONTAL),
        "KAIOA" to WordLocation(12, 6, 12, 10, Direction.HORIZONTAL),
        "LUMA" to WordLocation(4, 4, 4, 7, Direction.HORIZONTAL),
        "MOREA" to WordLocation(2, 11, 6, 11, Direction.VERTICAL),
        "OBIPAROA" to WordLocation(11, 6, 11, 13, Direction.HORIZONTAL), // 8 letras
        "UDALA" to WordLocation(7, 2, 11, 2, Direction.VERTICAL),
        "MOKOA" to WordLocation(2, 2, 2, 6, Direction.HORIZONTAL),
        "HEGAZTIA" to WordLocation(4, 13, 11, 13, Direction.VERTICAL)     // 8 letras (fila 2, de derecha a izquierda)
    )

    private val palabrasEncontradas = mutableSetOf<String>()

    private var cellSize = 0f
    private var gridOffsetX = 0f
    private var gridOffsetY = 0f

    private val paintCellBackground = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val paintCell = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val paintText = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val paintSelected = Paint().apply {
        color = ContextCompat.getColor(context, R.color.yellow)
        style = Paint.Style.FILL
        alpha = 180  // Más opaco para que se vea mejor
    }

    private val paintFound = Paint().apply {
        color = ContextCompat.getColor(context, R.color.blue_green)
        style = Paint.Style.FILL
        alpha = 150
    }

    private var startCell: Pair<Int, Int>? = null
    private var currentCell: Pair<Int, Int>? = null
    private val selectedCells = mutableSetOf<Pair<Int, Int>>()

    var onPalabraEncontrada: ((String) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val availableWidth = w - paddingLeft - paddingRight
        val availableHeight = h - paddingTop - paddingBottom

        cellSize = min(availableWidth / cols.toFloat(), availableHeight / rows.toFloat())

        gridOffsetX = paddingLeft + (availableWidth - cellSize * cols) / 2
        gridOffsetY = paddingTop + (availableHeight - cellSize * rows) / 2

        paintText.textSize = cellSize * 0.6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Dibujar la cuadrícula de fondo y los bordes
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = gridOffsetX + col * cellSize
                val y = gridOffsetY + row * cellSize
                canvas.drawRect(x, y, x + cellSize, y + cellSize, paintCellBackground)
                canvas.drawRect(x, y, x + cellSize, y + cellSize, paintCell)
            }
        }

        // 2. Dibujar el resaltado de las palabras encontradas
        for ((palabra, location) in palabras) {
            if (palabrasEncontradas.contains(palabra)) {
                drawWordHighlight(canvas, location, paintFound)
            }
        }

        // 3. Dibujar la selección actual del usuario
        for (cell in selectedCells) {
            val (row, col) = cell
            val x = gridOffsetX + col * cellSize
            val y = gridOffsetY + row * cellSize
            canvas.drawRect(x, y, x + cellSize, y + cellSize, paintSelected)
        }

        // 4. Dibujar las letras encima de todo
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = gridOffsetX + col * cellSize
                val y = gridOffsetY + row * cellSize
                val letter = grid[row][col].toString()
                val textX = x + cellSize / 2
                val textY = y + cellSize / 2 - (paintText.descent() + paintText.ascent()) / 2
                canvas.drawText(letter, textX, textY, paintText)
            }
        }
    }


    private fun drawWordHighlight(canvas: Canvas, location: WordLocation, paint: Paint) {
        val cells = location.getCells()
        for (cell in cells) {
            val (row, col) = cell
            val x = gridOffsetX + col * cellSize
            val y = gridOffsetY + row * cellSize
            canvas.drawRect(x, y, x + cellSize, y + cellSize, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cell = getCellFromTouch(event.x, event.y) ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startCell = cell
                currentCell = cell
                updateSelectedCells()
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (startCell != null) {
                    currentCell = cell
                    updateSelectedCells()
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (startCell != null) {
                    currentCell = cell
                    updateSelectedCells()
                    if (selectedCells.isNotEmpty()) {
                        checkForWord()
                    }
                    startCell = null
                    currentCell = null
                    selectedCells.clear()
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getCellFromTouch(x: Float, y: Float): Pair<Int, Int>? {
        val col = ((x - gridOffsetX) / cellSize).toInt()
        val row = ((y - gridOffsetY) / cellSize).toInt()

        return if (row in 0 until rows && col in 0 until cols) {
            Pair(row, col)
        } else {
            null
        }
    }

    private fun updateSelectedCells() {
        selectedCells.clear()

        val start = startCell ?: return
        val current = currentCell ?: return

        val (startRow, startCol) = start
        val (currentRow, currentCol) = current

        val rowDiff = currentRow - startRow
        val colDiff = currentCol - startCol

        // Determinar si es horizontal, vertical o diagonal
        val direction = when {
            rowDiff == 0 -> Direction.HORIZONTAL
            colDiff == 0 -> Direction.VERTICAL
            abs(rowDiff) == abs(colDiff) -> {
                if (rowDiff * colDiff > 0) Direction.DIAGONAL_DOWN_RIGHT
                else Direction.DIAGONAL_DOWN_LEFT
            }
            else -> return // No es una dirección válida
        }

        // Añadir todas las celdas en esa dirección
        val steps = maxOf(abs(rowDiff), abs(colDiff))
        val rowStep = if (rowDiff == 0) 0 else rowDiff / abs(rowDiff)
        val colStep = if (colDiff == 0) 0 else colDiff / abs(colDiff)

        for (i in 0..steps) {
            val row = startRow + i * rowStep
            val col = startCol + i * colStep
            selectedCells.add(Pair(row, col))
        }
    }

    private fun checkForWord() {
        val selectedWord = buildSelectedWord()

        android.util.Log.d("SopaDeLetras", "checkForWord: selectedWord='$selectedWord', selectedCells=$selectedCells")

        for ((palabra, location) in palabras) {
            if (!palabrasEncontradas.contains(palabra)) {
                val wordCells = location.getCells()

                android.util.Log.d("SopaDeLetras", "  Checking '$palabra': expected=$wordCells, selected=$selectedCells")

                // Comprobar si la selección coincide con la palabra (en cualquier dirección)
                if (selectedCells == wordCells.toSet() ||
                    selectedCells == wordCells.reversed().toSet()) {
                    android.util.Log.d("SopaDeLetras", "  >>> FOUND: $palabra <<<")
                    palabrasEncontradas.add(palabra)
                    onPalabraEncontrada?.invoke(palabra)
                    break
                }
            }
        }
    }

    private fun buildSelectedWord(): String {
        val start = startCell ?: return ""
        val current = currentCell ?: return ""

        val sb = StringBuilder()
        for (cell in selectedCells.sortedWith(compareBy({ it.first }, { it.second }))) {
            val (row, col) = cell
            sb.append(grid[row][col])
        }

        return sb.toString()
    }

    data class WordLocation(
        val startRow: Int,
        val startCol: Int,
        val endRow: Int,
        val endCol: Int,
        val direction: Direction
    ) {
        fun getCells(): List<Pair<Int, Int>> {
            val cells = mutableListOf<Pair<Int, Int>>()

            val rowStep = when (direction) {
                Direction.VERTICAL -> 1
                Direction.DIAGONAL_DOWN_RIGHT, Direction.DIAGONAL_DOWN_LEFT -> 1
                else -> 0
            }

            val colStep = when (direction) {
                Direction.HORIZONTAL -> 1
                Direction.DIAGONAL_DOWN_RIGHT -> 1
                Direction.DIAGONAL_DOWN_LEFT -> -1
                else -> 0
            }

            var row = startRow
            var col = startCol

            // Protección contra loops infinitos
            val maxIterations = maxOf(abs(endRow - startRow), abs(endCol - startCol)) + 1
            var iterations = 0

            // Añadir celdas desde start hasta end (inclusive)
            while (iterations < maxIterations) {
                cells.add(Pair(row, col))
                if (row == endRow && col == endCol) break
                row += rowStep
                col += colStep
                iterations++
            }
            return cells
        }
    }

    enum class Direction {
        HORIZONTAL, VERTICAL, DIAGONAL_DOWN_RIGHT, DIAGONAL_DOWN_LEFT
    }
}
