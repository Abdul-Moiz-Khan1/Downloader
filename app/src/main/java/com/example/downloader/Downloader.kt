package com.example.downloader

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

class Downloader(
    private val context: Context,
    private val downloadUrl: String,
    private val outputFile: File,
    private val chunkCount: Int = 4,
    private val onChunkProgress: (Int, Float) -> Unit
) {
    private val chunkJobs = mutableListOf<Job>()
    private var fileSize: Long = 0
    private var isPaused = false
    private val resumeMap = mutableMapOf<Int, Long>()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startDownload(onProgress: (Float) -> Unit, onComplete: () -> Unit, onError: (Exception) -> Unit) {
        Log.d("Download_CLASS", "inStartDownlad")
        coroutineScope.launch {
            try {
                Log.d("Download_CLASS", "inTry")
                fileSize = getContentLength(downloadUrl)
                if (fileSize <= 0L) throw Exception("Invalid file size")

                val chunkSize = fileSize / chunkCount
                val progressArray = LongArray(chunkCount) { 0L }

                RandomAccessFile(outputFile, "rw").setLength(fileSize)

                for (i in 0 until chunkCount) {
                    val startByte = i * chunkSize
                    val endByte = if (i == chunkCount - 1) fileSize - 1 else (startByte + chunkSize - 1)

                    val downloadedBytes = getDownloadedLength(outputFile, startByte, endByte)
                    resumeMap[i] = downloadedBytes
                    progressArray[i] = downloadedBytes

                    val resumeStart = startByte + downloadedBytes

                    if (resumeStart > endByte) {
                        Log.d("Download", "Chunk $i already downloaded. Skipping.")
                        continue
                    }

                    val job = coroutineScope.launch {
                        downloadChunk(i, resumeStart, endByte, downloadedBytes, progressArray, onProgress)
                    }
                    chunkJobs.add(job)
                }

                chunkJobs.joinAll()
                if (!isPaused) onComplete()

            } catch (e: Exception) {
                Log.d("Download_CLASS", "inCatch ${e.message}")
                onError(e)
            }
        }
    }

    fun pauseDownload() {

        Log.d("Download_CLASS", "inPause")
        isPaused = true
        chunkJobs.forEach { it.cancel() }
    }

    private suspend fun downloadChunk(
        chunkIndex: Int,
        resumeStart: Long,
        end: Long,
        alreadyDownloaded: Long,
        progressArray: LongArray,
        onProgress: (Float) -> Unit
    ) {
        val connection = URL(downloadUrl).openConnection() as HttpURLConnection
        connection.setRequestProperty("Range", "bytes=$resumeStart-$end")
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        if (connection.responseCode != HttpURLConnection.HTTP_PARTIAL &&
            connection.responseCode != HttpURLConnection.HTTP_OK
        ) {
            throw Exception("Server does not support partial content")
        }

        val input = connection.inputStream
        val raf = RandomAccessFile(outputFile, "rw")
        raf.seek(resumeStart)

        val buffer = ByteArray(8192)
        var downloaded = alreadyDownloaded
        var bytesRead: Int

        while (input.read(buffer).also { bytesRead = it } != -1 && !isPaused) {
            raf.write(buffer, 0, bytesRead)
            downloaded += bytesRead
            progressArray[chunkIndex] = downloaded

            val totalDownloaded = progressArray.sum()
            val totalProgress = totalDownloaded.toFloat() / fileSize
            val chunkProgress = downloaded.toFloat() / (end - resumeStart + alreadyDownloaded + 1)
            onChunkProgress(chunkIndex, chunkProgress)
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

private fun getDownloadedLength(file: File, start: Long, end: Long): Long {
    val raf = RandomAccessFile(file, "r")
    val buffer = ByteArray(8192)
    var downloaded = 0L

    raf.seek(start)
    var position = start
    while (position <= end) {
        val bytesToRead = minOf(buffer.size.toLong(), end - position + 1).toInt()
        val read = raf.read(buffer, 0, bytesToRead)
        if (read == -1) break

        // Check if bytes are non-zero (i.e., downloaded)
        if (buffer.copyOf(read).all { it == 0.toByte() }) break

        downloaded += read
        position += read
    }

    raf.close()
    return downloaded
}


