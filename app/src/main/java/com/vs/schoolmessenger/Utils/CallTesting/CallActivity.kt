package com.vs.schoolmessenger.Utils.CallTesting

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.vs.schoolmessenger.R

class CallActivity : AppCompatActivity() {

    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        val url = intent.getStringExtra("voice_url") ?: ""

        playAudio(url)

        findViewById<Button>(R.id.btnEndCall).setOnClickListener {
            stopAudio()
            finish()
        }
    }

    private fun playAudio(url: String) {
        stopAudio()
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(url)
                prepare()
                start()

                setOnCompletionListener {
                    stopAudio()
                    finish()
                }
            }
        } catch (_: Exception) {
            finish()
        }
    }

    private fun stopAudio() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        stopAudio()
        super.onDestroy()
    }
}


//package com.vs.schoolmessenger.Utils.CallTesting
//
//import android.media.AudioAttributes
//import android.media.MediaPlayer
//import android.os.Bundle
//import android.widget.Button
//import androidx.appcompat.app.AppCompatActivity
//import com.vs.schoolmessenger.R
//
//class CallActivity : AppCompatActivity() {
//
//    private var player: MediaPlayer? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_call)
//
//        val url = intent.getStringExtra("voice_url") ?: ""
//
//        playVoice(url)
//
//        findViewById<Button>(R.id.btnEndCall).setOnClickListener {
//            stopVoice()
//            finish()
//        }
//    }
//
//    private fun playVoice(url: String) {
//        player = MediaPlayer().apply {
//            setDataSource(url)
//            setAudioAttributes(
//                AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                    .build()
//            )
//            prepare()
//            start()
//
//            setOnCompletionListener {
//                stopVoice()
//                finish()
//            }
//        }
//    }
//
//    private fun stopVoice() {
//        try { player?.stop() } catch(_:Exception){}
//        try { player?.release() } catch(_:Exception){}
//        player = null
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopVoice()
//    }
//}
//
//
//
//
//
//
//
////package com.vs.schoolmessenger.Utils.CallTesting
////
////import android.media.AudioAttributes
////import android.media.MediaPlayer
////import android.os.Bundle
////import android.telecom.DisconnectCause
////import android.widget.ImageView
////import android.widget.TextView
////import androidx.appcompat.app.AppCompatActivity
////import com.vs.schoolmessenger.R
////
////class CallActivity : AppCompatActivity() {
////
////    private var player: MediaPlayer? = null
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_call)
////
////        val caller = intent.getStringExtra("caller_name") ?: "Caller"
////        val url = intent.getStringExtra("voice_url") ?: ""
////
////        findViewById<TextView>(R.id.txtCallerName).text = caller
////
////        playAudio(url)
////
////        findViewById<ImageView>(R.id.btnEndCall).setOnClickListener {
////
////            MyConnectionService.activeConnection?.apply {
////                setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
////                destroy()
////            }
////
////            // Close your screen
////            finish()
////        }
////
////    }
////
////    private fun playAudio(url: String) {
////
////        player = MediaPlayer().apply {
////            setAudioAttributes(
////                AudioAttributes.Builder()
////                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
////                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
////                    .build()
////            )
////            setDataSource(url)
////            prepareAsync()
////
////            setOnPreparedListener { start() }
////
////            setOnCompletionListener {
////
////                // 1. Disconnect call from Telecom
////                MyConnectionService.activeConnection?.apply {
////                    setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
////                    destroy()
////                }
////
////                // 2. Remove reference
////                MyConnectionService.activeConnection = null
////
////                // 3. Close your screen
////                finish()
////            }
////        }
////
////
////
//////        player = MediaPlayer().apply {
//////            setAudioAttributes(
//////                AudioAttributes.Builder()
//////                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
//////                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//////                    .build()
//////            )
//////            setDataSource(url)
//////            prepareAsync()
//////            setOnPreparedListener { start() }
//////            setOnCompletionListener { finish() }
//////        }
////    }
////
////    override fun onDestroy() {
////        super.onDestroy()
////        player?.release()
////        player = null
////    }
////}
