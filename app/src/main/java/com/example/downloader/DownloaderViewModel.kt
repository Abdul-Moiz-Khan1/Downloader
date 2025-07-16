package com.example.downloader


import android.app.Application
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
        val file = File(getApplication<Application>().filesDir, "largefile.zip")
        val url = "https://r2---sn-npoeenee.gvt1.com/edgedl/android/repository/emulator-windows_x64-9529220.zip?met=1752645931,&mh=Av&pl=22&rms=ltu,ltu&shardbypass=sd&cm2rm=sn-2uja-5ube7z,sn-hjuk76&rrc=80,80&fexp=24352568,24352573,24352574&req_id=1961c7f64d8cbbd9&redirect_counter=2&cms_redirect=yes&cmsv=e&mip=39.60.147.236&mm=34&mn=sn-npoeenee&ms=ltu&mt=1752645651&mv=m&mvi=2&smhost=r1---sn-npoe7nz7.gvt1.com"

        downloader = Downloader(application.applicationContext , url, file, chunkCount = 4)

        _isDownloading.value = true

        downloader?.startDownload(
            onProgress = {
                _progress.value = it
            },
            onComplete = {
                _isDownloading.value = false
            },
            onError = {
                _isDownloading.value = false
            }
        )
    }

    private fun pauseDownload() {
        downloader?.pauseDownload()
        _isDownloading.value = false
    }
}

