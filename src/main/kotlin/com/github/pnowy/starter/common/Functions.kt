package com.github.pnowy.starter.common

/**
 * Returns `true` if enum T contains an entry with the specified name.
 */
inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
    return enumValues<T>().any { it.name.toLowerCase() == name.toLowerCase()}
}
