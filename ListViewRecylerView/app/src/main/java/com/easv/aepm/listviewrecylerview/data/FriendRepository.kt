package com.easv.aepm.listviewrecylerview.data

import java.util.*

class FriendRepository {

    private var id: Int = 8
    private val friends: MutableList<BEFriend> = mutableListOf<BEFriend>(
        BEFriend(1,"Anders","42435049","AEPM@hotmail.com", true, "WWW.EUROPARK.DK", Date()),
        BEFriend(2,"Kim","21218214", "KHB", true, "", null),
        BEFriend(3,"Lars","32554788", "LVPH",true,"", null),
        BEFriend(4,"Hassan","52515333", "HOM",true,"", null),
        BEFriend(5,"Henrik","21211082", "HDM",false,"", null),
        BEFriend(6,"Nikolaj","31311082", "LPH",true,"lenschow.dk", null),
        BEFriend(7,"Jacob","42410210", "JLV",true,"", null),
        BEFriend(8,"Martin","42435049", "MLV",false,"", null),
        )

    fun addFriend(friend: BEFriend){
        friend.id = id++;
        this.friends.add(friend)
    }

    fun updateFriend(friend: BEFriend){
        var index: Int = this.friends.indexOfFirst { f -> f.id === friend.id }
        this.friends[index] = friend
    }

    fun deleteFriend(friend: BEFriend){
        var index: Int = this.friends.indexOfFirst { f -> f.id === friend.id }
        friends.removeAt(index)
    }

    fun getAll(): List<BEFriend> {
        return this.friends.toList().sortedBy { friend -> friend.name }
    }

    fun getAllNames(): Array<String>{
        return this.friends.map { friend -> friend.name }.toTypedArray()
    }

}