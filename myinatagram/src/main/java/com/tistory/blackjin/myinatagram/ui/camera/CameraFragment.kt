package com.tistory.blackjin.myinatagram.ui.camera

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.tistory.blackjin.myinatagram.R
import com.tistory.blackjin.myinatagram.databinding.FragmentCameraBinding
import timber.log.Timber
import java.io.File

class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding

    private val PICK_FROM_CAMERA = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.button.setOnClickListener {
            takePhoto()
        }

    }

    private var tempFile: File? = null

    private fun takePhoto() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        tempFile = createImageFile()

        tempFile?.let { file ->
            Timber.d("tempFile : ${file.absolutePath}")

            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider", file
            )

            Timber.d("photoUri : $photoUri")

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(intent, PICK_FROM_CAMERA)
        }

    }

    private fun createImageFile(): File {

        // 이미지 파일 이름 ( blackJin_ )
        val imageFileName = "blackJin_"

        // 이미지가 저장될 폴더 이름 ( blackJin )
        val storageDir =
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath)

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Timber.d("requestCode : $requestCode , resultCode : $resultCode, data : $data")

        tempFile?.let {
            val options = BitmapFactory.Options()
            val originalBm = BitmapFactory.decodeFile(it.absolutePath, options)

            binding.imageView.setImageBitmap(originalBm)
        }
    }

    companion object {
        fun newInstance() = CameraFragment()
    }

}
