package com.easv.aepm.listviewrecylerview.data
import java.io.Serializable

data class BEFriend(var id: Int = 0, var name: String, var number: String, var mail: String, var isFavorite: Boolean, var url: String?) : Serializable {}