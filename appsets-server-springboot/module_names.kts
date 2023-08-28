package xcj.model.definations

object ModuleNames{
    const val UserServer = "UserServer"
    const val PayServer = "PayServer"
    const val OrderServer = "OrderServer"
    const val ImServer = "ImServer"
    const val Share = "Share"
    const val RetrofitStater = "RetrofitStater"
    const val Processor = "Processor"
    const val Launcher = "Launcher"
    const val AppSetsWeb = "AppSetsWeb"
    fun isSpringBootModule(name:String):Boolean{
        return (name == UserServer ||
                name == PayServer ||
                name == OrderServer ||
                name == ImServer ||
                name == RetrofitStater)
    }
}