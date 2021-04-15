package com.easv.aepm.friendapplication.data

import android.app.Application
import com.easv.aepm.friendapplication.DAL.FriendRepository

class FriendApplication : Application() {

    //On app startup initialize repository
    override fun onCreate() {
        super.onCreate()
        FriendRepository.initialize(this)
    }
}

