package com.example.downloader

import android.content.Context
import kotlinx.coroutines.*
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

class Downloader(
    private val context: Context,
    private val downloadUrl: String,
    private val outputFile: File,
    private val chunkCount: Int = 4
) {
    private val chunkJobs = mutableListOf<Job>()
    private var fileSize: Long = 0
    private var isPaused = false

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startDownload(onProgress: (Float) -> Unit, onComplete: () -> Unit, onError: (Exception) -> Unit) {
        coroutineScope.launch {
            try {
                fileSize = getContentLength(downloadUrl)
                if (fileSize <= 0L) throw Exception("Invalid file size")

                val chunkSize = fileSize / chunkCount
                val progressArray = LongArray(chunkCount) { 0L }

                RandomAccessFile(outputFile, "rw").setLength(fileSize)

                for (i in 0 until chunkCount) {
                    val startByte = i * chunkSize
                    val endByte = if (i == chunkCount - 1) fileSize - 1 else (startByte + chunkSize - 1)

                    val job = coroutineScope.launch {
                        downloadChunk(i, startByte, endByte, progressArray, onProgress)
                    }
                    chunkJobs.add(job)
                }

                chunkJobs.joinAll()
                if (!isPaused) onComplete()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun pauseDownload() {
        isPaused = true
        chunkJobs.forEach { it.cancel() }
    }

    private suspend fun downloadChunk(
        chunkIndex: Int,
        start: Long,
        end: Long,
        progressArray: LongArray,
        onProgress: (Float) -> Unit
    ) {
        val connection = URL(downloadUrl).openConnection() as HttpURLConnection
        connection.setRequestProperty("Range", "bytes=$start-$end")
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        if (connection.responseCode != HttpURLConnection.HTTP_PARTIAL &&
            connection.responseCode != HttpURLConnection.HTTP_OK
        ) {
            throw Exception("Server does not support partial content")
        }

        val input = connection.inputStream
        val raf = RandomAccessFile(outputFile, "rw")
        raf.seek(start)

        val buffer = ByteArray(8192)
        var downloaded = 0L
        var bytesRead: Int

        while (input.read(buffer).also { bytesRead = it } != -1 && !isPaused) {
            raf.write(buffer, 0, bytesRead)
            downloaded += bytesRead
            progressArray[chunkIndex] = downloaded

            val totalDownloaded = progressArray.sum()
            val totalProgress = totalDownloaded.toFloat() / fileSize
            onProgress(totalProgress)
        }

        raf.close()
        input.close()
    }

    private fun getContentLength(fileUrl: String): Long {
        val connection = URL(fileUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.connect()
        return connection.contentLengthLong
    }
}

