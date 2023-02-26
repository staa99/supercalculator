package com.staa.staacalcengine.util

import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.ExpressionType
import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.expressions.zero

/**
 * Returns a list containing first elements satisfying the given [predicate].
 *
 * @sample samples.collections.Collections.Transformations.take
 */
inline fun <T> Array<out T>.skipWhile(predicate: (T) -> Boolean): List<T>
{
    val list = ArrayList<T>()
    var broken = false
    for (item in this)
    {
        if (!broken && predicate(item))
            continue
        else broken = true

        list.add(item)
    }
    return list
}


/**
 * Returns a list containing first [n] elements.
 *
 * @sample samples.collections.Collections.Transformations.take
 */
fun <T> Array<out T>.skip(n: Int): List<T>
{
    require(n >= 0) { "Requested element count $n is less than zero." }
    if (n == 0) return toList()
    if (n >= size) return emptyList()
    if (n == size - 1) return listOf(this.last())
    val list = ArrayList<T>(size - n)
    for (i in (n + 1) until size)
    {
        list.add(this[i])
    }
    return list
}


fun <T> Array<Array<out T>>.flatMap() = flatMap { it.toList() }
fun <T> Iterable<Array<out T>>.flatMap() = flatMap { it.toList() }

fun <T : Collection<Expression>> T.sum(variables: Array<Pair<String, Double>>? = null): Expression
{
    if (!any()) return zero

    val str = joinToString(prefix = "summation(",
                           postfix = ")")

    val summation = Expression(
            ExpressionType.Exp, str, "summation", size)
    for ((i, child) in this.withIndex())
    {
        summation.children[i] = child
        summation.variables.addAll(child.variables)
    }

    return if (variables != null)
    {
        summation.evaluate(*variables)
    }
    else
    {
        summation.evaluate()
    }
}

