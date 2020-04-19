package com.example.ftpclient.util

import androidx.core.content.FileProvider
import com.example.ftpclient.App

class CustomFileProvider : FileProvider() {
    companion object {
        fun getAuthorities(): String {
            return App.instance.packageName + ".fileprovider"
        }
    }

}