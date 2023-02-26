package com.staa.staacalcengine.booleanalgebra.diagrams

import com.staa.staacalcengine.booleanalgebra.BooleanAlgebraTerm
import junit.framework.Assert.*
import org.junit.Test

class DiagramRepresentationTests
{
    @Test
    fun testDepthAndLevel()
    {
        val code = "(AB + C + -D).(AB)"
        val root = BooleanAlgebraTerm.fromString(code)
        val diagram = Diagram(root, false)

        assertTrue("Diagram depth not greater than 4", diagram.depth > 4)
    }
}