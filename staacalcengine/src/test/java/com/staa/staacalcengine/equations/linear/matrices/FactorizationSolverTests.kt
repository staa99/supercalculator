package com.staa.staacalcengine.equations.linear.matrices

import com.staa.staacalcengine.expressions.Expression
import org.junit.Assert.*
import org.junit.Test

class FactorizationSolverTests
{
    @Test
    fun testFactorizationSolver()
    {
        val equations = arrayOf(
                Expression.fromString("(6)x - y = 0"),
                Expression.fromString("y - (2)x = 6")
                               )

        val result = solveSystemOfEquationsFS(equations)
        print("\nx: ${result.values["x"]}\ny: ${result.values["y"]}")
        assertEquals(Expression.createConst("1.5"), result.values["x"])
        assertEquals(Expression.createConst("9"), result.values["y"])
    }
}