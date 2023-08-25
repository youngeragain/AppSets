package xcj.app.core.android.annotations

sealed interface PermissionType{
    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @kotlin.annotation.Target(AnnotationTarget.FUNCTION)
    annotation class Login(val isForce:Boolean)

    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @kotlin.annotation.Target(AnnotationTarget.FUNCTION)
    annotation class RunTimePermission(val androidRunTimePermissions:Array<String>)

    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @kotlin.annotation.Target(AnnotationTarget.FUNCTION)
    annotation class AppVersion(val min:String, val max:String)

    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @kotlin.annotation.Target(AnnotationTarget.FUNCTION)
    annotation class UserRole(val name:String)

    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @kotlin.annotation.Target(AnnotationTarget.FUNCTION)
    annotation class PreventRepeatClick(val interval:Int)
}