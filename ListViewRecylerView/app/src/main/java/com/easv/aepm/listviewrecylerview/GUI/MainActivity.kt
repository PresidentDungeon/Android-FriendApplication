package com.easv.aepm.listviewrecylerview.GUI

import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.easv.aepm.listviewrecylerview.R
import com.easv.aepm.listviewrecylerview.data.FriendRepository
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val friends: FriendRepository = FriendRepository()
        val friendNames = friends.getAllNames()

        val adapter: ListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, friendNames)

        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val name = FriendRepository().getAll()[position].name
            Toast.makeText(this, "Hi $name", Toast.LENGTH_SHORT).show()
        }

        setUI(adapter)
    }

    fun setUI(adapter: ListAdapter){
        listView.adapter = adapter
    }
}

