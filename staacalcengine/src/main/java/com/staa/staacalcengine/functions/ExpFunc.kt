package com.staa.staacalcengine.functions

import com.staa.staacalcengine.expressions.Expression

data class ExpFunc
(
        val func: (expression: Expression, variables: Map<String, Double>) -> Expression,
        val minArity: Int = -1,
        val maxArity: Int = -1
)