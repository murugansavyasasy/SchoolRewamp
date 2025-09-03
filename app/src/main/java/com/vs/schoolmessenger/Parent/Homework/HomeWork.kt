package com.vs.schoolmessenger.Parent.Homework

import android.content.Intent
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

    override fun getViewBinding(): ParentHomeworkActivityBinding {
        return ParentHomeworkActivityBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()


        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token
        binding.lblName.text = childDetails!!.name
        binding.lblSection.text = childDetails!!.standard_name + " - " + childDetails.section_name
        binding.imgBack.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)
        binding.recyclerViewCalendar.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        dateList = generateCalendarDates()

        val todayDate =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        isHomeWorkDate = todayDate
        calendarAdapter = CalendarAdapter(dateList, todayDate) {
            isHomeWorkDate = it.fullDate
            val isHomeWorkData = isHomeWorkData?.find { it.date == isHomeWorkDate }
            if (isHomeWorkData != null) {
                binding.cytNoDataFound.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                mAdapter!!.updateList(isHomeWorkData.homework, isHomeWorkData.date)
            } else {
                binding.cytNoDataFound.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                mAdapter!!.updateList(emptyList(), "")
            }
        }

        isHomeWorkList()
        binding.recyclerViewCalendar.adapter = calendarAdapter

        binding.recyclerViewCalendar.post {
            val centerOffset = binding.recyclerViewCalendar.width / 2 - 35
            (binding.recyclerViewCalendar.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                10, centerOffset
            )
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
            if (response!!.status) {
                isHomeWorkData = response.data
                val isHomeWorkData = isHomeWorkData?.find { it.date == isHomeWorkDate }
                if (isHomeWorkData != null) {
                    binding.cytNoDataFound.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    isLoadHomeWorkData(isHomeWorkData.homework, isHomeWorkData.date)
                } else {
                    binding.cytNoDataFound.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    isLoadHomeWorkData(emptyList(), "")
                }
            }
        }
    }

    fun isLoadHomeWorkData(data: List<GetHomeworkDetails>, isHomeWorkDate: String) {
        mAdapter = HomeworkParentAdapter(data, this, Constant.isShimmerViewDisable, isHomeWorkDate)
        binding.recyclerView.layoutManager =
            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.setHasFixedSize(true)
    }

    fun generateCalendarDates(): List<CalendarDate> {
        val list = mutableListOf<CalendarDate>()
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DATE, -10)

        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("dd", Locale.getDefault())
        val fullFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())

        for (i in 0..10) {
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



    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.imgSearch -> {
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
            emptyList(), this, Constant.isShimmerViewShow, isHomeWorkDate
        )
        binding.recyclerView.layoutManager =
            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.setHasFixedSize(true)
        appViewModel?.isHomeWorkDetails(isAccessToken!!, this)
    }

    override fun onItemClick(data: GetHomeworkDetails, isHomeWorkDate: String) {
        if (data.is_unread) {
            val jsonObject = JsonObject().apply {
                addProperty(APIKeyNames.type, "HOMEWORK")
                addProperty(APIKeyNames.detail_id, data.id)
            }
            isAccessToken?.let {
                appViewModel?.isUpdateStatusCommunication(it, jsonObject, this)
            }
        }

        val isHomeWorkData = FilePreview(
            id = data.id,
            title = data.title,
            description = data.description,
            subjectName = data.subject_name,
            sentBy = data.sent_by,
            thumbnail = data.thumbnail,
            isUnread = data.is_unread,
            isCompleted = data.is_completed,
            isMenuType = Constant.M_HOMEWORK,
            fileList = data.file_path,
        )

        val intent = Intent(this@HomeWork, ChildHomeWork::class.java)
        intent.putExtra("isPreViewData", isHomeWorkData)
        intent.putExtra("isHomeWorkDate", isHomeWorkDate)
        Log.d("samekkeaaaaa", isHomeWorkDate)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

}
