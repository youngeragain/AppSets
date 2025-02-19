package xcj.app.main.util;

interface ContentCheckChain {
    fun <I, O> proceed(input: I?): O?
}
