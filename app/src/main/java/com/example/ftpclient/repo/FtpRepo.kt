package com.example.ftpclient.repo

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import timber.log.Timber
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.SocketException

class FtpRepo {
    private var ftpClient = FTPClient()
    var isConnected = false
        private set

    fun connect(hostname: String) {
        try {
            val address = InetAddress.getByName(hostname)
            ftpClient.connect(address)
            val replyCode = ftpClient.replyCode
            println("Code: ${replyCode} ${ftpClient.replyStrings.joinToString("")}")
            isConnected = FTPReply.isPositiveCompletion(replyCode)
            if (!isConnected) {
                println("Disconnecting")
                ftpClient.disconnect()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun connect(hostname: String, port: Int) {
        try {
            val address = InetAddress.getByName(hostname)
            ftpClient.connect(address, port)
            val replyCode = ftpClient.replyCode
            isConnected = FTPReply.isPositiveCompletion(replyCode)
            if (!isConnected) {
                println("Disconnecting")
                ftpClient.disconnect()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun login(username: String, password: String) {
        try {
            ftpClient.login(username, password)

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            ftpClient.enterLocalPassiveMode()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun getFiles(path: String = ""): Array<FTPFile> {
        var files = emptyArray<FTPFile>()
        try {
            files = ftpClient.listFiles(path)
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        } finally {
            println("Code ${ftpClient.replyCode} Reply ${ftpClient.replyStrings.joinToString("")}")
        }
        return files
    }

    fun getDirectories(path: String = ""): Array<FTPFile> {
        var files = emptyArray<FTPFile>()
        try {
            files = ftpClient.listDirectories(path)
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return files
    }

    fun getFileNames(path: String = "") : Array<String> {
        var files = emptyArray<String>()
        try {
            files = ftpClient.listNames(path)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return files
    }

    fun logout() {
        try {
            ftpClient.logout()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun changeDir(path: String, goToParent: Boolean = false) {
        try {
            if (goToParent) {
                ftpClient.changeToParentDirectory()
            } else {
                ftpClient.changeWorkingDirectory(path)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }

    fun getCurrentPath(): String {
        var result = ""
        try {
            result = ftpClient.printWorkingDirectory()
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return result
    }

    fun deleteFile(fileName: String) {
        try {
            val exists = ftpClient.deleteFile(fileName)
            println("File exists and deleted $exists")
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }

    fun downloadFile(file: FTPFile, outputStream: OutputStream) {
        try {
            val downloaded = ftpClient.retrieveFile(file.name, outputStream)
            println("File downloaded $downloaded")
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }

    fun uploadFile(fileName: String, inputStream: InputStream) {
        try {
            val uploaded = ftpClient.appendFile(fileName, inputStream)
            println("File uploaded $uploaded")
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            ftpClient.disconnect()
            isConnected = false
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}

fun main() {
    val repo = FtpRepo()
    repo.connect("192.168.0.104")
    println("Connected: ${repo.isConnected}")
    repo.login("ftpclient001", "qwerty")
    var files = repo.getFiles("")
    println("Files: \n${files.map { it.name }.joinToString("\n")}")
//    repo.changeDir("music")
    repo.changeDir("files", false)

//    files = repo.getFiles("")
//    println("Files: \n${files.map { it.name }.joinToString("\n")}")
//
//
//    files.getOrNull(0)?.let{
//        println("File to delete link=${it.link} name=${it.name}")
//        repo.deleteFile(it.name)
//    }


    files = repo.getFiles("")
//    println("Files: \n${files.map { it.name }.joinToString("\n")}")

}