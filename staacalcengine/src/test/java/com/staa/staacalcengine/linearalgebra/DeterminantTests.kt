package com.staa.staacalcengine.linearalgebra

import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.evaluate
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

class DeterminantTests
{
    // If it works for all 2*2 and 3*3 matrices, by induction, it can be shown to work for larger matrices
    @Test
    fun testDeterminant_isCorrect()
    {
        val times = 10000
        for (count in 0 until times)
        {
            val random = Random()

            val n = random.nextInt(1) + 2
            val matrix = ExpressionMatrix(n, n, emptyArray())
            for (i in 0 until n)
            {
                for (j in 0 until n)
                {
                    matrix[i, j] = Expression.createConst((random.nextDouble() * 100).toString())
                }
            }

            val e: Expression
            e = if (n == 2)
            {
                (matrix[0, 0] * matrix[1, 1]) - (matrix[0, 1] * matrix[1, 0])
            }
            else // if (n == 3)
            {
                (matrix[0, 0] * ((matrix[1, 1] * matrix[2, 2]) - (matrix[1, 2] * matrix[2, 1]))) +
                        (matrix[0, 1] * ((matrix[1, 0] * matrix[2, 2]) - (matrix[1, 2] * matrix[2, 0]))) +
                        (matrix[0, 2] * ((matrix[1, 0] * matrix[2, 1]) - (matrix[1, 1] * matrix[2, 0])))
            }

            val determinant = matrix.getDeterminant().str.toDouble()
            val expected = e.evaluate().str.toDouble()
            assertEquals("Determinant is not correct", expected, determinant, 1e-13)
        }
    }
}