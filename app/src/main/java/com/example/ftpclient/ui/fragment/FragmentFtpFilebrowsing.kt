package com.example.ftpclient.ui.fragment

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ftpclient.R
import com.example.ftpclient.adapter.FileListAdapter
import com.example.ftpclient.model.FileOption
import com.example.ftpclient.ui.CustomDialog
import com.example.ftpclient.ui.CustomFilePicker
import com.example.ftpclient.ui.activity.MainActivity
import com.example.ftpclient.util.OnBackPressedListener
import com.example.ftpclient.vm.FtpFilesViewModel
import kotlinx.android.synthetic.main.fragment_ftp_filebrowsing.*
import kotlinx.coroutines.delay
import org.apache.commons.net.ftp.FTPFile
import timber.log.Timber
import java.io.File
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentFtpFilebrowsing : Fragment(), OnBackPressedListener {

    lateinit var ftpFilesViewModel: FtpFilesViewModel
    var adapter: FileListAdapter? = null

    var options: List<FileOption> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ftp_filebrowsing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initFileOptions()

        ftpFilesViewModel = ViewModelProvider(activity!!).get(FtpFilesViewModel::class.java)

        ftpFilesViewModel.currentDirFiles.observe(viewLifecycleOwner, Observer {
            adapter?.setItems(it)
        })

        ftpFilesViewModel.needShowProgress.observe(viewLifecycleOwner, Observer {
            showProgress(it)
        })

        ftpFilesViewModel.currentPath.observe(viewLifecycleOwner, Observer {
            tvPath.setText(it)
        })

        btnBack.setOnClickListener {
            back()
        }
        btnUpload.setOnClickListener {
            upload()
        }
        btnCreateDir.setOnClickListener {
            createDirectory()
        }
        containerSwipeRefresh.setOnRefreshListener {
            Timber.d("Refresh swipe")
            ftpFilesViewModel.initDirectory{
                containerSwipeRefresh.isRefreshing = false
            }
        }
//        containerSwipeRefresh.setOnChildScrollUpCallback { parent, child ->
//            Timber.d("Child scroll up")
//            true
//        }
        adapter?.onItemClick = {
            adapter?.getItem(it)?.let { file ->
                if (file.isDirectory) {
                    ftpFilesViewModel.goToDirectory(file.name)
                } else {
                    showFileInfo(file)
                }
            }
        }
        adapter?.onOptionsClick = {
            showFileOptions(it)
        }
    }

    private fun initFileOptions() {
        val ctx = context ?: return
        options = listOf(
            FileOption(ctx.getString(R.string.options_file_info)) {
                adapter?.getItem(it)?.let { file ->
                    showFileInfo(file)
                }
            },
            FileOption(ctx.getString(R.string.options_file_delete)) {
                adapter?.getItem(it)?.let { file ->
                    if (file.isDirectory) {
                        ftpFilesViewModel.deleteDirectory(file.name)
                    } else {
                        ftpFilesViewModel.deleteFile(file.name)
                    }
                }
            },
            FileOption(ctx.getString(R.string.options_file_download)) {
                adapter?.getItem(it)?.let { file ->
                    download(file)
                }
            }
        )
    }

    private fun initRecycler() {
        adapter = FileListAdapter()
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rcvFiles.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(context, layoutManager.orientation)
        rcvFiles.addItemDecoration(itemDecoration)
        rcvFiles.adapter = adapter
        rcvFiles.setHasFixedSize(true)
    }

    override fun onStart() {
        ftpFilesViewModel.connect {
            ftpFilesViewModel.initDirectory()
        }
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
        ftpFilesViewModel.disconnect()
        super.onStop()
    }

    override fun onBackPressed() {
        back()
    }

    private fun back() {
        if (ftpFilesViewModel.dirPath.isEmpty()) {
            (context as? MainActivity)?.navigateTo(MainActivity.Pages.FTP_CONNECTION)
        } else {
            ftpFilesViewModel.goToParentDirectory()
        }
    }

    private fun showProgress(needShow: Boolean) {
        if (needShow){
            val animation = AlphaAnimation(0.0f, 1.0f).apply {
                setInterpolator(AccelerateInterpolator())
                duration = 300
            }
            containerProgress.animation = animation
            containerProgress.visibility = View.VISIBLE
        } else {
            val animation = AlphaAnimation(1.0f, 0.0f).apply {
                setInterpolator(DecelerateInterpolator())
                duration = 300
            }
            containerProgress.animation = animation
            containerProgress.visibility = View.GONE
        }
    }

    private fun showFileOptions(idx: Int) {
        val ctx = context ?: return
        val optionsDialog = AlertDialog.Builder(ctx)
        optionsDialog.setTitle(R.string.title_file_options)
        optionsDialog.setItems(options.map { it.option }.toTypedArray(),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    options.getOrNull(which)?.onSelected?.invoke(idx)
                }
            })
        optionsDialog.create().show()
    }

    private fun showFileInfo(file: FTPFile) {
        val ctx = context ?: return
        CustomDialog.Builder()
            .setTitle(ctx.getString(R.string.title_file_info))
            .setMessage(file.getInfo())
            .setMessageAlign(CustomDialog.TextAlign.LEFT)
            .build(ctx)
            .show()
    }

    private fun download(file: FTPFile?) {
        val ctx = context ?: return
        file ?: return
        CustomFilePicker.Builder()
            .setTitle(ctx.getString(R.string.title_choose_download_dir))
            .setOnFileSelected {
                it?.firstOrNull()?.let { path ->
                    Timber.d("Selected download dir: ${path?.absolutePath}")
                    inputDownloadFileName(file, path)
                }
            }
            .setInitialFolder(
                ctx.externalMediaDirs?.getOrNull(0)
                    ?: ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: Environment.getRootDirectory()
            )
            .build(ctx).show()
    }

    private fun inputDownloadFileName(fileToDownload: FTPFile, selectedDir: File, errorMessage: String = "") {
        val ctx = context ?: return
        val dialogBuilder = CustomDialog.Builder()
            .setTitle(ctx.getString(R.string.title_input_file_name))
            .setInputEnabled(true)
            .setInputHint(ctx.getString(R.string.title_input_file_name))
            .setInputText(fileToDownload.name)
            .setCancelButtonEnabled(true)
            .setOnOkClickListener {
                val newFile = File(selectedDir, it)
                if (it.isEmpty() || newFile.exists()) {
                    inputDownloadFileName(fileToDownload, selectedDir, "File already exists")
                } else {
                    ftpFilesViewModel.download(fileToDownload, newFile) {
                        Timber.d("Download finished")
                    }
                }
            }
        if (errorMessage.isNotEmpty()) {
            dialogBuilder.setMessage(errorMessage)
        }
        dialogBuilder.build(ctx).show()
    }

    private fun upload() {
        val ctx = context ?: return
        CustomFilePicker.Builder()
            .setTitle(ctx.getString(R.string.title_choose_upload_file))
            .setMinimumFiles(1)
            .setMaximumFiles(Int.MAX_VALUE)
            .setOnFileSelected {
                it?.firstOrNull()?.let { file ->
                    Timber.d("Selected upload file: ${file.absolutePath}")
                    inputUploadFileName(file)
                }
            }
            .setInitialFolder(
                ctx.externalMediaDirs?.getOrNull(0)
                    ?: ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: Environment.getRootDirectory()
            )
            .build(ctx).show()
    }

    private fun inputUploadFileName(file: File, errorMessage: String = "") {
        val ctx = context ?: return
        val dialogBuilder = CustomDialog.Builder()
            .setTitle(ctx.getString(R.string.title_input_file_name))
            .setInputEnabled(true)
            .setInputHint(ctx.getString(R.string.title_input_file_name))
            .setInputText(file.name)
            .setCancelButtonEnabled(true)
            .setOnOkClickListener {
                if (it.isEmpty() || ftpFilesViewModel.isFileNameExists(it)) {
                    inputUploadFileName(file, "File already exists")
                } else {
                    ftpFilesViewModel.upload(file, it) {
                        Timber.d("Upload finished")
                    }
                }
            }
        if (errorMessage.isNotEmpty()) {
            dialogBuilder.setMessage(errorMessage)
        }
        dialogBuilder.build(ctx).show()
    }

    private fun createDirectory(errorMessage: String = "") {
        val ctx = context ?: return
        val dialogBuilder = CustomDialog.Builder()
            .setTitle(ctx.getString(R.string.title_input_file_name))
            .setInputEnabled(true)
            .setInputHint(ctx.getString(R.string.title_input_file_name))
            .setCancelButtonEnabled(true)
            .setOnOkClickListener {
                if (it.isEmpty() || ftpFilesViewModel.isDirNameExists(it)) {
                    createDirectory("Directory already exists")
                } else {
                    ftpFilesViewModel.createDirectory(it) {
                        Timber.d("Create dir finished")
                    }
                }
            }
        if (errorMessage.isNotEmpty()) {
            dialogBuilder.setMessage(errorMessage)
        }
        dialogBuilder.build(ctx).show()
    }

}

fun FTPFile.getInfo(): String {
    val info = StringBuilder()
    val type = if (isDirectory) "directory"
    else if (isFile) "file"
    else if (isSymbolicLink) "symbolic link"
    else "unknown"
    val owner = this.user

    val dotIdx = this.name.lastIndexOf(".")
    var extension = ""
    if (dotIdx >= 0) {
        extension = name.substring(dotIdx)
    }

    var sizeType = "Byte"
    var size = this.size.toDouble()
    if (size > 1024) {
        size /= 1024.0
        sizeType = "KB"
    }
    if (size > 1024) {
        size /= 1024.0
        sizeType = "MB"
    }
    if (size > 1024) {
        size /= 1024.0
        sizeType = "GB"
    }

    val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US)
    val createdAt = format.format(this.timestamp.time)

    info.append("Name: ").append(name).append("\n")
        .append("Type: ").append(type).append("\n")
        .append("Size: ").append(String.format("%.4g", size)).append(" $sizeType").append("\n")
        .append("Extension: ").append(extension).append("\n")
        .append("Created at: ").append(createdAt).append("\n")
        .append("Owner: ").append(owner).append("\n")
        .append("Link: ").append(link).append("\n")
    return info.toString()
}