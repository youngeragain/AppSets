package xcj.app.starter.android.permission

import android.content.Context

object Greeting {

    private const val TAG = "Greeting"

    private fun showSpecialPermissionDialog(
        specialPermissions: List<String>,
        reasons: Map<String, String?>
    ) {

    }

    @JvmName("GreetingYou_greeting")
    fun greeting(
        context: Context,
        map: Map<String, String>,
        granted: (List<String>) -> Unit,
        denied: (List<String>) -> Unit
    ) = map.greeting(context, granted, denied)

    fun Map<String, String?>.greeting(
        context: Context,
        granted: ((List<String>) -> Unit)? = null,
        denied: ((List<String>) -> Unit)? = null
    ) = entries.map { it.key }.greeting(context, this, granted, denied)

    fun Pair<String, String?>.greeting(
        context: Context,
        granted: ((String?) -> Unit)? = null,
        denied: ((String?) -> Unit)? = null
    ) = mapOf<String, String?>(this).greeting(
        context,
        {
            if (it.isNotEmpty()) {
                if (it.size == 1) {
                    granted?.invoke(it[0])
                } else {
                    granted?.invoke(null)
                }
            }

        },
        {
            if (it.isNotEmpty()) {
                if (it.size == 1) {
                    denied?.invoke(it[0])
                } else {
                    denied?.invoke(null)
                }
            }
        })

    fun String.greeting(
        context: Context, granted: ((String?) -> Unit)? = null,
        denied: ((String?) -> Unit)? = null
    ) =
        (this to null).greeting(context, granted, denied)

    @Synchronized
    fun List<String>.greeting(
        context: Context,
        reasons: Map<String, String?>,
        granted: ((List<String>) -> Unit)? = null,
        denied: ((List<String>) -> Unit)? = null
    ) {

    }
}