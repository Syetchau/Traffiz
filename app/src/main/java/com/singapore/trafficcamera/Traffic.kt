package com.singapore.trafficcamera

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import androidx.viewbinding.BuildConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.stetho.Stetho
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.pixplicity.easyprefs.library.Prefs
import com.singapore.trafficcamera.activity.MainActivity
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Traffic: Application(), LifecycleObserver {

    companion object {
        var activities = ArrayList<Activity>()

        var isActivityVisible = false

        @SuppressLint("StaticFieldLeak")
        var instance: Traffic? = null
    }

    // Use to generate in-app notification
    var currentActivity: Activity? = null

    // Use mainActivity to refresh contents
    var startActivity: MainActivity? = null
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        Fresco.initialize(this)
        Stetho.initializeWithDefaults(this)
        setupConfiguration()
    }

    private fun setupConfiguration() {
        setupLifecycleCallback()
        setupLogger()
        setupPrefs()
    }

    private fun setupLogger() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

    private fun setupPrefs() {
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
    }

    private fun setupLifecycleCallback() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                isActivityVisible = true
                currentActivity = activity
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                activities.remove(activity)
                if (activity is MainActivity) {
                    startActivity = null
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activities.add(activity)
                if (activity is MainActivity) {
                    startActivity = activity
                }
            }
        })
    }
}