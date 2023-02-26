package com.staa.staacalcengine.numericalanalysis.polynomials.roots

import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.formulae.addFunctionOrConstant
import org.junit.Assert
import org.junit.Test

class FixedPointFormulaTests
{
    @Test
    fun testApproximate()
    {
        val fname = "realF"
        val x0 = 0.0
        val maxError = 1e-5

        addFunctionOrConstant("$fname(x) = (x^2+1)/2")
        val res = fixedPointApproximation(x0, fname, maxError)
        println()
        println(res.evaluate())
        Assert.assertTrue(res.function != "")
    }
}