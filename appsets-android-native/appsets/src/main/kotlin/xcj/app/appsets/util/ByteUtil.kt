package xcj.app.appsets.util

import android.icu.math.BigDecimal
import android.icu.text.DecimalFormat

object ByteUtil {
    const val KB_SIZE = 2 shl 9
    const val MB_SIZE = 2 shl 19
    const val GB_SIZE = 2 shl 29
    fun bytes2Unit(bytes: Long, unit: Int?): BigDecimal {
        val size = BigDecimal(bytes)
        val u = BigDecimal(unit!!)
        return size.divide(u, 2, BigDecimal.ROUND_DOWN)
    }

    fun unit2Byte(decimal: BigDecimal, unit: Int): Long {
        return decimal.multiply(BigDecimal.valueOf(unit.toLong())).toLong()
    }

    fun kb2Byte(decimal: BigDecimal): Long {
        return decimal.multiply(BigDecimal.valueOf(KB_SIZE.toLong())).toLong()
    }

    fun mb2Byte(decimal: BigDecimal): Long {
        return decimal.multiply(BigDecimal.valueOf(MB_SIZE.toLong())).toLong()
    }

    fun gb2Byte(decimal: BigDecimal): Long {
        return decimal.multiply(BigDecimal.valueOf(GB_SIZE.toLong())).toLong()
    }

    fun bytes2Kb(bytes: Long): BigDecimal {
        return bytes2Unit(bytes, KB_SIZE)
    }

    fun bytes2Mb(bytes: Long): BigDecimal {
        return bytes2Unit(bytes, MB_SIZE)
    }

    fun bytes2Gb(bytes: Long): BigDecimal {
        return bytes2Unit(bytes, GB_SIZE)
    }

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
        } else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B")
            } else {
                bytes.append(size.toInt()).append("B")
            }
        }
        return bytes.toString()
    }
}