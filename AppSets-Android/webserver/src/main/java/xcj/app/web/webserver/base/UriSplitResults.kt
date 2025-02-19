package xcj.app.web.webserver.base

import xcj.app.starter.android.util.PurpleLogger


class UriSplitResults(
    val rawUri: String,
    val results: List<UriSplitResult>?,
) {

    override fun toString(): String {
        return getFullPath()
    }


    fun getFullPath(): String {
        if (results == null) {
            return rawUri
        }
        return results.joinToString("") { it.fragment }
    }

    fun isSuccessful(): Boolean {
        return !results.isNullOrEmpty()
    }


    companion object {
        private const val TAG = "UriSplitResults"
        fun slice(input: String): UriSplitResults? {
            if (!input.contains('{') && !input.contains('}')) {
                return null
            }
            var resultList: MutableList<UriSplitResult> = mutableListOf()
            var startCharFound = false
            var endCharFound = false
            val fragmentBuilder: StringBuilder = StringBuilder()
            var isValidateUri: Boolean = true
            runCatching {
                for (char in input) {
                    if (char != '{' && char != '}') {
                        fragmentBuilder.append(char)
                    }
                    if (char == '{') {
                        startCharFound = true
                        if (fragmentBuilder.isEmpty()) {
                            continue
                        }
                        resultList.add(UriSplitResult(fragmentBuilder.toString(), false))
                        fragmentBuilder.clear()
                    } else if (char == '}') {
                        endCharFound = true
                        if (startCharFound && endCharFound) {
                            startCharFound = false
                            endCharFound = false
                            if (fragmentBuilder.contains("/")) {
                                //nothing to do
                                println("******** not validate uri *********")
                                isValidateUri = false
                                break
                            } else if (fragmentBuilder.isEmpty()) {
                                continue
                            } else {
                                resultList.add(UriSplitResult(fragmentBuilder.toString(), true))
                            }
                        }
                        fragmentBuilder.clear()
                    }
                }
                if (fragmentBuilder.isNotEmpty()) {
                    resultList.add(UriSplitResult(fragmentBuilder.toString(), false))
                }
            }.onFailure {
                PurpleLogger.current.d(TAG, "slice for input:$input, failed!, ${it.message}")
                return null
            }
            if (!isValidateUri) {
                return null
            }
            return UriSplitResults(input, resultList)
        }
    }
}