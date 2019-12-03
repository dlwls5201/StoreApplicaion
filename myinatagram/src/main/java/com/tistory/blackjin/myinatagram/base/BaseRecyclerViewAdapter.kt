package com.tistory.blackjin.myinatagram.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<ITEM : Any, B : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val bindingVariableId: Int? = null
) : RecyclerView.Adapter<BaseBindingViewHolder<B>>() {

    private val items = mutableListOf<ITEM>()

    fun replaceAll(items: List<ITEM>?) {
        items?.let {
            this.items.run {
                clear()
                addAll(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        object : BaseBindingViewHolder<B>(
            layoutRes = layoutRes,
            parent = parent,
            bindingId = bindingVariableId
        ) {}

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseBindingViewHolder<B>, position: Int) {
        holder.onBindView(items[position])
    }
}

abstract class BaseBindingViewHolder<B : ViewDataBinding>(
    @LayoutRes layoutRes: Int,
    parent: ViewGroup?,
    private val bindingId: Int?
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent?.context)
        .inflate(layoutRes, parent, false)
) {

    private val binding: B = DataBindingUtil.bind(itemView)!!

    fun onBindView(item: Any?) {
        binding.run {
            if (bindingId != null) {
                setVariable(bindingId, item)
            }
            executePendingBindings()
        }
    }
}