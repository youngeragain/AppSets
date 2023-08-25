package xcj.app.core.foundation


abstract class Group<I> {
    open var name:String = ""
    var expand: Boolean = true
    var subItems:MutableList<I>? = null
}

class Level6(val name: String)

class Level5(): Group<Level6>()

class Level4(): Group<Level5>()

class Level3(): Group<Level4>()

class Level2(): Group<Level3>()

class Level1(): Group<Level2>()

class Level0(): Group<Level1>()