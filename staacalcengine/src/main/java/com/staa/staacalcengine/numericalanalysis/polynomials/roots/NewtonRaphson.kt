package com.staa.staacalcengine.numericalanalysis.polynomials.roots

private fun generateRecursionString(f: String, fPrime: String, maxError: Double, maxTrials: Int, fixedTrials: Int): Pair<String, String>
{
    // f is expected to be the normal function name (with x as result)
    return Pair("fixedPointFor$f", "fixedPointFor$f(i,xi) = {" +
            "print(fixedPointFor$f,iteration=_(i)___x=_(xi)___f(x)=_($f(xi))___):$f(xi);" + // never called
            "i=$fixedTrials:$f(xi);" +
            "i>$maxTrials:error(The_fixed_point_approximation_has_exceeded_the_max_number_of_recursions);" +
            "abs(xi - $f(xi))<=$maxError:xi;" +
            "fixedPointFor$f(i+1,$f(xi))}")
}