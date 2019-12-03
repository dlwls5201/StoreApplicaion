package com.tistory.blackjin.myinatagram.adapter

import android.app.Activity
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tistory.blackjin.myinatagram.R
import com.tistory.blackjin.myinatagram.base.BaseDiffUtilCallback
import com.tistory.blackjin.myinatagram.base.BaseViewHolder
import com.tistory.blackjin.myinatagram.builder.type.SelectType
import com.tistory.blackjin.myinatagram.databinding.ItemMyGalleryMediaBinding
import com.tistory.blackjin.myinatagram.model.Media
import timber.log.Timber

internal class MediaAdapter(
    private val activity: Activity,
    private var selectType: SelectType = SelectType.SINGLE
) : RecyclerView.Adapter<BaseViewHolder<ViewDataBinding, Media>>() {

    private val maxCount = 10
    private val maxCountMessage = "10개 이상 선택할 수 없습니다."

    private val selectedUriList: MutableList<Uri> = mutableListOf()

    private val items = mutableListOf<Media>()

    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(data: Media, itemPosition: Int, layoutPosition: Int)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewDataBinding, Media> {
        return ImageViewHolder(parent).apply {
            onItemClickListener?.let { listener ->
                itemView.setOnClickListener {
                    listener.onItemClick(
                        getItem(adapterPosition),
                        getItemPosition(adapterPosition),
                        adapterPosition
                    )
                }
            }
        }
    }

    private fun getItem(position: Int) = items[getItemPosition(position)]

    private fun getItemPosition(adapterPosition: Int) = adapterPosition

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BaseViewHolder<ViewDataBinding, Media>, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: BaseViewHolder<ViewDataBinding, Media>) {
        holder.recycled()
        super.onViewRecycled(holder)
    }

    fun toggleMediaSelect(uri: Uri) {
        if (selectedUriList.contains(uri)) {
            removeMedia(uri)
        } else {
            addMedia(uri)
        }
    }

    fun toggleSingleAndMultiSelect(selectType: SelectType, uri: Uri) {
        this.selectType = selectType

        //이미 선택되어진 아이템은 체크 표시 해놔야 합니다.
        notifyDataSetChanged()
        toggleMediaSelect(uri)
    }

    fun replaceAll(items: List<Media>, useDiffCallback: Boolean = false) {
        val diffCallback = BaseDiffUtilCallback(this.items, items)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        Timber.d("items ; $items")
        this.items.run {
            clear()
            addAll(items)
        }
        if (useDiffCallback) {
            diffResult.dispatchUpdatesTo(this)
        } else {
            notifyDataSetChanged()
        }

    }

    private fun addMedia(uri: Uri) {
        if (selectedUriList.size == maxCount) {
            val message = maxCountMessage
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        } else {
            selectedUriList.add(uri)
            refreshSelectedView()
        }
    }

    private fun removeMedia(uri: Uri) {
        val position = getViewPosition(uri)
        selectedUriList.remove(uri)
        notifyItemChanged(position)
        refreshSelectedView()
    }

    private fun refreshSelectedView() {
        selectedUriList.forEach {
            val position: Int = getViewPosition(it)
            notifyItemChanged(position)
        }
    }

    private fun getViewPosition(it: Uri) = items.indexOfFirst { media -> media.uri == it }

    inner class ImageViewHolder(parent: ViewGroup) :
        BaseViewHolder<ItemMyGalleryMediaBinding, Media>(parent, R.layout.item_my_gallery_media) {

        override fun bind(data: Media) {
            binding.run {
                media = data
                isSelected = selectedUriList.contains(data.uri)
                selectType = this@MediaAdapter.selectType
                if (isSelected) {
                    selectedNumber = selectedUriList.indexOf(data.uri) + 1
                }
            }
        }

        override fun recycled() {
            Glide.with(itemView).clear(binding.ivImage)
        }
    }
}