package maxeem.america.ext

import maxeem.america.util.Utils

/**
 * String extensions
 */

fun String.fromHtml() = Utils.fromHtml(this)

fun String.mask(c : Char = '_') = kotlin.text.String(kotlin.CharArray(length) { c })

fun String.fixFileName() =
    replace("[^a-zA-Z0-9.-]".toRegex(), " ").replace("\\s{2,}".toRegex(), " ")

private val tickerDots = arrayOf("...", "   ", ".  ", ".. ")
/** generate string like: "Loading...", "Loading..", "Loading.", "Loading" */
fun String.toTicker(tik: Int) = this + tickerDots[tik % tickerDots.size]

private val PUNCTUATION = listOf(", ", "; ", ": ", " ")
/**
 * Truncate long text with a preference for word boundaries and without trailing punctuation.
 */
fun String.smartTruncate(length: Int): String {
    val words = split(" ")
    var added = 0
    var hasMore = false
    val builder = StringBuilder()
    for (word in words) {
        if (builder.length > length) {
            hasMore = true
            break
        }
        builder.append(word)
        builder.append(" ")
        added += 1
    }

    PUNCTUATION.map {
        if (builder.endsWith(it)) {
            builder.replace(builder.length - it.length, builder.length, "")
        }
    }

    if (hasMore) {
        builder.append("...")
    }
    return builder.toString()
}