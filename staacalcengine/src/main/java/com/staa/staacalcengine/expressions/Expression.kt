package com.staa.staacalcengine.expressions

import com.staa.staacalcengine.equations.util.productFuncs
import com.staa.staacalcengine.equations.util.summationFuncs
import com.staa.staacalcengine.expressions.ExpressionType.*
import com.staa.staacalcengine.functions.functions
import com.staa.staacalcengine.util.getLeastPrecedentOperator
import com.staa.staacalcengine.util.removeStartAndEndParenthesis
import com.staa.staacalcengine.util.symbolRegex
import java.util.*
import kotlin.math.abs

val one = Expression.createConst("1.0")
val zero = Expression.createConst("0.0")

class Expression(val type: ExpressionType, str: String, val function: String, argumentCount: Int)
{
    val variables = HashSet<String>()
    val children = arrayOfNulls<Expression>(argumentCount)

    /**
     * Conditional expressions set this property
     * the indices of the conditions correspond to the indices of the expressions in the children array.
     * The only condition allowed to be null is the last one,
     * in that case, if all preceding conditions are not met and the last one is null,
     * then the last child will be evaluated.
     * If there is no null condition and none of the conditions is met then an exception is thrown.
     * The result is that the current state of variables does not serve as a proper domain for the evaluation
     * of the expression. The expression therefore doesn't have a definition for that state, and is not defined for the state.
     */
    val conditions = arrayOfNulls<Expression>(argumentCount)

    val str: String

    init
    {
        val d = str.toDoubleOrNull()

        if (type == ExpressionType.Const && d != null && abs(d) < 1e-13)
        {
            this.str = "0.0"
        }
        else
        {
            this.str = str
        }
    }

    override fun toString(): String
    {
        if (type == Const || type == Variable) return str

        if (type == Conditional)
        {
            return conditions.mapIndexed { i, e -> "$e:${children[i]}" }.joinToString(
                    separator = ";",
                    prefix = "{",
                    postfix = "}")
        }

        if (function == "error" || function == "print")
        {
            return "$function($str)"
        }

        return if (function.matches(Regex("\\w+")))
        {
            if (children.any())
                "$function${children.joinToString(prefix = "(", postfix = ")",
                                                  transform = { it?.toString() ?: "" },
                                                  separator = ",")}"
            else
                "Constant functions not allowed as functions, define as constants"
        }
        else
        {
            "(${children[0].toString()}$function${children[1].toString()})"
        }
    }

    fun isZero() = type == ExpressionType.Const && abs(str.toDouble()) < 1e-13
    fun isOne() = type == ExpressionType.Const && abs(str.toDouble() - 1) < 1e-13


    private fun genericBuildFlat(exp: Expression, terms: ArrayList<Expression>, inverseTerms: ArrayList<Expression>, inverseFuncs: Array<String>, funcs: Array<String>)
    {
        val expression = exp.evaluate()
        if (expression.type == ExpressionType.Const || expression.type == ExpressionType.Variable)
        {
            terms.add(expression)
            return
        }
        else if (expression.type == ExpressionType.Exp)
        {
            when
            {
                expression.function in funcs        -> treatNormal(expression, terms, inverseTerms,
                                                                   funcs, inverseFuncs)
                expression.function in inverseFuncs -> treatInverse(expression, terms, inverseTerms,
                                                                    funcs, inverseFuncs)
                else                                -> terms.add(expression)
            }
        }
    }


    private fun treatNormal(expression: Expression, terms: ArrayList<Expression>, inverseTerms: ArrayList<Expression>, funcs: Array<String>, inverseFuncs: Array<String>)
    {
        for (e in expression.children)
        {
            genericBuildFlat(e!!, terms, inverseTerms, inverseFuncs, funcs)
        }
    }

    private fun treatInverse(expression: Expression, terms: ArrayList<Expression>, inverseTerms: ArrayList<Expression>, funcs: Array<String>, inverseFuncs: Array<String>)
    {
        val left = expression.children[0]!!
        val right = expression.children[1]!!

        genericBuildFlat(left, terms, inverseTerms, inverseFuncs, funcs)

        if (right.type == ExpressionType.Const ||
                right.type == ExpressionType.Variable ||
                (right.function !in funcs && right.function !in inverseFuncs))
        {
            inverseTerms.add(right)
            return
        }
        else if (right.function in funcs)
        {
            treatNormal(right, inverseTerms, inverseTerms, funcs, inverseFuncs)
        }
        else if (right.function in inverseFuncs)
        {
            treatInverse(right, inverseTerms, terms, funcs, inverseFuncs)
        }
    }


    private fun buildProduct(exp: Expression, productTerms: ArrayList<Expression>, inverseTerms: ArrayList<Expression>)
    {
        genericBuildFlat(exp, productTerms, inverseTerms, arrayOf("/"), productFuncs)
    }


    private fun buildSummation(exp: Expression, sumTerms: ArrayList<Expression>, inverseTerms: ArrayList<Expression>)
    {
        genericBuildFlat(exp, sumTerms, inverseTerms, arrayOf("-"), summationFuncs)
    }

    operator fun plus(b: Expression): Expression
    {
        val sumTerms = arrayListOf<Expression>()
        val inverseTerms = arrayListOf<Expression>()

        val sum = Expression(Exp,
                             "${toString()}+$b",
                             "+",
                             2)
        sum.children[0] = this
        sum.children[1] = b

        buildSummation(sum, sumTerms, inverseTerms)

        val str = sumTerms.joinToString(prefix = "summation(", postfix = ")",
                                        transform = { it.toString() })
        val func = "summation"

        val result = Expression(Exp, str, func, sumTerms.size + inverseTerms.size)
        for (i in sumTerms.indices)
        {
            result.children[i] = sumTerms[i]
            result.variables.addAll(sumTerms[i].variables)
        }

        for (i in inverseTerms.indices)
        {
            result.children[sumTerms.size + i] = fromString("0-(${inverseTerms[i]})")
            result.variables.addAll(result.children[sumTerms.size + i]!!.variables)
        }
        return result
    }


    operator fun unaryMinus() = fromString("0-($this)").evaluate()

    operator fun minus(b: Expression): Expression
    {
        return this + (-b)
    }

    operator fun times(b: Expression): Expression
    {
        val productTerms = arrayListOf<Expression>()
        val inverseTerms = arrayListOf<Expression>()
        val product = Expression(Exp,
                                 "${toString()}*$b",
                                 "*",
                                 2)
        product.children[0] = this
        product.children[1] = b

        buildProduct(product, productTerms, inverseTerms)
        val str = productTerms.joinToString(prefix = "product(", postfix = ")",
                                            transform = { it.toString() })
        val func = "product"

        val result = Expression(Exp, str, func, productTerms.size + inverseTerms.size)
        for (i in productTerms.indices)
        {
            result.children[i] = productTerms[i]
            result.variables.addAll(productTerms[i].variables)
        }

        for (i in inverseTerms.indices)
        {
            result.children[productTerms.lastIndex + i] = fromString("1/(${inverseTerms[i]})")
            result.variables.addAll(result.children[productTerms.size + i]!!.variables)
        }
        return result
    }


    operator fun div(b: Expression): Expression
    {
        return this * fromString("(1/($b))")
    }

    operator fun rem(b: Expression): Expression
    {
        return fromString("(${toString()})%($b)")
    }

    override fun hashCode(): Int
    {
        return toString().hashCode()
    }


    fun equals(other: Expression?, vararg variables: Pair<String, Double>): Boolean
    {
        if (other == null) return false
        return evaluate(*variables) == other.evaluate(*variables)
    }

    override operator fun equals(other: Any?): Boolean
    {
        if (other !is Expression) return false

        val a = evaluate()
        val b = other.evaluate()

        if (a.type == b.type && a.type == Const)
        {
            return abs(a.str.toDouble() - b.str.toDouble()) < 1e-13
        }

        return a.str == b.str
    }


    companion object
    {
        fun createConst(value: String): Expression
        {
            val double = value.toDoubleOrNull()
            val result = Expression(if (double != null) Const else Variable,
                                    double?.toString() ?: value,
                                    double?.toString() ?: value,
                                    0)

            if (double == null)
            {
                result.variables.add(value)
            }

            return result
        }


        private fun getExpressionType(str: String): ExpressionType
        {
            var compExp = ""

            for (c in str)
            {
                if (c == '>' || c == '<' || c == '=')
                {
                    if (compExp.any())
                    {
                        if (c != '=' || compExp.contains('='))
                        {
                            throw UnsupportedOperationException(
                                    "You cannot have multiple compare operators (>, <, =) in an expression")
                        }
                        else
                        {
                            compExp += c
                        }
                    }
                    else
                    {
                        compExp += c
                    }
                }
            }

            return if (!compExp.any()) Exp
            else if (compExp.any { it == '>' || it == '<' }) InEql
            else Eqn
        }


        fun fixExpressionsString(str: String, fixNegative: Boolean = true): String
        {
            //(?![-]*[0-9]+[.]*[0-9]+e[-]*[0-9]+)
            var string = str
                    .replace(Regex("\\s+"), "")
                    .replace("--", "+")
                    .replace(")(", ")*(")
                    .replace(").(", ")*(")
                    .replace(Regex("\\)(\\.)*([a-zA-Z])"))
                    {
                        ")*${it.groupValues[2]}"
                    }
                    .replace(Regex("^-([a-zA-Z|(])"))
                    {
                        "(-1)*${it.groupValues[1]}"
                    }
                    .replace(Regex("([a-zA-Z|)])\\.([a-zA-Z|(])"))
                    {
                        "${it.groupValues[1]}*${it.groupValues[2]}"
                    }
                    .replace(Regex("([0-9])\\.([a-zA-Z])"))
                    {
                        "${it.groupValues[1]}*${it.groupValues[2]}"
                    }
                    .replace(Regex("([a-zA-Z])\\.([0-9])"))
                    {
                        "${it.groupValues[1]}*${it.groupValues[2]}"
                    }
                    .replace(Regex("(\\d+\\.*\\d*)[eE](-*\\d+)"))
                    {
                        "(${it.groupValues[1]}*(10^${it.groupValues[2]}))"
                    }
                    .removeStartAndEndParenthesis()

            if (fixNegative)
            {
                string = string.replace(Regex("([^a-zA-Z0-9_)]|^)-"))
                {
                    "${it.groupValues[1]}###"
                }

                /*
            This fixes expressions like ab to (a)*(b) but causes a bug in function names eg sin becomes (s)*(i)*(n)
            .replace(Regex("([a-zA-Z])([a-zA-Z])"))
            {
                "(${it.groupValues[1]})*(${it.groupValues[2]})"
            }
            */
                /*
            this fixes expressions like 2a to 2*a but causes a bug
            for example: 1.3323283e-9
            frontend has to manually put * or wrap them in parenthesis
            .replace(Regex("([0-9]+)([a-zA-Z]+)"))
            {
                "(${it.groupValues[1]})*(${it.groupValues[2]})"
            }
            */


                val starts = arrayListOf<Int>()
                var ind = string.indexOf("###")
                var last = 0

                while (ind != -1)
                {
                    starts += ind + last
                    ind += 3 + last
                    last = ind

                    if (string.length <= ind)
                    {
                        break
                    }

                    ind = string.substring(ind).indexOf("###")
                }

                for ((j, start) in starts.withIndex())
                {
                    var end = -1
                    if (string[start + 3 + j] == '(')
                    {
                        var depth = 1

                        for (i in start + 3..string.lastIndex)
                        {
                            val c = string[i]
                            if (c == '(') depth++
                            if (c == ')') depth--
                            if (depth == 0)
                            {
                                end = i + 1
                                break
                            }
                        }

                        if (end == -1)
                        {
                            throw UnsupportedOperationException(
                                    "Syntax error: The string ($string) is not a valid expression. Check parenthesis")
                        }
                    }
                    else
                    {
                        var depth = 0
                        for (i in start + 3 + j..string.lastIndex)
                        {
                            val c = string[i]
                            if (c == '(') depth++
                            if (c == ')') depth--
                            if ((!c.isLetterOrDigit() && c != '_' && c != '.' && c != '(' && c != ')') || c == ')' && depth < 0)
                            {
                                end = i
                                break
                            }
                        }

                        if (end == -1)
                        {
                            end = string.lastIndex + 1
                        }
                    }

                    string = string.replaceRange(start + j until end,
                                                 "(0-${
                                                 string.substring(start + 3 + j, end)
                                                 })")
                }
            }

            return string
        }


        private fun setExpressionChildren(expression: Expression, str: String, leastPrecedent: String, argCount: Int)
        {
            if (str.isEmpty()) throw UnsupportedOperationException("Syntax Error!")

            val string = fixExpressionsString(str)

            if (leastPrecedent.matches(Regex("\\W+")) && argCount == 2)
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
                            if (depth == 0 && leastPrecedent == string.substring(i,
                                                                                 i + leastPrecedent.length))
                            {
                                expr1Taken = true
                                increment = leastPrecedent.length
                            }
                            else
                            {
                                expr1String += c
                                increment = 1
                            }
                        }
                    }

                    i += increment
                }

                val expr1 = fromString(expr1String)
                val expr2 = fromString(expr2String)

                expression.children[0] = expr1
                expression.children[1] = expr2

                expression.variables.addAll(expr1.variables)
                expression.variables.addAll(expr2.variables)
            }
            else
            {
                var i = 0
                var cArgIndex = 0
                var cArgExpString = ""
                var funcFound = false
                var depth = 0

                while (i <= string.lastIndex)
                {
                    val c = string[i]

                    var increment = 1

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
                        funcFound                ->
                        {
                            if ((depth == 0 && c == ',') || (depth < 0 && c == ')'))
                            {
                                expression.children[cArgIndex] = fromString(cArgExpString)
                                expression.variables.addAll(
                                        expression.children[cArgIndex]!!.variables)
                                cArgIndex++
                                cArgExpString = ""
                            }
                            else
                            {
                                cArgExpString += c
                            }
                        }
                        !funcFound && depth == 0 ->
                        {
                            if (leastPrecedent == string.substring(i,
                                                                   i + leastPrecedent.length))
                            {
                                funcFound = true
                                increment = leastPrecedent.length + 1
                                if (string[i + increment - 1] != '(')
                                {
                                    throw UnsupportedOperationException(
                                            "Syntax Error: No opening parenthesis for functions")
                                }
                            }
                        }
                    }

                    i += increment
                }
            }
        }

        fun fromString(str: String, arity: Int = -1): Expression
        {
            fun parseConditional(str: String): Expression
            {
                val tokens = str.trimStart('{').trimEnd('}').split(';').filter { it.isNotEmpty() }
                if (tokens.isEmpty())
                {
                    throw UnsupportedOperationException(
                            "Syntax error: conditional expression must be non-empty")
                }

                val result = Expression(ExpressionType.Conditional, str, "conditional", tokens.size)

                var nonCondFound = false

                for (i in tokens.indices)
                {
                    if (nonCondFound) throw UnsupportedOperationException(
                            "Syntax error: A conditional expression can have only one non conditional clause. And it must be the last clause")

                    val token = tokens[i]
                    var cond: String? = null
                    val exp: String

                    val condResult: Expression?
                    val condition: Expression?
                    /** The ':' character must be the barrier between the condition and the expression
                     * It suffices to simply check for a contains.
                     */
                    if (token.contains(':'))
                    {
                        val colonIndex = token.indexOf(':')
                        cond = token.substring(0, colonIndex)
                        exp = token.substring(colonIndex + 1)
                    }
                    else
                    {
                        exp = token
                    }

                    condition = if (cond == null) null
                    else try
                    {
                        Expression.fromString(cond)
                    }
                    catch (e: Exception)
                    {
                        throw UnsupportedOperationException(
                                "Syntax error: A condition must be a valid expression. The string ($cond) in token[${i + 1}] is not valid")
                    }

                    condResult = try
                    {
                        Expression.fromString(exp)
                    }
                    catch (e: Exception)
                    {
                        throw UnsupportedOperationException(
                                "Syntax error: The result of a condition must be a valid expression. The string ($exp) in token[${i + 1}] is not valid")
                    }

                    if (condition == null)
                    {
                        nonCondFound = true
                    }

                    result.conditions[i] = condition
                    result.children[i] = condResult
                }

                return result
            }

            fun parseSpecial(str: String, name: String, argCount: Int = 0): Expression
            {
                val rest = str.removePrefix("$name(").removeSuffix(")")

                val exp: Expression
                if (name == "error")
                {
                    exp = Expression(Exp, rest, name, argCount)
                }
                else
                //if (name == "print")
                {
                    var depth = 0
                    var strings = listOf<String>()
                    var last = ""
                    for (c in rest)
                    {
                        if (c == '(') depth++
                        if (c == ')') depth--
                        if (c == ',' && depth == 0)
                        {
                            strings += last
                            last = ""
                        }
                        else
                        {
                            last += c
                        }
                    }

                    if (strings.size > 2)
                    {
                        throw UnsupportedOperationException(
                                "a print statement requires at most 2 childrenssss")
                    }

                    exp = Expression(Exp, last, name, strings.size)
                    for ((i, s) in strings.withIndex())
                    {
                        exp.children[i] = Expression(Exp, s, name, 0)
                    }
                }

                return exp
            }

            // remove spaces
            val string = fixExpressionsString(str)
            if (string.isEmpty()) throw UnsupportedOperationException("Syntax Error!")

            if (string.startsWith('{') && string.endsWith('}'))
            {
                return parseConditional(string)
            }

            // print and error statements must be aloness
            val specialName = when
            {
                string.startsWith("error(") -> "error"
                string.startsWith("print(") -> "print"
                else                        -> null
            }

            if (specialName != null && string.endsWith(')'))
            {
                return parseSpecial(string.replace("___", "\n").replace("_", " "), specialName)
            }


            val functionIsConstant = if (string.toDoubleOrNull() != null) true
            else !functions.any {
                it.key != string && string.contains(
                        Regex(if (it.key.matches(symbolRegex)) "\\${it.key}"
                              else "\\b${it.key}\\b"))
            }

            val expression: Expression


            if (functionIsConstant)
            {
                expression = createConst(string)
            }
            else
            {
                val expType = getExpressionType(string)
                val leastPrecedent = getLeastPrecedentOperator(string, functions.keys)
                {
                    fixExpressionsString(it)
                }

                val expFunc = functions[leastPrecedent]!!
                val argCount = if (arity > 0) arity else if (expFunc.maxArity < 0) expFunc.minArity else expFunc.maxArity

                expression = Expression(expType, string, leastPrecedent, argCount)
                setExpressionChildren(expression, string, leastPrecedent, argCount)
            }

            return expression
        }
    }
}


enum class ExpressionType
{
    Exp,
    Eqn,
    InEql,
    Const,
    Variable,
    Conditional
}