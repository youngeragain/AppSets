package xcj.app.userinfo.util

class RealContentCheckChain:ContentCheckChain{
    private val mContentCheckers = mutableListOf<ContentChecker>()
    private var mIndex = 0
    private var mIndexContentCheckerProceed = false

    fun setAddContentCheckers(contentCheckers:List<ContentChecker>){
        mContentCheckers.addAll(contentCheckers)
    }
    fun setIndex(index:Int){
        mIndex = index
    }
    override fun <I, O> proceed(input: I?): O? {
        if(mIndex>=mContentCheckers.size)
            throw Exception()
        mIndexContentCheckerProceed = true
        val nextCheckChain = RealContentCheckChain().apply {
            this@RealContentCheckChain.setAddContentCheckers(mContentCheckers)
            this@RealContentCheckChain.setIndex(mIndex+1)
        }

        val contentChecker = mContentCheckers.get(mIndex)
        val canPass = contentChecker.check<I>(input)
        if(!canPass)
            return null
        val transformedInput = contentChecker.transform<I, I>(input)
        if(mIndex+1==mContentCheckers.size)
            return transformedInput as? O
        return nextCheckChain.proceed<I, O>(transformedInput)
    }
}