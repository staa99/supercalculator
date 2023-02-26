package com.staa.staacalcengine.booleanalgebra

import com.staa.staacalcengine.util.getLeastPrecedentOperator
import com.staa.staacalcengine.util.removeStartAndEndParenthesis
import kotlin.math.max


class BooleanAlgebraTerm()
{

    constructor(op: BooleanOperator, value: String) : this()
    {
        this.op = op
        this.value = value
    }

    var value: String? = null
    var op: BooleanOperator? = null
    var operand1: BooleanAlgebraTerm? = null
    var operand2: BooleanAlgebraTerm? = null
    var depth = 0
        private set
    var width = 0
        private set
    lateinit var levels: Array<Array<BooleanAlgebraTerm>>

    val variables: Array<String>
        get()
        {
            if (op == BooleanOperator.value)
            {
                return arrayOf(value!!)
            }


            val l1 = operand1?.variables ?: emptyArray()
            val l2 = operand2?.variables ?: emptyArray()

            return l1.union(l2.toList()).sorted().toTypedArray()
        }

    fun evaluate(values: HashMap<String, Boolean>): Boolean
    {
        values["1"] = true
        values["0"] = false
        calculateLevels()
        calculateDepthAndWidth()
        return when (op)
        {
            BooleanOperator.not   -> !operand1!!.evaluate(values)
            BooleanOperator.or    -> operand1!!.evaluate(values) || operand2!!.evaluate(values)
            BooleanOperator.and   -> operand1!!.evaluate(values) && operand2!!.evaluate(values)
            BooleanOperator.value -> values[value]!!
            else                  -> values[value]!!
        }
    }


    private fun getLevelBin(): Array<BooleanAlgebraTerm>
    {
        if (op == BooleanOperator.value)
        {
            return arrayOf(this)
        }

        if (op == BooleanOperator.not)
        {
            return arrayOf(operand1!!)
        }

        return arrayOf(operand1!!, operand2!!)
    }

    private fun calculateLevels()
    {
        val levelList = arrayListOf(arrayOf(this))
        for (i in 1 until depth)
        {
            levelList.add(
                    levelList.last().flatMap { it.getLevelBin().toList() }.toTypedArray())
        }

        levels = levelList.toTypedArray()
    }

    private fun calculateDepthAndWidth()
    {
        if (op!! == BooleanOperator.value)
        {
            width = 1
            depth = 1
            return
        }

        operand1!!.calculateDepthAndWidth()
        if (op!! == BooleanOperator.not)
        {
            depth = operand1!!.depth + 1
            width = operand1!!.width
            return
        }

        operand2!!.calculateDepthAndWidth()

        depth = max(operand1!!.depth, operand2!!.depth) + 1
        width  = operand1!!.width + operand2!!.width + 1
    }



    override fun toString(): String
    {
        return when (op)
        {
            BooleanOperator.and -> "(" + operand1 + dot.toString() + operand2 + ")"
            BooleanOperator.or  -> "(" + operand1 + plus.toString() + operand2 + ")"
            BooleanOperator.not -> bar.toString() + operand1
            else                -> value!!
        }
    }

    companion object
    {
        private fun prepareString(str: String): String
        {
            var string = str
                    .replace(Regex("\\s+"), "")
                    .replace(")(", ").(")
                    .replace(Regex("([a-zA-Z])([a-zA-Z])"))
                    {
                        "${it.groupValues[1]}.${it.groupValues[2]}"
                    }
                    .replace(Regex("\\)([a-zA-Z])"))
                    {
                        ").${it.groupValues[1]}"
                    }
                    .replace(Regex("([a-zA-Z])\\("))
                    {
                        "${it.groupValues[1]}.("
                    }
            string = /*makeProductTerm(string).*/string.removeStartAndEndParenthesis()
            return string
        }


        private fun isOperation(string: String): Boolean
        {
            return string.length == 1 && string[0] != plus && string[0] != dot && string[0] != bar
        }


        private fun getOperator(char: Char) = when (char)
        {
            bar  -> BooleanOperator.not
            dot  -> BooleanOperator.and
            plus -> BooleanOperator.or
            else -> BooleanOperator.value
        }


        private fun setChildren(expression: BooleanAlgebraTerm, str: String, op: Char)
        {
            // get root depth
            if (str.isEmpty()) throw UnsupportedOperationException("Syntax Error!")

            val string = prepareString(str)

            if (op != bar)
            {
                var expr1String = ""
                var expr2String = ""
                var expr1Taken = false
                var depth = 0

                var i = 0
                while (i <= string.lastIndex)
                {
                    val c = string[i]
                    var increment = 0

                    if (c == '(')
                    {
                        depth++
                    }
                    else if (c == ')')
                    {
                        depth--
                    }
                    when
                    {
                        expr1Taken  ->
                        {
                            expr2String += string.substring(i)
                            increment = expr2String.length
                        }
                        !expr1Taken ->
                        {
                            if (depth == 0 && op == string[i])
                            {
                                expr1Taken = true
                            }
                            else
                            {
                                expr1String += c
                            }

                            increment = 1
                        }
                    }

                    i += increment
                }

                val expr1 = fromString(expr1String)
                val expr2 = fromString(expr2String)

                expression.operand1 = expr1
                expression.operand2 = expr2
            }
            else
            {
                var i = 0
                var cArgExpString = ""
                var funcFound = false
                var depth = 0
                var openParen = false

                while (i <= string.lastIndex)
                {
                    val c = string[i]

                    var increment = 1

                    if (c == '(')
                    {
                        depth++
                        openParen = true
                    }
                    else if (c == ')')
                    {
                        depth--
                    }

                    if (funcFound)
                    {
                        var shouldBreak = false
                        cArgExpString += c

                        if (((depth <= 0 && c == ')') && openParen) || !openParen)
                        {
                            shouldBreak = true
                        }

                        if (shouldBreak)
                        {
                            expression.operand1 = fromString(cArgExpString)
                            break
                        }
                    }
                    else if (!funcFound && depth == 0)
                    {
                        if (string[i] == bar)
                        {
                            funcFound = true
                            increment = 1
                        }
                    }

                    i += increment
                }
            }
        }


        fun fromString(str: String): BooleanAlgebraTerm
        {
            val string = prepareString(str)

            if (isOperation((string)))
            {
                return BooleanAlgebraTerm(op = BooleanOperator.value, value = string)
            }

            val leastPrecedent = getLeastPrecedentOperator(string, booleanOperators.toList()) { it }
            val op = getOperator(leastPrecedent[0])

            val term = BooleanAlgebraTerm(op, string)
            setChildren(term, string, leastPrecedent[0])

            return term
        }
    }
}