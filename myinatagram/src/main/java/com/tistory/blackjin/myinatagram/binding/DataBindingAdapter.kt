package com.tistory.blackjin.myinatagram.binding

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageUri")
fun ImageView.loadImage(uri: Uri) {
    Glide.with(context)
        .load(uri)
        .thumbnail(0.1f)
        .into(this)
}