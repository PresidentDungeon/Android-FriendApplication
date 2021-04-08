package com.easv.aepm.listviewrecylerview.data
import android.location.Location
import android.widget.DatePicker
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
data class BEFriend(
    @PrimaryKey(autoGenerate = true) var id: Int = 0, var name: String, var number: String,
    var mail: String, var isFavorite: Boolean, var url: String?, var birthdate: Date?,
    var image: String, var location: Location?) : Serializable {}

