package com.staa.staacalcengine.booleanalgebra

fun pointArrayEquals(o1: Array<Point>, o2: Array<Point>) = o1
        .zip(o2)
        { x, y ->
            x == y
        }
        .all { it }

fun pointArrayDistinctSelector(pointArray: Array<Point>): String
{
    return pointArray.joinToString()
}

fun pointArrayDistinctSelector(pointArray: Collection<Point>): String
{
    return pointArray.joinToString()
}

fun pointArrayDistinctSelector(pointArray: Iterable<Point>): String
{
    return pointArray.joinToString()
}