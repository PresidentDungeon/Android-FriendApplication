package com.easv.aepm.friendapplication.data
import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
data class BEFriend(
    //Setup information for Friend entity
    @PrimaryKey(autoGenerate = true) var id: Int = 0, var name: String, var number: String,
    var mail: String, var isFavorite: Boolean, var url: String?, var birthdate: Date?,
    var image: String, var location: Location?) : Serializable {}

