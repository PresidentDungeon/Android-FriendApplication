package com.easv.aepm.listviewrecylerview.data
import android.widget.DatePicker
import java.io.Serializable
import java.util.*

data class BEFriend(var id: Int = 0, var name: String, var number: String, var mail: String, var isFavorite: Boolean, var url: String?, var birthdate: Date?) : Serializable {}