package com.example.ftpclient.ui.activity

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.ftpclient.App
import com.example.ftpclient.R
import com.example.ftpclient.util.OnBackPressedListener

class MainActivity : AppCompatActivity() {
    companion object {
        const val PERMISSIONS_REQUEST_CODE = 1000
    }

    enum class Pages (val resourceId: Int){
        FTP_CONNECTION(R.id.fragmentFtpConnection),
        FTP_FILEBROWSING(R.id.fragmentFtpFilebrowsing)
    }

    lateinit var navController: NavController

    var onBackPressedCallbacks: MutableList<OnBackPressedListener> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkAllNeededPermissions()
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
    }

    private fun checkAllNeededPermissions() {
        val application = (applicationContext as App)
        val neededPermissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )

        val allPermissionsGranted = application.checkPermissions(this, *neededPermissions)
        if (!allPermissionsGranted) {
            application.requestPermissions(this, PERMISSIONS_REQUEST_CODE, *neededPermissions)
        }
    }

    fun navigateTo(page: Pages) {
        navController.navigate(page.resourceId)
    }

    override fun onBackPressed() {
        if (onBackPressedCallbacks.isEmpty()) {
            super.onBackPressed()
        } else {
            onBackPressedCallbacks.forEach {
                it.onBackPressed()
            }
        }
    }
}
