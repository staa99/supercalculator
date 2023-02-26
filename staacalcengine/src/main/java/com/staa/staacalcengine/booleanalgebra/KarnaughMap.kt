package com.staa.staacalcengine.booleanalgebra

import com.staa.staacalcengine.util.flatMap
import com.staa.staacalcengine.util.skip
import com.staa.staacalcengine.util.skipWhile
import java.lang.Math.pow
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log

typealias Minterms = ArrayList<HashMap<String, Boolean>>

class KarnaughMap(val rowVariables: Array<String>, val colVariables: Array<String>)
{
    private lateinit var columnHeaders: Array<String>
    private lateinit var rowHeaders: Array<String>
    private lateinit var grid: Array<Array<Boolean>>
    private lateinit var mintermEntries: Array<Point>

    lateinit var truthTable: Array<HashMap<String, Boolean>>
    lateinit var mapMinterms: Array<String>


    fun simplify(): BooleanAlgebraTerm
    {
        // The minterms are arranged in the correct row_col order
        // detect a rectangle
        val rectangles = findLargestRectangles()

        var term = ""


        if (!grid.any { it.any { it } })
        {
            return BooleanAlgebraTerm.fromString("0")
        }
        else if (grid.all { it.all { it } })
        {
            return BooleanAlgebraTerm.fromString("1")
        }



        for (rectangle in rectangles)
        {
            val result = buildFromRectangle(rectangle)

            if (result != "")
            {
                if (term != "")
                {
                    term = "($term)"
                    term += "+"
                }

                term += result.replace("-0", "").replace("-1", "")
            }
        }

        return BooleanAlgebraTerm.fromString(term)
    }

    private fun buildFromRectangle(rectangle: Array<Point>): String
    {
        // get the row header for a given row
        val rowHeaders = rectangle.map {
            Pair(rowHeaders[it.row].map { it.toString().toInt() }.toIntArray(), it.row)
        }.toTypedArray()

        val columnHeaders = rectangle.map {
            Pair(columnHeaders[it.col].map { it.toString().toInt() }.toIntArray(), it.col)
        }.toTypedArray()

        // get the variables for the row header indices that don't change throughout the rows
        val rValues = rowHeaders[0].first
        val cValues = columnHeaders[0].first

        for ((headerValues, _) in rowHeaders)
        {
            for (i in headerValues.indices)
            {
                if (rValues[i] != headerValues[i])
                {
                    rValues[i] = -1
                }
            }
        }

        for ((headerValues, _) in columnHeaders)
        {
            for (i in headerValues.indices)
            {
                if (cValues[i] != headerValues[i])
                {
                    cValues[i] = -1
                }
            }
        }

        val rVars = rValues.withIndex().map {
            when
            {
                it.value == -1 -> ""
                it.value == 1  -> rowVariables[it.index]
                else           -> "-${rowVariables[it.index]}"
            }
        }

        val cVars = cValues.withIndex().map {
            when
            {
                it.value == -1 -> ""
                it.value == 1  -> colVariables[it.index]
                else           -> "-${colVariables[it.index]}"
            }
        }

        return cVars.union(rVars).joinToString(separator = "")
    }


    private fun findLargestRectangles(): Array<Array<Point>>
    {
        // get highest row and lowest row and highest col and lowest col
        var maxRow = Int.MIN_VALUE
        var minRow = Int.MAX_VALUE
        var maxCol = Int.MIN_VALUE
        var minCol = Int.MAX_VALUE

        for ((r, c) in mintermEntries)
        {
            if (maxRow < r)
            {
                maxRow = r
            }

            if (maxCol < c)
            {
                maxCol = c
            }

            if (minRow > r)
            {
                minRow = r
            }

            if (minCol > c)
            {
                minCol = c
            }
        }


        val rowRectangles = arrayListOf<ArrayList<Point>>()
        val colRectangles = arrayListOf<ArrayList<Point>>()

        for (index in minRow..maxRow)
        {
            rowRectangles.addAll(getLinearRectangles(index, true))
        }

        for (index in minCol..maxCol)
        {
            colRectangles.addAll(getLinearRectangles(index, false))
        }


        val result =
                getLargestFromLinearRectangles(rowRectangles, { p -> p.col }, { p -> p.row })
                        .union(getLargestFromLinearRectangles(colRectangles, { p -> p.row },
                                                              { p -> p.col }).toList()).distinctBy(
                                ::pointArrayDistinctSelector).toTypedArray()


        val rectangles = arrayListOf<Array<Point>?>()

        for (rect in result)
        {
            // extract the rectangles with power of 2
            // rPrime is two raised to the largest power of 2 less than or equal to the number of rows
            val rPrime = pow(2.0, log(rect.map { it.row }.distinct().size.toDouble(),
                                      2.0).toInt().toDouble()).toInt()

            // cPrime is two raised to the largest power of 2 less than or equal to the number of rows
            val cPrime = pow(2.0, log(rect.map { it.col }.distinct().size.toDouble(),
                                      2.0).toInt().toDouble()).toInt()


            // extract the rectangles of rPrime by cPrime
            var grid = rect.groupBy { it.row }.map { g -> g.value.sortedBy { it.col }.toTypedArray() }.sortedBy { it[0].row }.toTypedArray()

            var rmax = Int.MIN_VALUE
            var rmin = Int.MAX_VALUE
            var cmax = Int.MIN_VALUE
            var cmin = Int.MAX_VALUE
            var rBreak = -1
            var cBreak = -1

            for (ind in grid.indices)
            {
                val rrr = grid[ind]
                for (cind in rrr.indices)
                {
                    val (r, c) = rrr[cind]
                    if (cind > 0)
                    {
                        if (rBreak == -1 && abs(r - rrr[cind - 1].row) > 1)
                        {
                            rBreak = ind
                        }

                        if (cBreak == -1 && abs(c - rrr[cind - 1].col) > 1)
                        {
                            cBreak = cind
                        }
                    }

                    if (rmax < r)
                    {
                        rmax = r
                    }

                    if (cmax < c)
                    {
                        cmax = c
                    }

                    if (rmin > r)
                    {
                        rmin = r
                    }

                    if (cmin > c)
                    {
                        cmin = c
                    }
                }
            }

            if (rmin == 0 && rmax == grid.size - 1 && rmax != rmin && rBreak > -1)
            {
                //order each row according to overflow rules
                val prerows = grid.takeWhile { it[0].row <= rBreak }
                grid = grid.skipWhile { it[0].row <= rBreak }.union(prerows).toTypedArray()
            }

            if (cmin == 0 && cmax == grid[0].last().col && cmax != cmin && cBreak > -1)
            {
                for (ind in grid.indices)
                {
                    val line = grid[ind]
                    val precols = line.takeWhile { it.col <= cBreak }
                    grid[ind] = line.skipWhile { it.col <= cBreak }.union(precols).toTypedArray()
                }

            }

            val top = grid.take(rPrime).flatMap().toTypedArray()
            val bottom = grid.skip(grid.size - rPrime).flatMap().toTypedArray()
            val left = grid.flatMap { it.take(cPrime) }.toTypedArray()
            val right = grid.flatMap { it.skip(it.size - cPrime) }.toTypedArray()

            rectangles.add(top.intersect(right.toList()).toTypedArray())
            rectangles.add(top.intersect(left.toList()).toTypedArray())
            rectangles.add(bottom.intersect(right.toList()).toTypedArray())
            rectangles.add(bottom.intersect(left.toList()).toTypedArray())
        }

        // remove all whose points are all contained in others
        // and count less than the others
        for (i in rectangles.indices)
        {
            val rect = rectangles[i]
            val rest = rectangles.filter { it != null && it.size >= rect!!.size }.minus(
                    arrayListOf(rect))


            if (rect!!.all { r -> rest.any { rr -> rr!!.contains(r) } })
            {
                rectangles[i] = null
            }
        }

        rectangles.removeAll { it == null }

        return rectangles.distinct().map { it!! }.toTypedArray()
    }


    private fun getLargestFromLinearRectangles(
            rectangles: ArrayList<ArrayList<Point>>,
            selectOther: (Point) -> Int,
            selectSame: (Point) -> Int): Array<Array<Point>>
    {
        // create a new list for the results
        val result = ArrayList<Array<Point>>()

        for (rectangle in rectangles)
        {
            // find largest rectangle that takes it completely

            // take other similar rectangles
            val similar = rectangles
                    .filter { rr ->
                        // rectangle.Count == rr.Count &&
                        rr.map(selectOther)
                                .intersect(
                                        rectangle.map(selectOther)).size == rectangle.size
                    }
                    .map { rr ->
                        rr.filter { p ->
                            rectangle.map(selectOther).contains(selectOther(p))
                        }.toTypedArray()
                    }


            val similarIndices = similar.map { selectSame(it[0]) }.toIntArray()
            val minIndex = similarIndices.min()
            val maxIndex = similarIndices.max()

            var startIndex = -1
            var endIndex = -1

            val index = selectSame(rectangle[0])

            var gotten = false
            for (current in minIndex!!..maxIndex!!)
            {
                if (similarIndices.indexOf(current) == -1)
                {
                    if (gotten)
                    {
                        break
                    }
                    startIndex = -1
                    endIndex = -1
                }
                else if (startIndex == -1)
                {
                    startIndex = current
                    endIndex = current

                    if (current == index)
                    {
                        gotten = true
                    }
                }
                else
                {
                    endIndex = current

                    if (current == index)
                    {
                        gotten = true
                    }
                }
            }

            val largestRectangle = similar
                    .filter { rr -> selectSame(rr[0]) in startIndex..endIndex }
                    .flatMap { rrr -> rrr.toList() }.toTypedArray()

            result.add(largestRectangle)
        }

        return result.distinct().toTypedArray()
    }


    private fun getLinearRectangles(index: Int, isRow: Boolean): ArrayList<ArrayList<Point>>
    {
        val line =

                if (isRow)
                {
                    grid[index]
                }
                else
                {
                    grid.map { it[index] }.toTypedArray()
                }


        val result = arrayListOf<ArrayList<Point>>()

        if (line[0])
        {
            result.add(arrayListOf())
        }


        for (i in line.indices)
        {
            val el = line[i]

            if (!el)
            {
                result.add(arrayListOf())
            }
            else
            {
                if (isRow)
                {
                    result.last().add(Point(index, i))
                }
                else
                {
                    result.last().add(Point(i, index))
                }
            }
        }

        result.removeAll { !it.any() }


        // The contiguous rectangles have to be merged
        if (
                result.any() &&
                (
                        (!isRow && line.size - 1 == result.last().last().row && result[0][0].row == 0) ||
                                (isRow && line.size - 1 == result.last().last().col && result[0][0].col == 0)
                        ) && result.size > 1)
        {
            result.last().addAll(result[0])
            result.removeAt(0)
        }

        return result
    }


    companion object
    {
        fun create(term: BooleanAlgebraTerm): KarnaughMap
        {
            val map = initialize(term)
            calculate(map, term)
            return map
        }


        private fun calculate(map: KarnaughMap, term: BooleanAlgebraTerm)
        {
            fun extractMinterms(): Minterms
            {
                // obtain the truth tables of the functions
                val vars = term.variables


                val count = pow(2.0, vars.size.toDouble()).toInt()
                val truthValues = generateSequence(count)


                val truthTable = Array<HashMap<String, Boolean>>(truthValues.size) { HashMap() }
                val minTerms = Minterms()

                for (i in 0 until count)
                {
                    for (j in 0 until vars.size)
                    {
                        truthTable[i][vars[j]] = truthValues[i][j] == '1'
                    }

                    val result = term.evaluate(truthTable[i])
                    truthTable[i].remove("1")
                    truthTable[i].remove("0")

                    if (result)
                    {
                        minTerms.add(truthTable[i])
                    }
                }

                map.truthTable = truthTable
                return minTerms
            }

            val minterms = extractMinterms()
            map.mapMinterms = minterms.map { p ->
                p.toList().joinToString("")
                { pair ->
                    when
                    {
                        pair.first == "0" || pair.first == "1" -> ""
                        pair.second                            -> pair.first
                        else                                   -> "-${pair.first}"
                    }
                }
            }.toTypedArray()
            val mintermEntries = ArrayList<Point>()

            // Place minterms on grid
            for (minterm in minterms)
            {
                val colHeader =
                        minterm.keys
                                .intersect(map.colVariables.toList())
                                .sorted().joinToString(
                                        separator = "") { k -> if (minterm[k]!!) "1" else "0" }

                val rowHeader =
                        minterm.keys
                                .intersect(map.rowVariables.toList())
                                .sorted().joinToString(
                                        separator = "") { k -> if (minterm[k]!!) "1" else "0" }

                val col = map.columnHeaders.indexOf(colHeader)
                val row = map.rowHeaders.indexOf(rowHeader)

                mintermEntries.add(Point(row, col))
                map.grid[row][col] = true
            }

            map.mintermEntries = mintermEntries.toTypedArray()
        }


        private fun initialize(term: BooleanAlgebraTerm): KarnaughMap
        {
            val vars = term.variables

            val count = ceil(vars.size / 2.0).toInt()
            val map = KarnaughMap(
                    colVariables = term.variables.take(count).toTypedArray(),
                    rowVariables = term.variables.takeLast(
                            term.variables.size - count).toTypedArray()
                                 )

            val rowCount = pow(2.0, map.rowVariables.size.toDouble()).toInt()
            val colCount = pow(2.0, map.colVariables.size.toDouble()).toInt()


            map.grid = Array(rowCount) {
                Array(colCount) { false }
            }

            map.columnHeaders = generateSequence(colCount)
            map.rowHeaders = generateSequence(rowCount)

            return map
        }


        private fun generateSequence(_count: Int): Array<String>
        {
            var count = _count
            fun next(current: String, generated: Array<String?>): String
            {
                // try from right to left
                var last = current.lastIndex

                while (last >= 0)
                {
                    val c = if (current[last] == '1') '0' else '1'
                    val temp = current.replaceRange(last..last, c.toString())

                    if (!generated.contains(temp))
                    {
                        return temp
                    }
                    last--
                }

                return current
            }

            val sequence = arrayOfNulls<String>(count--)

            sequence[count] = "".padEnd(log(count + 1.0, 2.0).toInt(), '0')
            count--

            for (c in (0..count).reversed())
            {
                sequence[c] = next(sequence[c + 1]!!, sequence)
            }

            return sequence.filterNotNull().toTypedArray()
        }
    }
}