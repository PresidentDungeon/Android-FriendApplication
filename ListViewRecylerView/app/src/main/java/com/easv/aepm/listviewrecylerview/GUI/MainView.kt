package com.easv.aepm.listviewrecylerview.GUI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.easv.aepm.listviewrecylerview.DAL.FriendRepository
import com.easv.aepm.listviewrecylerview.R
import com.easv.aepm.listviewrecylerview.RecyclerAdapter.RecyclerAdapter
import com.easv.aepm.listviewrecylerview.data.BEFriend
import com.easv.aepm.listviewrecylerview.data.IntentValues
import com.easv.aepm.listviewrecylerview.data.Sorting
import com.easv.aepm.listviewrecylerview.data.interfaces.IClickItemListener
import kotlinx.android.synthetic.main.activity_main3.*

class MainView : AppCompatActivity(), IClickItemListener {

    private var friends = FriendRepository.get()
    private lateinit var adapter: RecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        registerForContextMenu(recyclerView)
        btnSearch.setOnClickListener { view -> searchText() }
        cbFavorite.setOnCheckedChangeListener{ view, isChecked -> searchText()}
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecyclerAdapter(this, this)
        recyclerView.adapter = adapter

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.create_friend -> { openCreateActivity(); true }
            R.id.sortName -> {this.adapter.setSortingType(Sorting.SORTING_NAME); searchText(); true}
            R.id.sortAge -> {this.adapter.setSortingType(Sorting.SORTING_AGE); searchText(); true}

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun searchText(){
        val text: String = searchBar.text.toString()
        val favoriteEnabled = cbFavorite.isChecked
        adapter.filter(text, favoriteEnabled)
    }

    fun openCreateActivity(){
        val intent = Intent(this, DetailActivity::class.java)
        startActivityForResult(intent, IntentValues.REQUEST_DETAIL.code)
    }

    override fun onItemClick(friend: BEFriend, position: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("FRIEND", friend)
        startActivityForResult(intent, IntentValues.REQUEST_DETAIL.code)
    }

    override fun onItemLongClick(friend: BEFriend, position: Int, view: View) {

        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.context_menu);

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.call_friend -> {
                    var intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:(+45)${friend.number}"))
                    startActivity(intent);
                    true
                }
                R.id.text_friend -> {
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_CREATE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_CREATE") as BEFriend
            this.friends.addFriend(friend)
//            searchText()
        }

        else if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_UPDATE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_UPDATE") as BEFriend
            this.friends.updateFriend(friend)
//            searchText()
        }

        else if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_DELETE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_DELETE") as BEFriend
            this.friends.deleteFriend(friend)
//            searchText()
        }


        this.adapter.filter("", false)
    }
}
