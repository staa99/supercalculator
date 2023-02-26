package com.staa.staacalcengine.booleanalgebra

fun Boolean.nand(b: Boolean) = !(this and b)
fun Boolean.nor(b: Boolean) = !(this or b)
fun Boolean.xnor(b: Boolean) = !(this xor b)