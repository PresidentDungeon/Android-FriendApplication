package com.easv.aepm.listviewrecylerview.Database

import androidx.room.TypeConverter
import java.util.*

class FriendTypeConverters {

    @TypeConverter
    fun fromDate(date: Date?): Long?{
        return date?.time;
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date?{
        return millisSinceEpoch?.let { Date(it) }
    }
}