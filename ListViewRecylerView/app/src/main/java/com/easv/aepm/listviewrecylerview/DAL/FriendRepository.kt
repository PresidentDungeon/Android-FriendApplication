package com.easv.aepm.listviewrecylerview.DAL

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.easv.aepm.listviewrecylerview.Database.Database
import com.easv.aepm.listviewrecylerview.data.BEFriend
import java.lang.IllegalStateException
import java.util.concurrent.Executors

private const val DATABASE_NAME = "friend-database"

class FriendRepository private constructor(context: Context){

    private val database: Database = Room.databaseBuilder(context.applicationContext, Database::class.java, DATABASE_NAME).build()
    private val friendDao = database.friendDao()

    fun addFriend(friend: BEFriend){ friendDao.addFriend(friend);}
    fun getFriends(): LiveData<List<BEFriend>> = friendDao.getFriends()
    suspend fun getFriends(query: String, args: Array<Any>): List<BEFriend> = friendDao.getFriendsFilter(SimpleSQLiteQuery(query, args))
    suspend fun getFriend(id: Int): BEFriend? = friendDao.getFriend(id)
    fun updateFriend(friend: BEFriend){friendDao.updateFriend(friend)}
    fun deleteFriend(friend: BEFriend){friendDao.deleteFriend(friend)}

    companion object{
        private var INSTANCE: FriendRepository? = null

        fun initialize(context: Context){
            if(INSTANCE == null){INSTANCE = FriendRepository(context) }
        }

        fun get(): FriendRepository{
            return INSTANCE?: throw IllegalStateException("FriendRepository must be initialized first")
        }

    }

}