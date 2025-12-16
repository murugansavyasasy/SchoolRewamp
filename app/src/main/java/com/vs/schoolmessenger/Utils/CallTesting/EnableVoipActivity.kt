package com.vs.schoolmessenger.Utils.CallTesting

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vs.schoolmessenger.R

class EnableVoipActivity : AppCompatActivity() {

    private lateinit var txtStatus: TextView
    private lateinit var btnEnable: Button
    private lateinit var imgStatus: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_voip)

        txtStatus = findViewById(R.id.txtStatus)
        btnEnable = findViewById(R.id.btnEnable)
        imgStatus = findViewById(R.id.imgStatus)

        updateUI()

        btnEnable.setOnClickListener {
            openPhoneAccountSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        if (isPhoneAccountEnabled()) {
            txtStatus.text = "VoIP Calling Feature is ENABLED ✔"
            imgStatus.setImageResource(R.drawable.ic_up_arrow)
            btnEnable.text = "Continue"
            btnEnable.setOnClickListener { finish() }
        } else {
            txtStatus.text = "VoIP Calling Feature is DISABLED ❗"
            imgStatus.setImageResource(R.drawable.ic_down_arrow)
            btnEnable.text = "Enable Now"
        }
    }

    private fun isPhoneAccountEnabled(): Boolean {
        val telecom = getSystemService(TELECOM_SERVICE) as TelecomManager
        val handle = getPhoneAccountHandle()

        val account: PhoneAccount? = telecom.getPhoneAccount(handle)
        return account != null && account.isEnabled
    }

    private fun getPhoneAccountHandle(): PhoneAccountHandle {
        return PhoneAccountHandle(
            ComponentName(this, MyConnectionService::class.java),
            "school_voip_account"
        )
    }

    private fun openPhoneAccountSettings() {
        val intent = Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
