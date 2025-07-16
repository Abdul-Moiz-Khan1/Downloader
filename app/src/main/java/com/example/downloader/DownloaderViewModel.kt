package com.example.downloader


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class DownloaderViewModel(application: Application) : AndroidViewModel(application) {

    private var downloader: Downloader? = null

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    fun startOrPauseDownload() {
        if (_isDownloading.value) {
            pauseDownload()
        } else {
            startDownload()
        }
    }
    private fun startDownload() {

        Log.d("Download", "inStartDownload")
        val file = File(getApplication<Application>().filesDir, "newfiele.zip")
        val url =
          "https://r4---sn-npoe7ned.gvt1.com/edgedl/android/repository/emulator-linux_x64-12325540.zip?met=1752649127,&mh=B2&pl=22&rms=ltu,ltu&shardbypass=sd&cm2rm=sn-2uja-5ube7s,sn-hjuk7e&rrc=80,80&fexp=24352568,24352573,24352574&req_id=4eb6f071f5257020&redirect_counter=2&cms_redirect=yes&cmsv=e&mip=39.60.147.236&mm=34&mn=sn-npoe7ned&ms=ltu&mt=1752649006&mv=m&mvi=4&smhost=r1---sn-ojnpo5-58.gvt1.com"
        downloader = Downloader(application.applicationContext , url, file, chunkCount = 4)

        _isDownloading.value = true

        downloader?.startDownload(
            onProgress = {
                _progress.value = it
            },
            onComplete = {

                Log.d("Download", "inComplete")
                _isDownloading.value = false
            },
            onError = {

                Log.d("Download", "inError")
                _isDownloading.value = false
            }
        )
    }

    private fun pauseDownload() {

        Log.d("Download", "inPaused")
        downloader?.pauseDownload()
        _isDownloading.value = false
    }
}

