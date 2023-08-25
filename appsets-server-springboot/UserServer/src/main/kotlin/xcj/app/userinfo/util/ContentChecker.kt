package xcj.app.userinfo.util

interface ContentChecker{
    /**
     * @return 
     * true can pass
     * false cannot pass
     */
    fun <I> check(input: I?):Boolean
    fun <I, O>transform(input: I?):O?
}