package com.wit.jasonfagerberg.nightsout.addDrink.scanBarcode

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.main.MainActivity
import java.io.File
import java.io.FileOutputStream

class ScanBarcodeActivity : AppCompatActivity() {
    private var mCamera: Camera? = null
    private lateinit var camView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_scan_barcode)

        val x = findViewById<ImageView>(R.id.scan_barcode_close)
        x.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FRAGMENT_ID", 4)
            startActivity(intent)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        mCamera = getCameraInstance()

        camView = CameraView(this, mCamera!!)
        val fl = findViewById<FrameLayout>(R.id.layout_scan_barcode)
        fl.addView(camView)

        getCameraImage()

        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_UPC_A,
                        FirebaseVisionBarcode.FORMAT_UPC_E).build()

        super.onResume()
    }

    override fun onPause() {
        mCamera?.release()
        mCamera = null
        super.onPause()
    }

    private fun getCameraInstance(): Camera? {
        try {
            return Camera.open()
        } catch (e: Exception) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FRAGMENT_ID", 4)
            intent.putExtra("ERROR_MESSAGE", "failed to open camera")
            startActivity(intent)
            e.printStackTrace()
        }
        return null
    }
}
