package xcj.app.core.pipeline

interface Pipeline<I,O>{
    fun test(i:I):O
    fun <O1> then(action:(O)->O1):Pipeline<I, O1>{
        val pipeline:Pipeline<I, O1> = object :Pipeline<I, O1>{
            override fun test(i: I): O1 {
                return action(this@Pipeline.test(i))
            }
        }
        return pipeline
    }
}


fun <I> I.piping():Pipeline<I,I>{
    return object :Pipeline<I, I>{
        override fun test(i: I): I {
            return i
        }
    }
}


fun main() {
    "Hello".piping().then {
        "$it, world"
    }.then {
        println("${it}, are you ok?")
    }
}