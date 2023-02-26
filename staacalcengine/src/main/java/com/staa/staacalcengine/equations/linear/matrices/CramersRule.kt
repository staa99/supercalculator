package com.staa.staacalcengine.equations.linear.matrices

import com.staa.staacalcengine.equations.util.createCoefficientMatrix
import com.staa.staacalcengine.equations.util.prepareArgs
import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.linearalgebra.ExpressionMatrix
import com.staa.staacalcengine.linearalgebra.getDeterminant

// Cramer's rule solver
// det(A)/det()

/**
 * Solves a linear system using Cramer's rule
 */
fun solveSystemOfEquationsCM(eqns: Array<Expression>): MutableMap<String, Expression>
{
    val (equations, vars) = prepareArgs(eqns)
    val (A, B) = createCoefficientMatrix(vars, equations)

    if (vars.size != A.grid.size || A.grid.size != B.grid.size)
    {
        throw UnsupportedOperationException(
                "Cramer's rule requires that the number of variables is same as the number of equations")
    }

    val result = emptyMap<String, Expression>().toMutableMap()
    val detK = A.getDeterminant()

    if (detK.isZero())
    {
        throw UnsupportedOperationException("The system has no solution")
    }

    for ((i, variable) in vars.withIndex())
    {
        result[variable] = (getVariableMatrix(i, A, B).getDeterminant() / detK).evaluate(
                *A.variables.union(B.variables.toList()).toTypedArray())
    }

    return result
}


fun getVariableMatrix(varIndex: Int, A: ExpressionMatrix, B: ExpressionMatrix): ExpressionMatrix
{
    val vt = ExpressionMatrix(A.m, A.n, emptyArray())
    for (i in 0 until A.m)
    {
        for (j in 0 until A.n)
        {
            if (j != varIndex)
            {
                vt[i, j] = A[i, j]
            }
            else
            {
                vt[i, j] = B[i, 0]
            }
        }
    }
    return vt
}