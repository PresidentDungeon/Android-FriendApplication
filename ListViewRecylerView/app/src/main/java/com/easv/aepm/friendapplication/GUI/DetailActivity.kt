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

    lateinit var friend: BEFriend
    val myCalendar: Calendar = Calendar.getInstance()
    var updatedDate: Boolean = false
    val PERMISSION_REQUEST_CODE_CAMERA = 1
    val PERMISSION_REQUEST_CODE_GPS = 2
    val PERMISSION_REQUEST_CODE_GPS_DISTANCE = 3
    var mFile: File? = null
    var mLocation: Location? = null
    var mLocationManager: LocationManager? = null
    var TAG = "DetailA"

    var date = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar[Calendar.YEAR] = year
            myCalendar[Calendar.MONTH] = monthOfYear
            myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            this.updatedDate = true
            updateLabel()
        }

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
        btnGPS.setOnClickListener { view -> checkGPSPermission()}
        tvDistance.setOnClickListener{view -> checkDistancePermission()}

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
        }
    }


    fun createCustomer(view: View) {
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

    fun updateCustomer(view: View) {
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

    fun deleteCustomer(view: View) {
        val intent = Intent()
        intent.putExtra("FRIEND_DELETE", friend)
        setResult(IntentValues.RESPONSE_DETAIL_DELETE.code, intent)
        finish()
    }

    fun sendMail(){
        var emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "plain/text"
        val receivers = arrayOf("${friend.mail}")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, receivers)
        startActivity(emailIntent)
    }

    fun goToLink(){
        var url = this.friend.url;
        if (!url!!.startsWith("http://") && !url.startsWith("https://")){url = "http://" + url}
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse("${url}"))
        startActivity(intent)
    }

    fun openPopup(){
        DatePickerDialog(
            this, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
            myCalendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/YYYY" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.GERMAN)
        tvBirthday.setText(sdf.format(myCalendar.time))
    }

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

    private fun checkGPSPermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val permissions = mutableListOf<String>()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permissions.size > 0)
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE_GPS)
        else
            setGPSLocation()
    }

    private fun checkDistancePermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val permissions = mutableListOf<String>()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permissions.size > 0)
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE_GPS)
        else
            calculateDistance()
    }

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

    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            IntentValues.REQUESTCODE_IMAGE_APP.code -> if (resultCode == RESULT_OK) { ivImage.setImageURI(Uri.fromFile(mFile))}
            IntentValues.REQUESTCODE_IMAGE_DIRECT.code -> if (resultCode == RESULT_OK) { ivImage.setImageURI(Uri.fromFile(mFile))}
            else -> false
        }
    }

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

    private fun startInCameraActivity(){
        mFile = getOutputMediaFile()
        if (mFile == null) {Toast.makeText(this, "Could not create file...", Toast.LENGTH_LONG).show(); return}

        val intent = Intent(this, CameraX::class.java)
        intent.putExtra("FILEPATH", mFile)
        startActivityForResult(intent, IntentValues.REQUESTCODE_IMAGE_DIRECT.code)
    }

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
                    }
                }
            mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, myLocationListener!!)
    }

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

    override fun onStop() {
        mLocationDistanceListener?.let { mLocationManager?.removeUpdates(it) }
        super.onStop()
    }

}

