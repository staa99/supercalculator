package com.staa.staacalcengine.formulae

import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.functions.ExpFunc
import com.staa.staacalcengine.functions.functions

// This allows functions of
fun addFunctionOrConstant(str: String)
{
    val code = Expression.fixExpressionsString(str)

    val eqIndex = code.indexOf('=')

    if (eqIndex < 0)
        throw UnsupportedOperationException("Function definition must be specified by '='")

    val namePart = code.substring(0, eqIndex)
    val def = code.substring(eqIndex + 1)

    val argList = arrayListOf<String>()
    var fname = ""
    var lastArg = ""

    var hasParen = false
    var takingParams = false
    for (c in namePart)
    {
        when
        {
            c == '('     ->
            {
                if (hasParen)
                    throw UnsupportedOperationException(
                            "A function definition must be simple. Something like f(a,b,c) not f(a,(b),c)")

                takingParams = true
                hasParen = true
            }
            c == ')'     ->
            {
                if (!hasParen)
                {
                    throw UnsupportedOperationException(
                            "A function definition must be simple. Something like f(a,b,c) not f)(a,(b),c)")
                }
                takingParams = false
                argList.add(lastArg)
            }
            c == ','     ->
            {
                if (!takingParams)
                {
                    throw UnsupportedOperationException("A function name cannot contain comma")
                }

                if (argList.any() && argList.last().isEmpty())
                {
                    throw UnsupportedOperationException(
                            "A function definition must be simple. Something like f(a,b,c) not f(a,,c)")
                }

                argList.add(lastArg)
                lastArg = ""
            }
            takingParams -> lastArg += c
            else         -> fname += c
        }
    }

    if (fname.isEmpty())
    {
        throw UnsupportedOperationException("A function name is required eg the f in f(a,b,c)")
    }

    val charRegex = Regex("[a-zA-Z]+")

    if (!fname.matches(charRegex) || argList.any { !it.matches(charRegex) })
    {
        throw UnsupportedOperationException(
                "A function/argument name must be made of letters of the English alphabet")
    }

    if (takingParams)
    {
        throw UnsupportedOperationException(
                "A function definition must be simple. Something like f(a,b,c) not f(a,b")
    }


    val result = ExpFunc({ exp, vars ->
                             if (exp.function != fname)
                             {
                                 throw UnsupportedOperationException(
                                         "The function names don't correspond. Internal error. Report to the dev on play store")
                             }

                             if (exp.children.size != argList.size || exp.children.any{it==null})
                             {
                                 throw UnsupportedOperationException(
                                         "The function has an arity of ${argList.size}. Found ${exp.children.filterNotNull().size}")
                             }

                             var formedStr = def
                             val vList = vars.toList().toTypedArray()
                             // evaluate the children before using them in the resulting expression
                             for (i in exp.children.indices)
                             {
                                 exp.children[i] = exp.children[i]!!.evaluate(
                                         *vList)

                                 formedStr = formedStr.replace(
                                         Regex("\\b${argList[i]}\\b")) {
                                     exp.children[i].toString()
                                 }
                             }

                             Expression.fromString(formedStr).evaluate(*vList)
                         }, minArity = argList.size, maxArity = argList.size)
    functions[fname] = result
}