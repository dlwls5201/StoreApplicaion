package com.tistory.blackjin.myinatagram.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.tistory.blackjin.myinatagram.BuildConfig
import com.tistory.blackjin.myinatagram.R
import com.tistory.blackjin.myinatagram.databinding.ActivityInstagramBinding
import timber.log.Timber

class InstagramActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstagramBinding

    private val instagramAdapter by lazy {
        InstagramFragmentAdapter(
            supportFragmentManager,
            lifecycle
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_instagram
        )

        initTimber()
        initViewPager()
        initBottomNav()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initViewPager() {
        with(binding.vpInstagram) {
            adapter = instagramAdapter
        }
    }

    private fun initBottomNav() {
        binding.llBottomNav.setViewPager2(binding.vpInstagram)
    }
}
