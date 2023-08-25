package xcj.app.core.test

/**
 * @param designDefinitionKClass core包中预定义的Kotlin类
 * @param customKClassList 使用者自定义Kotlin类集合
 */
data class PurpleJsonDefinition(
    val designDefinitionKClass:String,
    val customKClassList: List<String>)