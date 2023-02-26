package com.staa.staacalcengine.functions

import com.staa.staacalcengine.expressions.Expression

object FunctionCache
{
    private val map = mutableMapOf<Pair<String, Map<String, String>>, Expression>()

    operator fun get(name: String, state: Map<String, String>) = map[Pair(name, state)]
    operator fun set(name: String, state: Map<String, String>, value: Expression)
    {
        if (name != "print" && name != "error")
        {
            map[Pair(name, state)] = value
        }
    }

    fun remove(name: String)
    {
        val keys = map.keys.filter { it.first == name }
        for (k in keys)
        {
            map.remove(k)
        }
    }

    fun remove(name: String, state: Map<String, String>)
    {
        map.remove(Pair(name,state))
    }

    fun invalidate() = map.clear()
}