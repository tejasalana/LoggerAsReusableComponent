package com.example.logger

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.loggerlibrary.Logger
import java.io.*
import java.lang.StringBuilder
import java.util.*

class MainActivity : AppCompatActivity() {

    private var count = 1
    private lateinit var textView: TextView
    private val STORAGE_PERMISSION_CODE = 123
    private val isSaveOnExternalStorage = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text)

        if (isSaveOnExternalStorage) {
            checkStoragePermission()
        } else {
            createLogger()
        }
    }

    private fun createLogger() {
        Logger.Builder(this)
            .saveOnExternalStorage(isSaveOnExternalStorage)
            .setLimit(true, 15)
            .setLogFileName("TejaLogs")
            .setTimeFormat(true, "dd-MMM-yyyy hh:mm:ss aa")
            .build()
            .createLogFile()
    }

    fun add(view: View) {
        Logger.info("$count - This is test line", "my info")
        count++
        showData()
    }

    fun get(view: View) {
        showData()
    }

    private fun showData() {
        val text = StringBuilder()
        try {
            Logger.getLogFile()?.let {
                val myReader = Scanner(it)
                while (myReader.hasNextLine()) {
                    val data: String = myReader.nextLine()
                    text.append(data)
                    text.append(System.lineSeparator())
                }
                myReader.close()
            }
        } catch (e: FileNotFoundException) {
            Toast.makeText(this, "Error reading file!" + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        textView.text = text
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            createLogger()
        } else {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createLogger()
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }
}