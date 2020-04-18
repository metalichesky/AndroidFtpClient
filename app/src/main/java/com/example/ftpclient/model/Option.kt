package com.example.ftpclient.model

class FileOption(){
    constructor(name: String, job: ((itemIdx: Int)->Unit)? = null) : this() {
        option = name
        onSelected = job
    }

    var option: String = ""
    var onSelected: ((itemIdx: Int)->Unit)? = null
}