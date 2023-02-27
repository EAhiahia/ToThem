package com.limboooo.tothem.activity

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this);
        super.onCreate()
        context = applicationContext
    }

}