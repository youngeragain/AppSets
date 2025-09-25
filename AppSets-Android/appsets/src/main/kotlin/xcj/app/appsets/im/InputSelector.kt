package xcj.app.appsets.im

object InputSelector {
    fun isComplex(inputSelector: Int): Boolean {
        return inputSelector > AD
    }

    const val NONE = 0
    const val TEXT = 1
    const val LOCATION = 2
    const val HTML = 3
    const val AD = 4

    const val IMAGE = 5

    const val VIDEO = 6
    const val MUSIC = 7
    const val VOICE = 8
    const val FILE = 9
}