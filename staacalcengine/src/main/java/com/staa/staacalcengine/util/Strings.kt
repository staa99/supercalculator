package com.staa.staacalcengine.util

val symbolRegex = Regex("\\W+")
fun String.insert(startIndex: Int, value: String): String
{
    val sb = StringBuilder()
    sb.append(this, 0, startIndex)
    sb.append(value)
    sb.append(this, startIndex + 1, length)
    return sb.toString()
}


fun String.removeStartAndEndParenthesis(): String
{
    var string = this
    var removeStartAndEndParenthesis = string.startsWith("(") && string.endsWith(")")

    // Despite the almost n! complexity, the length of the strings are very small and the chance of actually going full is really small
    // Therefore, this should suffice
    while (removeStartAndEndParenthesis)
    {
        var depth = 0
        for (c in string)
        {
            if (c == '(') depth++
            else if (c == ')') depth--

            if (depth == 0 && c != '(' && c != ')')
            {
                removeStartAndEndParenthesis = false
                break
            }
        }

        if (removeStartAndEndParenthesis)
        {
            string = string.substringAfter('(').substringBeforeLast(')')
            removeStartAndEndParenthesis = string.startsWith("(") && string.endsWith(")")
        }
    }

    return string
}

inline fun <FuncType> getLeastPrecedentOperator(str: String, functions: Iterable<FuncType>, fix: (String) -> String): String
{
    val string = fix(str)

    if (string.isEmpty()) throw UnsupportedOperationException("Syntax Error!")


    var depth = 0

    val rootSet = arrayListOf("")
    for (c in string)
    {
        if (c == '(')
        {
            depth++
            continue
        }

        if (c == ')')
        {
            depth--

            if (rootSet.last().trim().any())
                rootSet.add("")

            continue
        }

        if (depth == 0) // to handle the other stuff outside the parenthesis that contains the root elements
        {
            rootSet[rootSet.lastIndex] += c.toString()
        }
    }


    for (p in functions)
    {
        if (rootSet.any {
                    it.contains(
                            Regex(
                                    if(p.toString().matches(symbolRegex))"\\$p"
                                    else "\\b$p\\b"
                                 ))
                })
            return p.toString()
    }

    throw UnsupportedOperationException("Not a valid expression or function call. Function might be undefined")
}