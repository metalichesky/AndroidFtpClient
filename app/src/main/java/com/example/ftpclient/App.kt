package com.example.ftpclient

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ftpclient.util.Preferences
import timber.log.Timber

class App: Application(){
    companion object{
        lateinit var instance: App
        fun getContext(): Context = instance.applicationContext
    }
    lateinit var preferences: Preferences

    override fun onCreate() {
        instance = this
        Timber.plant(Timber.DebugTree())
        preferences = Preferences(this)
        super.onCreate()
    }

    fun checkPermission(context: Context, permission: String): Boolean{
        val currentAPIVersion = Build.VERSION.SDK_INT
        return if (currentAPIVersion >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun checkPermissions(context: Context, vararg permissions: String): Boolean{
        val currentAPIVersion = Build.VERSION.SDK_INT
        var result = true
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                val permissionGranted =
                    (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
                result = result && permissionGranted
            }
        }
        return  result
    }


    fun requestPermissions(activity: Activity, requestCode: Int, vararg permissions: String){
        ActivityCompat.requestPermissions(activity,  permissions, requestCode)
    }
}