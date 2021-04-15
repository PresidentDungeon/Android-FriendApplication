package com.easv.aepm.friendapplication.data.interfaces

import android.view.View
import com.easv.aepm.friendapplication.data.BEFriend

interface IClickItemListener {
    fun onItemClick(friend: BEFriend, position: Int)
    fun onItemLongClick(friend: BEFriend, position: Int, view: View)
}

