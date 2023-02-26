package com.staa.staacalcengine.booleanalgebra.diagrams

import com.staa.staacalcengine.booleanalgebra.BooleanAlgebraTerm
import java.io.Serializable

class Diagram(_term: BooleanAlgebraTerm, shouldFlatten: Boolean) : Serializable
{
    val variables = _term.variables
    val depth: Int
        get() = term.depth
    val term: BooleanAlgebraTerm
    val levels: Array<Array<BooleanAlgebraTerm>>
    val width
        get() = term.width

    init
    {
        _term.evaluate(hashMapOf(*_term.variables.map { Pair(it, true) }.toTypedArray()))

        term = if (shouldFlatten)
        {
            flatten(_term)
        }
        else
        {
            _term
        }

        levels = term.levels
    }

    private fun flatten(term: BooleanAlgebraTerm): BooleanAlgebraTerm
    {
        // Will flatten to multi-child trees
        // current impl just returns
        // todo flatten terms
        return term
    }
}