package com.limboooo.tothem.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.fragivity.navigator
import com.github.fragivity.push
import com.google.android.material.button.MaterialButton
import com.limboooo.tothem.R
import com.limboooo.tothem.adapter.MainAdapter
import com.limboooo.tothem.databinding.FragmentMainListBinding
import com.limboooo.tothem.viewmodel.MyViewModel
import com.limboooo.tothem.viewmodel.initAnimator
import kotlinx.coroutines.launch

class FragmentMainList : Fragment() {

    private lateinit var binding: FragmentMainListBinding
    private val viewModel by activityViewModels<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        //onBackPressedDispatcher会拦截系统的返回功能，使用场景为webView浏览网页时，内部的返回逻辑
        //使用场景2，弹出是否要退出的对话框
        requireActivity().onBackPressedDispatcher.apply {
            addCallback(viewLifecycleOwner) {
                if (viewModel.deleteMode) {
                    (binding.list.adapter as MainAdapter).submitList(viewModel.dataList.value.toList())
                    viewModel.backupList.run {
                        clear()
                        addAll(viewModel.dataList.value)
                    }
                    Toast.makeText(requireContext(), "已撤销所有修改", Toast.LENGTH_SHORT).show()
                    binding.finish.visibility = View.GONE
                    binding.list.children.forEach {
                        it.findViewById<MaterialButton>(R.id.delete_button).visibility = View.GONE
                    }
                    viewModel.deleteMode = false
                } else {
                    requireActivity().finish()
                    viewModel.save()
                    isEnabled = false
                    onBackPressed()
                }
            }
        }
        binding = FragmentMainListBinding.inflate(inflater, container, false)

        binding.goToSetting.setOnClickListener {
            navigator.push(FragmentSetting::class){
                initAnimator()
            }
        }
        binding.finish.setOnClickListener {
            viewModel.save()
            binding.finish.visibility = View.GONE
            viewModel.deleteMode = false
            binding.list.children.forEach {
                it.findViewById<MaterialButton>(R.id.delete_button).visibility = View.GONE
            }
        }
        binding.list.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MainAdapter(viewModel)
        }
        lifecycleScope.launch {
            //与UI相关的时候就需要这个，否则在后台也会更新UI
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataList.collect {
                    if (it.isEmpty()) {
                        binding.lottieContainer.visibility = View.VISIBLE
                        binding.list.visibility = View.GONE
                    } else {
                        binding.lottieContainer.visibility = View.GONE
                        binding.list.visibility = View.VISIBLE
                    }
                    (binding.list.adapter as MainAdapter).submitList(it)
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        isInDeleteMode()
        super.onResume()
    }

    private fun isInDeleteMode() {
        if (viewModel.deleteMode) {
            binding.finish.visibility = View.VISIBLE
            binding.list.children.forEach {
                it.findViewById<MaterialButton>(R.id.delete_button).visibility = View.VISIBLE
            }
        } else {
            binding.finish.visibility = View.GONE
            binding.list.children.forEach {
                it.findViewById<MaterialButton>(R.id.delete_button).visibility = View.GONE
            }
        }
    }

}