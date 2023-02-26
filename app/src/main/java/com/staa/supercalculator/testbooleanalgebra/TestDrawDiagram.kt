package com.staa.supercalculator.testbooleanalgebra

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.staa.staacalcengine.booleanalgebra.BooleanAlgebraTerm
import com.staa.staacalcengine.booleanalgebra.diagrams.Diagram
import com.staa.supercalculator.R
import kotlinx.android.synthetic.main.activity_test_draw_diagram.*

class TestDrawDiagram : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_draw_diagram)
        gen(boolTextView)
    }

    fun gen(view: View)
    {
        val code = BooleanAlgebraTerm.fromString(boolEditText.text.toString())
        val diagram = Diagram(code, false)
        circuitBoard.diagram = diagram
        circuitBoard.invalidate()
    }
}
