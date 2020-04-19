package com.example.ftpclient.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ftpclient.model.FtpConfig
import com.example.ftpclient.repo.FtpRepo
import com.example.ftpclient.util.FTPUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPFile
import timber.log.Timber
import java.io.File
import java.lang.StringBuilder
import java.util.*
import kotlin.coroutines.CoroutineContext

class FtpFilesViewModel : ViewModel(), CoroutineScope {
    companion object {
        const val ROOT = "/"
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    val repo = FtpRepo()
    val ftpConfig = FtpConfig()

    val needShowProgress = MutableLiveData<Boolean>()
    var currentDir = MutableLiveData<String>().apply { postValue("") }
    var currentPath = MutableLiveData<String>().apply { postValue(ROOT) }
    var dirPath: Queue<String> = LinkedList<String>()
    var currentDirFiles = MutableLiveData<List<FTPFile>>().apply { postValue(emptyList()) }

    fun setHostname(hostname: String) {
        ftpConfig.hostname = hostname
    }

    fun getHostname(): String = ftpConfig.hostname

    fun setPort(port: String) {
        ftpConfig.port = port.toIntOrNull()
    }

    fun getPort(): String = ftpConfig.port?.toString() ?: ""

    fun setUsername(username: String) {
        ftpConfig.username = username
    }

    fun getUsername(): String = ftpConfig.username

    fun setPassword(password: String) {
        ftpConfig.password = password
    }

    fun getPassword(): String = ftpConfig.password

    fun setNeedAuth(needAuth: Boolean) {
        ftpConfig.needAuth = needAuth
    }

    fun getNeedAuth(): Boolean = ftpConfig.needAuth

    fun updatePath() {
        val path = repo.getCurrentPath()
        currentPath.postValue(path.toString())
    }

    fun isFileNameExists(fileName: String): Boolean {
        val index = currentDirFiles.value?.indexOfFirst { it.isFile && it.name == fileName }
        return index != null && index >= 0
    }

    fun isDirNameExists(dirName: String): Boolean {
        val index = currentDirFiles.value?.indexOfFirst { it.isDirectory && it.name == dirName }
        return index != null && index >= 0
    }

    fun connect(onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }

            Timber.d("Connect to FTP")
            val port = ftpConfig.port
            if (port == null || port == 0) {
                repo.connect(ftpConfig.hostname)
            } else {
                repo.connect(ftpConfig.hostname, port)
            }
            Timber.d("FTP is connected = ${repo.isConnected}")
            if (repo.isConnected && ftpConfig.needAuth && ftpConfig.username.isNotEmpty()) {
                Timber.d("Login to FTP")
                repo.login(ftpConfig.username, ftpConfig.password)
            }

            withContext(Dispatchers.Main) {
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }

    fun disconnect(onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }

            Timber.d("Disconnecting from FTP")
            repo.logout()
            repo.disconnect()

            withContext(Dispatchers.Main) {
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }

    fun initDirectory(onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }

            Timber.d("Init directory")
            val files = repo.getFiles()
            val filesList = mutableListOf<FTPFile>()
            filesList.addAll(files)

            withContext(Dispatchers.Main) {
                currentDirFiles.postValue(filesList)
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }

    fun goToDirectory(path: String, onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }

            Timber.d("Go to path")
            dirPath.offer(path)
            currentDir.postValue(path)
            repo.changeDir(path)
            initDirectory()
            updatePath()

            withContext(Dispatchers.Main) {
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }

    fun goToParentDirectory(onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }

            currentDir.postValue(dirPath.poll())
            repo.changeDir("", true)
            initDirectory()
            updatePath()

            withContext(Dispatchers.Main) {
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }

    fun createDirectory(dirName: String, onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }

            repo.createDir(dirName)
            initDirectory()

            withContext(Dispatchers.Main) {
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }

    fun deleteDirectory(dirName: String, onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }

            repo.removeDir(dirName)
            initDirectory()

            withContext(Dispatchers.Main) {
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }

    fun deleteFile(path: String, onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }

            repo.deleteFile(path)
            initDirectory()

            withContext(Dispatchers.Main) {
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }

    fun download(file: FTPFile, destinationFile: File, onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }
            val path = (currentPath.value ?: "" ) + file.name
            val uri = FTPUtil.UrlBuilder()
                .setHostname(getHostname())
                .setPort(getPort())
                .setUsername(getUsername())
                .setPassword(getPassword())
                .setPath(path)
                .build()
            Timber.d("Download by Uri: $uri")

            repo.downloadFile(file, destinationFile.outputStream())


            withContext(Dispatchers.Main) {
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }

    fun upload(file: File, destinationFileName: String, onFinished: OnFinished = null) {
        launch {
            withContext(Dispatchers.Main) {
                needShowProgress.postValue(true)
            }

            repo.uploadFile(destinationFileName, file.inputStream())
            initDirectory()

            withContext(Dispatchers.Main) {
                onFinished?.invoke()
                needShowProgress.postValue(false)
            }
        }
    }
}

typealias OnFinished = (() -> Unit)?