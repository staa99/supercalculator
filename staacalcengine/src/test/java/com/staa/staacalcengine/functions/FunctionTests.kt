package com.staa.staacalcengine.functions

import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.formulae.addFunctionOrConstant
import org.junit.Assert.assertEquals
import org.junit.Test

class FunctionTests
{
    @Test
    fun testAddedFunction()
    {
        // 0 1 1 2 3 5 8 13
        addFunctionOrConstant("fib(x) = {x=0:0;x=1:1;fib(x-1) + fib(x-2)}")
        val exp = Expression.fromString("fib(7)")
        for (i in 0..20)
        {
            val f = "fib($i)"
            println("$f=${Expression.fromString(f).evaluate()}")
        }
        assertEquals(Expression.createConst("13"), exp.evaluate())
    }

    @Test
    fun testDifferential()
    {
        addFunctionOrConstant("f(x) = x^3-(5/x)")
        assertEquals(Expression.createConst("75.2"), Expression.fromString("differentiate(f,5)").evaluate())
    }
}