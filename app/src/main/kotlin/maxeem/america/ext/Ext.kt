package maxeem.america.ext

import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import maxeem.america.app
import maxeem.america.common.ConsumableEvent

/**
 * Extensions
 */

val Any.hash get() = hashCode()
val Any.name get() = this::class.java.simpleName

fun Int.asString() = app.getString(this)
fun Int.asString(vararg args: Any) = app.getString(this, *args)
fun Int.asColor() = ContextCompat.getColor(app, this)

fun <T> MutableLiveData<T>.asImmutable() = this as LiveData<T>
fun <T> LiveData<T>.asMutable()          = this as MutableLiveData<T>
fun <T> LiveData<T>.asConsumable()        = this as ConsumableEvent

fun <K, V> Map<K,V>.asMutable() = this as MutableMap
