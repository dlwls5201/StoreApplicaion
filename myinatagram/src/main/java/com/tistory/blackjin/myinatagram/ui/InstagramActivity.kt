package com.tistory.blackjin.myinatagram.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.tistory.blackjin.myinatagram.R
import com.tistory.blackjin.myinatagram.databinding.ActivityInstagramBinding
import com.tistory.blackjin.myinatagram.ui.photo.OnShowImageListener
import com.tistory.blackjin.myinatagram.ui.photo.PhotoFragment
import timber.log.Timber
import java.io.File

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
                    fragment.onShowImageUrl()
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

    //카메라로부터 받아온 이미지는 여기에 저장됩니다.
    private var tempFile: File? = null

    private fun takePhoto() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        tempFile = createImageFile()

        tempFile?.let { file ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                val photoUri = FileProvider.getUriForFile(
                    this, "${packageName}.provider", file
                )

                Timber.d("file provider photoUri : $photoUri")

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)

            } else {

                val photoUri = Uri.fromFile(file)

                Timber.d("photoUri : $photoUri")

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
        }

    }

    internal fun createImageFile(): File {

        // 이미지 파일 이름 ( blackJin_ )
        val imageFileName = "blackJin_"

        // 이미지가 저장될 폴더
        val storageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath)

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("requestCode : $requestCode , resultCode : $resultCode , data ; $data , tempFile : ${tempFile?.exists()}")
        if(requestCode == PICK_FROM_CAMERA) {
            tempFile?.let{ file ->
                if(resultCode == Activity.RESULT_OK) {

                    //카메라에서 찍은 사진 파일은 사이즈가 크므로 후처리를 해주는게 좋습니다.
                    val uri = file.toUri()
                    finishWithResultUri(uri)

                } else {

                    //카메라 작업이 취소 되었으므로 생성한 임시 파일을 제거해 줍니다.
                    if(file.exists()) {
                        file.delete()
                    }
                    Toast.makeText(this, "취소", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    internal fun finishWithResultUri(uri: Uri) {
        setResult(
            Activity.RESULT_OK, Intent()
                .putExtra(EXTRA_PHOTO_URI, uri)
        )
        finish()
    }

    internal fun finishWithResultUri(uris: ArrayList<Uri>) {
        setResult(
            Activity.RESULT_OK, Intent()
                .putParcelableArrayListExtra(EXTRA_PHOTO_URI_LIST, uris)
        )
        finish()
    }

    companion object {

        private const val PICK_FROM_CAMERA = 1001

        const val EXTRA_PHOTO_URI = "EXTRA_PHOTO_URI"

        const val EXTRA_PHOTO_URI_LIST = "EXTRA_PHOTO_URI_LIST"
    }
}
