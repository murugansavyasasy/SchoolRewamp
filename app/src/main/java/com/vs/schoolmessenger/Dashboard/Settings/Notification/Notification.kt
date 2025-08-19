package com.vs.schoolmessenger.Dashboard.Settings.Notification
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
//    private lateinit var items: List<NotificationDataClass>

    private var items: MutableList<NotificationDataClass> = mutableListOf()

    private var isNotificationItems: MutableList<NotificationDataClass> = mutableListOf()



    private var appViewModel: App? = null
    private var isAccessToken: String? = null

    override fun getViewBinding(): NotificationBinding {
        return NotificationBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        setupToolbarBlue()
        binding.imgBack.setOnClickListener(this)


        if (Constant.isParentChoose) {
            isToolBarPrimaryTheme()
            binding.rlaLblNotification.setBackgroundResource(com.vs.schoolmessenger.R.drawable.gradient_theme_school)
        }
        else {
            setupToolbarBlue()
            binding.rlaLblNotification.setBackgroundResource(com.vs.schoolmessenger.R.drawable.gradient_theme_school)
        }

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
            Log.d("Notifications", "Raw Response: $response")

            if (response != null && response.status) {
                Log.d("Notifications", "Status: ${response.status}, Message: ${response.message}")
                Log.d("Notifications", "Data Size: ${response.data.size}")

                isNotificationItems.clear()

                response.data.forEach { item ->
                    isNotificationItems.add(
                        NotificationDataClass(
                            type = item.type ?: "",
                            title = item.name ?: "",
                            content = item.message ?: "",
                            sendBy = item.member_id ?: ""
                        )
                    )
            }

                isNotificationAdapter = NotificationAdapter(isNotificationItems, this, false)
                binding.rcyNotification.adapter = isNotificationAdapter
            } else {
                Constant.showDataValidation(
                    response?.status.toString(),
                    response?.message ?: "Unknown error",
                    this
                )
                Log.e("Notifications", "Failed: ${response?.message}")
            }
        }



//        items = listOf(
//            NotificationDataClass("text", "Text Message", "Probably at least of the constraints in the following list is one you don't want.", "Sathish"),
//            NotificationDataClass("voice", "Voice Message", "Come to office", "Murugan"),
//            NotificationDataClass(
//                "Assignment",
//                "Assignment Message",
//                "Complete the Assignment",
//                "Saran"
//            ),
//            NotificationDataClass("Image", "Image Message", "Drawing the Image", "Gayathri"),
//            NotificationDataClass(
//                "NoticeBoard",
//                "Notice Board Message",
//                "Follow the NoticeBoard",
//                "Narayanan"
//            ),
//            NotificationDataClass(
//                "HomeWork",
//                "HomeWork Message",
//                "Complete the HomeWork",
//                "Rakesh"
//            ),
//            NotificationDataClass("Attendance", "Attendance Message", "Your Absent today", "Priya"),
//            NotificationDataClass("Exam", "Exam Message", "Physics Exam", "Dinesh"),
//            NotificationDataClass("Event", "Event Message", "Tomorrow function", "Swathi"),
//            NotificationDataClass("Video", "Video Message", "View the Video", "Ganesh"),
//
//            )

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

//        isNotificationAdapter = NotificationAdapter(null, this, Constant.isShimmerViewShow)
//        binding.rcyNotification.layoutManager = LinearLayoutManager(this)
//        binding.rcyNotification.adapter = isNotificationAdapter
//        Constant.executeAfterDelay {
//            isNotificationAdapter =
//                NotificationAdapter(isNotificationItems, this, Constant.isShimmerViewDisable)
//            // Set GridLayoutManager (2 columns in this case)
//            binding.rcyNotification.adapter = isNotificationAdapter
//        }
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

//    private fun filter(text: String) {
//        isNotificationItems.clear()
//        if (text.isEmpty()) {
//            isNotificationItems.addAll(items)  // If search is empty, show all items
//        } else {
//            for (item in items) {
//                if (item.title.toLowerCase().contains(text.toLowerCase())) {
//                    isNotificationItems.add(item)  // Add the matching GridItem to filteredList
//                }
//            }
//        }
//        isNotificationAdapter.notifyDataSetChanged()
//    }

    private fun filter(text: String) {
        val filteredList = if (text.isEmpty()) {
            isNotificationItems
        } else {
            isNotificationItems.filter {
                it.title.contains(text, ignoreCase = true)
            }
        }

        // Replace adapter’s list
        isNotificationAdapter = NotificationAdapter(filteredList.toMutableList(), this, false)
        binding.rcyNotification.adapter = isNotificationAdapter
    }

}
