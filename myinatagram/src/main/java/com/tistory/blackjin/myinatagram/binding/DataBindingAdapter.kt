package com.tistory.blackjin.myinatagram.binding

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageUri")
fun loadImage(imageView: ImageView, uri: Uri) {
    Glide.with(imageView.context)
        .load(uri)
        .thumbnail(0.1f)
        .into(imageView)
}