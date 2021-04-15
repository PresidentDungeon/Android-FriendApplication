package com.easv.aepm.friendapplication.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.easv.aepm.friendapplication.data.BEFriend
import com.easv.aepm.friendapplication.data.interfaces.FriendDao

//Connect database with entities
@Database(entities = [BEFriend::class], version = 1)

//Converts the types for database storage
@TypeConverters(FriendTypeConverters::class)
abstract class Database : RoomDatabase() {

    abstract fun friendDao(): FriendDao
}

