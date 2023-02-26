package com.staa.staacalcengine.linearalgebra

import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.evaluate
import org.junit.Assert.assertEquals
import org.junit.Test

class MatrixOperationsTests
{
    @Test
    fun testMultiplication()
    {
        val matrix1 = ExpressionMatrix(2, 3, arrayOf(Pair("", 0.0)))
        matrix1[0, 0] = Expression.createConst(2.0.toString())
        matrix1[0, 1] = Expression.createConst(3.0.toString())
        matrix1[0, 2] = Expression.createConst(5.0.toString())
        matrix1[1, 0] = Expression.createConst(7.0.toString())
        matrix1[1, 1] = Expression.createConst(11.0.toString())
        matrix1[1, 2] = Expression.createConst(13.0.toString())

        val matrix2 = ExpressionMatrix(3, 2, arrayOf(Pair("", 0.0)))
        matrix2[0, 0] = Expression.createConst(17.0.toString())
        matrix2[1, 0] = Expression.createConst(19.0.toString())
        matrix2[2, 0] = Expression.createConst(23.0.toString())
        matrix2[0, 1] = Expression.createConst(29.0.toString())
        matrix2[1, 1] = Expression.createConst(31.0.toString())
        matrix2[2, 1] = Expression.createConst(37.0.toString())

        val matrix3 = ExpressionMatrix(2, 2, arrayOf(Pair("", 0.0)))
        matrix3[0, 0] = Expression.createConst(206.0.toString())
        matrix3[0, 1] = Expression.createConst(336.0.toString())
        matrix3[1, 0] = Expression.createConst(627.0.toString())
        matrix3[1, 1] = Expression.createConst(1025.0.toString())


        assertEquals(matrix3, matrix1 * matrix2)
    }


    @Test
    fun testScalarMultiplication()
    {
        val matrix = ExpressionMatrix(2, 1, arrayOf(Pair("", 0.0)))
        matrix[0, 0] = Expression.createConst(2.0.toString())
        matrix[1, 0] = Expression.createConst(3.0.toString())

        val times2 = matrix.scalarMultiple(2.0)

        val expected = ExpressionMatrix(2, 1, arrayOf(Pair("", 0.0)))
        expected[0, 0] = Expression.createConst(4.0.toString())
        expected[1, 0] = Expression.createConst(6.0.toString())

        assertEquals(expected, times2)
    }


    @Test
    fun testMatrixAddition()
    {
        val matrix1 = ExpressionMatrix(2, 1, arrayOf(Pair("", 0.0)))
        matrix1[0, 0] = Expression.createConst(2.0.toString())
        matrix1[1, 0] = Expression.createConst(3.0.toString())

        val matrix2 = ExpressionMatrix(2, 1, arrayOf(Pair("", 0.0)))
        matrix2[0, 0] = Expression.createConst(2.0.toString())
        matrix2[1, 0] = Expression.createConst(3.0.toString())

        val plus_1_2 = matrix1 + matrix2

        val expected = ExpressionMatrix(2, 1, arrayOf(Pair("", 0.0)))
        expected[0, 0] = Expression.createConst(4.0.toString())
        expected[1, 0] = Expression.createConst(6.0.toString())

        assertEquals(expected, plus_1_2)
    }


    @Test
    fun testTranspose()
    {
        val matrix1 = ExpressionMatrix(2, 1, arrayOf(Pair("", 0.0)))
        matrix1[0, 0] = Expression.createConst(2.0.toString())
        matrix1[1, 0] = Expression.createConst(3.0.toString())

        val expected = ExpressionMatrix(1, 2, arrayOf(Pair("", 0.0)))
        expected[0, 0] = Expression.createConst(2.0.toString())
        expected[0, 1] = Expression.createConst(3.0.toString())

        assertEquals(expected, matrix1.transpose())
    }


    @Test
    fun testEchelonRowOperations()
    {
        val matrix1 = ExpressionMatrix(4, 3, arrayOf(Pair("", 0.0)))
        matrix1[0, 0] = Expression.createConst(2.0.toString())
        matrix1[0, 1] = Expression.createConst(1.0.toString())
        matrix1[0, 2] = Expression.createConst(3.0.toString())
        matrix1[1, 0] = Expression.createConst(2.0.toString())
        matrix1[1, 1] = Expression.createConst(0.0.toString())
        matrix1[1, 2] = Expression.createConst(5.0.toString())
        matrix1[2, 0] = Expression.createConst(4.0.toString())
        matrix1[2, 1] = Expression.createConst(2.0.toString())
        matrix1[2, 2] = Expression.createConst(6.0.toString())
        matrix1[3, 0] = Expression.createConst(6.0.toString())
        matrix1[3, 1] = Expression.createConst(2.0.toString())
        matrix1[3, 2] = Expression.createConst(4.0.toString())

        val result = matrix1.reduceToRowCanonical()
        val operations = result.second

        val echelonGrid = Array(4) { i ->
            Array(3) { j ->
                matrix1[i, j]
            }
        }

        for (op in operations)
        {
            ExpressionMatrix.applyRowOperation(echelonGrid, op, arrayOf(Pair("", 0.0)))
        }

        val opEchelon = ExpressionMatrix(4, 3, arrayOf(Pair("", 0.0)))
        for (row in echelonGrid.indices)
        {
            for (column in echelonGrid[row].indices)
            {
                opEchelon[row, column] = echelonGrid[row][column]
            }
        }

        assertEquals(result.first, opEchelon)
    }


    @Test
    fun testRowCanonical()
    {
        val matrix1 = ExpressionMatrix(4, 3, arrayOf(Pair("", 0.0)))
        matrix1[0, 0] = Expression.createConst(2.0.toString()); matrix1[0, 1] = Expression.createConst(1.0.toString()); matrix1[0, 2] = Expression.createConst(3.0.toString())
        matrix1[1, 0] = Expression.createConst(2.0.toString()); matrix1[1, 1] = Expression.createConst(0.0.toString()); matrix1[1, 2] = Expression.createConst(5.0.toString())
        matrix1[2, 0] = Expression.createConst(4.0.toString()); matrix1[2, 1] = Expression.createConst(2.0.toString()); matrix1[2, 2] = Expression.createConst(6.0.toString())
        matrix1[3, 0] = Expression.createConst(6.0.toString()); matrix1[3, 1] = Expression.createConst(2.0.toString()); matrix1[3, 2] = Expression.createConst(4.0.toString())

        val canonical = matrix1.reduceToRowCanonical().first

        matrix1[0, 0] = Expression.createConst(1.0.toString()); matrix1[0, 1] = Expression.createConst(0.0.toString()); matrix1[0, 2] = Expression.createConst(0.0.toString())
        matrix1[1, 0] = Expression.createConst(0.0.toString()); matrix1[1, 1] = Expression.createConst(1.0.toString()); matrix1[1, 2] = Expression.createConst(0.0.toString())
        matrix1[2, 0] = Expression.createConst(0.0.toString()); matrix1[2, 1] = Expression.createConst(0.0.toString()); matrix1[2, 2] = Expression.createConst(1.0.toString())
        matrix1[3, 0] = Expression.createConst(0.0.toString()); matrix1[3, 1] = Expression.createConst(0.0.toString()); matrix1[3, 2] = Expression.createConst(0.0.toString())


        assertEquals(matrix1, canonical)
    }


    @Test
    fun testInverse()
    {
        val matrix1 = ExpressionMatrix(3, 3, arrayOf(Pair("", 0.0)))
        matrix1[0, 0] = Expression.createConst((2.0).toString()); matrix1[0, 1] = Expression.createConst(3.0.toString()); matrix1[0, 2] = Expression.createConst(5.0.toString())
        matrix1[1, 0] = Expression.createConst(7.0.toString()); matrix1[1, 1] = Expression.createConst(2.0.toString()); matrix1[1, 2] = Expression.createConst(9.0.toString())
        matrix1[2, 0] = Expression.createConst(4.0.toString()); matrix1[2, 1] = Expression.createConst(3.0.toString()); matrix1[2, 2] = Expression.createConst(8.0.toString())

        val inverse = matrix1.invert()
        val identity = ExpressionMatrix.getIdentity(3)
        val matrixTimesInverse = matrix1 * inverse.first!!

        for (i in 0 until 3)
        {
            for (j in 0 until 3)
            {
                assertEquals(identity[i, j].evaluate().toString().toDouble(), matrixTimesInverse[i, j].evaluate().toString().toDouble(), 1e-13)
            }
        }
    }
}