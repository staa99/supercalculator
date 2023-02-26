package com.staa.staacalcengine.booleanalgebra

import org.junit.Assert.assertEquals
import org.junit.Test

class InterpretationTests
{
    @Test
    fun testInterpretation()
    {
        val code = "(AB + C + -D).(AB)"
        val expected = "(((A.B)+(C+-D)).(A.B))"
        val codeBack = BooleanAlgebraTerm.fromString(code).toString()
        assertEquals(expected, codeBack)
    }


    @Test
    fun testSimplification()
    {
        val code = "(AB + C + -D).(AB)"
        val expected = "(A.B)"

        val term = BooleanAlgebraTerm.fromString(code)
        val karnaughMap = KarnaughMap.create(term)
        val simple = karnaughMap.simplify().toString()
        assertEquals(expected, simple)
    }


    @Test
    fun testBarSimplification()
    {
        val code = "-(AB + C + -D) + -(AB)"
        val expected = "(-B+-A)"

        val term = BooleanAlgebraTerm.fromString(code)
        val karnaughMap = KarnaughMap.create(term)
        val simple = karnaughMap.simplify().toString()
        assertEquals(expected, simple)
    }
}