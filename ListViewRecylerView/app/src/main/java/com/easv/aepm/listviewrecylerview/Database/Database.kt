package com.easv.aepm.listviewrecylerview.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.easv.aepm.listviewrecylerview.data.BEFriend
import com.easv.aepm.listviewrecylerview.data.interfaces.FriendDao

@Database(entities = [BEFriend::class], version = 1)
@TypeConverters(FriendTypeConverters::class)
abstract class Database : RoomDatabase() {

    abstract fun friendDao(): FriendDao
}

