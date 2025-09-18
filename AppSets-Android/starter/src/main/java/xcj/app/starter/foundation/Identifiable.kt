package xcj.app.starter.foundation

interface Identifiable<I> {
    val id: I

    companion object {
        fun fromString(str: String): Identifiable<String> {
            return object : Identifiable<String> {
                override val id: String
                    get() = str
            }
        }
    }
}