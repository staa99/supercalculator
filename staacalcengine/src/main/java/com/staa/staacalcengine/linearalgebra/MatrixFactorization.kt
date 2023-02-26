package com.staa.staacalcengine.linearalgebra

import com.staa.staacalcengine.util.sum
import kotlin.math.abs

/**
 * Factorizes matrices into a product of a lower and an upper triangular matrix.
 * Also provides an endpoint to solve a system of linear equations.
 */

fun ExpressionMatrix.factorize(): Pair<ExpressionMatrix, Pair<ExpressionMatrix, Array<RowOperation>>>
{
    if (!isSquareMatrix())
        throw UnsupportedOperationException("Cannot use factorize a non-square matrix")

    val echelonResult = reduceToRowEchelon()
    val u = echelonResult.first
    val l = ExpressionMatrix(m, n, variables)
    val operations = echelonResult.second.toTypedArray()

    populateLTMatrix(u, l)

    return Pair(l, Pair(u, operations))
}


/**
 * Populates the lower triangular matrix from the entries of the upper triangular matrix and the result matrix
 */
fun ExpressionMatrix.populateLTMatrix(u: ExpressionMatrix, l: ExpressionMatrix)
{
    if (u.grid.any {abs(it.toList().sum().str.toDoubleOrNull() ?: 0.0) < 0.0})
    {
        return
    }


    val columns = u.transpose().grid
    for (j in 0 until n)
    {
        for (i in 0 until m)
        {
            val lSelection = l.grid[i].take(j)
            val uSelection = columns[j].take(j)

            l[i, j] = (this[i, j] - lSelection.zip(uSelection)
            { lE, uE ->
                lE * uE
            }.sum())
            l[i, j] = l[i, j] / columns[j][j]
        }
    }
}