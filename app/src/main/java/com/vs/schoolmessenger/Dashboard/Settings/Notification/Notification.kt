package com.vs.schoolmessenger.Dashboard.Settings.Notification

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.NotificationBinding

class Notification : BaseActivity<NotificationBinding>(), View.OnClickListener {

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

        setupToolbarBlueWhite()
        binding.imgBack.setOnClickListener(this)

        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()

        isNotificationAdapter = NotificationAdapter(isNotificationItems, this, true)
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
                                    isHeader = false
                                )
                            )
                        }
                    }
                }

                // Refresh adapter
                isNotificationAdapter = NotificationAdapter(isNotificationItems, this, false)
                binding.rcyNotification.adapter = isNotificationAdapter

            } else {
                Constant.showDataValidation(
                    response?.status.toString(),
                    response?.message ?: "Unknown error",
                    this
                )
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
        isNotificationAdapter = NotificationAdapter(filteredList.toMutableList(), this, false)
        binding.rcyNotification.adapter = isNotificationAdapter
    }
}
