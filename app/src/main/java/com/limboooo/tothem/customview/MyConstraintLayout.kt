package com.limboooo.tothem.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.fragivity.navigator
import com.github.fragivity.push
import com.limboooo.tothem.fragment.FragmentSetting
import com.limboooo.tothem.viewmodel.initAnimator
import kotlin.math.abs

class MyConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var startX = -1
    private var startY = -1
    private var secondX = 0
    private var secondY = 0
    private var thirdX = 0
    private var thirdY = 0
    private var lastX = 0
    private var lastY = 0
    private var second = false
    private var third = false
    private var x = 0
    private var y = 0
    private var ll = false

    var widthAHalf = 0
    var heightAHalf = 0

    init {
        post {
            widthAHalf = width / 2
            heightAHalf = height / 2
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        x = ev.x.toInt()
        y = ev.y.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (x < widthAHalf && y < heightAHalf) {
                    startX = x
                    startY = y
                    lastX = x
                    lastY = y
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (x - startX > 20 && startX != -1 && startY != -1) {
                    if ((abs(startY - y) / (x - startX)).toFloat() < 0.3) {
                        lastX = x
                        lastY = y
                        ll = true
                        return true
                    }
                }
            }
            else -> return super.onInterceptTouchEvent(ev)
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        x = event.x.toInt()
        y = event.y.toInt()
        //判断用户是否画Z，然后进入二级界面
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (!second) {
                    if (x > widthAHalf && y < heightAHalf && x < lastX && y > lastY) {
                        secondX = x
                        secondY = y
                        second = true
                    }
                }
                if (second && !third) {
                    if (x < widthAHalf && y > heightAHalf && x > lastX) {
                        thirdX = x
                        thirdY = y
                        third = true
                    }
                }
                if (!second || !third) {
                    lastX = x
                    lastY = y
                }
            }
            MotionEvent.ACTION_UP -> {
                if (x > widthAHalf && y > heightAHalf && second && third) {
                    //跳转fragment进入设置界面
                    navigator.push(FragmentSetting::class) {
                        initAnimator()
                    }
                    clearProperty()
                    return true
                }
                clearProperty()
                x = 0
                y = 0
            }
            else -> return super.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    private fun clearProperty() {
        startX = -1
        startY = -1
        secondX = 0
        secondY = 0
        thirdX = 0
        thirdY = 0
        lastX = 0
        lastY = 0
        second = false
        third = false
        ll = false
    }

}