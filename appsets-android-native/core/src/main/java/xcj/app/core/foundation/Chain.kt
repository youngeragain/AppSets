package xcj.app.core.foundation

import xcj.app.core.foundation.Chain.Companion.TYPE_ONE_WAY_NEXT
import xcj.app.core.foundation.Chain.Companion.TYPE_ONE_WAY_PREVIOUS
import xcj.app.core.foundation.Chain.Companion.TYPE_TWO_WAY
import xcj.app.core.foundation.Chain.Companion.TYPE_TWO_WAY_CLOSED

interface Chain<T> {
    var previous:T?
    var next:T?
    fun hasNext():Boolean = next!=null
    fun hasPrevious():Boolean = previous!=null
    fun deconstruction(){
        val first = this
        val all = mutableListOf<Chain<*>>()
        all.add(first)
        var next: Chain<*>? = first.next as? Chain<*>
        do {
            if (next != null) {
                all.add(next)
                next = next.next as? Chain<*>
            }
        } while (next != first)
        all.forEach {
            it.previous = null
            it.next = null
        }
    }
    companion object{
        const val TYPE_ONE_WAY_NEXT = 0
        const val TYPE_ONE_WAY_PREVIOUS = 1
        const val TYPE_TWO_WAY = 2
        const val TYPE_TWO_WAY_CLOSED = 3
    }
}

/**
 * 默认转换为双向链表
 * @param type
 * @see TYPE_ONE_WAY_NEXT
 * @see TYPE_ONE_WAY_PREVIOUS
 * @see TYPE_TWO_WAY
 * @see TYPE_TWO_WAY_CLOSED
 */
fun <T> List<Chain<T>>.toChain(type:Int = TYPE_TWO_WAY){
    for(i in this.indices){
        if(type != TYPE_ONE_WAY_NEXT){
            if(i < this.size-1)
                this[i].next = this[i+1] as? T
            if(i>0)
                this[i].previous = this[i-1] as? T
        }
    }
    if(type == TYPE_TWO_WAY_CLOSED && size>1){
        val last = this[size-1]
        val first = this[0]
        first.previous = last as? T
        last.next = first as? T
    }
}


