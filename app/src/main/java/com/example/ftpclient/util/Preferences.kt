package com.example.ftpclient.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.prefs.Preferences
import kotlin.properties.Delegates

class Preferences(val context: Context) {

    private var pref : SharedPreferences by Delegates.notNull()

    init {
        pref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    inner class Connection {
        fun setHostname(hostname: String?) {
            val editor = pref.edit()
            editor.putString("connection_hostname", hostname)
            editor.apply()
        }

        fun getHostname(): String? {
            return pref.getString("connection_hostname", null)
        }

        fun setPort(port: String?) {
            val editor = pref.edit()
            editor.putString("connection_port", port)
            editor.apply()
        }

        fun getPort(): String? {
            return pref.getString("connection_port", null)
        }

        fun setUsername(username: String?) {
            val editor = pref.edit()
            editor.putString("connection_username", username)
            editor.apply()
        }

        fun getUsername(): String? {
            return pref.getString("connection_username", null)
        }

        fun setPassword(port: String?) {
            val editor = pref.edit()
            editor.putString("connection_password", port)
            editor.apply()
        }

        fun getPassword(): String? {
            return pref.getString("connection_password", null)
        }

        fun setNeedAuth(needAuth: Boolean) {
            val editor = pref.edit()
            editor.putBoolean("connection_need_auth", needAuth)
            editor.apply()
        }

        fun getNeedAuth(): Boolean? {
            return pref.getBoolean("connection_need_auth", false)
        }
    }
}