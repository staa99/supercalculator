package com.staa.staacalcengine.equations.util

import com.staa.staacalcengine.expressions.*
import com.staa.staacalcengine.linearalgebra.ExpressionMatrix


fun verifyNormalizedLinear(equations: Array<Expression>)
{
    if (equations.any { it.children.any { it!!.children.any { it!!.variables.size > 1 } } })
    {
        throw UnsupportedOperationException(
                "The equations are either non-linear or not well formed")
    }
}


fun getColumnCount(equations: Array<Expression>): Int
{
    var columnCount = 0

    for (e in equations)
    {
        val eqn = e.children[0]!!

        val size = if (eqn.type == ExpressionType.Variable) 1
        else eqn.children
                .filter { it!!.variables.any() }
                .distinctBy { it!!.variables.first() }.size

        if (size > columnCount)
        {
            columnCount = size
        }
    }

    return columnCount
}


fun createAugmentedMatrix(sortedVars: Array<String>, equations: Array<Expression>): ExpressionMatrix
{
    verifyNormalizedLinear(equations)
    val columnCount = sortedVars.size//getColumnCount(equations)
    val matrix = ExpressionMatrix(equations.size, columnCount + 1, emptyArray())

    for (i in equations.indices)
    {
        val eqn = equations[i].children[0]!!
        for (j in 0 until columnCount)
        {
            if (eqn.type == ExpressionType.Variable || powerFunctions.contains(eqn.function))
            {
                if (sortedVars.indexOf(eqn.str) == j)
                {
                    matrix[i, j] = one
                }
                else
                {
                    matrix[i, j] = zero
                }
            }
            else if (summationFuncs.contains(eqn.function))
            {
                val children = eqn.children.filter {
                    it!!.variables.size == 1 && sortedVars.indexOf(it.variables.first()) == j
                }
                matrix[i, j] = if (children.any())
                    children.reduce { acc, expression ->
                        (acc ?: zero) + (expression ?: zero)
                    }?.evaluate(Pair(sortedVars[j], 1.0)) ?: zero
                else zero
            }
            else if (productFuncs.contains(eqn.function))
            {
                val children = eqn.children.filter {
                    it!!.variables.size == 1 && sortedVars.indexOf(it.variables.first()) == j
                }
                matrix[i, j] = if (children.any())
                    children.reduce { acc, expression ->
                        (acc ?: zero) * (expression ?: zero)
                    }?.evaluate(Pair(sortedVars[j], 1.0)) ?: zero
                else zero
            }
        }

        // add the constant term
        matrix[i, columnCount] = equations[i].children[1]!!
    }

    return matrix
}


fun createCoefficientMatrix(sortedVars: Array<String>, equations: Array<Expression>): Pair<ExpressionMatrix, ExpressionMatrix>
{
    verifyNormalizedLinear(equations)
    val columnCount = getColumnCount(equations)
    val matrix = ExpressionMatrix(equations.size, columnCount, emptyArray())
    val resultMatrix = ExpressionMatrix(equations.size, 1, emptyArray())

    for (i in equations.indices)
    {
        for (j in 0 until columnCount)
        {
            val eqn = equations[i].children[0]!!
            if (eqn.type == ExpressionType.Variable || powerFunctions.contains(eqn.function))
            {
                if (sortedVars.indexOf(eqn.str) == j)
                {
                    matrix[i, j] = one
                }
                else
                {
                    matrix[i, j] = zero
                }
            }
            else if (summationFuncs.contains(eqn.function))
            {
                val children = eqn.children.filter {
                    it!!.variables.size == 1 && sortedVars.indexOf(it.variables.first()) == j
                }
                matrix[i, j] = if (children.any())
                    children.reduce { acc, expression ->
                        (acc ?: zero) + (expression ?: zero)
                    }?.evaluate(Pair(sortedVars[j], 1.0)) ?: zero
                else zero
            }
            else if (productFuncs.contains(eqn.function))
            {
                val children = eqn.children.filter {
                    it!!.variables.size == 1 && sortedVars.indexOf(it.variables.first()) == j
                }
                matrix[i, j] = if (children.any())
                    children.reduce { acc, expression ->
                        (acc ?: zero) * (expression ?: zero)
                    }?.evaluate(Pair(sortedVars[j], 1.0)) ?: zero
                else zero
            }
        }

        resultMatrix[i, 0] = equations[i].children[1]!!
    }

    return Pair(matrix, resultMatrix)
}