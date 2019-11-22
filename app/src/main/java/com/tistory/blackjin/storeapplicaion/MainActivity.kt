package com.tistory.blackjin.storeapplicaion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tistory.blackjin.myinatagram.ui.InstagramActivity
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val permissionCheckCamera by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
    }

    /**
     * WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE
     * 둘 중 하나만 받으면 적용 됩니다.
     */
    private val permissionCheckWrite by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    private val permissionCheckRead by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGallery.setOnClickListener {
            TedImagePicker.with(this)
                .start { uri -> showSingleImage(uri) }
        }

        btnCamera.setOnClickListener {
            TedImagePicker.with(this)
                .startMultiImage { uriList -> showMultiImage(uriList) }
        }

        btnInstagram.setOnClickListener {
            chkPermission()
        }


    }

    private fun showSingleImage(uri: Uri) {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
        imageView.setImageBitmap(bitmap)
    }

    private fun showMultiImage(uris: List<Uri>) {

        val uri = uris[0]

        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
        imageView.setImageBitmap(bitmap)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Timber.d("requestCode : $requestCode , grantResults : ${grantResults.contentToString()}")
        if (requestCode == REQUEST_PERMISSION) {
            for (value in grantResults) {
                if (value != PackageManager.PERMISSION_GRANTED) {
                    Timber.e("permission reject")
                    return
                }
            }
            goToInstagram()
        }
    }

    private fun chkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (permissionCheckCamera == PackageManager.PERMISSION_DENIED
                || permissionCheckRead == PackageManager.PERMISSION_DENIED
                || permissionCheckWrite == PackageManager.PERMISSION_DENIED
            ) {

                // 권한 없음
                showRequestPermission()

            } else {

                // 권한 있음
                Timber.e("---- already have permission ----")
                goToInstagram()

            }
        }
    }

    private fun showRequestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION
        )
    }

    private fun goToInstagram() {
        startActivity(
            Intent(this, InstagramActivity::class.java)
        )
    }

    companion object {
        private const val REQUEST_PERMISSION = 1000
    }
}
