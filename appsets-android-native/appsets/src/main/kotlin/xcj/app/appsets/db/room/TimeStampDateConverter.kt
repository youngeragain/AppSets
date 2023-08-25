package xcj.app.appsets.db.room

import androidx.room.TypeConverter
import java.util.Date

class TimeStampDateConverter {
    @TypeConverter
    fun fromLong(timeMillis: Long): Date {
        return Date(timeMillis)
    }

    @TypeConverter
    fun toLong(date: Date): Long {
        return date.time
    }
}