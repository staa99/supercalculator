package com.staa.staacalcengine.basic

import kotlin.math.*

fun Int.factorial () : Int
{
    if (this == 0 || this == 1)
    {
        return 1
    }

    var fact = 2

    for (i in 3..this)
    {
        fact *= i
    }

    return fact
}


infix fun  Int.permutation (num2: Int) =
        factorial() / (this-num2).factorial()


infix fun Int.combination (num2: Int) =
        (this permutation num2)/ num2.factorial()


fun Double.sin(drg: DegRadGrad) = sin(convertDRG(drg, DegRadGrad.Rad))

fun Double.asin(drg: DegRadGrad) = asin(this).convertDRG(DegRadGrad.Rad, drg)

fun Double.cos(drg: DegRadGrad) = cos(convertDRG(drg, DegRadGrad.Rad))

fun Double.acos(drg: DegRadGrad) = acos(this).convertDRG(DegRadGrad.Rad, drg)

fun Double.tan(drg: DegRadGrad) = tan(convertDRG(drg, DegRadGrad.Rad))

fun Double.atan(drg: DegRadGrad) = atan(this).convertDRG(DegRadGrad.Rad, drg)

fun Double.sinh(drg: DegRadGrad) = sinh(convertDRG(drg, DegRadGrad.Rad))

fun Double.asinh(drg: DegRadGrad) = asinh(this).convertDRG(DegRadGrad.Rad, drg)

fun Double.cosh(drg: DegRadGrad) = cosh(convertDRG(drg, DegRadGrad.Rad))

fun Double.acosh(drg: DegRadGrad) = acosh(this).convertDRG(DegRadGrad.Rad, drg)

fun Double.tanh(drg: DegRadGrad) = tanh(convertDRG(drg, DegRadGrad.Rad))

fun Double.atanh(drg: DegRadGrad) = atanh(this).convertDRG(DegRadGrad.Rad, drg)

fun Double.root() = sqrt(this)

infix fun Double.root(r: Double) = pow(1 / r)


/***
 * Converts this number from a DegRadGrad to another. This means it does conversions between degrees, radians and gradians.
 * Example: Math.PI.convertDRG(DegRadGrad.Rad, DegRadGrad.Deg) returns 180.
 *
 * @param from The DegRadGrad from which you want to convert
 * @param to The DegRadGrad to which you want to convert
 */
fun Double.convertDRG(from: DegRadGrad, to: DegRadGrad) : Double
{
    val max = when (to)
    {
        DegRadGrad.Deg  -> 360.0
        DegRadGrad.Rad  -> 2 * PI
        DegRadGrad.Grad -> 400.0
    }

    val raw = when
    {
        from == DegRadGrad.Deg && to == DegRadGrad.Grad -> (this * 10.0 / 9)
        from == DegRadGrad.Grad && to == DegRadGrad.Deg -> (this * 9.0 / 10)
        from == DegRadGrad.Deg && to == DegRadGrad.Rad  -> (PI / 180 * this)
        from == DegRadGrad.Rad && to == DegRadGrad.Deg  -> (180 / PI * this)
        from == DegRadGrad.Grad && to == DegRadGrad.Rad -> (PI / 200 * this)
        from == DegRadGrad.Rad && to == DegRadGrad.Grad -> (200 / PI * this)
        else                                            -> this
    }

    return (max + raw) % max
}


enum class DegRadGrad
{
    Deg,
    Rad,
    Grad
}