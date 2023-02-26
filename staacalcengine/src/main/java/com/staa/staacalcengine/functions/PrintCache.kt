package com.staa.staacalcengine.functions

object PrintCache
{
    private val map = mutableMapOf<Pair<String, String>, String>()

    operator fun get(id: String, varName:String) = map[Pair(id,varName)]
    operator fun set(id: String, varName:String, value: String)
    {
        map[Pair(id,varName)] = value
        println("$id::$varName => $value")
    }

    fun invalidate() = map.clear()
}