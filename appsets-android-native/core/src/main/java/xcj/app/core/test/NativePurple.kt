package xcj.app.core.test

/**
 * 提供c/c++层hook
 */
class NativePurple {
    init {
        //System.load("native_purple")
    }

    external fun call(rawCall: () -> Unit)
    external fun watch()
}
