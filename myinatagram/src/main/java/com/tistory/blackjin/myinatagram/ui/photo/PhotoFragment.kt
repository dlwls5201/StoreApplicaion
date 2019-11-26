package com.tistory.blackjin.myinatagram.ui.photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.tistory.blackjin.myinatagram.R
import com.tistory.blackjin.myinatagram.builder.type.MediaType
import com.tistory.blackjin.myinatagram.databinding.FragmentPhotoBinding
import com.tistory.blackjin.myinatagram.model.Album
import com.tistory.blackjin.myinatagram.util.GalleryUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class PhotoFragment : Fragment() {

    private lateinit var binding: FragmentPhotoBinding

    private lateinit var disposable: Disposable

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
        loadMedia()
    }

    private fun loadMedia() {
        disposable = GalleryUtil.getMedia(requireContext(), MediaType.IMAGE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { albumList: List<Album> ->
                Timber.d("albumList : $albumList")
            }
    }

    companion object {
        fun newInstance() = PhotoFragment()
    }

}
