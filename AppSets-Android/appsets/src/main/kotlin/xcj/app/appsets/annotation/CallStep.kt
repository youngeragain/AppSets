package xcj.app.appsets.annotation

@Retention(AnnotationRetention.SOURCE)
annotation class CallStep(val step: Int, val before: String = "", val after: String = "")