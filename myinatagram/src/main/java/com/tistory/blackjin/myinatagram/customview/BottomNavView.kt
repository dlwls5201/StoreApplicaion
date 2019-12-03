package com.tistory.blackjin.myinatagram.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2

class BottomNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val items = hashMapOf<Int, View>()

    fun setViewPager(pager: ViewPager2) {

        pager.adapter?.let { adapter ->
            if (childCount == adapter.itemCount) {
                for (i in 0 until childCount) {
                    items[i] = getChildAt(i).apply {
                        setOnClickListener {
                            pager.currentItem = i
                        }
                    }
                }
            } else {
                error("child view not match the viewpager view count")
            }

        } ?: error("adapter is null")

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                for ((k, v) in items) {
                    v.isSelected = k == position
                }
            }
        })
    }
}