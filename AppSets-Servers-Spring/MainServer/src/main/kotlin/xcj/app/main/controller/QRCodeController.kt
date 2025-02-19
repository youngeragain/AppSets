package xcj.app.main.controller

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.*
import xcj.app.DesignResponse
import xcj.app.main.model.common.StringBody
import xcj.app.main.qr.QRCodeInfoInRedis
import xcj.app.util.PurpleLogger
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.getOrSet

@RestController
class QRCodeController {

    companion object {
        private const val TAG = "QRCodeController"
    }

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate
    private val sAi: ThreadLocal<AtomicInteger> = ThreadLocal()
    private val gson = Gson()

    /**
     *
     *
     * @return
     * qrcode state:
     * 0:new, expire time set to 60s
     * 1:scan_but_no_operation, expire time set to 60s
     * 2:operated, expire time set to 60s
     */
    @PostMapping("/login/qrcode_code/gen")
    fun generateQRCodeCode(
        @RequestParam(name = "providerId", required = false)
        providerId: String? = null,
        @RequestParam(name = "force", required = false)
        force: Boolean = false,
        @RequestParam(name = "expireTimes", required = false)
        expireTimes: Long? = 60000,
        @RequestBody
        providerInfo: StringBody
    ): DesignResponse<Map<String, String>> {
        PurpleLogger.current.d(TAG, "genQRCodeCode")
        if (force) {
            val atomicInteger = sAi.getOrSet {
                AtomicInteger(0)
            }
            val providerId1: String = UUID.randomUUID().toString()
            val qrCodeInfoInRedis = QRCodeInfoInRedis(
                providerId1,
                atomicInteger.incrementAndGet().toString(),
                "0",
                info = "new",
                providerInfo = providerInfo.content
            )
            val writeValueAsString = jsonMapper().writeValueAsString(qrCodeInfoInRedis)
            redisTemplate.opsForValue().set(providerId1, writeValueAsString, Duration.ofMinutes(1))
            return DesignResponse(
                data = mapOf(
                    "providerId" to providerId1,
                    "code" to qrCodeInfoInRedis.code,
                    "state" to qrCodeInfoInRedis.state
                )
            )
        }
        if (!providerId.isNullOrEmpty())
            if (redisTemplate.hasKey(providerId)) {
                val redisValue = redisTemplate.opsForValue().get(providerId)
                if (!redisValue.isNullOrEmpty()) {
                    val qrCodeInfoInRedis = gson.fromJson<QRCodeInfoInRedis>(redisValue, QRCodeInfoInRedis::class.java)
                    return DesignResponse(
                        data = mapOf(
                            "providerId" to providerId,
                            "code" to qrCodeInfoInRedis.code,
                            "state" to qrCodeInfoInRedis.state
                        )
                    )
                }
            }
        val atomicInteger = sAi.getOrSet {
            AtomicInteger(0)
        }
        val providerId1: String = UUID.randomUUID().toString()
        val qrCodeInfoInRedis = QRCodeInfoInRedis(
            providerId1,
            atomicInteger.incrementAndGet().toString(),
            "0",
            info = "new",
            providerInfo = providerInfo.content
        )
        val writeValueAsString = jsonMapper().writeValueAsString(qrCodeInfoInRedis)
        redisTemplate.opsForValue().set(providerId1, writeValueAsString, Duration.ofMinutes(1))
        return DesignResponse(
            data = mapOf(
                "providerId" to providerId1,
                "code" to qrCodeInfoInRedis.code,
                "state" to qrCodeInfoInRedis.state
            )
        )
    }

    @GetMapping("/login/qrcode_code/state")
    fun qrCodeCodeStatus(
        @RequestParam(name = "providerId") providerId: String,
        @RequestParam(name = "code") qrCodeCode: String
    ): DesignResponse<Map<String, String?>> {
        PurpleLogger.current.d(TAG, "qrCodeCodeStatus")
        if (!redisTemplate.hasKey(providerId)) {
            return DesignResponse(info = "not exist or expired!")
        }
        val redisValue = redisTemplate.opsForValue().get(providerId)
        if (redisValue.isNullOrEmpty())
            return DesignResponse(info = "not exist or expired!")
        val qrCodeInfoInRedis = gson.fromJson<QRCodeInfoInRedis>(redisValue, QRCodeInfoInRedis::class.java)
        if (qrCodeInfoInRedis.code != qrCodeCode)
            return DesignResponse(info = "code not matched!")
        return DesignResponse(
            data = mapOf(
                "code" to qrCodeInfoInRedis.code,
                "state" to qrCodeInfoInRedis.state,
                "info" to qrCodeInfoInRedis.info,
                "extra" to qrCodeInfoInRedis.extra,
                "scannerInfo" to qrCodeInfoInRedis.scannerInfo
            )
        )
    }

    @PostMapping("/login/qrcode_code/scan")
    fun scanQRCode(
        @RequestParam(name = "providerId") providerId: String,
        @RequestParam(name = "code") code: String,
        @RequestBody scannerInfo: StringBody
    ): DesignResponse<Map<String, String>> {
        if (!redisTemplate.hasKey(providerId)) {
            return DesignResponse(info = "not exist or expired!")
        }
        val redisValue = redisTemplate.opsForValue().get(providerId)
        if (redisValue.isNullOrEmpty()) {
            return DesignResponse(info = "not exist or expired!")
        }
        val qrCodeInfoInRedis = gson.fromJson<QRCodeInfoInRedis>(redisValue, QRCodeInfoInRedis::class.java)
        if (qrCodeInfoInRedis.code != code) {
            return DesignResponse(info = "code not matched!")
        }
        if (qrCodeInfoInRedis.state == "2") {
            return DesignResponse(info = "code is operated!")
        }
        if (qrCodeInfoInRedis.state == "1") {
            return DesignResponse(info = "code is scanned!")
        }
        qrCodeInfoInRedis.state = "1"
        qrCodeInfoInRedis.info = "scan_but_no_operation"
        qrCodeInfoInRedis.scannerInfo = scannerInfo.content
        val writeValueAsString = jsonMapper().writeValueAsString(qrCodeInfoInRedis)
        redisTemplate.opsForValue().set(providerId, writeValueAsString, Duration.ofMinutes(1))
        return DesignResponse(
            data = mapOf(
                "code" to qrCodeInfoInRedis.code,
                "state" to qrCodeInfoInRedis.state,
                "providerInfo" to qrCodeInfoInRedis.providerInfo
            )
        )
    }

    @PostMapping("/login/qrcode_code/confirm")
    fun confirmQRCode(
        @RequestParam(name = "providerId") providerId: String,
        @RequestParam(name = "code") code: String,
        @RequestBody confirmExtra: StringBody
    ): DesignResponse<Map<String, String>> {
        if (!redisTemplate.hasKey(providerId)) {
            return DesignResponse(info = "not exist or expired!")
        }
        val redisValue = redisTemplate.opsForValue().get(providerId)
        if (redisValue.isNullOrEmpty()) {
            return DesignResponse(info = "not exist or expired!")
        }
        val qrCodeInfoInRedis = gson.fromJson<QRCodeInfoInRedis>(redisValue, QRCodeInfoInRedis::class.java)
        if (qrCodeInfoInRedis.code != code) {
            return DesignResponse(info = "code not matched!")
        }
        if (qrCodeInfoInRedis.state == "2") {
            return DesignResponse(info = "code is operated!")
        }
        if (qrCodeInfoInRedis.state != "1") {
            return DesignResponse(info = "code is not scanned!")
        }
        qrCodeInfoInRedis.state = "2"
        qrCodeInfoInRedis.info = "operated"
        qrCodeInfoInRedis.extra = confirmExtra.content
        val writeValueAsString = jsonMapper().writeValueAsString(qrCodeInfoInRedis)
        redisTemplate.opsForValue().set(providerId, writeValueAsString, Duration.ofMinutes(1))
        return DesignResponse(data = mapOf("code" to qrCodeInfoInRedis.code, "state" to qrCodeInfoInRedis.state))
    }
}