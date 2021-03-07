package com.easv.aepm.listviewrecylerview.GUI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.easv.aepm.listviewrecylerview.R
import com.easv.aepm.listviewrecylerview.data.BEFriend
import com.easv.aepm.listviewrecylerview.data.FriendRepository
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val friends: FriendRepository = FriendRepository()
        val friendNames = friends.getAllNames()
        val listAdapter = SimpleAdapter(this, asListMap(friends.getAll().toTypedArray()),
            R.layout.cell, arrayOf("name", "alias", "number"), intArrayOf(
                R.id.name,
                R.id.alias,
                R.id.phone
            ))

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val name = FriendRepository().getAll()[position].name
                Toast.makeText(this, "Hi $name", Toast.LENGTH_SHORT).show()
            }

        setUI(listAdapter)
    }

    private fun asListMap(source: Array<BEFriend>): List<Map<String, String?>>{
        return source.map { person -> hashMapOf("name" to person.name, "alias" to "(${person.alias})", "number" to person.number) }
    }

    private fun setUI(adapter: ListAdapter){
        listView.adapter = adapter
    }
}


