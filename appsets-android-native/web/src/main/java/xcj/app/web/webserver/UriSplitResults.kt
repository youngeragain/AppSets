package xcj.app.web.webserver

class UriSplitResults(
    val rawUri:String,
    val results:List<UriSplitResult>?,
){

    override fun toString(): String {
        return results.toString()
    }
    data class UriSplitResult(val fragment:String, val isDynamicPart:Boolean=false)

    fun getFullPath():String{
        if(results==null)
            return rawUri
        return results.joinToString(""){ it.fragment }
    }

    fun isSuccessful():Boolean{
        return !results.isNullOrEmpty()
    }


    companion object{
        fun slice(input:String): UriSplitResults {
            var resultList:MutableList<UriSplitResult>? = mutableListOf()
            var startCharFound = false
            var endCharFound = false
            val fragmentBuilder:StringBuilder = StringBuilder()
            var isValidateUri:Boolean = true
            for(c in input){
                if(c!='{'&&c!='}'){
                    fragmentBuilder.append(c)
                }
                if(c=='{'){
                    startCharFound = true
                    if(fragmentBuilder.isEmpty())
                        continue
                    resultList?.add(UriSplitResult(fragmentBuilder.toString(), false))
                    fragmentBuilder.clear()
                }else if(c=='}'){
                    endCharFound = true
                    if(startCharFound&&endCharFound){
                        startCharFound = false
                        endCharFound = false
                        if(fragmentBuilder.contains("/")){
                            //nothing to do
                            println("******** not validate uri *********")
                            isValidateUri = false
                            break
                        }else if(fragmentBuilder.isEmpty()){
                            continue
                        }else{
                            resultList?.add(UriSplitResult(fragmentBuilder.toString(), true))
                        }
                    }
                    fragmentBuilder.clear()
                }
            }
            if(fragmentBuilder.isNotEmpty()){
                resultList?.add(UriSplitResult(fragmentBuilder.toString(), false))
            }
            if(!isValidateUri)
                resultList = null
            return UriSplitResults(input, resultList)
        }
    }
}