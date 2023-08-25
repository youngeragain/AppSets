package xcj.app.appsets.usecase

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ktx.isHttpUrl
import xcj.app.appsets.ktx.requestNotNullRaw
import xcj.app.appsets.ktx.toast
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.ui.nonecompose.base.UriHolder
import xcj.app.appsets.usecase.models.ApplicationForCreate


sealed class CreateApplicationState {
    data class Creating(val tips: String? = null) : CreateApplicationState()
    data class CreateFinish(val tips: String? = null) : CreateApplicationState()
    data class CreateFailed(val tips: String? = null) : CreateApplicationState()
}

class CreateApplicationUseCase(private val coroutineScope: CoroutineScope) {
    private var applicationForCreate: ApplicationForCreate? = null
    private var chooseContentUseAge: String? = null
    private var chooseContentUriHolder: MutableState<UriHolder?>? = null

    val createApplicationState: MutableState<CreateApplicationState?> = mutableStateOf(null)

    fun getApplication(): ApplicationForCreate {
        if (applicationForCreate != null)
            return applicationForCreate!!
        val tempApp = ApplicationForCreate()
        applicationForCreate = tempApp
        return tempApp
    }

    fun clear() {
        applicationForCreate = null
        chooseContentUseAge = null
        chooseContentUriHolder = null
        createApplicationState.value = null
    }

    fun finishCreateApp(context: Context) {
        val creatingState = createApplicationState.value
        if (creatingState != null && creatingState is CreateApplicationState.Creating) {
            "正在创建应用，请稍后".toast()
            return
        }
        if (!checkAppIntegrity()) {
            return
        }
        coroutineScope.requestNotNullRaw({
            createApplicationState.value = CreateApplicationState.Creating("创建中")
            delay(500)
            val createApplicationPreCheckRes =
                AppSetsRepository.getInstance()
                    .createApplicationPreCheck(applicationForCreate!!.name.value)
            if (createApplicationPreCheckRes.data != true) {
                createApplicationState.value =
                    CreateApplicationState.CreateFailed("存在同名称应用，请更换名称")
                delay(200)
                createApplicationState.value = null
                return@requestNotNullRaw
            }
            val createApplicationRes =
                AppSetsRepository.getInstance().createApplication(context, applicationForCreate!!)
            if (createApplicationRes.data != true) {
                createApplicationState.value = CreateApplicationState.CreateFailed("创建应用失败")
                delay(200)
                createApplicationState.value = null
                return@requestNotNullRaw
            }
            createApplicationState.value = CreateApplicationState.CreateFinish("创建应用成功")
            delay(200)
            createApplicationState.value = null
        }, onFailed = {
            Log.e("CreateApplicationUseCase", "finishCreateApp failed:${it}")
            createApplicationState.value = CreateApplicationState.CreateFailed("异常，创建应用失败")
            delay(200)
            createApplicationState.value = null
        })
    }

    private fun checkAppIntegrity(): Boolean {
        val tempApp = applicationForCreate
        if (tempApp == null) {
            "没有找到Application".toast()
            return false
        }
        if (tempApp.iconUriHolderState.value == null) {
            "请选择图标".toast()
            return false
        }
        if (tempApp.bannerUriHolderState.value == null) {
            "请选择Banner".toast()
            return false
        }
        if (tempApp.name.value.isEmpty()) {
            "请输入应用名称".toast()
            return false
        }
        if (tempApp.category.value.isEmpty()) {
            "请输入应用类型".toast()
            return false
        }
        if (tempApp.website.value.isNotEmpty()) {
            if (!tempApp.website.value.isHttpUrl()) {
                "请输入正确的网站链接".toast()
                return false
            }
        }
        tempApp.platformForCreates.forEach { platformForCreate ->
            if (platformForCreate.packageName.value.isEmpty()) {
                "请添加${platformForCreate.name}的应用包名".toast()
                return false
            }
            if (platformForCreate.introduction.value.isEmpty()) {
                "请添加${platformForCreate.name}的介绍".toast()
                return false
            }
            platformForCreate.versionInfoForCreates.forEach { versionInfoForCreate ->
                if (versionInfoForCreate.version.value.isEmpty()) {
                    "请输入${platformForCreate}平台的版本".toast()
                    return false
                }
                if (versionInfoForCreate.versionCode.value.isEmpty()) {
                    "请输入${platformForCreate}平台的版本Code".toast()
                    return false
                }
                if (versionInfoForCreate.changes.value.isEmpty()) {
                    "请输入${platformForCreate}平台${versionInfoForCreate.version}版本的日志".toast()
                    return false
                }
                if (versionInfoForCreate.privacyPolicyUrl.value.isEmpty()) {
                    "请输入${platformForCreate}平台${versionInfoForCreate.version}版本的隐私链接".toast()
                    return false
                }
                if (versionInfoForCreate.versionIconUriHolderState.value == null) {
                    "请选择${platformForCreate}平台${versionInfoForCreate.version}版本的图标".toast()
                    return false
                }
                if (versionInfoForCreate.versionIconUriHolderState.value == null) {
                    "请选择${platformForCreate}平台${versionInfoForCreate.version}版本的Banner".toast()
                    return false
                }
                versionInfoForCreate.downloadInfoForCreates.forEach { downloadInfoForCreate ->
                    if (!downloadInfoForCreate.url.value.isHttpUrl()) {
                        "请输入正确的下载链接".toast()
                        return false
                    }
                }
            }
        }
        return true
    }

    fun setCurrentUseAgeAndUriHolderState(
        useAge: String,
        uriHolderState: MutableState<UriHolder?>
    ) {
        chooseContentUseAge = useAge
        chooseContentUriHolder = uriHolderState
    }

    fun updateSelectPicture(imageUri: MediaStoreDataUriWrapper) {
        chooseContentUriHolder?.value = imageUri
        chooseContentUseAge = null
        chooseContentUriHolder = null
    }
}