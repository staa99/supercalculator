package com.staa.staacalcengine.equations.linear.matrices

import com.staa.staacalcengine.expressions.Expression
import org.junit.Assert
import org.junit.Test

class InverseSolverTests
{
    @Test
    fun testInverseSolver()
    {
        val equations = arrayOf(
                Expression.fromString("(6)x - y = 0"),
                Expression.fromString("y - (2)x = 6")
                               )

        val result = solveSystemOfEquationsInverse(equations)
        print("\nx: ${result.first["x"]}\ny: ${result.first["y"]}")
        Assert.assertEquals(Expression.createConst("1.5"), result.first["x"])
        Assert.assertEquals(Expression.createConst("9"), result.first["y"])
    }
}