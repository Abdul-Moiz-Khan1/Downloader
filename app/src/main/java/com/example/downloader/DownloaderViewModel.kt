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
                downloadUrl = "https://vscode.download.prss.microsoft.com/dbazure/download/stable/5437499feb04f7a586f677b155b039bc2b3669eb/VSCodeUserSetup-x64-1.90.2.exe",
                outputFile = File(getApplication<Application>().filesDir, "newf24243324209932.zip"),
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
            val resumeList = downloader?.getChunkProgressList() ?: emptyList()
            _chunkProgress.value = resumeList
            downloader?.startDownload(
                onProgress = { _progress.value = it },
                onComplete = { _isDownloading.value = false },
                onError = { _isDownloading.value = false }
            )
        }
    }


}