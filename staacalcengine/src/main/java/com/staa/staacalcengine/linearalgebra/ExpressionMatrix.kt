package com.staa.staacalcengine.linearalgebra

import com.staa.staacalcengine.expressions.*
import com.staa.staacalcengine.util.sum
import java.lang.Math.pow

class ExpressionMatrix(val m: Int, val n: Int, val variables: Array<Pair<String, Double>>)
{
    val grid: Array<Array<Expression>> = Array(m) { _ ->
        Array(n) { _ ->
            zero
        }
    }

    operator fun get(i: Int, j: Int) = grid[i][j]

    operator fun set(i: Int, j: Int, b: Expression)
    {
        grid[i][j] = b.evaluate(*variables)
    }


    operator fun plus(b: ExpressionMatrix): ExpressionMatrix
    {
        if (m == b.m && n == b.n)
        {
            val result = ExpressionMatrix(m, n, variables)
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


    operator fun times(b: ExpressionMatrix): ExpressionMatrix
    {
        if (n == b.m)
        {
            val result = ExpressionMatrix(m, b.n, variables)
            val columns = b.transpose()

            for (i in 0 until m)
            {
                for (j in 0 until b.n)
                {
                    result[i, j] = Expression.fromString(grid[i].zip(columns.grid[j]) { _a, _b ->
                        _a * _b
                    }.joinToString(separator = "+") { expression -> "($expression)" }).evaluate(
                            *variables).evaluate(*variables)
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


    fun transpose(): ExpressionMatrix
    {
        val result = ExpressionMatrix(n, m, variables)

        for (i in 0 until m)
        {
            for (j in 0 until n)
            {
                result[j, i] = this[i, j].evaluate(*variables)
            }
        }

        return result
    }


    fun scalarMultiple(scalar: Double): ExpressionMatrix
    {
        val result = ExpressionMatrix(m, n, variables)
        val s = Expression.createConst(scalar.toString())
        for (i in 0 until m)
        {
            for (j in 0 until n)
            {
                result[i, j] = (s * this[i, j]).evaluate(*variables)
            }
        }
        return result
    }

    operator fun minus(b: ExpressionMatrix) = this + (-b)

    operator fun unaryMinus() = scalarMultiple(-1.0)


    infix fun isRowEquivalentTo(b: ExpressionMatrix) = reduceToRowCanonical() == b.reduceToRowCanonical()


    private fun sortAndSwapRows(rows: Array<Array<Expression>>, rowOperationList: ArrayList<RowOperation>)
    {
        // do all necessary swaps
        val oldRows = rows.copyOf()
        rows.sortBy {
            it.withIndex().sumByDouble {
                if (it.value.isZero()) pow(10.0, n - it.index + 0.0) else 0.0
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

    fun reduceToRowEchelon(): Pair<ExpressionMatrix, ArrayList<RowOperation>>
    {
        val rowOperationList = arrayListOf<RowOperation>()
        val rows = grid.copyOf().map { it.map { it }.toTypedArray() }.toTypedArray()
        sortAndSwapRows(rows, rowOperationList)

        // get pivots
        var lastIndex = -1

        for (top in 0 until m)
        {
            if (top + 1 >= m)
                break

            lastIndex = rows[top].withIndex().indexOfFirst { it.index > lastIndex && !it.value.isZero() }
            for (row in top + 1 until m)
            {
                if (lastIndex >= 0 && !rows[row][lastIndex].isZero())
                {
                    // TODO perform more neatly with the use of lcm. For now simply convert the other
                    val r1Scalar = -(rows[row][lastIndex] / (rows[top][lastIndex]).evaluate())

                    val rowOperation = RowOperation(top, row, r1Scalar = r1Scalar, targetR1 = false)

                    // apply the row operation
                    applyRowOperation(rows, rowOperation, variables)
                    rowOperationList.add(rowOperation)
                }
                else
                {
                    break
                }
            }
            sortAndSwapRows(rows, rowOperationList)
        }

        val result = ExpressionMatrix(m, n, variables)
        for (i in 0 until m)
        {
            for (j in 0 until n)
            {
                result[i, j] = rows[i][j]
            }
        }

        return Pair(result, rowOperationList)
    }


    fun hasZeroRows() = grid.any { it.toList().sum().isZero() }


    fun reduceToRowCanonical(): Pair<ExpressionMatrix, ArrayList<RowOperation>>
    {
        val echelonResult = reduceToRowEchelon()
        val echelon = echelonResult.first
        val rowOperationList = echelonResult.second

        // since it is already in echelon form,
        // retrieving the pivots is simple
        val pivots = echelon.grid.withIndex().mapNotNull {
            val pair = Pair(it.index, it.value.indexOfFirst {
                !it.evaluate(*variables).isZero()
            })
            if (pair.second == -1)
                null
            else
                pair
        }

        val rows = echelon.grid.copyOf()


        for (pair in pivots)
        {
            val pivot = echelon.grid[pair.first][pair.second]
            val r1 = pair.first

            val op = RowOperation(r1, r1Scalar = (one / pivot).evaluate(
                    *variables), targetR1 = true)
            applyRowOperation(rows, op, variables)
            rowOperationList.add(op)
        }

        // all pivots have been set to one
        // time to start subtracting
        for (pair in pivots)
        {
            val top = pair.first
            for (row in top + 1 until m)
            {
                val rowPivot = pivots.firstOrNull { it.first == row }?.second
                if (rowPivot != null)
                {
                    if (rows[row][rowPivot] != zero)
                    {
                        val r2Scalar = -rows[top][rowPivot]

                        val rowOperation = RowOperation(top, row, r2Scalar = r2Scalar,
                                                        targetR1 = true)

                        // apply the row operation
                        applyRowOperation(rows, rowOperation, variables)
                        rowOperationList.add(rowOperation)
                    }
                }
            }
            sortAndSwapRows(rows, rowOperationList)
        }


        val result = ExpressionMatrix(m, n, variables)
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

    fun invert(): Pair<ExpressionMatrix?, Array<RowOperation>?>
    {
        if (!isSquareMatrix())
        {
            return Pair(null, null)
        }

        val canonicalResult = reduceToRowCanonical()
        val inverse = getIdentity(m)

        if (canonicalResult.first != inverse)
        {
            return Pair(null, canonicalResult.second.toTypedArray())
        }

        // the matrix is invertible
        // let's form the inverse
        val operations = canonicalResult.second
        for (op in operations)
        {
            applyRowOperation(inverse.grid, op, variables)
        }

        return Pair(inverse, operations.toTypedArray())
    }

    fun isSingular() = invert().first != null

    override fun hashCode(): Int
    {
        return grid.contentDeepHashCode()
    }

    override operator fun equals(other: Any?): Boolean
    {
        if (other is ExpressionMatrix && m == other.m && n == other.n)
        {
            for (i in 0 until m)
            {
                for (j in 0 until n)
                {
                    if (this[i, j].evaluate(*variables) != other[i, j].evaluate(*variables))
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
        fun getIdentity(size: Int): ExpressionMatrix
        {
            val result = ExpressionMatrix(size, size, emptyArray())
            for (i in 0 until size)
            {
                for (j in 0 until size)
                {
                    result[i, j] = if (i == j) one
                    else zero
                }
            }

            return result
        }


        fun applyRowOperation(rows: Array<Array<Expression>>, rowOperation: RowOperation, variables: Array<Pair<String, Double>>)
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
                        ((r1 * rowOperation.r1Scalar) + (r2 * rowOperation.r2Scalar)).evaluate(
                                *variables)
                    }.toTypedArray()

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
                    rows[rowOperation.r1] = rows[rowOperation.r1].map {
                        (rowOperation.r1Scalar * it).evaluate(*variables)
                    }.toTypedArray()
                }
            }
        }
    }
}

