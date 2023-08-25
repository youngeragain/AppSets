package xcj.app.userinfo.qr

abstract class State<T>{
    abstract val value:T?
}
interface Composer

val compsers = mutableListOf<Composer>()

class MutableState<T>: State<T>(){
    override var value: T? = null
        set(value) {
            field = value
            onChanged(value)
        }
    fun onChanged(value: T?){

    }
}