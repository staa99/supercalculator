package com.staa.circuitboard.view

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.staa.circuitboard.R
import com.staa.staacalcengine.booleanalgebra.BooleanAlgebraTerm
import com.staa.staacalcengine.booleanalgebra.BooleanOperator
import com.staa.staacalcengine.booleanalgebra.diagrams.Diagram
import kotlin.math.max

/**
 * This class is responsible for drawing the circuit of any boolean algebra expression,
 * It depends on the core calculator engine for representation of the logic circuit
 *
 * It takes the @gateSize uses it to scale the width and the height
 * A margin of (0.1 * @gateSize) is applied to each gate
 */
class CircuitBoard : View
{

    var gateSize = 50f
    var gateMargin = 0.2f * gateSize
    var textXDistance = 20f
    var textYDistance = 0.45f * gateSize
    lateinit var diagram: Diagram

    private var contentWidth = width - paddingLeft - paddingRight
    private var contentHeight = height - paddingTop - paddingBottom
    private val compWidth
        get() = (gateMargin * 2) + gateSize


    private val andStyle = Paint()
    private val orStyle = Paint()
    private val notStyle = Paint()
    private val valueStyle = Paint()
    private val andTextStyle = TextPaint()
    private val orTextStyle = TextPaint()
    private val notTextStyle = TextPaint()
    private val valueTextStyle = TextPaint()
    private val connectorPaint = Paint()

    constructor(context: Context) : super(context)
    {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs,
                                                                              defStyle)
    {
        init(attrs, defStyle)
    }

    constructor(context: Context, gateSize: Float, diagram: Diagram) : super(context)
    {
        this.gateSize = gateSize
        this.diagram = diagram
    }

    private fun init(attrs: AttributeSet?, defStyle: Int)
    {
        // Load attributes
        setWillNotDraw(false)
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.CircuitBoard, defStyle, 0)
        gateSize = a.getFloat(R.styleable.CircuitBoard_gateSize, gateSize)
        gateMargin = a.getFloat(R.styleable.CircuitBoard_gateMargin, gateMargin)
        textXDistance = a.getFloat(R.styleable.CircuitBoard_textXDistance, textXDistance)
        textYDistance = a.getFloat(R.styleable.CircuitBoard_textYDistance, textYDistance)
        a.recycle()

        initANDStyle()
        initORStyle()
        initNOTStyle()
        initVALUEStyle()
        initConnectorStyle()
    }


    fun initANDStyle()
    {
        andStyle.isAntiAlias = true
        andStyle.isLinearText = true
        andStyle.isSubpixelText = true
        andStyle.isFakeBoldText = true
        valueStyle.style = Paint.Style.STROKE
        andStyle.color = Color.argb(255, 20, 220, 167)
        andStyle.textAlign = Paint.Align.CENTER
        andStyle.textSize = 15.0f

        andTextStyle.set(andStyle)
        andTextStyle.color = Color.WHITE

        andStyle.strokeWidth = 0.25f
    }


    fun initVALUEStyle()
    {
        valueStyle.isAntiAlias = true
        valueStyle.isLinearText = true
        valueStyle.isSubpixelText = true
        valueStyle.isFakeBoldText = true
        valueStyle.style = Paint.Style.STROKE
        valueStyle.color = Color.WHITE
        valueStyle.textAlign = Paint.Align.CENTER
        valueStyle.textSize = 15.0f

        valueTextStyle.set(valueStyle)
        valueTextStyle.color = Color.BLACK
    }


    fun initORStyle()
    {
        orStyle.isAntiAlias = true
        orStyle.isLinearText = true
        orStyle.isSubpixelText = true
        orStyle.isFakeBoldText = true
        orStyle.color = Color.argb(255, 167, 220, 70)
        orStyle.textAlign = Paint.Align.CENTER
        orStyle.textSize = 15.0f
        valueStyle.style = Paint.Style.STROKE

        orTextStyle.set(orStyle)
        orTextStyle.color = Color.WHITE
    }


    fun initNOTStyle()
    {
        notStyle.isAntiAlias = true
        notStyle.isLinearText = true
        notStyle.isSubpixelText = true
        notStyle.isFakeBoldText = true
        notStyle.color = Color.argb(255, 220, 60, 167)
        notStyle.textAlign = Paint.Align.CENTER
        notStyle.textSize = 15.0f
        valueStyle.style = Paint.Style.STROKE

        notTextStyle.set(notStyle)
        notTextStyle.color = Color.WHITE
    }


    fun initConnectorStyle()
    {
        connectorPaint.color = Color.BLACK
        connectorPaint.style = Paint.Style.FILL
        connectorPaint.isAntiAlias
    }


    private fun drawRECTGate(canvas: Canvas, point: PointF, paint: Paint, textPaint: TextPaint, text: String)
    {
        // the x of the point is supposed to be the center,
        // so we provide the start x
        val startX = point.x - gateSize / 2
        val endX = point.x + gateSize / 2

        val rect = RectF(startX,
                         point.y,
                         endX,
                         point.y + gateSize)

        canvas.drawRect(rect, paint)

        // text location
        val textStartX = startX + textXDistance
        val textStartY = point.y + textYDistance


        canvas.drawText(text, textStartX, textStartY, textPaint)
    }


    private fun drawConnectorLines(canvas: Canvas, parentCenter: PointF, point: PointF)
    {
        if (parentCenter.x == point.x)
        {
            canvas.drawLine(point.x, parentCenter.y + gateSize, point.x, point.y, connectorPaint)
            return
        }

        // if a straight line from this center to parent center is positive, we draw right else we draw left
        val multiplier = if ((parentCenter.y - point.y) / (parentCenter.x - point.x) > 0)
        {
            1
        }
        else
        {
            -1
        }


        // draw the first vertical line
        val lineY = (point.y + (parentCenter.y + gateSize)) / 2
        canvas.drawLine(point.x, lineY, point.x, point.y, connectorPaint)

        // draw the horizontal line
        val parentJoinX = (multiplier * gateSize / 4) + parentCenter.x
        canvas.drawLine(point.x, lineY, parentJoinX, lineY, connectorPaint)

        // draw second vertical line
        canvas.drawLine(parentJoinX, lineY, parentJoinX, parentCenter.y + gateSize, connectorPaint)
    }


    private fun drawANDGate(canvas: Canvas, point: PointF, parentCenter: PointF?)
    {
        if (parentCenter != null)
        {
            drawConnectorLines(canvas, parentCenter, point)
        }

        drawRECTGate(canvas, point, andStyle, andTextStyle, BooleanOperator.and.name.capitalize())
    }


    private fun drawORGate(canvas: Canvas, point: PointF, parentCenter: PointF?)
    {
        if (parentCenter != null)
        {
            drawConnectorLines(canvas, parentCenter, point)
        }

        drawRECTGate(canvas, point, orStyle, orTextStyle, BooleanOperator.or.name.capitalize())
    }

    private fun drawNOTGate(canvas: Canvas, point: PointF, parentCenter: PointF?)
    {
        if (parentCenter != null)
        {
            drawConnectorLines(canvas, parentCenter, point)
        }

        drawRECTGate(canvas, point, notStyle, notTextStyle, BooleanOperator.not.name.capitalize())
    }

    private fun drawInputGate(canvas: Canvas, point: PointF, name: String, parentCenter: PointF?)
    {
        if (parentCenter != null)
        {
            drawConnectorLines(canvas, parentCenter, point)
        }

        drawRECTGate(canvas, point, valueStyle, valueTextStyle, name)
    }


    private fun drawElement(term: BooleanAlgebraTerm, point: PointF, canvas: Canvas, parentCenter: PointF?)
    {
        when (term.op!!)
        {
            BooleanOperator.and   -> drawANDGate(canvas, point, parentCenter)
            BooleanOperator.or    -> drawORGate(canvas, point, parentCenter)
            BooleanOperator.not   -> drawNOTGate(canvas, point, parentCenter)
            BooleanOperator.value -> drawInputGate(canvas, point, term.value!!, parentCenter)
        }
    }


    fun drawTerm(canvas: Canvas, term: BooleanAlgebraTerm, y: Float, startX: Float = paddingStart.toFloat(), endX: Float = contentWidth.toFloat() - paddingStart, parentCenter: PointF? = null)
    {
        var centerX = (startX + endX) / 2
        if (term.op == BooleanOperator.value)
        {
            return drawElement(term, PointF(centerX, y), canvas, parentCenter)
        }

        val childY = y + compWidth

        if (term.op == BooleanOperator.not)
        {
            val center = PointF(centerX, y)
            drawElement(term, center, canvas, parentCenter)
            return drawTerm(canvas, term.operand1!!, childY, startX, endX, center)
        }

        val termWidth = term.width
        val rightWidth = term.operand2!!.width

        val x = ((endX - startX) * (termWidth - rightWidth - 1) / (termWidth))
        val xPlus = (endX - startX) / (termWidth)
        val leftEndX = startX + x
        val rightStartX = leftEndX + xPlus

        centerX = (leftEndX + rightStartX) / 2

        val center = PointF(centerX, y)
        drawElement(term, center, canvas, parentCenter)
        drawTerm(canvas, term.operand1!!, childY, startX, leftEndX, center)
        drawTerm(canvas, term.operand2!!, childY, rightStartX, endX, center)
    }


    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)
        contentWidth = width - paddingLeft - paddingRight
        contentHeight = height - paddingTop - paddingBottom/*
        if (contentWidth < compWidth * diagram.width + paddingLeft + paddingRight)
        {
            contentWidth = (compWidth * diagram.width + paddingLeft + paddingRight + 1).toInt()
        }*/
        drawTerm(canvas, diagram.term, gateMargin * 4 + paddingTop)
        /*
        exampleString?.let {
            // Draw the text.
            canvas.drawText(it,
                            paddingLeft + (contentWidth - textWidth) / 2,
                            paddingTop + (contentHeight + textHeight) / 2,
                            textPaint)
        }

        // Draw the example drawable on top of the text.
        exampleDrawable?.let {
            it.setBounds(paddingLeft, paddingTop,
                         paddingLeft + contentWidth, paddingTop + contentHeight)
            it.draw(canvas)
        }*/
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        val compWidth = (gateMargin * 2) + gateSize
        val extraVerticalSpace = (gateMargin * 4) + paddingTop + paddingBottom

        val height = max(View.MeasureSpec.getSize(heightMeasureSpec),
                         ((diagram.depth * compWidth) + extraVerticalSpace).toInt())
        val width = max(View.MeasureSpec.getSize(widthMeasureSpec),
                        (compWidth * diagram.width + paddingLeft + paddingRight).toInt())

        setMeasuredDimension(width, height)
    }
}