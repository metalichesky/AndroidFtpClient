package com.example.ftpclient.util

import android.net.Uri

object FTPUtil {
    class UrlBuilder() {
        private val protocolPrefix = "ftp://"
        private var hostname = ""
        private var port = ""
        private var username = ""
        private var password = ""
        private var path = ""

        fun setHostname(hostname: String): UrlBuilder {
            this.hostname = hostname
            return this
        }

        fun setPort(port: String): UrlBuilder {
            this.port = port
            return this
        }

        fun setUsername(username: String): UrlBuilder {
            this.username = username
            return this
        }

        fun setPassword(password: String): UrlBuilder {
            this.password = password
            return this
        }

        fun setPath(path: String): UrlBuilder {
            this.path = path
            return this
        }

        fun build(): String {
            var url = protocolPrefix
            if (username.isNotEmpty()) {
                url += "$username:$password@"
            }
            url += hostname
            if (port.isNotEmpty()) {
                url += ":$port"
            }
            url += path
            return url
        }

        fun buildUri(): Uri {
            return Uri.parse(build())
        }
    }


}