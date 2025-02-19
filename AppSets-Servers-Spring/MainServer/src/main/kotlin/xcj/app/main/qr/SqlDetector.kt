package xcj.app.main.qr

class SqlDetector {
    companion object {
        const val UNSTABLE = -1
        const val STABLE = 0
    }

    fun test(premise: String, sql: String): Int {
        return STABLE
    }
}