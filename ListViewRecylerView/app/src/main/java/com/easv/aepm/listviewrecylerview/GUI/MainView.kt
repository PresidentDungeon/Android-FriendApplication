package com.easv.aepm.listviewrecylerview.GUI

import android.content.Intent
import android.location.Location
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

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
            R.id.openMap -> {openMapActivity(); true}

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

    fun openMapActivity(){
        val intent = Intent(this, MapActivity::class.java)
        startActivityForResult(intent, IntentValues.REQUESTCODE_MAP.code)
    }

    override fun onItemClick(friend: BEFriend, position: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        val location: Location? = friend.location
        friend.location = null
        intent.putExtra("FRIEND", friend)
        intent.putExtra("Location", location)
        startActivityForResult(intent, IntentValues.REQUEST_DETAIL.code)
        friend.location = location
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

        var job: Deferred<Unit>? = null

        if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_CREATE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_CREATE") as BEFriend
            val location = data?.extras?.getParcelable<Location>("Location")
            friend.location = location

            job = GlobalScope.async { friends.addFriend(friend) }
        }

        else if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_UPDATE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_UPDATE") as BEFriend
            val location = data?.extras?.getParcelable<Location>("Location")
            friend.location = location

            job = GlobalScope.async { friends.updateFriend(friend) }
        }

        else if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_DELETE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_DELETE") as BEFriend

            job = GlobalScope.async { friends.deleteFriend(friend) }
        }

        else if(requestCode == IntentValues.REQUESTCODE_MAP.code){
            searchText()
        }

        job?.let {
            job.invokeOnCompletion { _ -> searchText() }
        }
    }
}
