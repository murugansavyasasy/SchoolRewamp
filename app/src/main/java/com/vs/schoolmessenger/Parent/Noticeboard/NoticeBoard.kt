package com.vs.schoolmessenger.Parent.Noticeboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData
import com.vs.schoolmessenger.School.NoticeBoard.NoticeBoardClickListener
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.NoticeRevampBinding


class NoticeBoard : BaseActivity<NoticeRevampBinding>(), View.OnClickListener,
    NoticeBoardClickListener {

    override fun getViewBinding(): NoticeRevampBinding {
        return NoticeRevampBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: SchoolNoticeBoardAdapter
    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    var userDetails: UserDetails? = null

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)
        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)

        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)

            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )

            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            "${isChildDetails?.standard_name ?: ""} - ${isChildDetails?.section_name ?: ""}"

        binding.root.post {
            val finalName =
                Constant.isSelectedMenuName?.takeIf { it.isNotEmpty() } ?: menu_name ?: ""
            Log.d("NoticeBoard_HeaderFinal", "Setting headerview text: $finalName")
            binding.headerview.text = finalName
            binding.headerview.visibility = View.VISIBLE
        }


        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()
        isGetNoticeBoardList()

        // Listeners
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::mAdapter.isInitialized) {
                    mAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        appViewModel?.isNoticeBoardReport?.observe(this) { response ->
            Constant.hideLoading(this)

            if (response != null) {
                if (response.status == true && !response.data.isNullOrEmpty()) {

                    binding.rcyNoticeBoard.visibility = View.VISIBLE
                    binding.nomessage.visibility = View.GONE
                    binding.txtNoData.visibility = View.GONE

                    isloadhomeworkData(response.data)


                    mAdapter.updateList(response.data)

                    if (fromNotification) {
                        scrollToMessageId(headerId)
                    }

                } else {
                    binding.rcyNoticeBoard.visibility = View.GONE
                    binding.nomessage.visibility = View.VISIBLE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.txtNoData.text =
                        response?.message ?: getString(R.string.no_data_found)
                }
            }
        }


        val channel = NotificationChannel(
            Constant.reminder_channel, Constant.Reminders, NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun scrollToMessageId(headerId: String?) {
        val dataList = mAdapter.getCurrentList()
        if (dataList.isNullOrEmpty()) return

        val index = dataList.indexOfFirst { it.id == headerId }
        if (index != -1) {
            Log.d("ScrollDebug", "Scrolling to index $index for headerId: $headerId")
            binding.rcyNoticeBoard.post {
                binding.rcyNoticeBoard.smoothScrollToPosition(index)
                highlightItemTemporarily(binding.rcyNoticeBoard, index)
            }
        } else {
            Log.d("ScrollDebug", "No item found with headerId: $headerId")
        }
    }

    private fun highlightItemTemporarily(recyclerView: RecyclerView, position: Int) {
        recyclerView.post {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.itemView?.let { itemView ->
                val originalBackground = itemView.background

                itemView.setBackgroundColor(Color.parseColor("#FFE082"))

                Handler(Looper.getMainLooper()).postDelayed({
                    itemView.background = originalBackground
                }, 3000)
            }
        }
    }


    private fun isloadhomeworkData(newData: List<NoticeStaffData>?) {
        if (newData.isNullOrEmpty()) {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            mAdapter = SchoolNoticeBoardAdapter(
                emptyList(), this, this, false,
                binding.nomessage,
                binding.txtNoData,
                true
            )
            binding.rcyNoticeBoard.adapter = mAdapter
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.imgSearchToolBar -> {
                if (binding.toolbarLayout.rytSearch.visibility == View.VISIBLE) {
                    binding.toolbarLayout.rytSearch.visibility = View.GONE
                    binding.toolbarLayout.txtVideoMenu.setText("")
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.toolbarLayout.txtVideoMenu.windowToken, 0)
                } else {
                    binding.toolbarLayout.rytSearch.visibility = View.VISIBLE
                    binding.toolbarLayout.txtVideoMenu.setText("")
                    binding.toolbarLayout.txtVideoMenu.requestFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(
                        binding.toolbarLayout.txtVideoMenu,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }
            }
        }
    }

    override fun onClickListener(
        data: NoticeStaffData,
        anchorView: View,
        adapterPosition: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_matching_notices_found)
            binding.rcyNoticeBoard.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcyNoticeBoard.visibility = View.VISIBLE
        }
    }

    private fun isGetNoticeBoardList() {
        Constant.showLoading(this)
        mAdapter = SchoolNoticeBoardAdapter(
            emptyList(), this, this, false,
            binding.nomessage,
            binding.txtNoData,
            true
        )
        binding.rcyNoticeBoard.layoutManager = GridLayoutManager(this, 2)
        binding.rcyNoticeBoard.isNestedScrollingEnabled = false
        binding.rcyNoticeBoard.adapter = mAdapter
        appViewModel!!.isNoticeBoardReport(isAccessToken!!, this)
    }
}
