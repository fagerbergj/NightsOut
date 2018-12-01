package com.wit.jasonfagerberg.nightsout.addDrink.scanBarcode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.otaliastudios.cameraview.CameraListener
import kotlinx.android.synthetic.main.activity_scan_barcode.*

class ScanBarcodeActivity : BaseCameraActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                val bitmap = jpeg?.size?.let { BitmapFactory.decodeByteArray(jpeg, 0, it) }
                bitmap?.let { runBarcodeScanner(it) }
                showPreview()
                imagePreview.setImageBitmap(bitmap)
            }
        })
    }

    private fun runBarcodeScanner(bitmap: Bitmap) {
        //Create a FirebaseVisionImage
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        //Optional : Define what kind of barcodes you want to scan
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        //Detect all kind of barcodes
                        FirebaseVisionBarcode.FORMAT_ALL_FORMATS
                        //Or specify which kind of barcode you want to detect
                        /*
                            FirebaseVisionBarcode.FORMAT_QR_CODE,
                        FirebaseVisionBarcode.FORMAT_AZTEC
                         */
                )
                .build()

        //Get access to an instance of FirebaseBarcodeDetector
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

        //Use the detector to detect the labels inside the image
        detector.detectInImage(image)
                .addOnSuccessListener {
                    Log.v("ScanBarcodeActivity", "Success! ${it.size}")
                    // Task completed successfully
                    for (firebaseBarcode in it) {
                        Log.v("ScanBarcodeActivity", "UPC type: ${firebaseBarcode.valueType}")

                        Toast.makeText(baseContext, "UPC type: ${firebaseBarcode.valueType}", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    progressBar.visibility = View.GONE
                    Toast.makeText(baseContext, "Sorry, something went wrong!", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    progressBar.visibility = View.GONE
                }

    }

    override fun onClick(v: View?) {
        progressBar.visibility = View.VISIBLE
        cameraView.captureSnapshot()
    }

}
