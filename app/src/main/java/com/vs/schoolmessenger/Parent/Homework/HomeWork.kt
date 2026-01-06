package com.vs.schoolmessenger.Parent.Homework

import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.CalendarAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.HomeworkParentAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.CalendarDate
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentHomeworkActivityBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeWork : BaseActivity<ParentHomeworkActivityBinding>(), View.OnClickListener,
    HomeWorkDateClickListener {
    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isHomeWorkDate = ""
    private var mAdapter: HomeworkParentAdapter? = null
    var isHomeWorkData: List<GetDateWiseHomeworkData>? = null
    private lateinit var dateList: List<CalendarDate>
    private lateinit var calendarAdapter: CalendarAdapter

    private var msg_id: Int = -1

    private var fromNotification: Boolean = false

    override fun getViewBinding(): ParentHomeworkActivityBinding {
        return ParentHomeworkActivityBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token

        binding.toolbarLayout.lblStudentName.text = childDetails!!.name
        binding.lblHomeWork.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblStudentSection.text =
            childDetails!!.standard_name + " - " + childDetails.section_name
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)
        binding.recyclerViewCalendar.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        msg_id = intent.getIntExtra(Constant.msg_id, -1)

        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)




        dateList = generateCalendarDates()

        val todayDate =
            SimpleDateFormat(
                Constant.ddMMyyyy,
                Locale.getDefault()
            ).format(Calendar.getInstance().time)
        isHomeWorkDate = todayDate
        calendarAdapter = CalendarAdapter(dateList, todayDate) {
            isHomeWorkDate = it.fullDate
            val isHomeWorkData = isHomeWorkData?.find { it.date == isHomeWorkDate }
            if (isHomeWorkData != null) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                binding.cytNoDataFound.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.lytSearch.visibility = View.GONE
                binding.edtSearch.setText("")
                mAdapter!!.updateList(isHomeWorkData.homework, isHomeWorkData.date)
            } else {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
                binding.lytSearch.visibility = View.GONE
                binding.edtSearch.setText("")
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                binding.cytNoDataFound.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                mAdapter!!.updateList(emptyList(), "")
            }
        }

        isHomeWorkList()
        binding.recyclerViewCalendar.adapter = calendarAdapter

        binding.recyclerViewCalendar.post {
            val todayDate = SimpleDateFormat(Constant.ddMMyyyy, Locale.getDefault())
                .format(Calendar.getInstance().time)

            val todayPos = dateList.indexOfFirst { it.fullDate == todayDate }
            if (todayPos != -1) {
                val centerOffset = binding.recyclerViewCalendar.width / 2 - 35
                (binding.recyclerViewCalendar.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(todayPos, centerOffset)
            }
        }


        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                mAdapter?.filter(query)

                binding.recyclerView.post {
                    if (mAdapter?.itemCount == 0) {
                        binding.recyclerView.visibility = View.GONE
                        binding.cytNoDataFound.visibility = View.VISIBLE
                    } else {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.cytNoDataFound.visibility = View.GONE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        appViewModel?.isHomeWorkDetailsList?.observe(this) { response ->
            if (response != null) {
                if (response!!.status) {
                    val mobileNumber = SharedPreference.getMobileNumber(this)
                    val jsonObject = JsonObject().apply {
                        addProperty(APIKeyNames.mobile_number, mobileNumber)
                        addProperty(APIKeyNames.activity, Constant.add_points_homework)
                        addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
                        addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
                    }
                    appViewModel?.isAddRewardPoints(isAccessToken ?: "", jsonObject,this)

                    isHomeWorkData = response.data
                    val isHomeWorkData = isHomeWorkData?.find { it.date == isHomeWorkDate }
                    if (isHomeWorkData != null) {
                        binding.lytSearch.visibility = View.GONE
                        binding.edtSearch.setText("")
                        Log.d("isHomeWorkData", isHomeWorkData.toString())
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                        binding.cytNoDataFound.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        isLoadHomeWorkData(isHomeWorkData.homework, isHomeWorkData.date)
                        scrollToMessageId(msg_id)
                    } else {
                        binding.lytSearch.visibility = View.GONE
                        binding.edtSearch.setText("")
                        Log.d("isHomeWorkData", isHomeWorkData.toString())
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                        binding.cytNoDataFound.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                        isLoadHomeWorkData(emptyList(), "")
                    }
                } else {
                    binding.lytSearch.visibility = View.GONE
                    binding.edtSearch.setText("")
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.cytNoDataFound.visibility = View.VISIBLE
                    binding.lblNoData.text =
                        response.message ?: getString(R.string.no_homework_found)
                    binding.recyclerView.visibility = View.GONE
                    isLoadHomeWorkData(emptyList(), "")
                }
            }
        }
    }

    fun isLoadHomeWorkData(data: List<GetHomeworkDetails>, isHomeWorkDate: String) {
        mAdapter = HomeworkParentAdapter(data, this, Constant.isShimmerViewDisable, isHomeWorkDate,this)
        binding.recyclerView.layoutManager =
            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.setHasFixedSize(true)
    }

    fun generateCalendarDates(): List<CalendarDate> {
        val list = mutableListOf<CalendarDate>()
        val calendar = Calendar.getInstance()

        val today = calendar.time

        calendar.add(Calendar.MONTH, -6)

        val dayFormatter = SimpleDateFormat(Constant.EEE, Locale.getDefault())
        val dateFormatter = SimpleDateFormat(Constant.dd, Locale.getDefault())
        val fullFormatter = SimpleDateFormat(Constant.ddMMyyyy, Locale.getDefault())
        val monthFormatter = SimpleDateFormat(Constant.MMM_, Locale.getDefault())

        while (!calendar.time.after(today)) {
            val date = calendar.time
            list.add(
                CalendarDate(
                    day = dayFormatter.format(date),
                    date = dateFormatter.format(date),
                    fullDate = fullFormatter.format(date),
                    month = monthFormatter.format(date)
                )
            )
            calendar.add(Calendar.DATE, 1)
        }
        return list
    }

    fun updateList(homeworkId: String) {
        val data = mAdapter?.isHomeWorkData?.find { it.id == homeworkId }
        data?.let {
            val updatedData = it.copy(is_completed = true)
            mAdapter?.updateItem(updatedData)
        }
    }


    override fun onResume() {
        super.onResume()
        isHomeWorkList()
        Constant.isCompletedHomeworkId?.let { homeworkId ->
            updateList(homeworkId)
            Constant.isCompletedHomeworkId = null
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.imgSearchToolBar -> {
                if (binding.lytSearch.visibility == View.VISIBLE) {
                    binding.lytSearch.visibility = View.GONE
                    binding.edtSearch.setText("")
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
                } else {
                    binding.lytSearch.visibility = View.VISIBLE
                    binding.edtSearch.setText("")
                    binding.edtSearch.requestFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.edtSearch, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
    }


    fun isHomeWorkList() {
        mAdapter = HomeworkParentAdapter(
            emptyList(), this, Constant.isShimmerViewShow, isHomeWorkDate,this
        )
        binding.recyclerView.layoutManager =
            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.setHasFixedSize(true)
        appViewModel?.isHomeWorkDetails(isAccessToken!!, this)
    }

    override fun onItemClick(data: GetHomeworkDetails, isHomeWorkDate: String) {
        Log.d("flag", data.is_unread.toString())
        if (data.is_unread) {
            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.type, Constant.HOMEWORK)
                addProperty(APIKeyNames.detail_id, data.detail_id)
            }
            isAccessToken?.let {
                appViewModel?.isUpdateStatusCommunication(it, jsonObject, this)
            }
            // locally mark as read (before/after API success)
            val updatedData = data.copy(is_unread = false)
            mAdapter?.updateItem(updatedData)
        }

        val isHomeWorkData = FilePreview(
            id = data.id,
            title = data.title,
            description = data.description,
            subjectName = data.subject_name,
            sentBy = data.sent_by,
            thumbnail = data.thumbnail,
            created_date = isHomeWorkDate,
            isUnread = data.is_unread,
            isCompleted = data.is_completed,
            isMenuType = Constant.M_HOMEWORK,
            fileList = data.file_path,
        )

        val intent = Intent(this@HomeWork, ChildHomeWork::class.java)
        intent.putExtra(Constant.isPreViewData, isHomeWorkData)
        intent.putExtra(Constant.isHomeWorkDate, isHomeWorkDate)
        Log.d("samekkeaaaaa", isHomeWorkDate)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }


    private fun scrollToMessageId(msg_id: Int) {
        if (msg_id == -1) return

        isHomeWorkData?.let { dateWiseList ->
            val flatList = dateWiseList.flatMap { it.homework }

            val index = flatList.indexOfFirst { it.id.toIntOrNull() == msg_id }

            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index (msg_id: $msg_id)")
                binding.recyclerView.post {
                    binding.recyclerView.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.recyclerView, index)
                }
            } else {
                Log.d("ScrollDebug", "No index found for msg_id $msg_id")
            }
        }
    }


    private fun highlightItemTemporarily(recyclerView: RecyclerView, position: Int) {
        recyclerView.post {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.itemView?.setBackgroundColor(Color.parseColor("#FFE082"))
            recyclerView.postDelayed({
                viewHolder?.itemView?.setBackgroundColor(Color.TRANSPARENT)
            }, 2000)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
