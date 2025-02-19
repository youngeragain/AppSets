package xcj.app.appsets.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import xcj.app.appsets.server.api.ThirdPartApi1
import xcj.app.appsets.server.api.ApiProvider
import xcj.app.io.components.LocalFileIO
import xcj.app.io.tencent.TencentCosInfoProvider
import xcj.app.io.tencent.TencentCosRegionBucket
import xcj.app.io.tencent.TencentCosSTS
import xcj.app.io.tencent.ThirdPartRepository
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalPurpleCoroutineScope

class ThirdPartUseCase private constructor(
    private val coroutineScope: CoroutineScope = LocalPurpleCoroutineScope.current,
    private val thirdPartRepository: ThirdPartRepository
) {
    fun initSimpleFileIO() {
        if (LocalFileIO.current.isThirdComponentsInit()) {
            return
        }
        coroutineScope.launch {
            val tencentCosRegionBucketRes = thirdPartRepository.getTencentCosRegionBucket()
            val tencentCosInfoProvider = ATencentCosInfoProvider()
            tencentCosInfoProvider.updateRegionBucket(tencentCosRegionBucketRes.data?.decode())
            LocalFileIO.current.initThirdComponents(tencentCosInfoProvider)
        }
    }

    private inner class ATencentCosInfoProvider : TencentCosInfoProvider {
        private val mLock = Any()
        private var mTencentCosSTS: TencentCosSTS? = null
        private var mTencentCosRegionBucket: TencentCosRegionBucket? = null
        private var lastSTSRequestTimeMills = 0L

        fun requestTencentCosSTS(by: String): TencentCosSTS? {
            if (System.currentTimeMillis() - lastSTSRequestTimeMills < 10 * 60 * 1000) {
                PurpleLogger.current.d(
                    TAG,
                    "requestTencentCosSTS, by:$by, Request too frequently，return!"
                )
                return null
            }
            lastSTSRequestTimeMills = System.currentTimeMillis()
            val tencentCosSTS = runBlocking {
                withContext(Dispatchers.IO) {
                    thirdPartRepository.getTencentCosSTS().data
                }
            }
            PurpleLogger.current.d(
                TAG,
                "requestTencentCosSTS, by:$by, sts:${tencentCosSTS}"
            )
            return tencentCosSTS
        }

        fun updateRegionBucket(regionBucket: TencentCosRegionBucket?) {
            mTencentCosRegionBucket = regionBucket
        }

        private fun updateSts(sts: TencentCosSTS?) {
            mTencentCosSTS = sts
        }

        override fun getTencentCosSTS(): TencentCosSTS? {
            PurpleLogger.current.d(TAG, "getTencentCosSTS")
            synchronized(mLock) {
                val cosSTS = mTencentCosSTS
                if (cosSTS == null) {
                    val sts = requestTencentCosSTS("no sts, request new one")
                    PurpleLogger.current.d(
                        TAG,
                        "getTencentCosSTS, client systemTimeMills:${System.currentTimeMillis()}"
                    )
                    updateSts(sts)
                    if (sts == null) {
                        PurpleLogger.current.w(TAG, "getTencentCosSTS, return null.")
                    }
                    return sts
                }
                if (cosSTS.isOutOfDate()) {
                    val sts = requestTencentCosSTS("out of date")
                    updateSts(sts)
                    if (sts == null) {
                        PurpleLogger.current.w(TAG, "getTencentCosSTS, return null.")
                    }
                    return sts
                }

                return cosSTS
            }
        }

        override fun getTencentCosRegionBucket(): TencentCosRegionBucket? {
            val tencentCosRegionBucket = mTencentCosRegionBucket
            if (tencentCosRegionBucket == null) {
                PurpleLogger.current.d(
                    TAG,
                    "generatePreSign, cosXmlService is null when generatePreSign"
                )
            }
            return tencentCosRegionBucket
        }
    }

    companion object {

        private const val TAG = "ThirdPartUseCase"

        fun newInstance(): ThirdPartUseCase {
            val api = ApiProvider.provide(ThirdPartApi1::class.java)
            val thirdPartRepository = ThirdPartRepository(api)
            return ThirdPartUseCase(thirdPartRepository = thirdPartRepository)
        }
    }
}