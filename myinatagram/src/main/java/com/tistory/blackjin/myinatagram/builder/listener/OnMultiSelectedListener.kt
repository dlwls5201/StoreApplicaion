package com.tistory.blackjin.myinatagram.builder.listener

import android.net.Uri

interface OnMultiSelectedListener {
    fun onSelected(uriList: List<Uri>)
}