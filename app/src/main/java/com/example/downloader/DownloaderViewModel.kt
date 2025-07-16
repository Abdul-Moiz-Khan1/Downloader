package com.example.downloader


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class DownloaderViewModel(application: Application) : AndroidViewModel(application) {

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _chunkProgress = MutableStateFlow<List<Float>>(emptyList())
    val chunkProgress: StateFlow<List<Float>> = _chunkProgress

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    private var downloader: Downloader? = null


    fun startOrPauseDownload() {
        if (_isDownloading.value) {
            downloader?.pauseDownload()
            _isDownloading.value = false
        } else {
            _isDownloading.value = true

            downloader = Downloader(
                context = application.applicationContext,
                downloadUrl = "https://r4---sn-npoe7ned.gvt1.com/edgedl/android/repository/emulator-linux_x64-12325540.zip?met=1752649127,&mh=B2&pl=22&rms=ltu,ltu&shardbypass=sd&cm2rm=sn-2uja-5ube7s,sn-hjuk7e&rrc=80,80&fexp=24352568,24352573,24352574&req_id=4eb6f071f5257020&redirect_counter=2&cms_redirect=yes&cmsv=e&mip=39.60.147.236&mm=34&mn=sn-npoe7ned&ms=ltu&mt=1752649006&mv=m&mvi=4&smhost=r1---sn-ojnpo5-58.gvt1.com",
                outputFile = File(getApplication<Application>().filesDir, "newf22.zip"),
                chunkCount = 10,
                onChunkProgress = { chunkIndex, chunkProgressValue ->
                    _chunkProgress.update { current ->
                        val updated = current.toMutableList()
                        while (updated.size <= chunkIndex) updated.add(0f)
                        updated[chunkIndex] = chunkProgressValue
                        updated
                    }
                }
            )

            downloader?.startDownload(
                onProgress = { _progress.value = it },
                onComplete = { _isDownloading.value = false },
                onError = {  _isDownloading.value = false }
            )
        }
    }


}