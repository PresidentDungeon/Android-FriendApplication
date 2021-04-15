package com.easv.aepm.friendapplication.GUI

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.easv.aepm.friendapplication.DAL.FriendRepository
import com.easv.aepm.friendapplication.R
import com.easv.aepm.friendapplication.RecyclerAdapter.RecyclerAdapter
import com.easv.aepm.friendapplication.data.BEFriend
import com.easv.aepm.friendapplication.data.IntentValues
import com.easv.aepm.friendapplication.data.Sorting
import com.easv.aepm.friendapplication.data.interfaces.IClickItemListener
import kotlinx.android.synthetic.main.activity_main3.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class MainView : AppCompatActivity(), IClickItemListener {

    private var friendRepo = FriendRepository.get() // Get the friend repository
    private lateinit var adapter: RecyclerAdapter // Adapter for recyclerview

    // Creates the menu and inserts adapter for recyclerview
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

    // Creates the options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // What to do when selecting action from option menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.create_friend -> { openCreateActivity(); true }
            R.id.sortName -> {this.adapter.setSortingType(Sorting.SORTING_NAME); searchText(); true}
            R.id.sortAge -> {this.adapter.setSortingType(Sorting.SORTING_AGE); searchText(); true}
            R.id.openMap -> {openMapActivity(); true}

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Search function
    fun searchText(){
        val text: String = searchBar.text.toString()
        val favoriteEnabled = cbFavorite.isChecked
        adapter.filter(text, favoriteEnabled)
    }

    // Open details activity to create new friend
    fun openCreateActivity(){
        val intent = Intent(this, DetailActivity::class.java)
        startActivityForResult(intent, IntentValues.REQUEST_DETAIL.code)
    }

    // Open map
    fun openMapActivity(){
        val intent = Intent(this, MapActivity::class.java)
        startActivityForResult(intent, IntentValues.REQUESTCODE_MAP.code)
    }

    // Open detail view with selected friend
    override fun onItemClick(friend: BEFriend, position: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        val location: Location? = friend.location
        friend.location = null
        intent.putExtra("FRIEND", friend)
        intent.putExtra("Location", location)
        startActivityForResult(intent, IntentValues.REQUEST_DETAIL.code)
        friend.location = location
    }

    // Longclick to quickly call or text selected friend
    override fun onItemLongClick(friend: BEFriend, position: Int, view: View) {

        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.context_menu);

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.call_friend -> {
                    var intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:(+45)${friend.number}"))
                    startActivity(intent)
                    true
                }
                R.id.text_friend -> {
                    var intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${friend.number}"))
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    // Operation for create, update or delete to database
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var job: Deferred<Unit>? = null

        if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_CREATE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_CREATE") as BEFriend
            val location = data?.extras?.getParcelable<Location>("Location")
            friend.location = location

            job = GlobalScope.async { friendRepo.addFriend(friend) }
        }

        else if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_UPDATE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_UPDATE") as BEFriend
            val location = data?.extras?.getParcelable<Location>("Location")
            friend.location = location

            job = GlobalScope.async { friendRepo.updateFriend(friend) }
        }

        else if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_DELETE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_DELETE") as BEFriend

            job = GlobalScope.async { friendRepo.deleteFriend(friend) }
        }

        else if(requestCode == IntentValues.REQUESTCODE_MAP.code){
            searchText()
        }

        job?.let {
            job.invokeOnCompletion { _ -> searchText() }
        }
    }
}
