package com.staa.staacalcengine.functions

import com.staa.staacalcengine.basic.*
import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.ExpressionType.*
import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.expressions.zero
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow

// name, simplification of complex expression
// in order of precedence from lowest to highest
// user-defined functions can be added here at runtime
val functions = mutableMapOf(
        Pair("<=", ExpFunc(::genericBinaryComparison, 2, 2)),
        Pair(">=", ExpFunc(::genericBinaryComparison, 2, 2)),
        Pair(">", ExpFunc(::genericBinaryComparison, 2, 2)),
        Pair("<", ExpFunc(::genericBinaryComparison, 2, 2)),
        Pair("=", ExpFunc(::genericBinaryComparison, 2, 2)),
        Pair("+", ExpFunc(::plus, 2, 2)),
        Pair("summation", ExpFunc(::summation, 1, -1)),
        Pair("-", ExpFunc(::minus, 2, 2)),
        Pair("*", ExpFunc(::multiply, 2, 2)),
        Pair("product", ExpFunc(::product, 1, -1)),
        Pair("/", ExpFunc(::divide, 2, 2)),
        Pair("%", ExpFunc(::modulus, 2, 2)),
        Pair("^", ExpFunc(::pow, 2, 2)),
        Pair("pow", ExpFunc(::pow, 2, 2)),
        Pair("factorial", ExpFunc(::factorial, 1, 1)),
        Pair("permutation", ExpFunc(::permutation, 2, 2)),
        Pair("combination", ExpFunc(::combination, 2, 2)),
        Pair("sin", ExpFunc(::sin, 2, 2)),
        Pair("asin", ExpFunc(::asin, 2, 2)),
        Pair("cos", ExpFunc(::cos, 2, 2)),
        Pair("acos", ExpFunc(::acos, 2, 2)),
        Pair("tan", ExpFunc(::tan, 2, 2)),
        Pair("atan", ExpFunc(::atan, 2, 2)),
        Pair("sinh", ExpFunc(::sinh, 2, 2)),
        Pair("asinh", ExpFunc(::asinh, 2, 2)),
        Pair("cosh", ExpFunc(::cosh, 2, 2)),
        Pair("acosh", ExpFunc(::acosh, 2, 2)),
        Pair("tanh", ExpFunc(::tanh, 2, 2)),
        Pair("atanh", ExpFunc(::atanh, 2, 2)),
        Pair("abs", ExpFunc(::abs, 1, 1)),
        Pair("error", ExpFunc(::error, 0, 0)),
        Pair("print", ExpFunc(::print, 0, 2)),
        Pair("differentiate", ExpFunc(::differentiate, 2, 2)))


fun verifyArity(error: String, actual: Int, min: Int = 1, max: Int = -1)
{
    val valid = ((min in 1..actual) && ((min > 0 && max <= 0))) ||
            ((min in 1..actual && actual in 1..max) && (min > 0 && max > 0)) ||
            ((actual in 1..max) && (min <= 0 && max > 0))

    if (!valid)
    {
        throw UnsupportedOperationException(error)
    }
}

fun differentiate(expression: Expression, variables: Map<String, Double>): Expression
{
    verifyArity("The differentiate function requires exactly two children, " +
                        "the first of which is a registered function of one variable. " +
                        "The second is the argument at which point the differential is taken",
                expression.children.size, 2, 2)


    val e0 = expression.children[0]!!
    val e1 = expression.children[1]!!.evaluate(*variables.toList().toTypedArray())

    val f = functions[e0.str]
    if (e1.type == Const && f != null && f.minArity == 1 && f.maxArity == 1)
    {
        val x = (e1.str.toDoubleOrNull() ?: variables[e1.str]!!)
        val h = 1e-10
        val name = e0.str

        // this uses the five point central difference formula
        // 8f(x+h)-8f(x-h)-f(x+2h)+f(x-2h)
        // 12h

        val a = Expression.fromString("8*$name(${x + h})")
        val b = Expression.fromString("8*$name(${x - h})")
        val c = Expression.fromString("$name(${x + (2 * h)})")
        val d = Expression.fromString("$name(${x - (2 * h)})")


        val arr = variables.toList().toTypedArray()
        /*val double = */
        return ((a - b - c + d) / Expression.createConst("${12 * h}")).evaluate(*arr)
        /*.str.toDouble()
        val result = round(double * 1e5) * 1e-5
        return Expression.createConst(result.toString())*/
    }
    else
    {
        throw UnsupportedOperationException(
                "The differentiate function does not currently support multivariate functions and requires the function to have been added")
    }
}


fun error(expression: Expression, variables: Map<String, Double>): Expression
{
    throw UnsupportedOperationException(expression.str)
}

fun print(expression: Expression, variables: Map<String, Double>): Expression
{
    verifyArity("The print function requires at least zero and at most 2 children",
                expression.children.size, 0, 2)
    val value = expression.str
    val id: String
    val varName: String

    when
    {
        expression.children.isEmpty() ->
        {
            id = UUID.randomUUID().toString()
            varName = "default"
        }
        expression.children.size == 1 ->
        {
            id = UUID.randomUUID().toString()
            varName = expression.children[0]!!.str
        }
        else                          ->
        {
            id = expression.children[0]!!.str
            varName = expression.children[1]!!.str
        }
    }

    PrintCache[id, varName] = value
    return zero
}


fun plus(expression: Expression, variables: Map<String, Double>): Expression
{
    return genericBinary("+",
                         { e0String, e1String ->
                             val e0Double = e0String.toDoubleOrNull()
                                     ?: variables[e0String]
                             val e1Double = e1String.toDoubleOrNull()
                                     ?: variables[e1String]

                             if (e0Double != null && e1Double != null)
                             {
                                 (e0Double + e1Double).toString()
                             }
                             else
                             {
                                 "${e0Double?.toString() ?: e0String}+${e1Double?.toString()
                                         ?: e1String}"
                             }
                         },
                         expression,
                         variables)
}


fun minus(expression: Expression, variables: Map<String, Double>): Expression
{
    return genericBinary("-",
                         { e0String, e1String ->
                             val e0Double = e0String.toDoubleOrNull()
                                     ?: variables[e0String]
                             val e1Double = e1String.toDoubleOrNull()
                                     ?: variables[e1String]

                             if (e0Double != null && e1Double != null)
                             {
                                 (e0Double - e1Double).toString()
                             }
                             else
                             {
                                 "${e0Double?.toString() ?: e0String}-${e1Double?.toString()
                                         ?: e1String}"
                             }
                         },
                         expression,
                         variables)
}

fun divide(expression: Expression, variables: Map<String, Double>): Expression
{
    return genericBinary("/",
                         { e0String, e1String ->
                             val e0Double = e0String.toDoubleOrNull()
                                     ?: variables[e0String]
                             val e1Double = e1String.toDoubleOrNull()
                                     ?: variables[e1String]

                             if (e0Double != null && e1Double != null)
                             {
                                 (e0Double / e1Double).toString()
                             }
                             else
                             {
                                 "${e0Double?.toString() ?: e0String}/${e1Double?.toString()
                                         ?: e1String}"
                             }
                         },
                         expression,
                         variables)
}

fun multiply(expression: Expression, variables: Map<String, Double>): Expression
{
    return genericBinary("*",
                         { e0String, e1String ->
                             val e0Double = e0String.toDoubleOrNull()
                                     ?: variables[e0String]
                             val e1Double = e1String.toDoubleOrNull()
                                     ?: variables[e1String]

                             if (e0Double != null && e1Double != null)
                             {
                                 (e0Double * e1Double).toString()
                             }
                             else
                             {
                                 "${e0Double?.toString() ?: e0String}*${e1Double?.toString()
                                         ?: e1String}"
                             }
                         },
                         expression,
                         variables)
}

fun modulus(expression: Expression, variables: Map<String, Double>): Expression
{
    return genericBinary("%",
                         { e0String, e1String ->
                             val e0Double = e0String.toDoubleOrNull()
                                     ?: variables[e0String]
                             val e1Double = e1String.toDoubleOrNull()
                                     ?: variables[e1String]

                             if (e0Double != null && e1Double != null)
                             {
                                 (e0Double % e1Double).toString()
                             }
                             else
                             {
                                 "${e0Double?.toString() ?: e0String}%${e1Double?.toString()
                                         ?: e1String}"
                             }
                         },
                         expression,
                         variables)
}


fun summation(expression: Expression, variables: Map<String, Double>): Expression
{
    if (expression.children.size == 1)
    {
        return expression.children[0]!!
    }

    // If there is not exactly one term signifying that a summation has been simplified to just one term, then there must be at least two
    verifyArity("There must be at least two terms in a summation", expression.children.size,
                min = 2)
    val args = expression.children.map {
        it!!.evaluate(
                *variables.toList().toTypedArray())
    }
    val numArgsSum = args.filter { it.type == Const }.sumByDouble {
        it.str.toDoubleOrNull()
                ?: variables[it.str]!!
    }
    val varArgs = args.filter { it.type == Exp || it.type == Variable }
    if (varArgs.any())
    {
        val constIsZero = abs(numArgsSum) < 1e-13
        val start = if (constIsZero) 0 else 1
        // start must be either of 0  or 1 based on whether const is zero

        val e = Expression(Exp,
                           "($numArgsSum+${varArgs.joinToString(
                                   separator = "+",
                                   transform = { it.toString() })})",
                           "summation",
                           varArgs.size + start)

        if (!constIsZero)
        {
            e.children[0] = Expression(
                    Const,
                    numArgsSum.toString(),
                    numArgsSum.toString(),
                    0)
        }

        for (i in start until varArgs.size + start)
        {
            e.children[i] = varArgs[i - start]
            e.variables.addAll(varArgs[i - start].variables)
        }
        return e
    }
    else
    {
        return Expression.createConst(numArgsSum.toString())
    }
}

fun product(expression: Expression, variables: Map<String, Double>): Expression
{
    if (expression.children.size == 1)
    {
        return expression.children[0]!!
    }

    // If there is not exactly one term signifying that a product has been simplified to just one term, then there must be at least two

    verifyArity("There must be at least two terms in a product", expression.children.size, min = 2)
    val args = expression.children.map {
        it!!.evaluate(
                *variables.toList().toTypedArray())
    }
    val numArgs = args.filter { it.type == Const }
    var product = 1.0

    for (it in numArgs)
    {
        product *= it.str.toDoubleOrNull()
                ?: variables[it.str]!!
    }

    val varArgs = args.filter { it.type == Exp || it.type == Variable }
    if (varArgs.any())
    {
        val e = Expression(
                Exp,
                "($product*${varArgs.joinToString(
                        separator = "*",
                        transform = { it.toString() })})",
                "summation",
                varArgs.lastIndex + 2)
        e.children[0] = Expression(
                Const,
                product.toString(),
                product.toString(),
                0)
        for (i in 0..(varArgs.lastIndex + 1))
        {
            e.children[i + 1] = varArgs[i]
        }
        return e
    }
    else
    {
        return Expression(
                Const,
                product.toString(),
                product.toString(),
                0)
    }
}


fun abs(expression: Expression, variables: Map<String, Double>): Expression
{
    verifyArity("The absolute value function is unary", expression.children.size, min = 1, max = 1)
    val e = expression.children[0]!!.evaluate(*variables.toList().toTypedArray())


    val res = (e.str.toDoubleOrNull() ?: variables[e.str])?.absoluteValue?.toString()

    return if (res != null)
    {
        Expression.createConst(res)
    }
    else
    {
        val eString = e.children[0].toString()
        val expr = Expression(Exp,
                              "abs($eString)",
                              "abs($eString)",
                              1)
        expr.children[0] = e
        expr
    }
}


fun factorial(expression: Expression, variables: Map<String, Double>): Expression
{
    verifyArity("Factorial is a unary operator", expression.children.size, min = 1, max = 1)

    val e = expression.children[0]!!.evaluate(*variables.toList().toTypedArray())

    if (e.type == Const)
    {
        val res = (e.str.toIntOrNull()?.factorial()
                ?: variables[e.str]!!.toInt().factorial()).toString()
        return Expression.createConst(res)

    }
    else
    {
        val eString = e.toString()
        val expr = Expression(Exp,
                              "factorial($eString)",
                              "factorial($eString)",
                              1)
        expr.children[0] = e
        return expr
    }
}

fun permutation(expression: Expression, variables: Map<String, Double>): Expression
{
    return genericBinary("permutation",
                         { e0String, e1String ->
                             val e0Int = e0String.toIntOrNull()
                                     ?: variables[e0String]?.toInt()
                             val e1Int = e1String.toIntOrNull()
                                     ?: variables[e1String]?.toInt()

                             if (e0Int != null && e1Int != null)
                             {
                                 (e0Int permutation e1Int).toString()
                             }
                             else
                             {
                                 "permutation(${e0Int?.toString() ?: e0String},${e1Int?.toString()
                                         ?: e1String})"
                             }
                         },
                         expression,
                         variables)
}

fun combination(expression: Expression, variables: Map<String, Double>): Expression
{
    return genericBinary("combination",
                         { e0String, e1String ->
                             val e0Int = e0String.toIntOrNull()
                                     ?: variables[e0String]?.toInt()
                             val e1Int = e1String.toIntOrNull()
                                     ?: variables[e1String]?.toInt()

                             if (e0Int != null && e1Int != null)
                             {
                                 (e0Int combination e1Int).toString()
                             }
                             else
                             {
                                 "combination(${e0Int?.toString() ?: e0String},${e1Int?.toString()
                                         ?: e1String})"
                             }
                         },
                         expression,
                         variables)
}

fun genericBinary(name: String,
                  constFunc: (String, String) -> String,
                  expression: Expression,
                  variables: Map<String, Double>): Expression
{
    verifyArity("$name is a binary operator", expression.children.size, min = 2, max = 2)

    val e0 = expression.children[0]!!.evaluate(*variables.toList().toTypedArray())
    val e1 = expression.children[1]!!.evaluate(*variables.toList().toTypedArray())

    if (e0.type == e1.type && e1.type == Const)
    {
        val res = constFunc(e0.str, e1.str)
        return Expression.createConst(res)
    }
    else
    {
        val e0String = e0.toString()
        val e1String = e1.toString()

        val e = if (name.matches(Regex("\\w+")))
        {
            Expression(Exp,
                       "$name($e0String, $e1String)",
                       name,
                       2)
        }
        else
        {
            Expression(Exp,
                       "($e0String$name$e1String)",
                       name,
                       2)
        }
        e.children[0] = e0
        e.children[1] = e1

        e.variables.clear()
        e.variables.addAll(e.children.flatMap { it!!.variables })
        return e
    }
}

fun genericBinaryComparison(expression: Expression, variables: Map<String, Double>): Expression
{
    return genericBinary(expression.function,
                         { e0String, e1String ->
                             val e0Double = e0String.toDoubleOrNull()
                                     ?: variables[e0String]
                             val e1Double = e1String.toDoubleOrNull()
                                     ?: variables[e1String]

                             if (e0Double != null && e1Double != null)
                             {
                                 if (when (expression.function)
                                         {
                                             ">"  -> (e0Double > e1Double)
                                             "<"  -> (e0Double < e1Double)
                                             "="  -> (e0Double == e1Double)
                                             ">=" -> (e0Double >= e1Double)
                                             else -> (e0Double <= e1Double)
                                         }) "1"
                                 else "0"
                             }
                             else
                             {
                                 "${e0Double?.toString()
                                         ?: e0String}${expression.function}${e1Double?.toString()
                                         ?: e1String}"
                             }
                         },
                         expression,
                         variables)
}

fun sin(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("sin", Double::sin, expression, variables)
}


fun cos(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("cos", Double::cos, expression, variables)
}


fun tan(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("tan", Double::tan, expression, variables)
}


fun asin(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("asin", Double::asin, expression, variables)
}


fun acos(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("acos", Double::acos, expression, variables)
}


fun atan(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("atan", Double::atan, expression, variables)
}


fun sinh(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("sinh", Double::sinh, expression, variables)
}


fun cosh(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("cosh", Double::cosh, expression, variables)
}


fun tanh(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("tanh", Double::tanh, expression, variables)
}


fun asinh(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("asinh", Double::asinh, expression, variables)
}


fun acosh(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("acosh", Double::acosh, expression, variables)
}


fun atanh(expression: Expression, variables: Map<String, Double>): Expression
{
    return trig("atanh", Double::atanh, expression, variables)
}


fun trig(name: String, func: (Double, DegRadGrad) -> Double, expression: Expression, variables: Map<String, Double>): Expression
{
    verifyArity("$name is a binary operator (the number and flag for deg/rad/grad)",
                expression.children.size, min = 2, max = 2)

    val e0 = expression.children[0]!!.evaluate(*variables.toList().toTypedArray())
    val e1 = expression.children[1]!!

    val drg = DegRadGrad.values().find { it.name.equals(e1.str, true) }
            ?: throw UnsupportedOperationException(
                    "The second argument of $name must be one of (${DegRadGrad.values().joinToString(
                            transform = { it.name })})")


    if (e0.type == Const)
    {
        val res = func((e0.str.toDoubleOrNull() ?: variables[e0.str]!!), drg).toString()
        return Expression.createConst(res)
    }
    else
    {
        val e0String = e0.toString()
        val e1String = e1.toString()

        val e = Expression(Exp,
                           "$name($e0String, $e1String)",
                           "$name($e0String, $e1String)",
                           2)
        e.children[0] = e0
        e.children[1] = e1

        e.variables.clear()
        e.variables.addAll(e.children.flatMap { it!!.variables })
        return e
    }
}

fun pow(expression: Expression, variables: Map<String, Double>): Expression
{
    return genericBinary("pow",
                         { e0String, e1String ->
                             val e0Double = e0String.toDoubleOrNull()
                                     ?: variables[e0String]
                             val e1Double = e1String.toDoubleOrNull()
                                     ?: variables[e1String]

                             if (e0Double != null && e1Double != null)
                             {
                                 (e0Double.pow(e1Double)).toString()
                             }
                             else
                             {
                                 "pow(${e0Double?.toString() ?: e0String},${e1Double?.toString()
                                         ?: e1String})"
                             }
                         },
                         expression,
                         variables)
}