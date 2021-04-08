package com.easv.aepm.listviewrecylerview.GUI

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.easv.aepm.listviewrecylerview.DAL.FriendRepository
import com.easv.aepm.listviewrecylerview.R
import com.easv.aepm.listviewrecylerview.data.BEFriend
import com.easv.aepm.listviewrecylerview.data.IntentValues
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class MapActivity : AppCompatActivity(), OnMapReadyCallback{

    private val TAG = "xyz"
    private var lastSelectedMaker: Marker? = null
    private val Denmark = LatLng(55.53411, 10.52368)
    private lateinit var mMap: GoogleMap
    private lateinit var friendRepository: FriendRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        friendRepository = FriendRepository.get()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.setOnMarkerClickListener { marker ->
            this.lastSelectedMaker = marker
            val friend: BEFriend = marker.tag as BEFriend
            val intent = Intent(this, DetailActivity::class.java)
            val location: Location? = friend.location
            friend.location = null
            intent.putExtra("FRIEND", friend)
            intent.putExtra("Location", location)
            startActivityForResult(intent, IntentValues.REQUEST_DETAIL.code)
            friend.location = location

            false
        }

        val getDataJob = GlobalScope.async { friendRepository.getFriends(
            "SELECT * FROM BEFriend WHERE location",
            emptyArray()
        ) }

        getDataJob.invokeOnCompletion { _ ->
            val friends: List<BEFriend> = getDataJob.getCompleted()
            friends.forEach { friend ->
                    this.runOnUiThread { val marker: Marker = mMap.addMarker(MarkerOptions().position(LatLng(friend.location!!.latitude, friend.location!!.longitude)).title(friend.name));
                    marker.tag = friend}
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Denmark, 6f))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_UPDATE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_UPDATE") as BEFriend
            val location = data?.extras?.getParcelable<Location>("Location")
            friend.location = location
            GlobalScope.async { friendRepository.updateFriend(friend) }

            this.lastSelectedMaker?.let { it.tag = friend; it.title = friend.name; it.position = LatLng(friend.location!!.latitude, friend.location!!.longitude) }
        }

        else if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_DELETE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_DELETE") as BEFriend

            GlobalScope.async { friendRepository.deleteFriend(friend) }
            this.lastSelectedMaker?.let { it.remove()}
        }
    }
}