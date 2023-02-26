package com.staa.staacalcengine.equations.util

import com.staa.staacalcengine.expressions.*

fun verifyLinear(exp: Expression)
{
    val expression = exp.evaluate()
    if (expression.type == ExpressionType.Const && expression.type == ExpressionType.Variable)
    {
        return
    }

    if (expression.type == ExpressionType.Eqn || expression.type == ExpressionType.InEql)
    {
        throw UnsupportedOperationException("The expression cannot be an equation or inequality")
    }

    // if a product
    if (productFuncs.contains(expression.function))
    {
        if (!expression.children.filter {
                    powerFunctions.contains(it!!.function)
                }.all { it!!.children[0]!!.type == ExpressionType.Const || (it.children[0]!!.type == ExpressionType.Variable && it.children[1]!!.str.toDoubleOrNull() == 1.0) })
        {
            throw UnsupportedOperationException(
                    "A linear expression must not have variables raised to powers other than one")
        }

        if (expression.children.groupBy { it!!.str }.count { it.value[0]!!.type == ExpressionType.Variable } > 1)
        {
            throw UnsupportedOperationException(
                    "A linear expression cannot have a product of more than one variable")
        }
    }

    if (summationFuncs.contains(expression.function))
    {
        for (c in expression.children)
        {
            verifyLinear(c!!)
        }
    }
}