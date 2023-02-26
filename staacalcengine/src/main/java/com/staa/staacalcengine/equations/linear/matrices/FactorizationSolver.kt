package com.staa.staacalcengine.equations.linear.matrices

import com.staa.staacalcengine.equations.util.createCoefficientMatrix
import com.staa.staacalcengine.equations.util.prepareArgs
import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.util.sum
import com.staa.staacalcengine.expressions.zero
import com.staa.staacalcengine.linearalgebra.ExpressionMatrix
import com.staa.staacalcengine.linearalgebra.RowOperation
import com.staa.staacalcengine.linearalgebra.factorize
import java.util.*


data class FactorizationResult
(
        val values: MutableMap<String, Expression>,
        val operations: Array<RowOperation>,
        val upperTriangular: ExpressionMatrix,
        val lowerTriangular: ExpressionMatrix,
        val x: Array<Expression>,
        val y: Array<Expression>
)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FactorizationResult

        if (values != other.values) return false
        if (!Arrays.equals(operations, other.operations)) return false
        if (upperTriangular != other.upperTriangular) return false
        if (lowerTriangular != other.lowerTriangular) return false
        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = values.hashCode()
        result = 31 * result + Arrays.hashCode(operations)
        result = 31 * result + upperTriangular.hashCode()
        result = 31 * result + lowerTriangular.hashCode()
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}

/**
 * The function solves a system of linear equations by using factorization
 * The system is originally in the form Ax=B. The matrix A gets factorized to the
 * product of two matrices L and U.
 * Therefore LUx=B.
 * We set Ux=Y and solve the easier system, LY=B.
 * We then solve Ux=Y. This implementation uses back substitution to solve for x
 */
fun solveSystemOfEquationsFS(eqns: Array<Expression>): FactorizationResult
{
    val (equations, vars) = prepareArgs(eqns)
    val (A, B) = createCoefficientMatrix(vars, equations)
    val (L, uRes) = A.factorize()
    val (U, operations) = uRes

    val Y = solveLY(L, B)
    val X = solveUx(U, Y)

    return FactorizationResult(
            vars.zip(X).toMap().toMutableMap(),
            operations,
            U,
            L,
            X,
            Y
                              )
}


/**
 * Solves LY=B. L must be a square matrix
 */
fun solveLY(L: ExpressionMatrix, B: ExpressionMatrix): Array<Expression>
{
    if (B.n > 1)
    {
        throw UnsupportedOperationException("The matrix B in Ax=B must be n*1")
    }

    if (!L.isSquareMatrix())
    {
        throw UnsupportedOperationException("L must be a square matrix")
    }

    if (L.m != B.m)
    {
        throw UnsupportedOperationException(
                "The matrix B is not suitable for obtaining the solution of Ax=B")
    }


    val result = Array(B.m) { zero }
    for ((i, row) in L.grid.withIndex())
    {
        val s = result.take(i).zip(row)
        { a, b ->
            a * b
        }.sum()

        result[i] = ((B[i, 0] - s) / row[i]).evaluate()
    }

    return result
}


/**
 * Solves Ux=Y. U must be a square matrix.
 */
fun solveUx(U: ExpressionMatrix, Y: Array<Expression>): Array<Expression>
{
    if (!U.isSquareMatrix())
    {
        throw UnsupportedOperationException("L must be a square matrix")
    }

    if (U.m != Y.size)
    {
        throw UnsupportedOperationException(
                "The matrix B is not suitable for obtaining the solution of Ax=B")
    }

    val n = Y.size
    val result = Array(n) { zero }
    for ((i, row) in U.grid.withIndex().reversed())
    {
        val s = result.takeLast(n - i).zip(row)
        { a, b ->
            a * b
        }.sum()

        result[i] = ((Y[i] - s) / row[i]).evaluate()
    }

    return result
}