package com.staa.staacalcengine.equations.linear.matrices

import com.staa.staacalcengine.equations.util.createAugmentedMatrix
import com.staa.staacalcengine.equations.util.normalizeEquation
import com.staa.staacalcengine.equations.util.verifyLinear
import com.staa.staacalcengine.expressions.Expression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GaussianEliminationTests
{
    @Test
    fun testVerifyLinear()
    {
        val expression = Expression.fromString("2*x + 3*y + 4^2*z + 5^2")
        val throwsException = try
        {
            verifyLinear(expression)
            false
        }
        catch (ex: UnsupportedOperationException)
        {
            true
        }

        assert(!throwsException)
    }


    @Test
    fun testNormalizeEquation()
    {
        val equation = Expression.fromString("2*x + 3*y + 4^2*z + 5^2 = 2*z - 3*y")
        val normalized = normalizeEquation(equation)
        assertEquals(-25.0, normalized.children[1]!!.str.toDouble(), 1e-13)
    }


    @Test
    fun testAugmentedMatrix()
    {
        val equations = arrayOf(
                normalizeEquation(Expression.fromString("2*a - (4/3)*b + 3*c = 4*c + 5*a - 14")),
                normalizeEquation(Expression.fromString("a + 2*b = 2*c"))
                               )

        val sortedVars = arrayOf("a", "b", "c")

        val matrix = createAugmentedMatrix(sortedVars, equations).reduceToRowCanonical()

        assertEquals(2, matrix.first.m)
        assertEquals(4, matrix.first.n)
    }


    @Test
    fun testSolveEquation()
    {
        val equations = arrayOf(
                Expression.fromString("a + b + c + d = (1 + 1)a + b + 1"),
                Expression.fromString("a + b + c + d = (2)b + c + 1"),
                Expression.fromString("a + b + c + d = (2)c + d + 1"),
                Expression.fromString("a + b + c + d = (2)a + c + 1")
                               )

        val results = solveSystemOfEquationsGaussian(equations).first
        println("a:${results["a"]}, b:${results["b"]}, c:${results["c"]}, d:${results["d"]}")
        assertEquals("a", results["a"]!!.str.toDouble(), 1.0, 1e-13)
        assertEquals("b", results["b"]!!.str.toDouble(), 1.0, 1e-13)
        assertEquals("c", results["c"]!!.str.toDouble(), 1.0, 1e-13)
        assertEquals("d", results["d"]!!.str.toDouble(), 1.0, 1e-13)

        val equations1 = arrayOf(
                Expression.fromString("x + y + z = 1"),
                Expression.fromString("a + b + c = 1"),
                Expression.fromString("f + g + h = 1"),
                Expression.fromString("x + a + f = 1"),
                Expression.fromString("y + b + g = 1"),
                Expression.fromString("z + c + h = 1")
                               )

        val results1 = solveSystemOfEquationsGaussian(equations1).first
        println(results1.toList().joinToString { "${it.first}: ${it.second}" })
    }


    @Test
    fun testNoSolutions()
    {
        val equations = arrayOf(
                Expression.fromString("a + b = 1"),
                Expression.fromString("a + b = 2")
                               )

        val results = solveSystemOfEquationsGaussian(equations).first
        assertTrue(results.isEmpty())
    }
}