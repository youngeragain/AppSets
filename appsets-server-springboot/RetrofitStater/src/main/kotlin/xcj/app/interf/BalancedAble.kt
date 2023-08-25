package xcj.app.interf


interface BalancedAble<in I, out O> {
    fun balance(i:I):O
}