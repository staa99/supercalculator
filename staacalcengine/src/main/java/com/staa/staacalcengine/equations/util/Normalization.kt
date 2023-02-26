package com.staa.staacalcengine.equations.util

import com.staa.staacalcengine.expressions.*



fun prepareArgs(eqns: Array<Expression>): Pair<Array<Expression>, Array<String>>
{
    val equations = eqns.map { normalizeEquation(it) }.toTypedArray()
    val vars = getVariables(equations)

    return Pair(equations, vars)
}

fun getVariables(eqns: Array<Expression>): Array<String>
{
    val vars = eqns.flatMap { it.variables }.distinct().toTypedArray()
    vars.sort()
    val varCount = vars.size
    if (varCount == 0)
    {
        throw UnsupportedOperationException(
                "The equation solver requires at least one variable to solve")
    }
    return vars
}


fun normalizeEquation(equation: Expression): Expression
{
    if (equation.function != "=")
    {
        throw UnsupportedOperationException("The equation normalizer requires an equation")
    }

    val lhs = equation.children[0]!!
    val rhs = equation.children[1]!!

    verifyLinear(lhs)
    verifyLinear(rhs)

    val lhsMinRhs = (lhs - rhs).evaluate()
    // based on the expected implementation, lhsMinRhs has to be a summation
    val constTerm = lhsMinRhs.children.singleOrNull { it!!.type == ExpressionType.Const }

    val newLhs: Expression
    val newRhs: Expression

    if (constTerm != null)
    {
        // this equation is not part of a homogenous system
        newLhs = (lhsMinRhs - constTerm).evaluate()
        newRhs = (zero - constTerm).evaluate()
    }
    else
    {
        newLhs = lhsMinRhs
        newRhs = zero
    }

    val result = Expression(ExpressionType.Eqn,
                            "$newLhs=$newRhs",
                            "=",
                            2)
    result.children[0] = newLhs
    result.children[1] = newRhs

    result.variables.addAll(newLhs.variables)
    result.variables.addAll(newRhs.variables)

    return result
}