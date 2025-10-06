package com.vs.schoolmessenger.Dashboard.Settings.Notification

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.Parent.ExamMark
import com.vs.schoolmessenger.Parent.Assignment.Assignment
import com.vs.schoolmessenger.Parent.Attachment.Attachment
import com.vs.schoolmessenger.Parent.Attendance.Attendance
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequest
import com.vs.schoolmessenger.Parent.Communication.CommunicationParent
import com.vs.schoolmessenger.Parent.EBooks.Ebooks
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Event
import com.vs.schoolmessenger.Parent.FeeDetails.FeeDetails
import com.vs.schoolmessenger.Parent.Homework.HomeWork
import com.vs.schoolmessenger.Parent.InteractionWithStaff.InteractionWithStaff
import com.vs.schoolmessenger.Parent.LSRW.LSRW
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoard
import com.vs.schoolmessenger.Parent.PTM.PTM
import com.vs.schoolmessenger.Parent.QuizExam.Quiz
import com.vs.schoolmessenger.Parent.Timetable.TimeTable
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.EventReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.NotificationBinding

class Notification : BaseActivity<NotificationBinding>(), View.OnClickListener,
    NotificationClickListener {

    private lateinit var isNotificationAdapter: NotificationAdapter
    private var items: MutableList<NotificationDataClass> = mutableListOf()
    private var isNotificationItems: MutableList<NotificationDataClass> = mutableListOf()
    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    override fun getViewBinding(): NotificationBinding {
        return NotificationBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = "Notifications"

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isNotificationAdapter = NotificationAdapter(isNotificationItems, this, this,true)
        binding.rcyNotification.layoutManager = LinearLayoutManager(this)
        binding.rcyNotification.adapter = isNotificationAdapter


        loadNotifications()

        appViewModel!!.apiParentRepositories.isNotificationResponseLiveData.observe(this) { response ->
            Constant.hideLoading(this@Notification)
            if (response != null && response.status) {
                isNotificationItems.clear()

                response.data.forEach { menu ->
                    if (!menu.menu_name.isNullOrEmpty()) {
                        isNotificationItems.add(
                            NotificationDataClass(
                                type = "",
                                title = menu.menu_name,
                                content = "",
                                sendBy = "",
                                category = menu.menu_name,
                                menu_id = menu.menu_id,
                                isHeader = true
                            )
                        )

                        menu.details?.forEach { item ->
                            isNotificationItems.add(
                                NotificationDataClass(
                                    type = item.type ?: "",
                                    title = item.type ?: "",
                                    content = item.message ?: "",
                                    sendBy = item.name ?: "",
                                    category = menu.menu_name,
                                    menu_id = menu.menu_id,
                                    isHeader = false
                                )
                            )
                        }
                    }
                }
                binding.lytList.visibility=View.GONE
                binding.rcyNotification.visibility=View.VISIBLE
                // Refresh adapter
                isNotificationAdapter = NotificationAdapter(isNotificationItems, this, this,false)
                binding.rcyNotification.adapter = isNotificationAdapter

            } else {
                binding.rcyNotification.visibility=View.GONE
                binding.lytList.visibility=View.VISIBLE
                binding.txtNoData.text=response?.message?:"No notification received"
            }
        }



        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
        })

        isNotificationItems.addAll(items)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    private fun loadNotifications() {
        Constant.showLoading(this)
        appViewModel!!.isNotificationList(isAccessToken ?: "", "Android")
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    private fun filter(text: String) {
        val filteredList = if (text.isEmpty()) {
            isNotificationItems
        } else {
            isNotificationItems.filter {
                it.title?.contains(text, ignoreCase = true) == true
            }
        }
        isNotificationAdapter = NotificationAdapter(filteredList.toMutableList(), this, this,false)
        binding.rcyNotification.adapter = isNotificationAdapter
    }

    override fun onClickListener(
        data: NotificationDataClass,
        anchorView: View,
        adapterPosition: Int
    ) {
        when (data.menu_id) {
            Constant.M_COMMUNICATION -> {
                val detailIntent = Intent(this, CommunicationParent::class.java)
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(CommunicationParent::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_HOMEWORK  -> {
                val detailIntent = Intent(this, HomeWork::class.java)

                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(CommunicationParent::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_ATTENDANCE_REPORT  -> {
                val detailIntent = Intent(this, Attendance::class.java)

                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(Attendance::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_FEE_DETAILS  -> {
                val detailIntent = Intent(this, FeeDetails::class.java)

                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(FeeDetails::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_EXAM  -> {
                val detailIntent = Intent(this, ExamMark::class.java)

                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(ExamMark::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_QUIZ_EXAM  -> {
                val detailIntent = Intent(this, Quiz::class.java)

                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(Quiz::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_CLASS_TIME_TABLE  -> {
                val detailIntent = Intent(this, TimeTable::class.java)

                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(TimeTable::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_NOTICEBOARD  -> {
                val detailIntent = Intent(this, NoticeBoard::class.java)

                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(NoticeBoard::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_ASSIGNMENT  -> {
                val detailIntent = Intent(this, Assignment::class.java)

                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(Assignment::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_ATTACHMENTS  -> {
                val detailIntent = Intent(this, Attachment::class.java)

                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(Attachment::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_SCHOOL_CLASS_EVENTS  -> {
                val detailIntent = Intent(this, Event::class.java)

                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(EventReport::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_PARENT_CLASS_EVENTS  -> {
                val detailIntent = Intent(this, Event::class.java)

                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(Event::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_PTM  -> {
                val detailIntent = Intent(this, PTM::class.java)

                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(CommunicationParent::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_CERTIFICATE_REQUEST  -> {
                val detailIntent = Intent(this, CertificateRequest::class.java)
                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(CertificateRequest::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_LSRW  -> {
                val detailIntent = Intent(this, LSRW::class.java)
                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(LSRW::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }

            Constant.M_INTERACTION_WITH_STAFF  -> {
                val detailIntent = Intent(this, InteractionWithStaff::class.java)
                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(InteractionWithStaff::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                pendingIntent?.send()
            }
            Constant.M_ONLINE_TEXT_BOOK  -> {
                val detailIntent = Intent(this, Ebooks::class.java)
                // Build proper back stack
                val pendingIntent = TaskStackBuilder.create(this).apply {
                    addParentStack(Ebooks::class.java)
                    addNextIntent(detailIntent)
                }.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                pendingIntent?.send()
            }
            else -> {
                // default behavior
            }
        }
    }
}
