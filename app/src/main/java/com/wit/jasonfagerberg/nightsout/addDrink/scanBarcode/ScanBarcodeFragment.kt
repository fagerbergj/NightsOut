package com.wit.jasonfagerberg.nightsout.addDrink.scanBarcode

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.hardware.Camera
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView

import com.wit.jasonfagerberg.nightsout.R
import com.wit.jasonfagerberg.nightsout.main.MainActivity

class ScanBarcodeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_scan_barcode, container, false)
        activity!!.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        activity!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        activity!!.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        view.findViewById<ImageView>(R.id.scan_barcode_close).setOnClickListener {
            (context as MainActivity).onBackPressed()
        }

        return view
    }

    private fun getCameraInstance(): Camera? {
        var camId = -1
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                camId = i
            }
        }
        return try {
            Camera.open(camId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
