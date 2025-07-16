package com.example.downloader

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DownloadScreen(viewModel: DownloaderViewModel = viewModel()) {
    val progress by viewModel.progress.collectAsState()
    val chunkProgress by viewModel.chunkProgress.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Overall Progress: ${(progress * 100).toInt()}%")
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        chunkProgress.forEachIndexed { index, chunk ->
            Text("Chunk ${index + 1}: ${(chunk * 100).toInt()}%")
            LinearProgressIndicator(
                progress = chunk,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            Log.d("Download", "ButtonClicked")
            viewModel.startOrPauseDownload()
        }) {
            Text(if (isDownloading) "Pause" else "Start Download")
        }
    }
}
