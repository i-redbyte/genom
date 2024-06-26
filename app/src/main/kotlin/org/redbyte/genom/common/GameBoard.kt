package org.redbyte.genom.common

import org.redbyte.genom.common.data.Cell
import org.redbyte.genom.common.data.GameSettings

typealias CellMatrix = Array<Array<Cell>>

class GameBoard(val settings: GameSettings) {

    val matrix: CellMatrix =
        Array(settings.width) { Array(settings.height) { Cell(false, mutableSetOf(6)) } }
    private val newMatrix =
        Array(settings.width) { Array(settings.height) { Cell(false, mutableSetOf(6)) } }

    init {
        var populated = 0
        val maxCount = matrix.size * matrix[0].size
        val pCount =
            if (settings.initialPopulation > maxCount) maxCount / 10 else settings.initialPopulation
        while (populated < pCount) {
            val x = matrix.indices.random()
            val y = matrix[0].indices.random()
            if (!matrix[x][y].isAlive) {
                matrix[x][y].isAlive = true
                populated++
            }
        }
    }

    fun update() {
        for (i in matrix.indices) {
            for (j in matrix[0].indices) {
                val neighbors = countNeighbors(i, j)
                val cell = matrix[i][j]
                newMatrix[i][j].isAlive =
                    if (matrix[i][j].isAlive) neighbors in 2..3 else neighbors == 3
                if (newMatrix[i][j].isAlive) {
                    newMatrix[i][j].turnsLived = cell.turnsLived
                    newMatrix[i][j].genes = cell.genes
                }
            }
        }
        for (i in matrix.indices) {
            for (j in matrix[0].indices) {
                matrix[i][j] = Cell(newMatrix[i][j].isAlive, newMatrix[i][j].genes.toMutableSet())
            }
        }
    }

    fun countLivingCells() = matrix.flatten().count { it.isAlive }

    private fun countNeighbors(x: Int, y: Int): Int {
        val directions = arrayOf(
            Pair(-1, -1),
            Pair(-1, 0),
            Pair(-1, 1),
            Pair(0, -1),
            Pair(0, 1),
            Pair(1, -1),
            Pair(1, 0),
            Pair(1, 1)
        )
        var neighborCount = 0
        for ((dx, dy) in directions) {
            val neighborX = x + dx
            val neighborY = y + dy
            if (neighborX in 0 until settings.width && neighborY in 0 until settings.height) {
                if (matrix[neighborX][neighborY].isAlive) {
                    neighborCount++
                }
            }

        }
        return neighborCount
    }

}