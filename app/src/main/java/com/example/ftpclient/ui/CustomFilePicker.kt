package com.example.ftpclient.ui

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.zaphlabs.filechooser.KnotFileChooser
import com.zaphlabs.filechooser.Sorter
import com.zaphlabs.filechooser.utils.FileType
import java.io.File

class CustomFilePicker {
    private var fileChooser: KnotFileChooser? = null

    constructor(context: Context, params: Params) {
        fileChooser = KnotFileChooser(
            context,
            allowBrowsing = true, // Allow User Browsing
            allowCreateFolder = true, // Allow User to create Folder
            allowMultipleFiles = false, // Allow User to Select Multiple Files
            allowSelectFolder = false, // Allow User to Select Folder
            minSelectedFiles = 0, // Allow User to Selec Minimum Files Selected
            maxSelectedFiles = 0, // Allow User to Selec Minimum Files Selected
            showFiles = true, // Show Files or Show Folder Only
            showFoldersFirst = true, // Show Folders First or Only Files
            showFolders = true, //Show Folders
            showHiddenFiles = true, // Show System Hidden Files
            initialFolder = params.initialFolder, //Initial Folder
            restoreFolder = false, //Restore Folder After Adding
            cancelable = true
        ) //Dismiss Dialog On Cancel (Optional)

        fileChooser?.apply {
//            title(params.title)
//            sorter(Sorter.ByLatestModification) // Sort Data (Optional)
            onSelectedFilesListener {
                // Callback Returns Selected File Object  (Optional)
                params.onFileSelected?.invoke(it)
            }
            onSelectedFileUriListener {
                // Callback Returns Uri of File (Optional)
                params.onUriSelected?.invoke(it)
            }
        }

    }

    fun show() {
        fileChooser?.show()
    }

    class Builder() {
        private var params = Params()

        fun setOnFileSelected(onFileSelected: OnFileSelected = null): Builder {
            params.onFileSelected = onFileSelected
            return this
        }

        fun setOnUriSelected(onUriSelected: OnUriSelected = null): Builder {
            params.onUriSelected = onUriSelected
            return this
        }

        fun setInitialFolder(initialFolder: File): Builder {
            params.initialFolder = initialFolder
            return this
        }

        fun setTitle(title: String): Builder {
            params.title = title
            return this
        }

        fun build(context: Context): CustomFilePicker {
            return CustomFilePicker(context, params)
        }
    }


    class Params() {
        var title = "Choose File"
        var initialFolder: File = Environment.getRootDirectory()
        var onFileSelected: OnFileSelected = null
        var onUriSelected: OnUriSelected = null
    }

}

typealias OnFileSelected = ((List<File>?)->Unit)?
typealias OnUriSelected = ((List<Uri>?)->Unit)?