package com.staa.staacalcengine.equations.linear.matrices

import com.staa.staacalcengine.expressions.Expression
import org.junit.Assert
import org.junit.Test

class CramersRuleTests
{
    @Test
    fun testCramersRule()
    {
        val equations = arrayOf(
                Expression.fromString("(6)x - y = 0"),
                Expression.fromString("y - (2)x = 6")
                               )

        val result = solveSystemOfEquationsCM(equations)
        println("\nx: ${result["x"]}\ny: ${result["y"]}")
        Assert.assertEquals(Expression.createConst("1.5"), result["x"])
        Assert.assertEquals(Expression.createConst("9"), result["y"])
    }
}