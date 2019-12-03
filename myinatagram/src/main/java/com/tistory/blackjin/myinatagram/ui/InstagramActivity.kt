package com.tistory.blackjin.myinatagram.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.tistory.blackjin.myinatagram.R
import com.tistory.blackjin.myinatagram.databinding.ActivityInstagramBinding
import com.tistory.blackjin.myinatagram.ui.photo.PhotoFragment
import timber.log.Timber
import java.io.File

/**
 * https://github.com/ParkSangGwon/TedImagePicker
 * https://github.com/Yalantis/uCrop
 */
class InstagramActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstagramBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_instagram
        )

        initTopBar()
        initBottomNav()
        setFragment()
    }

    private fun initTopBar() {
        binding.tvCancel.setOnClickListener {
            finish()
        }
        binding.tvDone.setOnClickListener {
            supportFragmentManager.findFragmentById(R.id.fl_fragment)?.let { fragment ->
                if (fragment is OnShowImageListener) {
                    fragment.onShowImageUrls()
                }
            }
        }
    }

    private fun initBottomNav() {
        binding.tvLibrary.isSelected = true
        binding.tvTakePhoto.setOnClickListener {
            takePhoto()
        }
    }

    private fun setFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fl_fragment, PhotoFragment.newInstance())
            .commit()
    }

    private var tempFile: File? = null

    private fun takePhoto() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        tempFile = createImageFile()

        tempFile?.let { file ->
            Timber.d("tempFile : ${file.absolutePath}")

            val photoUri = FileProvider.getUriForFile(
                this, "${packageName}.provider", file
            )

            Timber.d("photoUri : $photoUri")

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(intent, PICK_FROM_CAMERA)
        }

    }

    private fun createImageFile(): File {

        // 이미지 파일 이름 ( blackJin_ )
        val imageFileName = "blackJin_"

        // 이미지가 저장될 폴더
        val storageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath)

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    companion object {

        private const val PICK_FROM_CAMERA = 1001

        const val EXTRA_PHOTO_URI = "EXTRA_PHOTO_URI"

        const val EXTRA_PHOTO_URI_LIST = "EXTRA_PHOTO_URI_LIST"
    }
}
