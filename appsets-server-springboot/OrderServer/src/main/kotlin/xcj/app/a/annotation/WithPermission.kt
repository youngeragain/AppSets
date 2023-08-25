package xcj.app.a.annotation

annotation class WithPermission(
    val includeRoles:Array<String>,
    val excludeRoles:Array<String>)