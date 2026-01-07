package com.vs.schoolmessenger.Utils.CallTesting

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager.PRESENTATION_ALLOWED

class MyConnectionService : ConnectionService() {

    companion object {
        var activeConnection: Connection? = null
    }

    // GLOBAL PLAYER → must stop when call ends
    private var player: MediaPlayer? = null

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {

        val extras = request?.extras
        val callerName = extras?.getString("caller_name") ?: "School Caller"
        val voiceUrl = extras?.getString("voice_url") ?: ""

        val connection = object : Connection() {

            override fun onAnswer() {
                super.onAnswer()

                audioModeIsVoip = true
                setActive()

                playVoiceMessage(voiceUrl)
            }

            override fun onReject() {
                stopAndReleasePlayer()
                setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
                destroy()
            }

            override fun onDisconnect() {
                // USER PRESSED RED BUTTON → STOP AUDIO
                stopAndReleasePlayer()

                setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
                destroy()
            }
        }

        activeConnection = connection

        // System ringtone + incoming call UI
        connection.audioModeIsVoip = false
        connection.setAddress(Uri.parse("tel:school"), PRESENTATION_ALLOWED)
        connection.setCallerDisplayName(callerName, PRESENTATION_ALLOWED)
        connection.setRinging()

        return connection
    }

    private fun playVoiceMessage(url: String) {
        Thread {
            try {
                player = MediaPlayer().apply {
                    setDataSource(url)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    prepare()
                    start()

                    setOnCompletionListener {

                        // STOP AUDIO CLEANLY
                        stopAndReleasePlayer()

                        // Delay to avoid abrupt hangup
                        Thread.sleep(1000)

                        // END CALL
                        activeConnection?.apply {
                            setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
                            destroy()
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    // Safely stop and release audio player
    private fun stopAndReleasePlayer() {
        try {
            player?.stop()
        } catch (_: Exception) {}

        try {
            player?.release()
        } catch (_: Exception) {}

        player = null
    }
}












//package com.vs.schoolmessenger.Utils.CallTesting
//
//import android.media.AudioAttributes
//import android.media.MediaPlayer
//import android.net.Uri
//import android.telecom.*
//import android.telecom.TelecomManager.PRESENTATION_ALLOWED
//
//class MyConnectionService : ConnectionService() {
//
//    companion object {
//        var activeConnection: Connection? = null
//    }
//
//    private var player: MediaPlayer? = null
//
//    override fun onCreateIncomingConnection(
//        connectionManagerPhoneAccount: PhoneAccountHandle?,
//        request: ConnectionRequest?
//    ): Connection {
//
//        val extras = request?.extras
//        val callerName = extras?.getString("caller_name") ?: "School Caller"
//        val voiceUrl = extras?.getString("voice_url") ?: ""
//
//        val connection = object : Connection() {
//
//            override fun onAnswer() {
//                super.onAnswer()
//
//                setAudioModeIsVoip(true)
//                setActive()
//
//                playVoiceMessage(voiceUrl)
//            }
//
//            override fun onReject() {
//                setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
//                destroy()
//            }
//
//            override fun onDisconnect() {
//                setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
//                destroy()
//            }
//        }
//
//        activeConnection = connection
//
//        // PLAY PHONE RINGTONE
//        connection.setAudioModeIsVoip(false)
//
//        // Caller info (shows in default phone screen)
//        connection.setAddress(Uri.parse("tel:school"), PRESENTATION_ALLOWED)
//        connection.setCallerDisplayName(callerName, PRESENTATION_ALLOWED)
//
//        // SHOW SYSTEM INCOMING CALL UI
//        connection.setRinging()
//
//        return connection
//    }
//
//    private fun playVoiceMessage(url: String) {
//        Thread {
//            try {
//                player = MediaPlayer().apply {
//                    setDataSource(url)
//                    setAudioAttributes(
//                        AudioAttributes.Builder()
//                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
//                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                            .build()
//                    )
//                    prepare()
//                    start()
//
//                    setOnCompletionListener {
//                        Thread {
//                            try {
//                                Thread.sleep(1500) // 1.5 sec delay
//                            } catch (e: Exception) {}
//
//                            // STOP & RELEASE AUDIO BEFORE ENDING THE CALL
//                            try {
//                                player?.stop()
//                                player?.release()
//                                player = null
//                            } catch (e: Exception) {}
//
//                            activeConnection?.apply {
//                                setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
//                                destroy()
//                            }
//                        }.start()
//                    }
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }.start()
//    }
//
////    private fun playVoiceMessage(url: String) {
////        Thread {
////            try {
////                val player = MediaPlayer()
////                player.setDataSource(url)
////                player.setAudioAttributes(
////                    AudioAttributes.Builder()
////                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
////                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
////                        .build()
////                )
////                player.prepare()
////                player.start()
////
////                player.setOnCompletionListener {
////                    activeConnection?.apply {
////                        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
////                        destroy()
////                    }
////                }
////
////            } catch (e: Exception) {
////                e.printStackTrace()
////            }
////        }.start()
////    }
//
//}
//
//
//
//
//
//
//
//
//
//
////package com.vs.schoolmessenger.Utils.CallTesting
////
////import android.content.Intent
////import android.net.Uri
////import android.os.Bundle
////import android.telecom.Connection
////import android.telecom.ConnectionRequest
////import android.telecom.ConnectionService
////import android.telecom.DisconnectCause
////import android.telecom.PhoneAccountHandle
////import android.telecom.TelecomManager.PRESENTATION_ALLOWED
////
////class MyConnectionService : ConnectionService() {
////
////    companion object {
////        var activeConnection: Connection? = null
////    }
////
////    override fun onCreateIncomingConnection(
////        connectionManagerPhoneAccount: PhoneAccountHandle?,
////        request: ConnectionRequest?
////    ): Connection {
////
////        val extras = request?.extras
////        val callerName = extras?.getString("caller_name") ?: "School Caller"
////        val voiceUrl = extras?.getString("voice_url") ?: ""
////
////        val connection = object : Connection() {
////
////            override fun onAnswer() {
////                super.onAnswer()
////
////                setAudioModeIsVoip(true)
////                setActive()
////
//////                val intent = Intent(applicationContext, CallActivity::class.java).apply {
//////                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//////                    putExtra("caller_name", callerName)
//////                    putExtra("voice_url", voiceUrl)
//////                }
//////                startActivity(intent)
////            }
////
////            override fun onReject() {
////                setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
////                destroy()
////            }
////
////            override fun onDisconnect() {
////                setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
////                destroy()
////            }
////        }
////
////        activeConnection = connection
////
////        // DON'T USE SELF-MANAGED MODE HERE
////        connection.setAudioModeIsVoip(false)
////
////        // Caller info for system UI
////        connection.setAddress(Uri.parse("tel:school"), PRESENTATION_ALLOWED)
////        connection.setCallerDisplayName(callerName, PRESENTATION_ALLOWED)
////
////        // This shows system incoming call UI + ringtone
////        connection.setRinging()
////
////        return connection
////    }
////}
