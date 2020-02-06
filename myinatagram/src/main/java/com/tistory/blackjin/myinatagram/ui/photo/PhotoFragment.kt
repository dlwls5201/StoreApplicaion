package com.tistory.blackjin.myinatagram.ui.photo

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.tistory.blackjin.myinatagram.R
import com.tistory.blackjin.myinatagram.adapter.GridSpacingItemDecoration
import com.tistory.blackjin.myinatagram.adapter.MediaAdapter
import com.tistory.blackjin.myinatagram.databinding.FragmentPhotoBinding
import com.tistory.blackjin.myinatagram.model.Album
import com.tistory.blackjin.myinatagram.model.Media
import com.tistory.blackjin.myinatagram.type.MediaType
import com.tistory.blackjin.myinatagram.ui.InstagramActivity
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

    //갤러리 선택 시 크롭된 이미지들은 GestureCropImageView 에 저장됩니다.
    //GestureCropImageView 를 너무 많이 저장하게 되면 OOM 이 발생합니다.
    private val mGestureCropImageViewList = mutableListOf<GestureCropImageView>()
    private var currentShowingUri: Uri? = null

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
        super.onStop()
    }

    private fun initCropView() {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val deviceWidth = displayMetrics.widthPixels

        with(binding.ucrop) {
            layoutParams = ConstraintLayout.LayoutParams(deviceWidth, deviceWidth)
        }
    }

    private fun initButton() {
        with(binding.ivChangeSelect) {
            setOnClickListener {
                if (mediaAdapter.isTypeSingle()) {
                    mediaAdapter.setMultiSelectType()
                    currentShowingUri?.let {
                        mediaAdapter.toggleMediaSelect(it)
                    }
                    setImageResource(R.drawable.ic_multi_select_on)

                    //현재 보여지고 있는 크롭 이미지를 제외하고 전부 제거합니다.
                    mGestureCropImageViewList.clear()
                    mGestureCropImageViewList.add(binding.ucrop.cropImageView)
                } else {
                    mediaAdapter.setSingleSelectType()
                    setImageResource(R.drawable.ic_multi_select_off)
                }
            }
        }
    }

    private fun setupMediaRecyclerView() {
        mediaAdapter = MediaAdapter().apply {
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
        if (mediaAdapter.isTypeSingle()) {
            onSingleMediaClick(uri)
        } else {
            onMultiMediaClick(uri)
        }
    }

    private fun onSingleMediaClick(uri: Uri) {
        if (currentShowingUri == uri) {
            return
        } else {
            setUriToPreview(uri)
        }
    }

    //MultiMedia 모드 일 때 선택할 수 있는 최대 숫자
    private val maxCount = 10
    private val maxCountMessage = "You can select up to 10"

    private fun onMultiMediaClick(uri: Uri) {

        val index = mediaAdapter.getSelectedUriListIndex(uri)

        if (index > -1) {
            //기존아이템 선택
            if (mGestureCropImageViewList.size > 1) {

                //클릭한 이미지 삭제
                mGestureCropImageViewList.removeAt(index)

                //마지막 아이템의 이미지로 설정
                val lastGestureCropImageView = mGestureCropImageViewList.last()
                binding.ucrop.changeCropImageView(lastGestureCropImageView)
                mediaAdapter.toggleMediaSelect(uri)
                currentShowingUri = uri
            } else {
                Timber.d("only selected one item")
            }
        } else {
            //새로운 아이템 선택
            //아이템 갯수가 maxCount 를 초과한 경우
            if (mGestureCropImageViewList.size >= maxCount) {
                Toast.makeText(requireContext(), maxCountMessage, Toast.LENGTH_LONG).show()
                return
            }

            setUriToPreview(uri)
        }
    }

    //TransformImageListener 의 onLoadComplete() 함수를 순차적으로 호출해야 합니다.
    //동시에 setTransformImageListener 호출하면 에러가 발생하므로 isPreviewLoadComplete 변수를 사용해 동작 중일 때는 다른 작업이 이뤄지지 않게 합니다.
    private var isPreviewLoadComplete = true

    //크롭된 이미지의 최대 사이즈를 정합니다.
    private val maxImageSize = 1000

    private fun setUriToPreview(uri: Uri) {

        if (isPreviewLoadComplete) {
            isPreviewLoadComplete = false

            with(binding.ucrop) {
                resetCropImageView()

                with(cropImageView) {

                    targetAspectRatio = 1f

                    isScaleEnabled = true
                    isRotateEnabled = false

                    setMaxResultImageSizeX(maxImageSize)
                    setMaxResultImageSizeY(maxImageSize)

                    setTransformImageListener(object :
                        TransformImageView.TransformImageListener {
                        override fun onRotate(currentAngle: Float) {}

                        override fun onScale(currentScale: Float) {}

                        override fun onLoadComplete() {
                            isPreviewLoadComplete = true
                        }

                        override fun onLoadFailure(e: Exception) {
                            isPreviewLoadComplete = true
                            Timber.wtf(e)
                        }
                    })
                }
            }

            val tempFile = (requireActivity() as InstagramActivity).createImageFile()
            val outputUri = Uri.fromFile(tempFile)

            currentShowingUri = uri

            binding.ucrop.cropImageView.let {
                it.setImageUri(uri, outputUri)

                if (!mediaAdapter.isTypeSingle()) {
                    mGestureCropImageViewList.add(it)
                    mediaAdapter.toggleMediaSelect(uri)
                }
            }
        }
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

        mediaAdapter.replaceAll(album.mediaUris)

        if (album.mediaUris.isNotEmpty()) {
            onMediaClick(album.mediaUris[0].uri)
        }
    }

    private var isLoading = false

    //GestureCropImageViewList 의 onBitmapCropped 함수는 백그라운드 쓰레드에서 병렬로 동작하므로 순서 동기화를 위한 변수와 함수입니다.
    private var indexCount = 0

    @Synchronized
    private fun plusIndexCount() {
        indexCount++
    }

    override fun onShowImageUrl() {

        if (isLoading) {
            Toast.makeText(requireContext(), "loadding...", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        val mCompressFormat = Bitmap.CompressFormat.JPEG
        val mCompressQuality = 90

        if (mediaAdapter.isTypeSingle()) {
            binding.ucrop.cropImageView.cropAndSaveImage(
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
                        (requireActivity() as InstagramActivity).finishWithResultUri(resultUri)
                    }

                    override fun onCropFailure(t: Throwable) {
                        finishForError(t)
                    }
                })
        } else {

            //클릭한 순서대로 uri 를 배치하기 위한 변수
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

                            plusIndexCount()

                            //indexCount 를 확인해 마지막 스레드에서 uri 순서를 정렬한 후 작업을 마칩니다.
                            if (indexCount == mGestureCropImageViewList.size) {

                                val uris = ArrayList<Uri>()

                                for ((key, uri) in uriMap.toSortedMap()) {
                                    Timber.d("$key = $uri")
                                    uris.add(uri)
                                }

                                (requireActivity() as InstagramActivity).finishWithResultUri(uris)
                            }
                        }

                        override fun onCropFailure(t: Throwable) {
                            Timber.wtf("index : $index -> ${t.message}")
                            finishForError(t)
                        }
                    })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deleteZeroSizeBlackJinFile()
    }

    //크롭된 이미지를 프리뷰에 보여주기 위해 createImageFile 함수에서 blackJin 이름의 파일을 갤러리 선택시 매번 생성합니다.
    //최종 작업을 진행하면 blackJin 으로 시작하는 파일에 크롭된 이미지를 저장하는데 선택되지 않은 빈 파일 또한 생성되어 있기 때문에 이 파일들을 제거해 줍니다.
    //
    //아래 경로에서 파일을 확인하실 수 있습니다.
    //usb 디버깅 모드 연결 후 shift 연속두번 -> Device File Explorer 검색 ->
    //storage > self > primary > Android > data > {package name} > files > Pictures
    private fun deleteZeroSizeBlackJinFile() {
        val path =
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        val files = File(path).listFiles()

        for (tempFile in files) {
            if (tempFile.name.startsWith("blackJin")) {
                if (tempFile.length() == 0L) {
                    tempFile.delete()
                }
            }
        }
    }

    private fun finishForError(t: Throwable) {
        Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
        requireActivity().finish()
        Timber.wtf(t)
    }

    companion object {
        private const val IMAGE_SPAN_COUNT = 4

        fun newInstance() = PhotoFragment()
    }
}
