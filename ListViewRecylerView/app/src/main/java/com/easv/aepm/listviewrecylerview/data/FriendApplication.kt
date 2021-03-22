package com.easv.aepm.listviewrecylerview.data

import android.app.Application
import com.easv.aepm.listviewrecylerview.DAL.FriendRepository

class FriendApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FriendRepository.initialize(this)
    }
}

