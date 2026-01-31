package xcj.app.multiplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform