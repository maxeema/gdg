package maxeem.america.common

class Consumable<T>(private var data: T?) {

    fun consume() = data?.also { data = null }

}