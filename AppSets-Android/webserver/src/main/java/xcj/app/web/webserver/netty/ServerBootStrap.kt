package xcj.app.web.webserver.netty

import android.app.Application
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.web.webserver.base.DefaultControllerCollector
import xcj.app.web.webserver.interfaces.ListenersProvider
import xcj.app.web.webserver.netty.ServerBootStrap.ActionLister

class ServerBootStrap(
    private val apiPort: Int,
    private val fileApiPort: Int,
    private val listenersProvider: ListenersProvider?
) {

    interface ActionLister {
        fun onSuccess()

        fun onFailure(reason: String? = null)
    }

    companion object {
        private const val TAG = "ServerBootStrap"
    }

    private var apiServerBootStrap: ApiServerBootStrap? = null

    private var fileApiServerBootStrap: ApiServerBootStrap? = null

    suspend fun close(actionLister: ActionLister?) {
        withContext(Dispatchers.IO) {
            runCatching {
                apiServerBootStrap?.close()
                fileApiServerBootStrap?.close()
            }.onSuccess {
                actionLister?.onSuccess()
            }.onFailure {
                actionLister?.onFailure()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun main(application: Application, actionLister: ActionLister?) {
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "main")
            val handlerMappings = runCatching {
                buildHandlerMappings(application)
            }.getOrNull()
            if (handlerMappings == null) {
                actionLister?.onFailure()
                return@withContext
            }

            val apiChannelChannelInitializer =
                ApiChannelChannelInitializer(apiPort, handlerMappings, listenersProvider)
            apiServerBootStrap = ApiServerBootStrap(apiPort, apiChannelChannelInitializer)

            val fileApiChannelChannelInitializer =
                FileApiChannelChannelInitializer(fileApiPort, handlerMappings, listenersProvider)
            fileApiServerBootStrap =
                ApiServerBootStrap(fileApiPort, fileApiChannelChannelInitializer)

            runCatching {
                apiServerBootStrap?.main()
                fileApiServerBootStrap?.main()
            }.onSuccess {
                PurpleLogger.current.d(TAG, "main, success")
                actionLister?.onSuccess()
            }.onFailure {
                PurpleLogger.current.d(TAG, "main, failed")
                actionLister?.onFailure(it.message)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun buildHandlerMappings(application: Application): List<HandlerMapping>? {
        val handler: Handler? = null
        val handlerMethodList = DefaultControllerCollector.collect(
            application,
            application::class.java.`package`?.name?.removeSuffix(".container"),
            handler
        )
        PurpleLogger.current.d(TAG, "main, handlerMethodList: $handlerMethodList")
        if (handlerMethodList.isNullOrEmpty()) {
            return null
        }
        PurpleLogger.current.d(TAG, "main, create serverBootstrap pre step 0")
        val fixedUriHandlerMethod = handlerMethodList.filter { it.uriSplitResults == null }
        PurpleLogger.current.d(
            TAG,
            "main, create serverBootstrap pre step 1, fixedUriHandlerMethod$fixedUriHandlerMethod"
        )
        val dynamicUriHandlerMethod =
            handlerMethodList.filter { it.uriSplitResults != null }
        PurpleLogger.current.d(
            TAG,
            "main, create serverBootstrap pre step 2, dynamicUriHandlerMethod:$dynamicUriHandlerMethod"
        )
        val fixedUriHandlerMethodMap = fixedUriHandlerMethod.associateBy { it.uri }
        val dynamicUriHandlerMethodMap = dynamicUriHandlerMethod.associateBy { it.uri }
        val requestPathHandlerMapping =
            RequestPathHandlerMapping(fixedUriHandlerMethodMap, dynamicUriHandlerMethodMap)
        PurpleLogger.current.d(TAG, "main, create serverBootstrap pre step 3")
        val handlerMappings = listOf(requestPathHandlerMapping)
        return handlerMappings
    }
}


