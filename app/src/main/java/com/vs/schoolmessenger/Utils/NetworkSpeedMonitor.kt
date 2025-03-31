package com.vs.schoolmessenger.Utils

import android.app.Dialog
import android.content.Context
import android.net.TrafficStats
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import java.text.DecimalFormat
import com.vs.schoolmessenger.R


class NetworkSpeedMonitor(private val context: Context) {
    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTxBytes = TrafficStats.getTotalTxBytes()
    private var lastTime = System.currentTimeMillis()
    private val handler = Handler(Looper.getMainLooper())

    fun showNetworkSpeedPopup() {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_network_speed, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)

        val downloadText = view.findViewById<TextView>(R.id.downloadSpeedText)
        val uploadText = view.findViewById<TextView>(R.id.uploadSpeedText)
        val closeButton = view.findViewById<Button>(R.id.closeButton)

        closeButton.setOnClickListener {
            dialog.dismiss()
            handler.removeCallbacksAndMessages(null) // Stop updating speed when closed
        }

        dialog.show()

        val updateSpeedTask = object : Runnable {
            override fun run() {
                val speed = getNetworkSpeed()
                downloadText.text = "Download Speed: ${speed.first}"
                uploadText.text = "Upload Speed: ${speed.second}"
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(updateSpeedTask)
    }

    private fun getNetworkSpeed(): Pair<String, String> {
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        val currentTime = System.currentTimeMillis()

        val timeDiff = (currentTime - lastTime) / 1000.0 // Convert to seconds
        val downloadSpeed = (currentRxBytes - lastRxBytes) / timeDiff
        val uploadSpeed = (currentTxBytes - lastTxBytes) / timeDiff

        lastRxBytes = currentRxBytes
        lastTxBytes = currentTxBytes
        lastTime = currentTime

        return Pair(formatSpeed(downloadSpeed), formatSpeed(uploadSpeed))
    }

    private fun formatSpeed(speed: Double): String {
        val df = DecimalFormat("#.##")
        return when {
            speed > 1_000_000 -> "${df.format(speed / 1_000_000)} MB/s"
            speed > 1_000 -> "${df.format(speed / 1_000)} KB/s"
            else -> "${df.format(speed)} B/s"
        }
    }
}
