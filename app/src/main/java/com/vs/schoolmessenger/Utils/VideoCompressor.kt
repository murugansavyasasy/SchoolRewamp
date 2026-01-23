package com.vs.schoolmessenger.Utils

import android.content.Context
import android.media.*
import android.net.Uri
import java.io.File

object VideoCompressor {

    fun compressVideo(
        context: Context,
        inputUri: Uri,
        outputFile: File,
        targetWidth: Int = 720,
        targetHeight: Int = 1280,
        bitrate: Int = 2_000_000, // 2 Mbps
        callback: (Boolean) -> Unit
    ) {
        Thread {
            try {
                val extractor = MediaExtractor()
                extractor.setDataSource(context, inputUri, null)

                var videoTrackIndex = -1
                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                    if (mime.startsWith("video/")) {
                        videoTrackIndex = i
                        break
                    }
                }

                if (videoTrackIndex == -1) {
                    callback(false)
                    return@Thread
                }

                extractor.selectTrack(videoTrackIndex)
                val inputFormat = extractor.getTrackFormat(videoTrackIndex)

                val outputFormat = MediaFormat.createVideoFormat(
                    MediaFormat.MIMETYPE_VIDEO_AVC,
                    targetWidth,
                    targetHeight
                )

                outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
                outputFormat.setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                )

                val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
                encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

                val decoder = MediaCodec.createDecoderByType(
                    inputFormat.getString(MediaFormat.KEY_MIME)!!
                )
                decoder.configure(inputFormat, null, null, 0)

                val muxer = MediaMuxer(
                    outputFile.absolutePath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                )

                encoder.start()
                decoder.start()

                var outputTrackIndex = -1
                val bufferInfo = MediaCodec.BufferInfo()
                var muxerStarted = false

                while (true) {
                    val encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, 10000)

                    if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        val newFormat = encoder.outputFormat
                        outputTrackIndex = muxer.addTrack(newFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (encoderStatus >= 0 && muxerStarted) {
                        val encodedData = encoder.getOutputBuffer(encoderStatus) ?: continue

                        if (bufferInfo.size > 0) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(outputTrackIndex, encodedData, bufferInfo)
                        }

                        encoder.releaseOutputBuffer(encoderStatus, false)

                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            break
                        }
                    }
                }

                encoder.stop()
                encoder.release()
                decoder.stop()
                decoder.release()
                muxer.stop()
                muxer.release()

                callback(true)

            } catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }
        }.start()
    }
}
