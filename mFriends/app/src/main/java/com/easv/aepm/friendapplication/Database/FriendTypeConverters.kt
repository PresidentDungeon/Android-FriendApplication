package com.easv.aepm.friendapplication.Database

import android.location.Location
import android.util.Log
import androidx.room.TypeConverter
import java.lang.Exception
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

    @TypeConverter
    fun fromLocation(location: Location?): String?{
        if(location != null){
            var latitude: String = location.latitude.toString()
            var longitude: String = location.longitude.toString()
            return "${latitude}/${longitude}"
        }
        return null
    }

    @TypeConverter
    fun toLocation(locationString: String?): Location?{
        if(locationString != null){
            var positions: List<String> = locationString.split("/")

            try{
                var location: Location = Location("GPS")
                location.latitude = positions[0].toDouble()
                location.longitude = positions[1].toDouble()
                return location
            }
            catch (e: Exception){
                Log.d("FriendConverter", "Error converting from string to location")
            }
        }
        return null
    }
}