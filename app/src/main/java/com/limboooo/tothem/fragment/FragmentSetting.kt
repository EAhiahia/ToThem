package com.limboooo.tothem.fragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.github.fragivity.push
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.limboooo.tothem.VideoEntity
import com.limboooo.tothem.databinding.FragmentSettingBinding
import com.limboooo.tothem.viewmodel.MyViewModel
import com.limboooo.tothem.viewmodel.initAnimator
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.launch
import kotlin.random.Random

class FragmentSetting : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private val viewModel by activityViewModels<MyViewModel>()
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var videoUri: String
    private var videoWidth = 0
    private var videoHeight = 0
    private var videoCoverPath = ""
    private var videoPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        //无论如何都要重复注册的
        registerVideoPicker()
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        //选择视频
        binding.changeVideo.setOnClickListener {
            checkPermission()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataList.collect {
                    binding.changeList.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
        //保存按钮
        binding.save.setOnClickListener {
            if (binding.title.editText!!.text.isNotEmpty()) {
                val saveEntity = VideoEntity(
                    Random.nextInt(100000),
                    binding.title.editText!!.text.toString(),
                    videoUri,
                    videoCoverPath,
                    videoWidth,
                    videoHeight
                )
                lifecycleScope.launch {
                    if (viewModel.save(videoPosition, saveEntity)) {
                        Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "保存失败", Toast.LENGTH_SHORT).show()
                    }
                    videoPosition = -1
                }
            } else {
                MaterialAlertDialogBuilder(requireContext()).setMessage("视频标题没有填写")
                    .setPositiveButton("知道了", null).show()
            }
        }
        binding.title.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.title.editText!!.text.toString().let { text ->
                    for ((index, single) in viewModel.dataList.value.withIndex()) {
                        if (text == single.title) {
                            MaterialAlertDialogBuilder(requireContext()).setMessage("已存在相同标题的教程视频，是否修改其视频")
                                .setPositiveButton("是的，我要修改视频") { _, _ ->
                                    videoPosition = index
                                    loadVideo(single.uri_string)
                                }.setNegativeButton("算了，我重新起个标题") { _, _ ->
                                    //todo 聚焦到标题输入框
                                }.show()
                            break
                        }
                    }
                }
            }
        }
        binding.videoCover.setOnClickListener {
            navigator.push(FragmentFullscreenPlayer::class) {
                initAnimator()
                arguments = bundleOf("fragment" to "setting", "uri" to videoUri)
            }
        }
        //更改图标
        binding.changeLauncherStyle.setOnClickListener {
            FragmentModifyLauncherIcon().show(parentFragmentManager, "modify")
        }
        //更改列表，删除教程
        binding.changeList.setOnClickListener {
            viewModel.deleteMode = true
            navigator.pop()
        }
        return binding.root
    }

    private fun glideLoad(uri: String, view: ImageView) {
        Glide.with(this).load(uri.toUri()).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(view)
    }

    private fun loadVideo(uri: String) {
        videoUri = uri
        binding.changeVideo.text = "更改视频"
        binding.save.visibility = View.VISIBLE
        binding.videoCover.let {
            it.visibility = View.VISIBLE
            glideLoad(uri, it)
        }
    }

    private fun registerVideoPicker() {
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                var exist = false
                for ((index, single) in viewModel.dataList.value.withIndex()) {
                    if (uri.toString() == single.uri_string) {
                        exist = true
                        MaterialAlertDialogBuilder(requireContext()).setMessage("该视频已添加过，名称为  ${single.title}  是否修改标题")
                            .setPositiveButton("好") { _, _ ->
                                binding.title.editText!!.text.apply {
                                    if (this.isEmpty()) {
                                        append(single.title)
                                    }
                                }
                                videoPosition = index
                                saveUriWithName(uri)
                            }.setNegativeButton("不用修改了", null).show()
                        break
                    }
                }
                if (!exist) saveUriWithName(uri)
            } else {
                Toast.makeText(context, "您没有选择任何视频", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUriWithName(uri: Uri) {
        videoUri = uri.toString()
        //保留uri永久的使用权限
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        requireContext().contentResolver.takePersistableUriPermission(uri, flag)
        binding.changeVideo.text = "更改视频"
        binding.save.visibility = View.VISIBLE
        binding.videoCover.let {
            it.visibility = View.VISIBLE
            glideLoad(uri.toString(), it)
        }
    }

    private fun chooseVideo() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
    }

    private fun checkPermission() {
        val requestList = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestList.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (requestList.isNotEmpty()) {
            PermissionX.init(this).permissions(requestList)
                .onExplainRequestReason { scope, deniedList ->
                    val message = "给他们的说明书需要您同意读取视频文件才能添加视频"
                    scope.showRequestReasonDialog(deniedList, message, "允许", "拒绝")
                }.request { allGranted, _, deniedList ->
                    if (allGranted) {
                        chooseVideo()
                    } else {
                        Toast.makeText(
                            activity,
                            "您拒绝了如下权限：$deniedList",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

}