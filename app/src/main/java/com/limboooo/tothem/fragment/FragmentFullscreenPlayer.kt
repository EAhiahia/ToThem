package com.limboooo.tothem.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.limboooo.tothem.databinding.FragmentFullscreenPlayerBinding
import com.limboooo.tothem.viewmodel.MyViewModel

class FragmentFullscreenPlayer : Fragment() {

    private lateinit var binding: FragmentFullscreenPlayerBinding
    private val viewModel by activityViewModels<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFullscreenPlayerBinding.inflate(inflater, container, false)

        requireArguments().let {
            if (it.getString("fragment") == "list") {
                it.getInt("position").let { position ->
                    viewModel.changeVideo(viewModel.backupList[position].uri_string)
                }
            } else {
                it.getString("uri")!!.let { uriString ->
                    viewModel.changeVideo(uriString)
                }
            }
        }
        binding.playerFullscreen.player = viewModel.exoPlayer
        binding.playerFullscreen
            .setOnTouchListener(object : View.OnTouchListener {

                private var startX = 0f
                private var startY = 0f
                private var isSeeking = false
                private var degree = 0L
                private var isDegree = false
                private var xx = 0F

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    event?.run {
                        when (action) {
                            MotionEvent.ACTION_DOWN -> {
                                startX = x
                                startY = y
                            }
                            MotionEvent.ACTION_MOVE -> {
                                xx = startX - x
                                if (xx > 50 || xx < -50)
                                    viewModel.exoPlayer.apply {
                                        if (!isDegree) {
                                            degree = currentPosition
                                            isDegree = true
                                        }
                                        isSeeking = true
                                        if (isPlaying) pause()
                                        seekTo((degree - xx * 10).toLong())
                                    }
                                return true
                            }
                            MotionEvent.ACTION_UP -> {
                                if (isSeeking) {
                                    viewModel.exoPlayer.apply {
                                        if (!isPlaying) play()
                                    }
                                }
                                startX = 0f
                                startY = 0f
                                isSeeking = false
                                isDegree = false
                                degree = 0L
                            }
                            else -> return false
                        }
                    }
                    return false
                }
            })
        return binding.root
    }

    override fun onStop() {
        viewModel.exoPlayer.stop()
        binding.playerFullscreen.player = null
        super.onStop()
    }
}