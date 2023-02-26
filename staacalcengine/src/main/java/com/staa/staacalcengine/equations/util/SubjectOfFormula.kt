/*
import com.staa.staacalcengine.equations.util.powerFunctions
import com.staa.staacalcengine.equations.util.productFuncs
import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.ExpressionType
import kotlin.math.min

*/
/*
package com.staa.staacalcengine.equations.util

import com.staa.staacalcengine.expressions.Expression
import com.staa.staacalcengine.expressions.ExpressionType
import com.staa.staacalcengine.expressions.evaluate
import com.staa.staacalcengine.functions.verifyArity


fun factorizeOut(expression: Expression, variable: String): Expression
{
    if (!expression.variables.contains(variable))
    {
        throw UnsupportedOperationException(
                "Both RHS and LHS are not functions of $variable. Cannot change to subject of formula")
    }

    if (expression.function == "+" || expression.function == "summation")
    {
        return factorizeOutOfSummation(expression, variable)
    }
}
*//*


private fun factorizeOutOfSummation(expression: Expression, variable: String): Expression
{
    // assumes function is + or summation

    val fx = expression.children.filter {
        (it!!.type == ExpressionType.Variable && it.str == variable) || (it.variables.contains(
                variable) && ((productFuncs.contains(
                it.function) || powerFunctions.contains(it.function))))
    }.filterNotNull().toList()

    val nfx = expression.children.filterNot {
        it!!.variables.contains(
                variable) && (it.type == ExpressionType.Variable || (productFuncs.contains(
                it.function) || powerFunctions.contains(it.function)))
    }.filterNotNull().toList()

    var power = Double.MAX_VALUE
    for (f in fx)
    {
        if (f.type == ExpressionType.Variable)
        {
            power = 1.0
            break
        }
        else if (powerFunctions.contains(f.function) && f.children[1]!!.type == ExpressionType.Const)
        {
            power = min(power, f.children[1]!!.str.toDouble())
        }
        else if (productFuncs.contains(f.function) && )
        {

        }
    }
}

*/
/*
fun changeSubjectOfFormula(expression: Expression, variable: String, vararg variables: Pair<String, Double>): Expression
{
    if (!expression.variables.contains(variable))
    {
        throw UnsupportedOperationException(
                "Both RHS and LHS are not functions of $variable. Cannot change to subject of formula")
    }

    if (!equationFunctions.contains(expression.function))
    {
        throw UnsupportedOperationException("The expression given is not an equation or inequality")
    }

    verifyArity("An equation or inequality has exactly to operands, LHS and RHS",
                expression.children.size, 2, 2)

    val lhs = expression.children[0]!!.evaluate(*variables)
    val rhs = expression.children[1]!!.evaluate(*variables)


}
*/
