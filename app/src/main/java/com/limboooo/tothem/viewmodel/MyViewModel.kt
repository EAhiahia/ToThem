package com.limboooo.tothem.viewmodel

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.github.fragivity.NavOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.limboooo.tothem.VideoEntity
import com.limboooo.tothem.activity.MyApplication
import com.limboooo.tothem.tools.MyPlayerLoadControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data")
const val disableLog = true

fun String.showLog(tag: String = "myapp") {
    if (!disableLog) Log.d(tag, this)
}

fun NavOptions.initAnimator() {
    enterAnim = R.animator.fragment_open_enter
    exitAnim = R.animator.fragment_open_exit
    popEnterAnim = R.animator.fragment_close_enter
    popExitAnim = R.animator.fragment_close_exit
}

class MyViewModel : ViewModel() {
    val backupList: MutableList<VideoEntity> = mutableListOf()
    val exoPlayer: Player by lazy { myExoPlayerBuilder() }
    private val _dataList: MutableStateFlow<List<VideoEntity>> =
        MutableStateFlow(mutableListOf())
    val dataList: StateFlow<List<VideoEntity>> = _dataList
    private val gson by lazy { Gson() }
    private val type by lazy { object : TypeToken<List<VideoEntity>>() {} }
    private val dataKey by lazy { stringPreferencesKey("all_data") }
    var deleteMode = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            MyApplication.context.dataStore.data
                .map { preferences ->
                    //map最后一行是流中的数据
                    preferences[dataKey] ?: ""
                    //collect没有返回值，后面就不能接flowOn，所以collect所在的线程是外界的线程
                    //flowOn改变的是上游所在的线程
                }.collect {
                    "检测到本地数据改动_$it".showLog()
                    _dataList.value =
                        if (it.isEmpty()) {
                            mutableListOf()
                        } else {
                            gson.fromJson(it, type)
                        }
                    backupList.run {
                        clear()
                        addAll(dataList.value)
                    }
                }
        }
    }

    private fun myExoPlayerBuilder(): Player {
        return ExoPlayer.Builder(MyApplication.context).setLoadControl(MyPlayerLoadControl())
            .build()
    }

    suspend fun save(position: Int, entity: VideoEntity): Boolean {
        if (position > -1) {
            backupList[position] = entity
        } else {
            backupList.add(0, entity)
        }
        return withContext(Dispatchers.IO) {
            MyApplication.context.dataStore.edit {
                "setting_保存按钮_${backupList}".showLog()
                it[dataKey] = gson.toJson(backupList)
            }
            return@withContext true
        }
    }

    fun changeVideo(uri: String) {
        exoPlayer.apply {
            stop()
            clearMediaItems()
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    fun save() {
        if (dataList.value == backupList) return
        viewModelScope.launch(Dispatchers.IO) {
            MyApplication.context.dataStore.edit {
                "save_将内容保存到本地_${backupList}".showLog()
                it[dataKey] = gson.toJson(backupList)
            }
        }
    }

//    fun read() {
//        //因为我们就需要里面的数据，所以就在主线程读，而且还要阻塞外界runBlocking,使用first就表示只用一次，如果用collect，那么就会一直阻塞主线程，
//        //所以如果要使用collect，那么请单独开一个协程launch，而且不要用runBlocking，这样就不会阻塞主线程/外部线程
//        runBlocking {
//            //runBlocking会阻塞外部线程，不让线程继续执行，但是他内部协程作用域的语句是按顺序执行，launch视为一条语句
//            //coroutineScope和withContext一样，会阻塞外部协程作用域，不让协程作用域继续执行，外部就算有n多个launch也会被卡住，除非他们先做完
//            //总结：协程作用域里面，会按顺序执行，每个launch视为一句话，和launch同级的也视为一句话，他们之间不阻塞；
//            // 除非遇到coroutineScope和withContext，那么他们里面的launch会先执行完，外部的协程作用域才能继续执行
//            dataList.value
//        }
//    }
//        viewModelScope.launch(Dispatchers.IO) {
//            MyApplication.context.dataStore.data
//                .map { preferences ->
//                    //map最后一行是流中的数据
//                    preferences[dataKey] ?: ""
//                }.flowOn(Dispatchers.Main).collect {
//                    _dataList.value =
//                        if (it.isEmpty()) {
//                            mutableListOf()
//                        } else {
//                            gson.fromJson(it, type)
//                        }
//                }
//        }

}