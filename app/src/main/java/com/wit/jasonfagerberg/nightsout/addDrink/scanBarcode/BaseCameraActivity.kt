package com.wit.jasonfagerberg.nightsout.addDrink.scanBarcode

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.wit.jasonfagerberg.nightsout.R
import kotlinx.android.synthetic.main.activity_scan_barcode.*

abstract class BaseCameraActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_barcode)
        btnRetry.setOnClickListener {
            if (cameraView.visibility == View.VISIBLE) showPreview() else hidePreview()
        }
        cameraView.setLifecycleOwner(this)
        fab_take_photo.setOnClickListener(this)
    }

    protected fun showPreview() {
        framePreview.visibility = View.VISIBLE
        cameraView.visibility = View.GONE
    }

    protected fun hidePreview() {
        framePreview.visibility = View.GONE
        cameraView.visibility = View.VISIBLE
    }
}