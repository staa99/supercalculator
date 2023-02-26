package com.staa.staacalcengine.equations.linear.matrices

import com.staa.staacalcengine.equations.util.createAugmentedMatrix
import com.staa.staacalcengine.equations.util.prepareArgs
import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.linearalgebra.ExpressionMatrix
import com.staa.staacalcengine.linearalgebra.RowOperation
import kotlin.math.abs

/**
 * gaussian elimination to solve system of linear equations
 * algorithm
 * verify that the expressions are equations
 * verify that the lhs and rhs are linear
 *    verify that both lhs and rhs are any of summations, products of constants and variables, powers to 1, variables and constants
 *        verify that every child of each of the above meet these conditions
 * flatten out summations
 * sort terms by variable name
 * extract coefs
 * create augmented matrix
 * reduce to row canonical as rc
 * if rc has at least one row of zeroes, the equation has too many solutions or none at all
 * else read the last column in the sort order
 * return map of variable to results and row canonical and set of operations
 */


fun solveSystemOfEquationsGaussian(eqns: Array<Expression>): Pair<MutableMap<String, Expression>, Array<RowOperation>>
{
    val (equations, vars) = prepareArgs(eqns)

    val reductionResult = createAugmentedMatrix(vars, equations).reduceToRowCanonical()
    val matrix = reductionResult.first
    val operations = reductionResult.second.toTypedArray()

    val map = extractResultsGaussian(vars, matrix)

    return Pair(map, operations)
}


fun extractResultsGaussian(sortedVars: Array<String>, matrix: ExpressionMatrix): MutableMap<String, Expression>
{
    val rows = matrix.grid
    val results = mutableMapOf<String, Expression>()

    for (i in rows.indices)
    {
        val row = rows[i]
        val varName = sortedVars[i]

        if (abs(row.sumByDouble { it.str.toDouble() }) < 1e-13)
        {
            break
        }

        if (row[i].isZero())
        {
            // The system is singular
            //continue
            return mutableMapOf()
        }


        val rowRes = row.last().str.toDouble()
        val varCoefs = row.take(row.lastIndex).withIndex().filter {
            it.index != i && abs(it.value.str.toDouble()) > 1e-13
        }.map { Pair(sortedVars[it.index], it.value.str.toDouble()) }.toMap()

        var expressionString = "summation($rowRes"

        for (v in varCoefs)
        {
            if (abs(v.value) > 1e-13)
            {
                expressionString += ",(0-${v.value})*${v.key}"
            }
        }

        expressionString += ")"

        results[varName] = Expression.fromString(expressionString, varCoefs.size + 1).evaluate()
    }

    return results
}