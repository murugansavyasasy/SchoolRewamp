package com.vs.schoolmessenger.FCM

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.vs.schoolmessenger.FCM.AnnouncementStatusManager.sendStatus
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference

class DismissReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
       val voiceUrl = intent.getStringExtra(Constant.isVoiceUrlNotifi)
       val welcomeUrl = intent.getStringExtra(Constant.isWelcomeUrlNotifi)
       val notificationId = intent.getIntExtra(Constant.isNotificationId, -1)

       val welcome_file = intent.getStringExtra(Constant.isWelcomeUrlNotifi)
        val school_name = intent.getStringExtra(Constant.school_name)
        val member_name = intent.getStringExtra(Constant.member_name)
        val call_title = intent.getStringExtra(Constant.call_title)

        val ei1 = intent.getStringExtra(Constant.ei1)
       val ei2 = intent.getStringExtra(Constant.ei2)
       val ei3 = intent.getStringExtra(Constant.ei3)
       val ei4 = intent.getStringExtra(Constant.ei4)
       val ei5 = intent.getStringExtra(Constant.ei5)
       val receiver_id = intent.getStringExtra(Constant.isReceiverId)
       val retrycount = intent.getStringExtra(Constant.retrycount)
       val circular_id = intent.getStringExtra(Constant.circularId)


        sendStatus(
            context,
            voiceUrl,
            welcomeUrl,
            notificationId,
            welcome_file,
            school_name,
            member_name,
            call_title,
            ei1,
            ei2,
            ei3,
            ei4,
            ei5,
            circular_id,
            receiver_id,
            retrycount
        )

        NotificationManagerCompat
            .from(context)
            .cancel(1001)
    }
}