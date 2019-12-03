package com.tistory.blackjin.myinatagram.ui.photo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.tistory.blackjin.myinatagram.R
import com.tistory.blackjin.myinatagram.adapter.GridSpacingItemDecoration
import com.tistory.blackjin.myinatagram.adapter.MediaAdapter
import com.tistory.blackjin.myinatagram.builder.type.MediaType
import com.tistory.blackjin.myinatagram.builder.type.SelectType
import com.tistory.blackjin.myinatagram.databinding.FragmentPhotoBinding
import com.tistory.blackjin.myinatagram.model.Album
import com.tistory.blackjin.myinatagram.model.Media
import com.tistory.blackjin.myinatagram.ui.InstagramActivity
import com.tistory.blackjin.myinatagram.ui.OnShowImageListener
import com.tistory.blackjin.myinatagram.util.GalleryUtil
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.GestureCropImageView
import com.yalantis.ucrop.view.TransformImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

class PhotoFragment : Fragment(), OnShowImageListener {

    private lateinit var binding: FragmentPhotoBinding
    private lateinit var mediaAdapter: MediaAdapter

    private var disposable: Disposable? = null

    private val mGestureCropImageViewList = mutableListOf<GestureCropImageView>()
    private var mGestureCropImageView: GestureCropImageView? = null
    private var currentShowingUri: Uri? = null

    private var selectType = SelectType.SINGLE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initCropView()
        initButton()

        setupMediaRecyclerView()
        loadMedia()
    }

    override fun onStop() {
        disposable?.dispose()
        mGestureCropImageView?.cancelAllAnimations()
        super.onStop()
    }

    private fun initCropView() {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val deviceWidth = displayMetrics.widthPixels

        with(binding.ucrop) {
            layoutParams = ConstraintLayout.LayoutParams(deviceWidth, deviceWidth)
            overlayView.apply { setPadding(0, 0, 0, 0) }
        }
    }

    private fun initButton() {
        with(binding.ivChangeSelect) {
            setOnClickListener {
                if (selectType == SelectType.SINGLE) {
                    selectType = SelectType.MULTI
                    setImageResource(R.drawable.ic_multi_select_on)
                } else {
                    selectType = SelectType.SINGLE
                    setImageResource(R.drawable.ic_multi_select_off)
                }

                currentShowingUri?.let {
                    mediaAdapter.toggleSingleAndMultiSelect(selectType, it)
                }
            }
        }
    }

    private fun setupMediaRecyclerView() {
        mediaAdapter = MediaAdapter(requireActivity()).apply {
            onItemClickListener = object : MediaAdapter.OnItemClickListener {
                override fun onItemClick(data: Media) {
                    onMediaClick(data.uri)
                }
            }
        }

        with(binding.rvMedia) {
            layoutManager = GridLayoutManager(requireContext(), IMAGE_SPAN_COUNT)
            addItemDecoration(GridSpacingItemDecoration(IMAGE_SPAN_COUNT, 4))
            itemAnimator = null
            adapter = mediaAdapter
        }
    }

    private fun onMediaClick(uri: Uri) {
        when (selectType) {
            SelectType.SINGLE -> onSingleMediaClick(uri)
            SelectType.MULTI -> onMultiMediaClick(uri)
        }
    }

    private fun onSingleMediaClick(uri: Uri) {

        if (currentShowingUri == uri) {
            return
        } else {
            currentShowingUri = uri
            mGestureCropImageViewList.clear()
            setUriToPreview(uri)
        }
    }

    private val maxCount = 10
    private val maxCountMessage = "10개 이상 선택할 수 없습니다."

    private fun onMultiMediaClick(uri: Uri) {

        val index = mediaAdapter.getSelectedUriListIndex(uri)

        if (index >= 0) {
            //기존아이템 선택
            if (mGestureCropImageViewList.size > 1) {

                //클릭한 제스터 삭제
                mGestureCropImageViewList.removeAt(index)

                //마지막 아이템의 제스터로 변경
                val lastGestureCropImageView = mGestureCropImageViewList.last()
                binding.ucrop.changeCropImageView(lastGestureCropImageView)

                mediaAdapter.toggleMediaSelect(uri)
            } else {
                Timber.d("only selected one item")
            }
        } else {
            //새로운 아이템 선택
            if (mGestureCropImageViewList.size >= maxCount) {
                Toast.makeText(requireContext(), maxCountMessage, Toast.LENGTH_LONG).show()
                return
            }

            currentShowingUri = uri
            setUriToPreview(uri)
            mediaAdapter.toggleMediaSelect(uri)
        }

    }

    private fun setUriToPreview(uri: Uri) {

        with(binding.ucrop) {
            resetCropImageView()

            mGestureCropImageView = cropImageView.apply {
                targetAspectRatio = 1f

                isScaleEnabled = true
                isRotateEnabled = false

                setTransformImageListener(object :
                    TransformImageView.TransformImageListener {
                    override fun onRotate(currentAngle: Float) {}

                    override fun onScale(currentScale: Float) {}

                    override fun onLoadComplete() {
                        binding.ucrop.animate().alpha(1f).setDuration(300).interpolator =
                            AccelerateInterpolator()
                    }

                    override fun onLoadFailure(e: Exception) {
                        Timber.wtf(e)
                    }
                })
            }
        }

        val tempFile = createImageFile()
        val outputUri = Uri.fromFile(tempFile)

        mGestureCropImageView?.let {
            it.setImageUri(uri, outputUri)
            mGestureCropImageViewList.add(it)
        }
    }

    private fun createImageFile(): File {

        // 이미지 파일 이름 ( blackJin_ )
        val imageFileName = "blackJin_"

        // 이미지가 저장될 폴더
        val storageDir =
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath)

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun loadMedia() {
        disposable = GalleryUtil.getMedia(requireContext(), MediaType.IMAGE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { albumList: List<Album> ->
                Timber.d("albumList : $albumList")
                setSelectedAlbum(albumList)
            }
    }

    private fun setSelectedAlbum(albumList: List<Album>) {

        //albumList's first index is "ALL"
        val album = albumList[0]
        Timber.d("albumList : $albumList")

        mediaAdapter.replaceAll(album.mediaUris)

        if (album.mediaUris.isNotEmpty()) {
            onMediaClick(album.mediaUris[0].uri)
        }
    }

    private var isLoading = false

    override fun onShowImageUrls() {

        if (isLoading) return

        isLoading = true

        val mCompressFormat = Bitmap.CompressFormat.JPEG
        val mCompressQuality = 90

        if (selectType == SelectType.SINGLE) {
            mGestureCropImageView?.cropAndSaveImage(
                mCompressFormat,
                mCompressQuality,
                object : BitmapCropCallback {
                    override fun onBitmapCropped(
                        resultUri: Uri,
                        offsetX: Int,
                        offsetY: Int,
                        imageWidth: Int,
                        imageHeight: Int
                    ) {
                        setResultUri(resultUri)
                    }

                    override fun onCropFailure(t: Throwable) {
                        Timber.e(t)
                    }
                })
        } else {

            var indexCount = 0
            val uriMap = mutableMapOf<Int, Uri>()

            for ((index, value) in mGestureCropImageViewList.withIndex()) {

                //백그라운드 스레드에서 동작하므로 순서가 뒤섞임
                value.cropAndSaveImage(
                    mCompressFormat,
                    mCompressQuality,
                    object : BitmapCropCallback {
                        override fun onBitmapCropped(
                            resultUri: Uri,
                            offsetX: Int,
                            offsetY: Int,
                            imageWidth: Int,
                            imageHeight: Int
                        ) {

                            uriMap[index] = resultUri

                            indexCount++

                            if (indexCount == mGestureCropImageViewList.size) {

                                val uris = ArrayList<Uri>()

                                for ((key, uri) in uriMap.toSortedMap()) {
                                    Timber.d("$key = $uri")
                                    uris.add(uri)
                                }

                                setResultUris(uris)
                            }
                        }

                        override fun onCropFailure(t: Throwable) {
                            //TODO java.lang.NullPointerException: CurrentImageRect is empty
                            Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
                            isLoading = false
                            Timber.wtf(t)
                        }
                    })
            }
        }
    }

    private fun setResultUri(uri: Uri) {
        with(requireActivity()) {
            setResult(
                Activity.RESULT_OK, Intent()
                    .putExtra(InstagramActivity.EXTRA_PHOTO_URI, uri)
            )
            finish()
        }
    }

    private fun setResultUris(uris: ArrayList<Uri>) {
        with(requireActivity()) {
            setResult(
                Activity.RESULT_OK, Intent()
                    .putParcelableArrayListExtra(InstagramActivity.EXTRA_PHOTO_URI_LIST, uris)
            )
            finish()
        }
    }

    companion object {
        private const val IMAGE_SPAN_COUNT = 4

        fun newInstance() = PhotoFragment()
    }
}
