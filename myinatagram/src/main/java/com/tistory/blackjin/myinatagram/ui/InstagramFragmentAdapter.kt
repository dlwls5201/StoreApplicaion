package com.tistory.blackjin.myinatagram.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tistory.blackjin.myinatagram.ui.camera.CameraFragment
import com.tistory.blackjin.myinatagram.ui.photo.PhotoFragment

class InstagramFragmentAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            UserScreen.PHOTO.pos -> PhotoFragment.newInstance()
            UserScreen.CAMERA.pos -> CameraFragment.newInstance()
            else -> PhotoFragment.newInstance()
        }
    }

    override fun getItemCount() = 2

    enum class UserScreen(val pos: Int) {
        PHOTO(0), CAMERA(1)
    }
}