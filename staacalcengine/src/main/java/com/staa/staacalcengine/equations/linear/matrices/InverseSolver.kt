package com.staa.staacalcengine.equations.linear.matrices

import com.staa.staacalcengine.equations.util.createCoefficientMatrix
import com.staa.staacalcengine.equations.util.prepareArgs
import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.linearalgebra.ExpressionMatrix
import com.staa.staacalcengine.linearalgebra.RowOperation

/**
 * The inverse solver works by expressing the system in the form Ax=B and finding inverse(A)=C
 * The solution is CB
 *
 * The number of equations must be the same as the number of variables and the coef matrix must be invertible
 * otherwise an error is thrown
 */

fun solveSystemOfEquationsInverse(eqns: Array<Expression>): Pair<MutableMap<String, Expression>, Array<RowOperation>>
{
    val (equations, vars) = prepareArgs(eqns)
    val equationMatrices = createCoefficientMatrix(vars, equations)
    val inversionResult = equationMatrices.first.invert()

    val (inverse, operations) = inversionResult

    if (inverse == null)
    {
        throw UnsupportedOperationException(
                "The coefficient matrix is not invertible\n" +
                        "The following operations yielded a matrix with zero rows\n" +
                        operations!!.contentDeepToString())
    }

    val resultMatrix = inverse * equationMatrices.second
    val map = extractResultsInverse(vars, resultMatrix)

    return Pair(map, operations!!)
}


fun extractResultsInverse(sortedVars: Array<String>, matrix: ExpressionMatrix): MutableMap<String, Expression>
{
    if (matrix.n != 1)
    {
        throw UnsupportedOperationException(
                "The product of the inverse and the result must be an (m x 1) matrix")
    }

    val results = matrix.grid.map { it[0] }
    return sortedVars.zip(results).toMap().toMutableMap()
}