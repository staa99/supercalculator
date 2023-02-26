package com.staa.staacalcengine.linearalgebra

import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.one

data class RowOperation
(
        val r1: Int,
        val r2: Int = -1, // for scalar product
        val r1Scalar: Expression = one,
        val r2Scalar: Expression = one,
        val isSwap: Boolean = false,
        val targetR1: Boolean = false
)
{
    override fun toString(): String
    {
        val r1string = "R${r1+1}"
        val r2string = "R${r2+1}"

        if (isSwap)
        {
            return "$r1string <-> $r2string"
        }

        if (r2 < 0)
        {
            return "$r1string -> $r1Scalar$r1string"
        }

        // if it gets here, it is a row addition operation
        val targetString = if (targetR1) r1string else r2string
        val r1ProductString = if(r1Scalar.isOne()) r1string else "$r1Scalar$r1string"
        val r2ProductString = if(r2Scalar.isOne()) r2string else "$r2Scalar$r2string"

        return "$targetString -> $r1ProductString + $r2ProductString"
    }
}