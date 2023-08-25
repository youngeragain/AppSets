package xcj.app.userinfo.util;

interface ContentCheckChain {
    fun<I, O> proceed(input:I?):O?
}
