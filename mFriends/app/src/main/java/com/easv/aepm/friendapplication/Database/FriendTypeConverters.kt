package com.easv.aepm.friendapplication.Database

import android.location.Location
import android.util.Log
import androidx.room.TypeConverter
import java.lang.Exception
import java.util.*

class FriendTypeConverters {

    //Converts type date to seconds
    @TypeConverter
    fun fromDate(date: Date?): Long?{
        return date?.time;
    }

    //Converts type seconds to readable date
    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date?{
        return millisSinceEpoch?.let { Date(it) }
    }

    //Converts type latitude and longitude to string
    @TypeConverter
    fun fromLocation(location: Location?): String?{
        if(location != null){
            var latitude: String = location.latitude.toString()
            var longitude: String = location.longitude.toString()
            return "${latitude}/${longitude}"
        }
        return null
    }

    //Converts type string to latitude and longitude
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