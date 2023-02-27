package com.limboooo.tothem.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.github.fragivity.navigator
import com.github.fragivity.push
import com.google.android.material.snackbar.Snackbar
import com.limboooo.tothem.R
import com.limboooo.tothem.VideoEntity
import com.limboooo.tothem.databinding.CardMainListBinding
import com.limboooo.tothem.fragment.FragmentFullscreenPlayer
import com.limboooo.tothem.viewmodel.MyViewModel
import com.limboooo.tothem.viewmodel.initAnimator

class MainAdapter(private val viewModel: MyViewModel) :
    ListAdapter<VideoEntity, MainAdapter.MyViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<VideoEntity>() {
            //这里说的是比如每个用户的id是固定的，那么就比较id，而不是说引用，因为flow每次都是新的object，不可能引用相同
            override fun areItemsTheSame(oldItem: VideoEntity, newItem: VideoEntity): Boolean {
                return oldItem.id == newItem.id
            }

            //使用data class会自动重写equals，所以直接写==就可以了，会自动比较内部的所有变量
            override fun areContentsTheSame(oldItem: VideoEntity, newItem: VideoEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdapter.MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.card_main_list, parent, false)
        val binding = CardMainListBinding.bind(view)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainAdapter.MyViewHolder, position: Int) {
        holder.bindData(position)
    }

//    override fun getItemCount() = viewModel.adapterList.size

    inner class MyViewHolder(val binding: CardMainListBinding) : ViewHolder(binding.root) {

        fun bindData(position: Int) {
            val entity = getItem(position)
            //设置标题
            binding.title.text = entity.title
            //删除按钮
            if (viewModel.deleteMode) {
                binding.deleteButton.visibility = View.VISIBLE
            } else {
                binding.deleteButton.visibility = View.GONE
            }
            binding.deleteButton.setOnClickListener {
                viewModel.backupList.remove(entity)
                submitList(viewModel.backupList.toList())
                Snackbar.make(itemView.parent as View, "已删除", Snackbar.LENGTH_SHORT)
                    .setAction("撤销") {
//                        viewModel.backupList.add(0, entity)
                        if (position == 0)
                            viewModel.backupList.add(0, entity)
                        else {
                            for (index in position - 1 downTo 0) {
                                if (viewModel.backupList.contains(viewModel.dataList.value[index])) {
                                    if (index + 1 != viewModel.backupList.size)
                                        viewModel.backupList.add(index + 1, entity)
                                    else {
                                        viewModel.backupList.add(entity)
                                    }
                                    break
                                }
                            }
                        }
                        submitList(viewModel.backupList.toList())
                    }
                    .show()
            }
            //player
            binding.videoCover.apply {
                Glide.with(this).load(entity.uri_string).into(this)
                setOnClickListener {
                    navigator.push(FragmentFullscreenPlayer::class) {
                        initAnimator()
                        arguments = bundleOf("fragment" to "list", "position" to position)
                    }
                }
            }
        }
    }

    //第一重缓存：有些itemView只是从recyclerView上卸下来，但是没有清除数据
    //所以此时需要更新deleteMode状态
    override fun onViewAttachedToWindow(holder: MainAdapter.MyViewHolder) {
        if (viewModel.deleteMode) {
            holder.binding.deleteButton.visibility = View.VISIBLE
        } else {
            holder.binding.deleteButton.visibility = View.GONE
        }
        super.onViewAttachedToWindow(holder)
    }

}