package maxeem.america.ext

/**
 * Core extensions
 */

fun Any?.isnil() = this == null
fun Any?.notnil() = this != null

inline infix fun <T, R> T.nil(action: (T)->R)
        = if (isnil()) action(this) else null

inline infix fun <R> Boolean?.tru(action: ()->R)
        = if (this == true) action() else null

infix fun <R> Boolean.fals(action: ()->R)
        = !this tru action
