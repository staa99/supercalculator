package com.staa.staacalcengine.linearalgebra

import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.expressions.one
import com.staa.staacalcengine.expressions.zero

fun ExpressionMatrix.getDeterminant(): Expression
{
    if (!isSquareMatrix())
    {
        throw UnsupportedOperationException("The determinant is not square")
    }
    val (matrix, operations) = reduceToRowCanonical()
    if (matrix.hasZeroRows())
    {
        return zero
    }

    var determinant = one
    for (operation in operations)
    {
        determinant *= if (operation.isSwap)
        {
            (zero - one)
        }
        else
        {
            if (operation.targetR1) operation.r1Scalar else operation.r2Scalar
        }
    }

    return (one / determinant).evaluate()
}