package org.radarbase.appserver.util

import kotlin.reflect.KProperty1

internal inline fun <reified T : Any> T.equalTo(other: Any?, vararg fields: KProperty1<T, *>): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as T
    return fields.all { field -> field.get(this) == field.get(other) }
}

internal inline fun <reified T : Any> T.stringRepresentation(vararg fields: KProperty1<T, *>): String {
    return "${this::class.simpleName}(${fields.joinToString(", ") { field ->
        val propertyName = field.name
        "$propertyName=${field.get(this)}"
    }})"
}
