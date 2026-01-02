package com.vs.schoolmessenger.Utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import kotlin.math.abs


internal object WaveformOptions {

    private const val TARGET_POINTS = 256

    @JvmStatic
    fun getSampleFrom(context: Context, pathOrUrl: String, onSuccess: (IntArray) -> Unit) {
        val amplitudes = if (pathOrUrl.startsWith("http")) {
            val tempPath = downloadToTempFile(context, pathOrUrl)
            if (tempPath != null) {
                val result = extractAmplitudesFromPath(tempPath)
                val file = java.io.File(tempPath)
                if (file.exists()) file.delete() // Clean up temp file
                result
            } else {
                intArrayOf()
            }
        } else {
            extractAmplitudesFromPath(pathOrUrl)
        }
        onSuccess(amplitudes)
    }

    @JvmStatic
    fun getSampleFrom(context: Context, resource: Int, onSuccess: (IntArray) -> Unit) {
        val afd = try {
            context.resources.openRawResourceFd(resource)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        val amplitudes = if (afd != null) {
            val result = extractAmplitudesFromAfd(afd)
            try {
                afd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            result
        } else {
            intArrayOf()
        }
        onSuccess(amplitudes)
    }

    @JvmStatic
    fun getSampleFrom(context: Context, uri: Uri, onSuccess: (IntArray) -> Unit) {
        val pfd = try {
            context.contentResolver.openFileDescriptor(uri, "r")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        val amplitudes = if (pfd != null) {
            val result = extractAmplitudesFromPfd(pfd)
            try {
                pfd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            result
        } else {
            intArrayOf()
        }
        onSuccess(amplitudes)
    }

    private fun downloadToTempFile(context: Context, urlStr: String): String? {
        return try {
            val url = URL(urlStr)
            val connection = url.openConnection().apply {
                connectTimeout = 5000
                readTimeout = 5000
            }
            val input: InputStream = connection.getInputStream()
            val tempFile = java.io.File(context.cacheDir, "audio_temp_${System.currentTimeMillis()}.dat")
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
            input.close()
            tempFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractAmplitudesFromPath(path: String): IntArray {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(path)
            extractAudioAmplitudes(extractor)
        } catch (e: Exception) {
            e.printStackTrace()
            intArrayOf()
        } finally {
            extractor.release()
        }
    }

    private fun extractAmplitudesFromAfd(afd: android.content.res.AssetFileDescriptor): IntArray {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.declaredLength)
            extractAudioAmplitudes(extractor)
        } catch (e: Exception) {
            e.printStackTrace()
            intArrayOf()
        } finally {
            extractor.release()
        }
    }

    private fun extractAmplitudesFromPfd(pfd: ParcelFileDescriptor): IntArray {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(pfd.fileDescriptor)
            extractAudioAmplitudes(extractor)
        } catch (e: Exception) {
            e.printStackTrace()
            intArrayOf()
        } finally {
            extractor.release()
        }
    }

    private fun extractAudioAmplitudes(extractor: MediaExtractor): IntArray {
        var audioTrackIndex = -1
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format: MediaFormat = extractor.getTrackFormat(i)
            val mime: String? = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("audio/")) {
                audioTrackIndex = i
                break
            }
        }
        if (audioTrackIndex == -1) return intArrayOf()

        val format: MediaFormat = extractor.getTrackFormat(audioTrackIndex)
        val mime = format.getString(MediaFormat.KEY_MIME)!!
        extractor.selectTrack(audioTrackIndex)

        val decoder: MediaCodec = try {
            MediaCodec.createDecoderByType(mime)
        } catch (e: Exception) {
            e.printStackTrace()
            return intArrayOf()
        }

        decoder.configure(format, null, null, 0)
        decoder.start()

        val bufferInfo = MediaCodec.BufferInfo()
        val pcmChunks = mutableListOf<ShortArray>()
        var inputDone = false
        var decoderDone = false

        while (!inputDone || !decoderDone) {
            // Feed input
            if (!inputDone) {
                val inputBufferIndex = decoder.dequeueInputBuffer(10000L)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputBufferIndex)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    var presentationTimeUs = 0L
                    var flags = 0
                    if (sampleSize < 0) {
                        inputDone = true
                        flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    } else {
                        presentationTimeUs = extractor.sampleTime
                        extractor.advance()
                    }
                    decoder.queueInputBuffer(inputBufferIndex, 0, if (inputDone) 0 else sampleSize, presentationTimeUs, flags)
                }
            }

            // Drain output
            var outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000L)
            while (outputBufferIndex >= 0) {
                if (bufferInfo.size > 0) {
                    val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)!!
                    outputBuffer.position(bufferInfo.offset)
                    val chunkSize = bufferInfo.size
                    val pcmChunk = ByteArray(chunkSize)
                    outputBuffer.get(pcmChunk)
                    val byteBuffer = ByteBuffer.wrap(pcmChunk)
                    val shorts = ShortArray(byteBuffer.remaining() / 2)
                    byteBuffer.asShortBuffer().get(shorts)
                    pcmChunks.add(shorts)
                }
                decoder.releaseOutputBuffer(outputBufferIndex, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    decoderDone = true
                }
                outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0L)
            }

            val infoCode = outputBufferIndex
            if (infoCode == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED || infoCode == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // Ignore for decoder
            }
        }

        decoder.stop()
        decoder.release()

        // Flatten all PCM samples
        val allPcm: List<Short> = pcmChunks.flatMap { it.toList() }
        if (allPcm.isEmpty()) return intArrayOf()

        // Downsample to target points using max absolute value per bin (simple peak detection for waveform)
        val step = maxOf(1, allPcm.size / TARGET_POINTS)
        val amplitudes = IntArray(TARGET_POINTS)
        for (i in 0 until TARGET_POINTS) {
            val start = i * step
            val end = minOf(start + step, allPcm.size)
            var maxAbs = 0
            for (j in start until end) {
                val absVal = abs(allPcm[j].toInt())
                if (absVal > maxAbs) maxAbs = absVal
            }
            amplitudes[i] = maxAbs
        }
        return amplitudes
    }
}