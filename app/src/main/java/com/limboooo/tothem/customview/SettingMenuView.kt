package com.limboooo.tothem.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import com.limboooo.tothem.MyUtils
import com.limboooo.tothem.R
import com.limboooo.tothem.databinding.MenuSettingBinding

class SettingMenuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    var binding: MenuSettingBinding

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_setting, this, true)
        binding = MenuSettingBinding.bind(view)
        this.isClickable = true
        binding.root.apply {
            setPadding(MyUtils.dip2px(dpValue = 16f))
//            isClickable = true
        }
        context.obtainStyledAttributes(attrs, R.styleable.SettingMenuView).apply {
            try {
                binding.title.text = getString(R.styleable.SettingMenuView_text) ?: "选项名称"
//                binding.icon.setImageDrawable(
//                    getDrawable(R.styleable.SettingMenuView_icon)
//                        ?: getDrawable(R.drawable.my_launcher_icon)
//                )
                binding.next.visibility = if (getBoolean(
                        R.styleable.SettingMenuView_nextVisible,
                        true
                    )
                ) View.VISIBLE else View.GONE
            } finally {
                recycle()
            }
        }
    }

}