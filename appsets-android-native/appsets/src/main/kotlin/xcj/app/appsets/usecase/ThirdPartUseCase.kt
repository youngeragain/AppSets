package xcj.app.appsets.usecase

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import xcj.app.appsets.server.api.ThirdPartApi1
import xcj.app.appsets.server.api.URLApi
import xcj.app.io.components.SimpleFileIO
import xcj.app.io.components.TencentCosInfoProvider
import xcj.app.io.components.TencentCosRegionBucket
import xcj.app.io.components.TencentCosSTS
import xcj.app.io.components.ThirdPartRepository

class ThirdPartUseCase private constructor() {
    private lateinit var coroutineScope: CoroutineScope
    private val thirdPartRepository: ThirdPartRepository by lazy {
        val thirdPartApi = URLApi.provide(ThirdPartApi1::class.java)
        val thirdPartRepository = ThirdPartRepository(thirdPartApi)
        thirdPartRepository
    }

    fun setCoroutineScope(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope
    }

    fun initSimpleFileIO(context: Context) {
        if (!::coroutineScope.isInitialized) {
            throw Exception("no coroutineScope found when initSimpleFileIO!!!")
        }
        coroutineScope.launch(Dispatchers.IO) {
            val tencentCosInfoProvider = object : TencentCosInfoProvider {
                private val mLock = Any()
                private var mTencentCosSTS: TencentCosSTS? = null
                private var mTencentCosRegionBucket: TencentCosRegionBucket? = null
                fun updateSts(sts: TencentCosSTS?) {
                    mTencentCosSTS = sts
                }

                fun updateRegionBucket(regionBucket: TencentCosRegionBucket?) {
                    mTencentCosRegionBucket = regionBucket
                }

                override fun getTencentCosSTS(): TencentCosSTS {
                    Log.e("ThirdPartUseCase", "getTencentCosSTS")
                    synchronized(mLock) {
                        val cosSTS = mTencentCosSTS
                        if (cosSTS == null) {
                            val sts = requestTencentCosSTS("no sts, request new one")
                            Log.i(
                                "ThirdPartUseCase",
                                "client systemTimeMills:${System.currentTimeMillis()}"
                            )
                            updateSts(sts)
                            return sts ?: throw Exception()
                        }
                        if (cosSTS.isOutOfDate()) {
                            val sts = requestTencentCosSTS("out of date")
                            updateSts(sts)
                            return sts ?: throw Exception()
                        } else
                            return cosSTS
                    }
                }

                override fun getTencentCosRegionBucket(): TencentCosRegionBucket {
                    return mTencentCosRegionBucket ?: throw Exception()
                }
            }
            val tencentCosRegionBucketRes = thirdPartRepository.getTencentCosRegionBucket()
            kotlin.runCatching {
                tencentCosRegionBucketRes.data?.decode()
            }
            tencentCosInfoProvider.updateRegionBucket(tencentCosRegionBucketRes.data)
            SimpleFileIO.getInstance().initThirdComponents(context, tencentCosInfoProvider)
        }
    }

    fun requestTencentCosSTS(by: String): TencentCosSTS? {
        if (!::coroutineScope.isInitialized) {
            throw Exception("no coroutineScope found when requestNewTencentCosSTS!!!")
        }
        val tencentCosSTS = runBlocking(coroutineScope.coroutineContext) {
            withContext(Dispatchers.IO) {
                thirdPartRepository.getTencentCosSTS().data
            }
        }
        Log.i("ThirdPartUseCase", "requestTencentCosSTS, by:$by, sts:${tencentCosSTS}")
        return tencentCosSTS
    }

    companion object {
        private var INSTANCE: ThirdPartUseCase? = null
        fun getInstance(): ThirdPartUseCase {
            return INSTANCE ?: synchronized(ThirdPartUseCase::class.java) {
                if (INSTANCE == null) {
                    INSTANCE = ThirdPartUseCase()
                }
                INSTANCE!!
            }
        }
    }
}