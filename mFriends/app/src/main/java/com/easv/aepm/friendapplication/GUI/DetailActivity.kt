package com.easv.aepm.friendapplication.GUI

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.easv.aepm.friendapplication.BuildConfig
import com.easv.aepm.friendapplication.R
import com.easv.aepm.friendapplication.data.BEFriend
import com.easv.aepm.friendapplication.data.IntentValues
import kotlinx.android.synthetic.main.activity_detail.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class DetailActivity : AppCompatActivity() {

    lateinit var friend: BEFriend // Friend sent from MainActivity
    val myCalendar: Calendar = Calendar.getInstance() // Calender for birthday
    var updatedDate: Boolean = false // To update birthday
    val PERMISSION_REQUEST_CODE_CAMERA = 1 // Request code for camera
    val PERMISSION_REQUEST_CODE_GPS = 2 // Request code for GPS
    val PERMISSION_REQUEST_CODE_GPS_DISTANCE = 3 // Request code for distance to friend
    val PERMISSION_REQUEST_CODE_GPS_MAP = 4 // Request code for map
    var mFile: File? = null // Temp file for inserting picture
    var mLocation: Location? = null // Current location on request
    var mLocationManager: LocationManager? = null // Manager to request current location

    // A date listener. Updates date-label, when new date is selected
    var date = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar[Calendar.YEAR] = year
            myCalendar[Calendar.MONTH] = monthOfYear
            myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            this.updatedDate = true
            updateLabel()
        }

    // Initialize listeners for the new/update form
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        tvName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { validateFriend() }
        })
        tvMail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { validateFriend() }
        })
        tvNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { validateFriend() }
        })

        imgCallFriend.setOnClickListener { view -> var intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${friend.number}")); startActivity(intent);}
        imgTextFriend.setOnClickListener { view -> var intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${friend.number}")); startActivity(intent);}
        imgMailFriend.setOnClickListener { view -> sendMail()}
        imgLinkFriend.setOnClickListener { view -> goToLink()}
        tvBirthday.setOnTouchListener { v, event -> if(event.action == MotionEvent.ACTION_UP){openPopup()}; true }
        ivImage.setOnClickListener { view -> checkCameraPermission()}
        btnGPS.setOnClickListener { view -> checkGPSPermission(LocationAction.GPS_LOCATION)}
        tvDistance.setOnClickListener{view -> checkGPSPermission(LocationAction.DISTANCE)}
        btnMap.setOnClickListener{view -> checkGPSPermission(LocationAction.GPS_LOCATION_MAP)}

        tvLink.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { validateFriend() }
        })

        if(intent.extras == null){
            imgCallFriend.isVisible = false
            imgTextFriend.isVisible = false
            imgLinkFriend.isVisible = false
            imgMailFriend.isVisible = false
        }
        else{
            btnCreate.isVisible = false
            updateLayout.isVisible = true
            friend = intent.extras?.getSerializable("FRIEND") as BEFriend
            mLocation = intent.extras?.getParcelable("Location")
            initializeText(friend)
            this.mFile = File(this.friend.image)
            if (this.mFile!!.exists()) {
                ivImage.setImageURI(Uri.fromFile(mFile))
            }
        }
    }

    // Validate required fields for create and update
    fun validateFriend(){
        if(tvName.text.isNotEmpty() && tvMail.text.isNotEmpty() && tvNumber.text.isNotEmpty()){
            btnCreate.isEnabled = true
            btnUpdate.isEnabled = true
        }
        else{
            btnCreate.isEnabled = false
            btnUpdate.isEnabled = false
        }
    }

    // Set text in form on friend selection
    fun initializeText(friend: BEFriend){
        tvName.setText(friend.name)
        tvMail.setText(friend.mail)
        tvNumber.setText(friend.number)
        tvLink.setText(friend.url)
        isFavorite.isChecked = friend.isFavorite

        if(friend.birthdate != null){
            this.updatedDate = true
            this.myCalendar.time = friend.birthdate
            updateLabel()
        }

        if(mLocation != null){
            btnGPS.text = "${this.mLocation!!.latitude}, ${this.mLocation!!.longitude}"
            tvDistance.isVisible = true
            btnMap.isVisible = true
        }
    }

    // Creates friend entity and finishes activity
    fun createFriend(view: View) {
        val name = tvName.text.toString()
        val mail = tvMail.text.toString()
        val number = tvNumber.text.toString()
        val url = tvLink.text.toString()
        val isFavorite = isFavorite.isChecked
        val friend = BEFriend(0, name, number, mail, isFavorite, url, if (this.updatedDate) myCalendar.time else null, if (this.mFile != null && this.mFile!!.exists()) mFile!!.path else "", null)

        val intent = Intent()
        intent.putExtra("FRIEND_CREATE", friend)
        intent.putExtra("Location", mLocation)
        setResult(IntentValues.RESPONSE_DETAIL_CREATE.code, intent)
        finish()
    }

    // Updates friend entity and finishes activity
    fun updateFriend(view: View) {
        friend.name = tvName.text.toString()
        friend.mail = tvMail.text.toString()
        friend.number = tvNumber.text.toString()
        friend.url = tvLink.text.toString()
        friend.isFavorite = isFavorite.isChecked
        friend.birthdate = if (this.updatedDate) myCalendar.time else null
        friend.image = if (this.mFile != null && this.mFile!!.exists()) mFile!!.path else ""

        val intent = Intent()
        intent.putExtra("FRIEND_UPDATE", friend)
        intent.putExtra("Location", mLocation)
        setResult(IntentValues.RESPONSE_DETAIL_UPDATE.code, intent)
        finish()
    }

    // Deletes friend entity and finishes activity
    fun deleteFriend(view: View) {
        val intent = Intent()
        intent.putExtra("FRIEND_DELETE", friend)
        setResult(IntentValues.RESPONSE_DETAIL_DELETE.code, intent)
        finish()
    }

    // Open default mail app
    fun sendMail(){
        var emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "plain/text"
        val receivers = arrayOf("${friend.mail}")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, receivers)
        startActivity(emailIntent)
    }

    // Open link in default browser
    fun goToLink(){
        var url = this.friend.url;
        if (!url!!.startsWith("http://") && !url.startsWith("https://")){url = "http://" + url}
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse("${url}"))
        startActivity(intent)
    }

    // Open datepicker popup to select a birthdate
    fun openPopup(){
        DatePickerDialog(
            this, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
            myCalendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    // Sets the selected date in form
    private fun updateLabel() {
        val myFormat = "dd/MM/YYYY"
        val sdf = SimpleDateFormat(myFormat, Locale.GERMAN)
        tvBirthday.setText(sdf.format(myCalendar.time))
    }

    // Checks permission to use camera app, if granted then camera dialog opens to select direct camera or camera app
    private fun checkCameraPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val permissions = mutableListOf<String>()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.CAMERA)
        if (permissions.size > 0)
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE_CAMERA)
        else
            showCameraDialog()
    }

    // Checks permission to use GPS location of the phone and if granted either uses GPS, Maps or Distance
    private fun checkGPSPermission(action: LocationAction){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val permissions = mutableListOf<String>()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permissions.size > 0)
        {
            when(action)
            {
                LocationAction.GPS_LOCATION -> {ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE_GPS); true}
                LocationAction.GPS_LOCATION_MAP -> {ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE_GPS_MAP); true}
                LocationAction.DISTANCE -> {ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE_GPS_DISTANCE); true}
                else -> false
            }
        }

        else
        {
            when(action)
            {
                LocationAction.GPS_LOCATION -> {setGPSLocation(); true}
                LocationAction.GPS_LOCATION_MAP -> {openDistanceMap(); true}
                LocationAction.DISTANCE -> {calculateDistance(); true}
                else -> false
            }
        }
    }

    // Start the camera app
    private fun startCameraActivity() {
        mFile = getOutputMediaFile()
        if (mFile == null) {Toast.makeText(this, "Could not create file...", Toast.LENGTH_LONG).show(); return}

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val applicationId = BuildConfig.APPLICATION_ID
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, "${applicationId}.provider", mFile!!))

        try{
            startActivityForResult(intent, IntentValues.REQUESTCODE_IMAGE_APP.code)
        }
        catch (e: ActivityNotFoundException){
            Toast.makeText(this, "Camera not found!", Toast.LENGTH_LONG).show()
        }

    }

    // Check permission results and takes action if granted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode === PERMISSION_REQUEST_CODE_CAMERA){
            if(grantResults.all { permission ->  permission == PackageManager.PERMISSION_GRANTED}){
                showCameraDialog()
            }
        }

        if (requestCode === PERMISSION_REQUEST_CODE_GPS){
            if(grantResults.all { permission ->  permission == PackageManager.PERMISSION_GRANTED}){
                setGPSLocation()
            }
        }

        if (requestCode === PERMISSION_REQUEST_CODE_GPS_DISTANCE){
            if(grantResults.all { permission ->  permission == PackageManager.PERMISSION_GRANTED}){
                calculateDistance()
            }
        }

        if (requestCode === PERMISSION_REQUEST_CODE_GPS_MAP){
            if(grantResults.all { permission ->  permission == PackageManager.PERMISSION_GRANTED}){
                openDistanceMap()
            }
        }

    }

    // Create file location for image to be taken
    private fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Camera")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val postfix = "jpg"
        val prefix = "IMG"
        return File(mediaStorageDir.path + File.separator + prefix + "_" + timeStamp + "." + postfix)
    }

    // Inserts image in imageview when direct or app camera is done
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            IntentValues.REQUESTCODE_IMAGE_APP.code -> if (resultCode == RESULT_OK) { ivImage.setImageURI(Uri.fromFile(mFile))}
            IntentValues.REQUESTCODE_IMAGE_DIRECT.code -> if (resultCode == RESULT_OK) { ivImage.setImageURI(Uri.fromFile(mFile))}
            else -> false
        }
    }

    // Select use of direct or app camera
    private fun showCameraDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Camera Handling")
        alertDialogBuilder
            .setMessage("Open build-in-camera-app or take picture directly?")
            .setCancelable(true)
            .setPositiveButton("Standard App") { dialog, id -> startCameraActivity() }
            .setNegativeButton("Directly", { dialog, id -> startInCameraActivity() })
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    // Start direct camera
    private fun startInCameraActivity(){
        mFile = getOutputMediaFile()
        if (mFile == null) {Toast.makeText(this, "Could not create file...", Toast.LENGTH_LONG).show(); return}

        val intent = Intent(this, CameraX::class.java)
        intent.putExtra("FILEPATH", mFile)
        startActivityForResult(intent, IntentValues.REQUESTCODE_IMAGE_DIRECT.code)
    }

    // Listener that sets text including position on top of button to display location
    var myLocationListener: LocationListener? = null
    @SuppressLint("MissingPermission")
    private fun setGPSLocation(){

        if(mLocationManager == null){
            this.mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
            if (myLocationListener == null)
                myLocationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        btnGPS.text = "${location.latitude}, ${location.longitude}"
                        mLocationManager!!.removeUpdates(this)
                        mLocation = location
                        tvDistance.isVisible = true
                        btnMap.isVisible = true
                    }
                }
            mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, myLocationListener!!)
    }

    // Listener that calculates distance between current location and friend location
    var mLocationDistanceListener: LocationListener? = null
    @SuppressLint("MissingPermission")
    private fun calculateDistance(){

        if(mLocationManager == null){
            this.mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        if(mLocationDistanceListener != null){
            mLocationManager!!.removeUpdates(mLocationDistanceListener!!)
            mLocationDistanceListener = null
            tvDistance.setText("Click to calculate distance")
            return
        }

        if(mLocationDistanceListener == null){
            mLocationDistanceListener = object : LocationListener{
                override fun onLocationChanged(location: Location) {
                tvDistance.setText("${location.distanceTo(mLocation)}")
            }
        }
    }
        Toast.makeText(this, "Started listening", Toast.LENGTH_SHORT).show()
        mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, mLocationDistanceListener!!)
    }

    // Calculates current location and open map activity to display current location and friend location
    var myLocationDistanceMapListener: LocationListener? = null
    @SuppressLint("MissingPermission")
    private fun openDistanceMap(){

        if(mLocationManager == null){
            this.mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        if (myLocationDistanceMapListener == null)
            myLocationDistanceMapListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {

                    mLocationManager!!.removeUpdates(this)

                    val intent = Intent(baseContext, MapActivity::class.java)

                    val friendName: String = tvName.text.toString()

                    intent.putExtra("FriendName", if(!friendName.isNullOrEmpty()) friendName else "Friend")
                    intent.putExtra("FriendLocation", mLocation)
                    intent.putExtra("CurrentLocation", location)
                    startActivity(intent)
                }
            }
        Toast.makeText(this, "Opening map...", Toast.LENGTH_SHORT).show()
        mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, myLocationDistanceMapListener!!)
    }

    // Stops listening to distance
    override fun onStop() {
        mLocationDistanceListener?.let { calculateDistance() }
        super.onStop()
    }

}

enum class LocationAction{
    GPS_LOCATION, GPS_LOCATION_MAP, DISTANCE
}


