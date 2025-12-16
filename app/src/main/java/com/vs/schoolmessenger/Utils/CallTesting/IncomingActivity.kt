package com.vs.schoolmessenger.Utils.CallTesting

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Build
import com.vs.schoolmessenger.R

class IncomingActivity : AppCompatActivity() {

    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        setContentView(R.layout.activity_incoming)

        val caller = intent.getStringExtra("caller_name") ?: "School Caller"
        val voiceUrl = intent.getStringExtra("voice_url") ?: ""

        findViewById<TextView>(R.id.txtCallerName).text = caller

        // Play ringtone
        val ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(applicationContext, ringUri)
        ringtone?.play()

        // Accept
        findViewById<Button>(R.id.btnAccept).setOnClickListener {
            ringtone?.stop()

            val callIntent = Intent(this, CallActivity::class.java)
            callIntent.putExtra("voice_url", voiceUrl)
            startActivity(callIntent)

            finish()
        }

        // Decline
        findViewById<Button>(R.id.btnDecline).setOnClickListener {
            ringtone?.stop()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
    }
}








//package com.vs.schoolmessenger.Utils.CallTesting
//
//import android.content.Intent
//import android.media.Ringtone
//import android.media.RingtoneManager
//import android.net.Uri
//import android.os.Bundle
//import android.view.WindowManager
//import android.widget.Button
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.vs.schoolmessenger.R
//
//class IncomingActivity : AppCompatActivity() {
//
//    private var ringtone: Ringtone? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_incoming)
//
//        window.addFlags(
//            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
//                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
//                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//        )
//
//
//        val callerName = intent.getStringExtra("caller_name") ?: "School Caller"
//        val voiceUrl = intent.getStringExtra("voice_url") ?: ""
//
//        findViewById<TextView>(R.id.txtCallerName).text = callerName
//
//        // Play default ringtone
//        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
//        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
//        ringtone?.play()
//
//        findViewById<Button>(R.id.btnAccept).setOnClickListener {
//            ringtone?.stop()
//
//            val callIntent = Intent(this, CallActivity::class.java)
//            callIntent.putExtra("voice_url", voiceUrl)
//            startActivity(callIntent)
//
//            finish()
//        }
//
//        findViewById<Button>(R.id.btnDecline).setOnClickListener {
//            ringtone?.stop()
//            finish()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        ringtone?.stop()
//    }
//}
