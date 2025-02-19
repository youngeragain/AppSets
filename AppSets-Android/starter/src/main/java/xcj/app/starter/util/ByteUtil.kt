package xcj.app.starter.util

import android.icu.math.BigDecimal
import android.icu.text.DecimalFormat

object ByteUtil {
    const val KB_SIZE = 2 shl 9
    const val MB_SIZE = 2 shl 19
    const val GB_SIZE = 2 shl 29

    @JvmStatic
    fun bytes2Unit(bytes: Long, unit: Int?): BigDecimal {
        val size = BigDecimal(bytes)
        val u = BigDecimal(unit!!)
        return size.divide(u, 2, BigDecimal.ROUND_DOWN)
    }

    @JvmStatic
    fun unit2Byte(decimal: BigDecimal, unit: Int): Long {
        return decimal.multiply(BigDecimal.valueOf(unit.toLong())).toLong()
    }

    @JvmStatic
    fun kb2Byte(decimal: BigDecimal): Long {
        return decimal.multiply(BigDecimal.valueOf(KB_SIZE.toLong())).toLong()
    }

    @JvmStatic
    fun mb2Byte(decimal: BigDecimal): Long {
        return decimal.multiply(BigDecimal.valueOf(MB_SIZE.toLong())).toLong()
    }

    @JvmStatic
    fun gb2Byte(decimal: BigDecimal): Long {
        return decimal.multiply(BigDecimal.valueOf(GB_SIZE.toLong())).toLong()
    }

    @JvmStatic
    fun bytes2Kb(bytes: Long): BigDecimal {
        return bytes2Unit(bytes, KB_SIZE)
    }

    @JvmStatic
    fun bytes2Mb(bytes: Long): BigDecimal {
        return bytes2Unit(bytes, MB_SIZE)
    }

    @JvmStatic
    fun bytes2Gb(bytes: Long): BigDecimal {
        return bytes2Unit(bytes, GB_SIZE)
    }

    @JvmStatic
    fun getNetFileSizeDescription(size: Long): String {
        val bytes = StringBuffer()
        val format = DecimalFormat("###.0")
        if (size >= 1024 * 1024 * 1024) {
            val i = size / (1024.0 * 1024.0 * 1024.0)
            bytes.append(format.format(i)).append("GB")
        } else if (size >= 1024 * 1024) {
            val i = size / (1024.0 * 1024.0)
            bytes.append(format.format(i)).append("MB")
        } else if (size >= 1024) {
            val i = size / 1024.0
            bytes.append(format.format(i)).append("KB")
        } else {
            if (size <= 0) {
                bytes.append("0B")
            } else {
                bytes.append(size.toInt()).append("B")
            }
        }
        return bytes.toString()
    }

    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    fun toHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (byte in bytes) {
            var s = byte.toInt().toHexString()
            if (byte < 0x10)
                s = "0$s"
            sb.append(s.substring(s.length - 2))
        }
        return sb.toString()
    }

    @JvmStatic
    fun longToByteArray(value: Long): ByteArray {
        val bytes = ByteArray(8)
        repeat(bytes.size) {
            bytes[it] = ((value ushr 8 * it) and 0xFF).toByte()
        }
        byteArrayToLong(bytes)
        return bytes
    }

    @JvmStatic
    fun byteArrayToLong(bytes: ByteArray): Long {
        var c = 0L
        repeat(bytes.size) {
            var byte = bytes[it].toLong()
            if (byte != 0L) {
                if (byte < 0L) {
                    byte += 256
                }
                c += (byte shl it * 8)
            }
        }
        return c
    }

    @JvmStatic
    fun intToByteArray(value: Int): ByteArray {
        val bytes = ByteArray(4)
        repeat(bytes.size) {
            bytes[it] = ((value ushr 8 * it) and 0xFF).toByte()
        }
        byteArrayToInt(bytes)
        return bytes
    }

    @JvmStatic
    fun byteArrayToInt(bytes: ByteArray): Int {
        var c = 0
        repeat(bytes.size) {
            var byte = bytes[it].toInt()
            if (byte != 0) {
                if (byte < 0) {
                    byte += 256
                }
                c += (byte shl it * 8)
            }
        }
        return c
    }
}