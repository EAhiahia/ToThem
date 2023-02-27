package com.limboooo.tothem.fragment

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isEmpty
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.limboooo.tothem.R
import com.limboooo.tothem.activity.MainActivity
import com.limboooo.tothem.databinding.FragmentLauncherModifyBinding

class FragmentModifyLauncherIcon : BottomSheetDialogFragment() {

    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var binding: FragmentLauncherModifyBinding
    private lateinit var imageUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        registerPicturePicker()
        binding = FragmentLauncherModifyBinding.inflate(inflater, container, false)
        binding.icon.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.save.setOnClickListener {
            if (binding.iconName.isEmpty()) {
                Toast.makeText(requireContext(), "图标名称还没有填写", Toast.LENGTH_SHORT).show()
            } else {
                val shortcutManager =
                    getSystemService(requireContext(), ShortcutManager::class.java)
                if (shortcutManager!!.isRequestPinShortcutSupported) {
                    val pinShortcutInfo = ShortcutInfo.Builder(context, "new_icon")
                        .setIcon(
                            if (!::imageUri.isInitialized) Icon.createWithResource(
                                requireContext(),
                                R.drawable.my_launcher_icon
                            ) else Icon.createWithContentUri(imageUri)
                        )
                        .setShortLabel(binding.iconName.editText!!.text.toString())
                        .setLongLabel(binding.iconName.editText!!.text.toString())
                        .setIntent(Intent(requireContext(), MainActivity::class.java).apply { action = "android.intent.action.MAIN" })
                        .build()
                    shortcutManager.requestPinShortcut(
                        pinShortcutInfo, null
                    )
                }
            }
        }
        return binding.root
    }

    private fun registerPicturePicker() {
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.icon.setImageURI(uri)
                imageUri = uri
            } else {
                Toast.makeText(context, "您没有选择任何图片", Toast.LENGTH_SHORT)
            }
        }
    }

}