package com.vs.schoolmessenger.Dashboard.Settings.Notification
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.NotificationBinding

class Notification : BaseActivity<NotificationBinding>(), View.OnClickListener {

    private lateinit var isNotificationAdapter: NotificationAdapter
    private lateinit var items: List<NotificationDataClass>
    private var isNotificationItems: MutableList<NotificationDataClass> = mutableListOf()

    override fun getViewBinding(): NotificationBinding {
        return NotificationBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()

        setupToolbar()
        binding.imgBack.setOnClickListener(this)

        items = listOf(
            NotificationDataClass("text", "Text Message", "Come to school", "Sathish"),
            NotificationDataClass("voice", "Voice Message", "Come to office", "Murugan"),
            NotificationDataClass(
                "Assignment",
                "Assignment Message",
                "Complete the Assignment",
                "Saran"
            ),
            NotificationDataClass("Image", "Image Message", "Drawing the Image", "Gayathri"),
            NotificationDataClass(
                "NoticeBoard",
                "Notice Board Message",
                "Follow the NoticeBoard",
                "Narayanan"
            ),
            NotificationDataClass(
                "HomeWork",
                "HomeWork Message",
                "Complete the HomeWork",
                "Rakesh"
            ),
            NotificationDataClass("Attendance", "Attendance Message", "Your Absent today", "Priya"),
            NotificationDataClass("Exam", "Exam Message", "Physics Exam", "Dinesh"),
            NotificationDataClass("Event", "Event Message", "Tomorrow function", "Swathi"),
            NotificationDataClass("Video", "Video Message", "View the Video", "Ganesh"),

            )

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

        isNotificationAdapter = NotificationAdapter(null, this, Constant.isShimmerViewShow)
        binding.rcyNotification.layoutManager = LinearLayoutManager(this)
        binding.rcyNotification.adapter = isNotificationAdapter
        Constant.executeAfterDelay {
            isNotificationAdapter =
                NotificationAdapter(isNotificationItems, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.rcyNotification.adapter = isNotificationAdapter
        }
    }


    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }

    private fun filter(text: String) {
        isNotificationItems.clear()
        if (text.isEmpty()) {
            isNotificationItems.addAll(items)  // If search is empty, show all items
        } else {
            for (item in items) {
                if (item.title.toLowerCase().contains(text.toLowerCase())) {
                    isNotificationItems.add(item)  // Add the matching GridItem to filteredList
                }
            }
        }
        isNotificationAdapter.notifyDataSetChanged()
    }
}