package com.vs.schoolmessenger.School.LSRW

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LSRW.Adapter.LsrwCompletedAdapter
import com.vs.schoolmessenger.School.LSRW.Listener.lsrwskillreportlistener
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.CompletedTasklistBinding


class CompletedTaskList : BaseActivity<CompletedTasklistBinding>(), View.OnClickListener,
    lsrwskillreportlistener {

    override fun getViewBinding(): CompletedTasklistBinding {
        return CompletedTasklistBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: LsrwCompletedAdapter
    private lateinit var LsrwTaskList: List<LsrwTask>

    var isLsrwId = ""
    var isLsrwPosition = 0

    private var deleteFrom: String = ""

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    override fun setupViews() {
        super.setupViews()

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel!!.init()


        binding = CompletedTasklistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearchbox.visibility == View.VISIBLE) {
                binding.rytSearchbox.visibility = View.GONE
                binding.txtSearchBox.text.clear()
                binding.root.hideKeyboard()

            } else {
                binding.rytSearchbox.visibility = View.VISIBLE
                binding.txtSearchBox.text.clear()
            }
        }

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.Completed_Task)
        val taskList =
            intent.getParcelableArrayListExtra<LsrwTask>(Constant.COMPLETED_TASK_LIST)
                ?: arrayListOf()
        LsrwTaskList = taskList


        if (taskList.isNullOrEmpty()) {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            ErrorMessage(getString(R.string.no_data_found))
        } else {
            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            ShowData()
            binding.rcycompletedtaskrcy.layoutManager = LinearLayoutManager(this)
            adapter = LsrwCompletedAdapter(
                itemList = taskList,
                context = this,
                this,
                noDataImage = binding.noDataImage,
                noDataText = binding.noDataFound
            )
            binding.rcycompletedtaskrcy.adapter = adapter
        }

        binding.txtSearchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TEXTCOMING", "Text changed to: ${s.toString()}")
                filter(s.toString())
            }
        })

        appViewModel!!.isLsrwDelete?.observe(this) { response ->
            Constant.hideLoading(this@CompletedTaskList)
            if (response != null) {
                if (response.status) {
                    showDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            }
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    private fun filter(text: String) {
        val filteredList = if (text.isBlank()) {
            LsrwTaskList
        } else {

            val searchWords = text.trim().lowercase().split("\\s+".toRegex())

            LsrwTaskList.filter { lsrw ->
                val fieldsToSearch = listOf(
                    lsrw.title.lowercase(),
                    lsrw.description.lowercase(),
                    lsrw.created_on.lowercase(),
                    lsrw.subject.lowercase(),
                    lsrw.submitted_average.lowercase(),
                    lsrw.activity_type.lowercase(),
                )

                searchWords.all { word ->
                    fieldsToSearch.any { field ->
                        field.contains(word)
                    }
                }
            }

        }

        if (filteredList.isNotEmpty()) {
            adapter.updateList(filteredList)
            ShowData()

        } else {
            binding.rcycompletedtaskrcy.visibility = View.GONE
            ErrorMessage(resources.getString(R.string.no_data_found))
        }
    }

    fun ErrorMessage(ErrorMessage: String) {
        binding.lytNoDataFound.visibility = View.VISIBLE
        binding.noDataFound.text = ErrorMessage
    }

    fun ShowData() {
        binding.rcycompletedtaskrcy.visibility = View.VISIBLE
        binding.lytNoDataFound.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }

    override fun onEditAndDeleteCompleted(
        data: LsrwTask,
        anchorView: View,
        adapterPosition: Int,
        source: String
    ) {
        isLsrwId = data.id
        isLsrwPosition = adapterPosition
        deleteFrom = source
        showEditDeletePopup(data, anchorView)

    }


    fun showEditDeletePopup(data: LsrwTask, anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_edit_delete, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        val layoutEdit = popupView.findViewById<LinearLayout>(R.id.layout_edit)
        val layoutDelete = popupView.findViewById<LinearLayout>(R.id.layout_delete)

        if (data.can_edit) {
            layoutEdit.visibility = View.GONE
        } else {
            layoutEdit.visibility = View.GONE
        }

        if (data.can_delete) {
            layoutDelete.visibility = View.VISIBLE
        } else {
            layoutDelete.visibility = View.GONE
        }

        layoutDelete.setOnClickListener {
            showSendConfirmationDialog(false)
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, 0, 10)
    }


    fun showSendConfirmationDialog(isEventUpdate: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.alert_popup, null)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val alertMessage = dialogView.findViewById<TextView>(R.id.alertMessage)
        val lblSelectTarget = dialogView.findViewById<TextView>(R.id.lblSelectTarget)
        alertMessage.text = getString(R.string.are_you_sure_want_to_delete)


        lblSelectTarget.visibility = View.GONE

        okButton.setOnClickListener {
            alertDialog.dismiss()
            Constant.showLoading(this)
            val jsonObject = JsonObject()
            jsonObject.addProperty(APIKeyNames.id, isLsrwId)
            appViewModel?.isLsrwDelete(isAccessToken!!, jsonObject, this)


        }
        btnCancel.setOnClickListener { alertDialog.dismiss() }
    }

    fun showDataValidation(title: String, message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val titleText = view.findViewById<TextView>(R.id.alertTitle)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        titleText.text = title
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            val intent = Intent(activity, LsrwMain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            activity.finish()
            closePopup()
        }
    }

}