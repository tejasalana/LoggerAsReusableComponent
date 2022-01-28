package com.example.loggerlibrary

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

open class Logger(builder: Builder) {

    private var fileName: String = "Application"
    private var limitFlag: Boolean = false
    private var limit: Int = 0
    private var isUTC: Boolean = true
    private var pattern: String = "dd-MMM-yyyy HH:mm:ss zzz"
    private var saveOnExternalStorage: Boolean = false
    private var context: Context

    init {
        this.fileName = builder.logFileName
        this.limitFlag = builder.limitingFlag
        this.limit = builder.limit
        this.isUTC = builder.isUTC
        this.pattern = builder.pattern
        this.saveOnExternalStorage = builder.saveOnExternalStorage
        this.context = builder.context
    }

    fun createLogFile() {
        FileUtils.setFileName(this.fileName)
        FileUtils.setLimitFlag(this.limitFlag)
        FileUtils.setLimit(this.limit)
        FileUtils.setFilePath(
            context.filesDir.let {
                it.path + "/" + this.fileName + ".log"
            }
        )
        FileUtils.setTimeFormat(this.isUTC, this.pattern)
        FileUtils.saveOnExternalStorage(this.saveOnExternalStorage)
        if (this.saveOnExternalStorage) {
            FileUtils.setExternalStorageFilePath(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                ).path + "/" + this.fileName + ".log"
            )
        }
        FileUtils.checkLogFile()
    }

    class Builder(var context: Context) {
        var limitingFlag: Boolean = false
            private set

        var limit: Int = 0
            private set

        var logFileName: String = "Application"
            private set

        var isUTC: Boolean = true
            private set

        var pattern: String = "dd-MMM-yyyy HH:mm:ss zzz"
            private set

        var saveOnExternalStorage: Boolean = false
            private set

        fun setLimit(limitFlag: Boolean, limit: Int = 0) = apply {
            this.limitingFlag = limitFlag
            this.limit = limit
        }

        fun setLogFileName(logFileName: String) = apply {
            this.logFileName = logFileName
        }

        fun setTimeFormat(isUTC: Boolean, pattern: String) = apply {
            this.isUTC = isUTC
            this.pattern = pattern
        }

        fun saveOnExternalStorage(saveOnExternalStorage: Boolean) = apply {
            this.saveOnExternalStorage = saveOnExternalStorage
        }

        fun build() = Logger(this)
    }

    companion object {
        fun getLogFile(): File? {
            return FileUtils.getLogFile()
        }

        fun info(message: String, tag: String = "INFO") {
            FileUtils.appendToLogFile(tag, message)
        }

        fun verbose(message: String, tag: String = "VERBOSE") {
            FileUtils.appendToLogFile(tag, message)
        }

        fun debug(message: String, tag: String = "DEBUG") {
            FileUtils.appendToLogFile(tag, message)
        }

        fun warn(message: String, tag: String = "WARN") {
            FileUtils.appendToLogFile(tag, message)
        }
    }
}

private object FileUtils {

    private var fileName: String? = null
    private var filePath: String? = null
    private var externalStorageFilePath: String? = null
    private var limitFlag: Boolean = false
    private var limit: Int = 0
    private var isUTC: Boolean = true
    private var pattern: String = "dd-MMM-yyyy HH:mm:ss zzz"
    private var saveOnExternalStorage: Boolean = false

    fun setFileName(fileName: String) {
        this.fileName = fileName
    }

    fun setFilePath(filePath: String) {
        this.filePath = filePath
    }

    fun setExternalStorageFilePath(filePath: String) {
        this.externalStorageFilePath = filePath
    }

    fun setLimitFlag(flag: Boolean) {
        this.limitFlag = flag
    }

    fun setLimit(limit: Int) {
        this.limit = limit
    }

    fun setTimeFormat(isUTC: Boolean, pattern: String) {
        this.isUTC = isUTC
        this.pattern = pattern
    }

    fun saveOnExternalStorage(saveOnExternalStorage: Boolean) = apply {
        this.saveOnExternalStorage = saveOnExternalStorage
    }

    fun checkLogFile() {
        try {
            getLogFile()?.let {
                if (!it.exists()) {
                    it.createNewFile()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getLogFile(): File? {
        return if (saveOnExternalStorage) {
            externalStorageFilePath?.let {
                File(it)
            } ?: kotlin.run {
                null
            }
        } else {
            filePath?.let {
                File(it)
            } ?: kotlin.run {
                null
            }
        }
    }

    fun appendToLogFile(tag: String, message: String) {
        try {
            getLogFile()?.let {
                if (it.exists()) {
                    it.appendText(
                        "${if (isUTC) getUtcDateTime() else getCurrentDateTime()} [$tag] : $message \n",
                        Charset.defaultCharset()
                    )
                    if (it.readLines().size > limit && limitFlag) {
                        removeFirstLine(it)
                    }
                }
            }
        } catch (ex: FileNotFoundException) {
            ex.printStackTrace()
        }
    }

    private fun removeFirstLine(fileName: File) {
        try {
            val raf = RandomAccessFile(fileName, "rw")
            var writePosition: Long = raf.filePointer
            raf.readLine()
            var readPosition: Long = raf.filePointer
            val buff = ByteArray(1024)
            var newLine: Int
            while (-1 != raf.read(buff).also { newLine = it }) {
                raf.seek(writePosition)
                raf.write(buff, 0, newLine)
                readPosition += newLine.toLong()
                writePosition += newLine.toLong()
                raf.seek(readPosition)
            }
            raf.setLength(writePosition)
            raf.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getUtcDateTime(): String {
        val usersDateFormat =
            SimpleDateFormat(pattern, Locale.ENGLISH)
        usersDateFormat.timeZone = getUTCTimeZOne()
        return (usersDateFormat.format(getUTCTime()))
    }

    private fun getUTCTimeZOne(): TimeZone {
        return TimeZone.getTimeZone("UTC")
    }

    private fun getUTCTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.timeZone = getUTCTimeZOne()
        return calendar.timeInMillis
    }

    fun getCurrentDateTime(): String {
        val date = Calendar.getInstance().timeInMillis
        val usersDateFormat =
            SimpleDateFormat(pattern, Locale.getDefault())
        return usersDateFormat.format(date)
    }

}