package com.staa.staacalcengine.expressions

import com.staa.staacalcengine.functions.functions
import com.staa.staacalcengine.util.getLeastPrecedentOperator
import org.junit.Assert.*
import org.junit.Test

class ExpressionTests
{
    @Test
    fun leastPrecedence_isCorrect()
    {
        val string = "10 * 4 + 3 - 2 * 2 + 1 * 5"
        assertEquals("Plus is not the least operator", "+", getLeastPrecedentOperator(string, functions.keys){Expression.fixExpressionsString(it)}.trim())
    }


    @Test
    fun unaryMinusTest()
    {
        val string = "-(d+e)"
        assertEquals("Unary minus failed", ExpressionType.Exp, Expression.fromString(string).type)
    }


    @Test
    fun expressionEvaluation_isCorrect()
    {
        val raw = "10 * 8 * sin(30, deg) + 3 - p * q + 1 * 5"
        val expression = Expression.fromString(raw)
        val evaluated = expression.evaluate(Pair("q", 2.0), Pair("p", 2.0))
        assertEquals(44.0, evaluated.str.toDouble(), 1e-6)
    }

    @Test
    fun variableRepresentation_isCorrect()
    {
        val raw = "10 * 8 * sin(30, deg) + 3 - p * q + 1 * 5"
        val expression = Expression.fromString(raw)
        val evaluated = expression.evaluate(Pair("q", 2.0))
        val string = evaluated.toString()
        val evalFromVar = Expression.fromString(string)
        assertEquals(1, evalFromVar.variables.size)
    }


    @Test
    fun expressionType_isCorrect()
    {
        assertEquals(ExpressionType.Exp, Expression.fromString("ulti + super").type)
        assertEquals(ExpressionType.Const, Expression.fromString("45").type)
        assertEquals(ExpressionType.Variable, Expression.fromString("super").type)
        assertEquals(ExpressionType.InEql, Expression.fromString("ulti >= super").type)
        assertEquals(ExpressionType.Eqn, Expression.fromString("ulti + super = 1").type)
    }


    @Test
    fun multipleCompare_throwsException()
    {
        val throwsException: Boolean = try
        {
            Expression.fromString("f > p > i")
            false
        }
        catch (ex: Exception)
        {
            true
        }

        assert(throwsException)
    }

    @Test
    fun conditionalFunction_isCorrect()
    {
        val expression = Expression.fromString("{a = 2:error(A_cannot_be_two);a=1:2}")
        val result = expression.evaluate(Pair("a", 1.0))
        assertEquals(Expression.createConst("2"), result)
    }

    @Test
    fun errorWorks()
    {
        val expression = Expression.fromString("error(It_works)")
        val res =
        try
        {
            expression.evaluate()
            ""
        }
        catch (e: UnsupportedOperationException)
        {
            e.message
        }

        assertEquals("It works", res)
    }
}