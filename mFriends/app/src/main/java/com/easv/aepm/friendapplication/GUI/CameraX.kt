package com.easv.aepm.friendapplication.GUI

import android.app.Activity
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.easv.aepm.friendapplication.R
import kotlinx.android.synthetic.main.activity_camera_x.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraX : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null //A modifiable image capture use case
    private lateinit var outputDirectory: File //Placement of file
    private lateinit var lastImage: File //Reference for last image taken
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA //Select front or back camera

    private var mediaPlayer = MediaPlayer() //Plays media

    // Initializer for onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_x)

        startCamera()

        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener { takePhoto() }
        camera_accept_button.setOnClickListener { view -> acceptImage() }
        camera_retake_button.setOnClickListener { view -> undoImage() }
        camera_rotate_button.setOnClickListener { view ->
            cameraSelector = if(cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
            startCamera() }

        if(intent.extras != null){
            outputDirectory = intent.extras?.getSerializable("FILEPATH") as File
        }
    }

    //Takes the photo
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        this.lastImage = File(outputDirectory, "")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(lastImage).build()

        // Set up image capture listener, which is triggered after photo has been taken
        playAudio()
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    ivDisplay.setImageURI(Uri.fromFile(lastImage))
                    imagePreview.visibility = View.INVISIBLE
                    photoDetail.visibility = View.VISIBLE
                }
            })
    }

    // Accepts the image and closes activity
    private fun acceptImage() {
        Toast.makeText(this, "Image updated", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // Undoes the image and deletes image
    private fun undoImage() {
        imagePreview.visibility = View.VISIBLE
        photoDetail.visibility = View.INVISIBLE

        try{ lastImage.delete(); ivDisplay.setImageResource(0)}
        catch (e: java.lang.Exception){}
    }

    // Starts the camera
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }
            imageCapture = ImageCapture.Builder()
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // Plays an audio file (For camera shutter sound)
    private fun playAudio() {
        var uriString: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.camera)

        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, uriString)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
    }
}