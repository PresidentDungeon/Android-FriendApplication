package com.easv.aepm.friendapplication.GUI

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.easv.aepm.friendapplication.DAL.FriendRepository
import com.easv.aepm.friendapplication.R
import com.easv.aepm.friendapplication.data.BEFriend
import com.easv.aepm.friendapplication.data.IntentValues
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class MapActivity : AppCompatActivity(), OnMapReadyCallback{

    private val TAG = "xyz"
    private var lastSelectedMaker: Marker? = null
    private val Denmark = LatLng(55.53411, 10.52368)
    private var friends = mutableListOf<BEFriend>()
    private var friendsName = mutableListOf("Select Friend")
    private var userTouch = false
    private lateinit var mMap: GoogleMap
    private lateinit var friendRepository: FriendRepository

    private var mLocationManager: LocationManager? = null
    private var locationListener: LocationListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        friendRepository = FriendRepository.get()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        spSelection.setOnTouchListener(View.OnTouchListener { v, event ->
            this.userTouch = true; false
        })
        spSelection.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {

                if (userTouch && position != 0) {

                    val selectedFriend: BEFriend = friends[position - 1]
                    val location = LatLng(
                        selectedFriend.location!!.latitude,
                        selectedFriend.location!!.longitude
                    )
                    val viewPoint = CameraUpdateFactory.newLatLngZoom(location, 12f)
                    mMap.animateCamera(viewPoint)

                    userTouch = false;
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    fun setSpinnerData(){
        val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            this.friendsName.toTypedArray()
        )
        spSelection.adapter = spinnerArrayAdapter
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        if(intent.extras != null)
        {
            spSelection.isVisible = false
            var friend: BEFriend = intent.extras!!.getSerializable("FRIEND")!! as BEFriend
            var friendLocation: Location = intent.extras!!.getParcelable("FriendLocation")!!
            var currentLocation: Location = intent.extras!!.getParcelable("CurrentLocation")!!


            val height = 70
            val width = 70
            val bitmapFriend = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.resources, R.drawable.location), width, height, false)
            val bitmapCurrent = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.resources, R.drawable.current), width, height, false)

            mMap.addMarker(MarkerOptions().position(LatLng(friendLocation.latitude, friendLocation.longitude)).title(friend.name).icon(BitmapDescriptorFactory.fromBitmap(bitmapFriend)))
            lastSelectedMaker = mMap.addMarker(MarkerOptions().position(LatLng(currentLocation.latitude, currentLocation.longitude)).title(friend.name).icon(BitmapDescriptorFactory.fromBitmap(bitmapCurrent)))

            this.mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lastSelectedMaker?.let { it.remove()}
                    lastSelectedMaker = mMap.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("Current Location").icon(BitmapDescriptorFactory.fromBitmap(bitmapCurrent)))
                }
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation.latitude, currentLocation.longitude), 18f))
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener!!)
        }

        else
        {
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
                "SELECT * FROM BEFriend WHERE location ORDER BY name ASC",
                emptyArray()
            ) }

            getDataJob.invokeOnCompletion { _ ->
                this.friends = getDataJob.getCompleted().toMutableList()

                friends.forEach { friend ->
                    this.runOnUiThread {
                        val marker: Marker = mMap.addMarker(MarkerOptions().position(LatLng(friend.location!!.latitude, friend.location!!.longitude)).title(friend.name))
                        marker.tag = friend
                        this.friendsName.add(friend.name)
                    }
                }

                this.runOnUiThread {setSpinnerData()}
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Denmark, 6f))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_UPDATE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_UPDATE") as BEFriend
            val location = data?.extras?.getParcelable<Location>("Location")
            friend.location = location
            GlobalScope.async { friendRepository.updateFriend(friend) }

            this.lastSelectedMaker?.let { it.tag = friend; it.title = friend.name; it.position = LatLng(
                friend.location!!.latitude,
                friend.location!!.longitude
            )}

            val friendLocation = this.friends.indexOfFirst { f -> f.id === friend.id }
            this.friends[friendLocation] = friend
            this.friendsName[friendLocation + 1] = friend.name

            this.friendsName.removeAt(0)
            this.friends.sortBy{ it.name }
            this.friendsName.sort()
            this.friendsName.add(0, "Select Friend")

            setSpinnerData()
        }

        else if(requestCode == IntentValues.REQUEST_DETAIL.code && resultCode == IntentValues.RESPONSE_DETAIL_DELETE.code) {
            val friend = data?.extras?.getSerializable("FRIEND_DELETE") as BEFriend

            GlobalScope.async { friendRepository.deleteFriend(friend) }
            this.lastSelectedMaker?.let { it.remove()}
            val friendLocation = this.friends.indexOfFirst { f -> f.id === friend.id }
            this.friends.removeAt(friendLocation)
            this.friendsName.removeAt(friendLocation + 1)
            setSpinnerData()
        }
    }

    override fun onStop() {
        locationListener?.let { mLocationManager?.removeUpdates(it) }
        super.onStop()
    }
}