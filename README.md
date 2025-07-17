
# Multi-Chunk File Downloader

An Android-based multi-threaded file downloader that supports chunked downloads, pause/resume functionality, and progress tracking per chunk using Kotlin, Coroutines and ViewModel

## Description

This project demonstrates how to build a robust file downloader that:

Splits large files into multiple chunks.

Downloads each chunk concurrently using coroutines.

Supports pause/resume.

Tracks and displays real-time progress for each chunk and the whole file.

It's a fully working proof of concept for building a custom download manager optimized for reliability and speed.
## Goal

Implement efficient parallel downloading using chunked ranges.

Allow pause and resume of downloads.

Track progress per chunk and overall.

Handle file I/O and resume logic accurately.

Build a maintainable MVVM architecture for Android apps.
## Working

 1. Chunked Downloading
The file is split into N equal parts (chunkCount).
Each chunk is downloaded in parallel using coroutines.
Byte ranges are defined via HTTP Range headers.

 2. Resume Support
Already downloaded portions of the file are detected using file I/O.
On re-launch or resume, incomplete chunks are resumed from where they left off.

3. Progress Tracking
Real-time download progress for each chunk is tracked using a StateFlow.The total progress is updated dynamically.

4. ViewModel Management
The entire download process is controlled from the DownloaderViewModel, ensuring a clean separation between business logic and UI.
