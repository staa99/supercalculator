package com.staa.staacalcengine.linearalgebra

import com.staa.staacalcengine.expressions.Expression
import org.junit.Assert
import org.junit.Test
import java.util.*

class MatrixFactorizationTests
{
    @Test
    fun testFactorization()
    {
        val matrix = ExpressionMatrix(3, 3, emptyArray())
        for (i in 0 until 3)
        {
            for (j in 0 until 3)
            {
                matrix[i, j] = Expression.createConst((Random().nextDouble() * 100).toString())
            }
        }

        val (l,u) = matrix.factorize()
        Assert.assertTrue("Factors not resulting in matrix", (l * u.first) == matrix)
    }
}