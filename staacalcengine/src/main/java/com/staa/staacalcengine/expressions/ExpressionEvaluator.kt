package com.staa.staacalcengine.expressions

import com.staa.staacalcengine.functions.FunctionCache
import com.staa.staacalcengine.functions.functions
import com.staa.staacalcengine.functions.predefinedConstants

fun Expression.evaluate(vararg variables: Pair<String, Double>): Expression
{
    val rKey = variables.map {
        Pair(it.first, it.second.toString())
    }.plus(children.withIndex().map { Pair(it.index.toString(), it.value!!.toString()) }).toMap()

    val cached = FunctionCache[function, rKey]

    if (cached != null) return cached

    val result = if (type == ExpressionType.Const || type == ExpressionType.Variable)
    {
        var string = (str.toDoubleOrNull() ?: variables.toMap()[str]
        ?: predefinedConstants[str])?.toString()

        if (string != null)
        {
            string = Expression.fixExpressionsString(string, fixNegative = false)
            Expression(
                    ExpressionType.Const,
                    string,
                    string,
                    0)
        }
        else
        {
            string = Expression.fixExpressionsString(str)
            Expression(
                    ExpressionType.Variable,
                    string,
                    string,
                    0
                      ).apply { this.variables.add(string) }
        }
    }
    else if (type == ExpressionType.Conditional)
    {
        // get first predicate that is true or null
        // this relies on the expectation that the only null is the last if any
        val i = conditions.indexOfFirst {
            val ev = it?.evaluate(*variables)
            ev == null || ev == one
        }

        if (i < 0)
        {
            throw UnsupportedOperationException("The function is undefined for the values provided")
        }

        return children[i]!!.evaluate(*variables)
    }
    else
    {
        functions[function]!!.func(this, variables.toMap())
    }

    FunctionCache[function, rKey] = result
    return result
}