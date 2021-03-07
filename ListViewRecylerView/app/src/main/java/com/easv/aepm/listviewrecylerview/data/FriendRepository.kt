package com.easv.aepm.listviewrecylerview.data

class FriendRepository {

    private var id: Int = 8
    private val friends: MutableList<BEFriend> = mutableListOf<BEFriend>(
        BEFriend(1,"Anders","42435049","AEPM", true),
        BEFriend(2,"Kim","21218214", "KHB", true),
        BEFriend(3,"Lars","32554788", "LVPH",true),
        BEFriend(4,"Hassan","52515333", "HOM",true),
        BEFriend(5,"Henrik","21211082", "HDM",false),
        BEFriend(6,"Nikolaj","31311082", "LPH",true),
        BEFriend(7,"Jacob","42410210", "JLV",true),
        BEFriend(8,"Martin","42435049", "MLV",false),
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