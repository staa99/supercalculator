/*
package com.staa.staacalcengine.linearalgebra

import java.lang.Math.pow
import java.util.*
import kotlin.collections.HashMap

class Matrix(val m: Int, val n: Int)
{
    private val grid: Array<DoubleArray> = Array(m) { i ->
        DoubleArray(n) { j ->
            0.0
        }
    }

    operator fun get(i: Int, j: Int) = grid[i][j]

    operator fun set(i: Int, j: Int, b: Double)
    {
        grid[i][j] = b
    }

    operator fun plus(b: Matrix): Matrix
    {
        if (m == b.m && n == b.n)
        {
            val result = Matrix(m, n)
            for (i in 0 until m)
            {
                for (j in 0 until n)
                {
                    result[i, j] = this[i, j] + b[i, j]
                }
            }

            return result
        }
        else
        {
            throw UnsupportedOperationException("Cannot add two matrices with different shapes")
        }
    }

    operator fun times(b: Matrix): Matrix
    {
        if (n == b.m)
        {
            val result = Matrix(m, b.n)
            val columns = b.transpose()

            for (i in 0 until m)
            {
                for (j in 0 until b.n)
                {
                    result[i, j] = grid[i].zip(columns.grid[j]) { _a, _b ->
                        _a * _b
                    }.sum()
                }
            }

            return result
        }
        else
        {
            throw UnsupportedOperationException(
                    "Cannot multiply a (${m}x$n) matrix with a (${b.m}x${b.n}) matrix")
        }
    }

    fun transpose(): Matrix
    {
        val result = Matrix(n, m)

        for (i in 0 until m)
        {
            for (j in 0 until n)
            {
                result[j, i] = this[i, j]
            }
        }

        return result
    }

    fun scalarMultiple(scalar: Double): Matrix
    {
        val result = Matrix(m, n)
        for (i in 0 until m)
        {
            for (j in 0 until n)
            {
                result[i, j] = scalar * this[i, j]
            }
        }
        return result
    }

    operator fun minus(b: Matrix) = this + (-b)

    operator fun unaryMinus() = scalarMultiple(-1.0)

    infix fun isRowEquivalentTo(b: Matrix) = reduceToRowCanonical() == b.reduceToRowCanonical()


    private fun sortAndSwapRows(rows: Array<DoubleArray>, rowOperationList: ArrayList<RowOperation>)
    {
        // do all necessary swaps
        val oldRows = rows.copyOf()
        rows.sortBy {
            it.withIndex().sumByDouble {
                if (it.value == 0.0) pow(10.0, n - it.index + 0.0) else 0.0
            }
        }

        // determine swaps done
        val oldRowsMap = oldRows.mapIndexed { index, row ->
            Pair(index, row.joinToString())
        }.toMutableList()
        val rowsMap = rows.mapIndexed { index, row ->
            Pair(index, row.joinToString())
        }.toMutableList()

        for (p in oldRowsMap)
        {
            val index = rowsMap.indexOfFirst { it.second == p.second }
            val q = rowsMap[index]

            if (p.first != q.first)
            {
                val op = RowOperation(p.first, q.first, isSwap = true)
                rowOperationList.add(op)
                val rInd = oldRowsMap.indexOfFirst { it.first == q.first }
                val pInd = oldRowsMap.indexOf(p)

                val r = oldRowsMap[rInd]
                val pReplacement = Pair(r.first, p.second)
                val rReplacement = Pair(p.first, r.second)

                oldRowsMap[pInd] = pReplacement
                oldRowsMap[rInd] = rReplacement
            }

            rowsMap.removeAt(index)
        }
    }

    fun reduceToRowEchelon(): Pair<Matrix, ArrayList<RowOperation>>
    {
        val rowOperationList = arrayListOf<RowOperation>()
        val rows = grid.copyOf()
        sortAndSwapRows(rows, rowOperationList)

        */
/*val  sortOpStack = Stack<RowOperation>()

        for (p in rowsMap)
        {
            val index = gridMap.indexOfFirst { it.second == p.second }
            val q = gridMap[index]

            if (p.first != q.first)
            {
                val op = RowOperation(p.first, q.first, isSwap = true)
                sortOpStack.push(op)
            }

            gridMap.removeAt(index)
        }

        while (sortOpStack.any())
        {
            rowOperationList.add(sortOpStack.pop())
        }*//*


        // get pivots
        val pivots = HashMap<Int, Int>()
        var lastIndex = 0



        for (i in 0 until m)
        {
            if (lastIndex >= n)
            {
                break
            }

            while (rows[i][lastIndex] == 0.0)
            {
                lastIndex++
            }

            pivots[i] = lastIndex++
        }

        for (pair in pivots)
        {
            val top = pair.key
            for (row in top + 1 until m)
            {
                if (rows[row][pair.value] != 0.0)
                {
                    // TODO perform more neatly with the use of lcm. For now simply convert the other
                    val r2Scalar = -(rows[top][pair.value] + 0.0) / rows[row][pair.value]

                    val rowOperation = RowOperation(top, row, targetR1 = false)

                    // apply the row operation
                    applyRowOperation(rows, rowOperation)
                    rowOperationList.add(rowOperation)
                }
            }
            sortAndSwapRows(rows, rowOperationList)
        }

        val result = Matrix(m, n)
        for (i in 0 until m)
        {
            for (j in 0 until n)
            {
                result[i, j] = rows[i][j]
            }
        }

        return Pair(result, rowOperationList)
    }


    fun reduceToRowCanonical(): Pair<Matrix, ArrayList<RowOperation>>
    {
        val echelonResult = reduceToRowEchelon()
        val echelon = echelonResult.first
        val rowOperationList = echelonResult.second

        // since it is already in echelon form,
        // retrieving the pivots is simple
        val pivots = echelon.grid.withIndex().map {
            val pair = Pair(it.index, it.value.indexOfFirst { it != 0.0 })
            if (pair.second == -1)
                null
            else
                pair
        }.filterNotNull()

        val rows = echelon.grid.copyOf()
        for (pair in pivots)
        {
            val pivot = echelon.grid[pair.first][pair.second]
            val r1 = pair.first

            val op = RowOperation(r1, r1Scalar = 1.0 / pivot)
            applyRowOperation(rows, op)
            rowOperationList.add(op)
        }

        // all pivots have been set to one
        // time to start subtracting
        for (pair in pivots)
        {
            val top = pair.first
            for (row in top + 1 until m)
            {
                val rowPivot = pivots.firstOrNull() { it.first == row }?.second
                if (rowPivot != null)
                {
                    if (rows[row][rowPivot] != 0.0)
                    {
                        val r2Scalar = -rows[top][rowPivot]

                        val rowOperation = RowOperation(top, row, r2Scalar = r2Scalar,
                                                        targetR1 = true)

                        // apply the row operation
                        applyRowOperation(rows, rowOperation)
                        rowOperationList.add(rowOperation)
                    }
                }
            }
            sortAndSwapRows(rows, rowOperationList)
        }


        val result = Matrix(m, n)
        for (i in 0 until m)
        {
            for (j in 0 until n)
            {
                result[i, j] = rows[i][j]
            }
        }

        return Pair(result, rowOperationList)
    }


    fun isSymmetric() = isSquareMatrix() && this == transpose()

    fun isSquareMatrix() = m == n

    fun invert(): Matrix?
    {
        if (!isSquareMatrix())
        {
            return null
        }

        val canonicalResult = reduceToRowCanonical()
        val identity = getIdentity(m)

        if (canonicalResult.first != identity)
        {
            return null
        }

        // the matrix is invertible
        // let's form the inverse
        val operations = canonicalResult.second
        for (op in operations)
        {
            applyRowOperation(identity.grid, op)
        }

        val copy = grid.copyOf()
        for (op in operations)
        {
            applyRowOperation(copy, op)
        }

        if (!copy.contentDeepEquals(getIdentity(m).grid))
        {
            throw Exception("Logic Error!")
        }
        return identity
    }

    fun isSingular() = invert() != null


    override fun hashCode(): Int
    {
        return grid.contentDeepHashCode()
    }

    override operator fun equals(other: Any?): Boolean
    {
        if (other is Matrix && m == other.m && n == other.n)
        {
            for (i in 0 until m)
            {
                for (j in 0 until n)
                {
                    if (this[i, j] != other[i, j])
                        return false
                }
            }

            return true
        }
        else
        {
            return false
        }
    }

    override fun toString(): String
    {
        return grid.joinToString(separator = "\n",
                                 transform = { it ->
                                     it.joinToString(transform = { it.toString() })
                                 })
    }


    companion object
    {
        fun getIdentity(size: Int): Matrix
        {
            val result = Matrix(size, size)
            for (i in 0 until size)
            {
                for (j in 0 until size)
                {
                    result[i, j] = if (i == j) 1.0 else 0.0
                }
            }

            return result
        }


        fun applyRowOperation(rows: Array<DoubleArray>, rowOperation: RowOperation)
        {
            if (rowOperation.isSwap)
            {
                // swap
                val temp = rows[rowOperation.r1].copyOf()
                rows[rowOperation.r1] = rows[rowOperation.r2].copyOf()
                rows[rowOperation.r2] = temp
            }
            else
            {
                // if this is an addition, perform
                if (rowOperation.r2 > 0)
                {
                    val result = rows[rowOperation.r1].zip(rows[rowOperation.r2]) { r1, r2 ->
                        (r1 * rowOperation.r1Scalar) + (r2 * rowOperation.r2Scalar)
                    }.toDoubleArray()

                    if (rowOperation.targetR1)
                    {
                        rows[rowOperation.r1] = result
                    }
                    else
                    {
                        rows[rowOperation.r2] = result
                    }
                }
                else
                {
                    // just a scalar product
                    rows[rowOperation.r1] = rows[rowOperation.r1].map { rowOperation.r1Scalar * it }.toDoubleArray()
                }
            }
        }
    }
}*/
