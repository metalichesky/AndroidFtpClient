package com.example.ftpclient.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ftpclient.App
import com.example.ftpclient.R
import com.example.ftpclient.ui.activity.MainActivity
import com.example.ftpclient.util.OnBackPressedListener
import com.example.ftpclient.vm.FtpFilesViewModel
import kotlinx.android.synthetic.main.fragment_ftp_connection.*

class FragmentFtpConnection : Fragment(), OnBackPressedListener {
    lateinit var ftpFilesViewModel: FtpFilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ftp_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ftpFilesViewModel = ViewModelProvider(activity!!).get(FtpFilesViewModel::class.java)
        initListeners()
    }

    override fun onStart() {
        loadData()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        (context as? MainActivity)?.onBackPressedCallbacks?.add(this)
    }

    override fun onPause() {
        super.onPause()
        (context as? MainActivity)?.onBackPressedCallbacks?.remove(this)
    }


    override fun onStop() {
        saveData()
        super.onStop()
    }

    private fun initListeners() {
        etHostname.doOnTextChanged { text, _, _, _ ->
            ftpFilesViewModel.setHostname(text.toString())
        }
        etPort.doOnTextChanged { text, _, _, _ ->
            ftpFilesViewModel.setPort(text.toString())
        }
        etUsername.doOnTextChanged { text, _, _, _ ->
            ftpFilesViewModel.setUsername(text.toString())
        }
        etPassword.doOnTextChanged { text, _, _, _ ->
            ftpFilesViewModel.setPassword(text.toString())
        }
        btnDone.setOnClickListener {
            (context as? MainActivity)?.navigateTo(MainActivity.Pages.FTP_FILEBROWSING)
        }
        cbAuthorize.setOnCheckedChangeListener { buttonView, isChecked ->
            ftpFilesViewModel.setNeedAuth(isChecked)
            etUsername.isEnabled = isChecked
            etPassword.isEnabled = isChecked
        }
    }

    private fun loadData() {
        val prefs = App.instance.preferences.Connection()
        prefs.getHostname()?.let{
            ftpFilesViewModel.setHostname(it)
        }
        prefs.getPort()?.let{
            ftpFilesViewModel.setPort(it)
        }
        prefs.getUsername()?.let{
            ftpFilesViewModel.setUsername(it)
        }
        prefs.getPassword()?.let{
            ftpFilesViewModel.setPassword(it)
        }
        prefs.getNeedAuth()?.let{
            ftpFilesViewModel.setNeedAuth(it)
        }

        etHostname.setText(ftpFilesViewModel.getHostname())
        etPort.setText(ftpFilesViewModel.getPort().toString())
        etUsername.setText(ftpFilesViewModel.getUsername())
        etPassword.setText(ftpFilesViewModel.getPassword())
        cbAuthorize.isChecked = ftpFilesViewModel.getNeedAuth()
    }

    private fun saveData() {
        val prefs = App.instance.preferences.Connection()
        prefs.setHostname(ftpFilesViewModel.getHostname())
        prefs.setPort(ftpFilesViewModel.getPort().toString())
        prefs.setUsername(ftpFilesViewModel.getUsername())
        prefs.setPassword(ftpFilesViewModel.getPassword())
        prefs.setNeedAuth(ftpFilesViewModel.getNeedAuth())
    }

    override fun onBackPressed() {

    }
}