package com.easv.aepm.listviewrecylerview.data.interfaces

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.easv.aepm.listviewrecylerview.data.BEFriend

@Dao
interface FriendDao {

    @Insert
    fun addFriend(friend: BEFriend)

    @Query("SELECT * FROM BEFriend ORDER BY name ASC")
    fun getFriends(): LiveData<List<BEFriend>>

    @RawQuery(observedEntities = [BEFriend::class])
    suspend fun getFriendsFilter(query: SupportSQLiteQuery): List<BEFriend>

    @Query("SELECT * FROM BEFriend WHERE name LIKE id=(:id)")
    suspend fun getFriend(id: Int): BEFriend?

    @Update
    fun updateFriend(friend: BEFriend)

    @Delete
    fun deleteFriend(friend: BEFriend)
}