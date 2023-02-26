package com.staa.staacalcengine.basic

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class BasicCalculatorTests
{
    @Test
    fun factorial_isCorrect()
    {
        assertEquals(6, 3.factorial())
        assertEquals(2, 2.factorial())
        assertEquals(1, 1.factorial())
        assertEquals(1, 0.factorial())
    }


    @Test
    fun permutation_isCorrect()
    {
        assertEquals(1, 5 permutation 0)
        assertEquals(20, 5 permutation 2)
    }

    @Test
    fun combination_isCorrect()
    {
        assertEquals(1, 5 combination 0)
        assertEquals(10, 5 combination 2)
    }


    inline fun trigCorrect(func: (Double, DegRadGrad) -> Double, inverse: (Double, DegRadGrad) -> Double)
    {
        val count = 5

        for (i in 1..count)
        {
            // generate a random number between 0 and 0.25 to correspond to an acute angle positive or negative [-pi/2, pi/2]
            val rand = Math.random() * 0.25


            val deg = rand * 360
            val grad = rand * 400
            val rad = rand * 2 * PI

            val sdeg = func(deg, DegRadGrad.Deg)
            val sgrad = func(grad, DegRadGrad.Grad)
            val srad = func(rad, DegRadGrad.Rad)

            val rdeg = inverse(sdeg, DegRadGrad.Deg)
            val rrad = inverse(srad, DegRadGrad.Rad)
            val rgrad = inverse(sgrad, DegRadGrad.Grad)

            assertEquals(sdeg, sgrad, 1e-6)
            assertEquals(sgrad, srad, 1e-6)

            // change the value to be within the range of 0 -> 360
            assertEquals((360 + deg) % 360, rdeg, 1e-6)

            // change the value to be within the range of 0 -> 2pi
            assertEquals(((2 * PI) + rad) % (2 * PI), rrad, 1e-6)

            // change the value to be within the range of 0 -> 400
            assertEquals((400 + grad) % 400, rgrad, 1e-6)
        }
    }

    @Test
    fun sin_isCorrect()
    {
        return trigCorrect(Double::sin, Double::asin)
    }

    @Test
    fun cos_isCorrect()
    {
        return trigCorrect(Double::cos, Double::acos)
    }

    @Test
    fun tan_isCorrect()
    {
        return trigCorrect(Double::tan, Double::atan)
    }


    @Test
    fun sinh_isCorrect()
    {
        return trigCorrect(Double::sinh, Double::asinh)
    }


    @Test
    fun cosh_isCorrect()
    {
        return trigCorrect(Double::cosh, Double::acosh)
    }


    @Test
    fun tanh_isCorrect()
    {
        return trigCorrect(Double::tanh, Double::atanh)
    }


    @Test
    fun rootIsCorrect()
    {
        assertEquals(3.0, 9.0.root(), 1e-6)
        assertEquals(3.0, 27.0 root 3.0, 1e-6)
    }

    @Test
    fun drgConversion_IsCorrect()
    {
        assertEquals(180.0, (-PI).convertDRG(DegRadGrad.Rad, DegRadGrad.Deg), 1e-6)
        assertEquals(PI, 180.0.convertDRG(DegRadGrad.Deg, DegRadGrad.Rad), 1e-6)
    }
}