package xcj.app.appsets.usecase

import xcj.app.appsets.server.api.ApiProvider
import xcj.app.appsets.server.api.ThirdPartApi1
import xcj.app.io.components.LocalFileIO
import xcj.app.io.tencent.TencentCosInfoProvider
import xcj.app.io.tencent.TencentCosRegionBucket
import xcj.app.io.tencent.TencentCosSTS
import xcj.app.io.tencent.ThirdPartRepository
import xcj.app.starter.android.util.PurpleLogger

class ThirdPartUseCase private constructor(
    private val thirdPartRepository: ThirdPartRepository,
) {
    fun initSimpleFileIO() {
        if (LocalFileIO.current.isThirdComponentsInit()) {
            return
        }
        val tencentCosInfoProvider = ATencentCosInfoProvider()
        LocalFileIO.current.initThirdComponents(tencentCosInfoProvider)
    }

    private inner class ATencentCosInfoProvider : TencentCosInfoProvider {
        private var mTencentCosSTS: TencentCosSTS? = null
        private var mTencentCosRegionBucket: TencentCosRegionBucket? = null
        private var lastSTSRequestTimeMills = 0L

        suspend fun requestTencentCosSTS(by: String): TencentCosSTS? {
            if (System.currentTimeMillis() - lastSTSRequestTimeMills < 10 * 60 * 1000) {
                PurpleLogger.current.d(
                    TAG,
                    "requestTencentCosSTS, by:$by, Request too frequently，return!"
                )
                return null
            }
            lastSTSRequestTimeMills = System.currentTimeMillis()
            val tencentCosSTS = thirdPartRepository.getTencentCosSTS().data
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

        override suspend fun getTencentCosSTS(): TencentCosSTS? {
            val cosSTS = mTencentCosSTS
            if (cosSTS == null) {
                val sts = requestTencentCosSTS("no sts, request new one")
                updateSts(sts)
                PurpleLogger.current.d(TAG, "getTencentCosSTS, cos:$sts")
                return sts
            } else if (cosSTS.isOutOfDate()) {
                val sts = requestTencentCosSTS("out of date")
                updateSts(sts)
                PurpleLogger.current.d(TAG, "getTencentCosSTS, cos:$sts")
                return sts
            } else {
                PurpleLogger.current.d(TAG, "getTencentCosSTS, cos:$cosSTS")
                return cosSTS
            }
        }

        override suspend fun getTencentCosRegionBucket(): TencentCosRegionBucket? {
            if (mTencentCosRegionBucket == null) {
                val tencentCosRegionBucketRes = thirdPartRepository.getTencentCosRegionBucket()
                updateRegionBucket(tencentCosRegionBucketRes.data?.decode())
            }
            if (mTencentCosRegionBucket == null) {
                PurpleLogger.current.d(
                    TAG,
                    "generatePreSign, cosXmlService is null when generatePreSign"
                )
            }
            return mTencentCosRegionBucket
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