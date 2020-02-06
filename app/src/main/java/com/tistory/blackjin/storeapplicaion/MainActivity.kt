package com.tistory.blackjin.storeapplicaion

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
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

        btnInstagram.setOnClickListener {
            chkPermission()
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

    private fun goToInstagram() {
        startActivityForResult(
            Intent(this, InstagramActivity::class.java),
            REQUEST_INSTAGRAM
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("requestCode : $requestCode , resultCode : $resultCode , data : $data")
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_INSTAGRAM) {
                val uri: Uri? = data?.getParcelableExtra(InstagramActivity.EXTRA_PHOTO_URI)
                val uris: List<Uri>? = data?.getParcelableArrayListExtra(InstagramActivity.EXTRA_PHOTO_URI_LIST)
                Timber.d("uri : $uri")
                Timber.d("uris : $uris")

                //단일 선택 모드 일 떄 받아온 값
                if (uri != null) {
                    showSingleImage(uri)
                }

                //다중 선택 모드 일 떄 받아온 값
                if (!uris.isNullOrEmpty()) {
                    showMultiImage(uris)
                }

            }
        }
    }

    private fun showSingleImage(uri: Uri) {
        setImages(listOf(uri))
    }

    private fun showMultiImage(uris: List<Uri>) {
        setImages(uris)
    }

    private fun setImages(uris: List<Uri>) {
        llImageParent.removeAllViews()

        uris.forEach {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(this.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            } else {
                contentResolver.openInputStream(it)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }

            val parent = RelativeLayout(this).apply {
                layoutParams =
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(0, 0, 10, 0)
                    }
            }

            val imageView = ImageView(this).apply {
                layoutParams =
                    RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.CENTER_IN_PARENT)
                    }
                setImageBitmap(bitmap)
            }

            val sizeText = TextView(this).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)

                val info = "width : ${bitmap?.width}\nheight : ${bitmap?.height}"
                text = info
            }

            parent.addView(imageView)
            parent.addView(sizeText)

            llImageParent.addView(parent)
        }
    }

    companion object {
        private const val REQUEST_PERMISSION = 1000
        private const val REQUEST_INSTAGRAM = 1001
    }
}
