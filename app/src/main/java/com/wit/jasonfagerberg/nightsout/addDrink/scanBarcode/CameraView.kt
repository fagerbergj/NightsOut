package com.wit.jasonfagerberg.nightsout.addDrink.scanBarcode

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Environment
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File
import java.io.FileOutputStream

//todo add overlay

class CameraView(context: Context, private val mCamera: Camera) : SurfaceView(context), SurfaceHolder.Callback {

    private val mHolder: SurfaceHolder = holder.apply {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        addCallback(this@CameraView)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        mCamera.apply {
            setPreviewDisplay(holder)
            startPreview()
        }
    }

    //taken care of in activity
    override fun surfaceDestroyed(holder: SurfaceHolder) {holder.removeCallback(this@CameraView)}

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }
        mCamera.setDisplayOrientation(90)

        // start preview with new settings
        mCamera.apply {
            setPreviewDisplay(mHolder)
            startPreview()
        }
    }
}