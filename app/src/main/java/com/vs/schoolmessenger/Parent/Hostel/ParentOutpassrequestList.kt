package com.vs.schoolmessenger.Parent.Hostel

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Hostel.Adapter.OutpassRequestList.OutpassRequestList
import com.vs.schoolmessenger.Parent.Hostel.Listner.gatePassClickListner
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.OutpassRequestData
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDetails.getParentHostelDetailsData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ParentHostelOutpassRequestListBinding

class ParentOutpassrequestList : BaseActivity<ParentHostelOutpassRequestListBinding>(),
    View.OnClickListener, gatePassClickListner {

    override fun getViewBinding(): ParentHostelOutpassRequestListBinding {
        return ParentHostelOutpassRequestListBinding.inflate(layoutInflater)
    }
    private var appViewModel: App? = null
    lateinit var nAdapter: OutpassRequestList
    private var isDialogShowing = false

    private var currentFilteredList: List<OutpassRequestData> = listOf()
    private var isParentHostelDetails: List<getParentHostelDetailsData> = listOf()





    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.consStudentDetails.visibility = View.GONE
        binding.toolbarLayout.imgCall.visibility = View.GONE
        binding.toolbarLayout.rlaSpinner.visibility = View.GONE
        binding.toolbarLayout.lblMenuName.visibility = View.VISIBLE
        binding.toolbarLayout.lblMenuName.text= getString(R.string.outpass_report)

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        binding.toolbarLayout.imgBack.setOnClickListener(this)

        binding.toolbarLayout.imgSearchIcon.setOnClickListener {
            if (binding.rytSearch.isVisible) {
                binding.rytSearch.visibility = View.GONE
                binding.txtSearch.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch.windowToken, 0)
            } else {
                binding.rytSearch.visibility = View.VISIBLE
                binding.txtSearch.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearch.windowToken, 0)

            }
        }

        binding.txtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextSSS", s.toString())
                filter(s.toString())

            }
        })


        val list = intent.getSerializableExtra("OUTPASS_LIST") as? ArrayList<OutpassRequestData> ?: arrayListOf()
        currentFilteredList=list


        val isHostelDetails = intent.getSerializableExtra("PARENT_HOSTEL_LIST") as? ArrayList<getParentHostelDetailsData> ?: arrayListOf()
        isParentHostelDetails=isHostelDetails

        if (list.size>0){
            binding.toolbarLayout.imgSearchIcon.visibility= View.VISIBLE
        }
        else{
            binding.toolbarLayout.imgSearchIcon.visibility= View.GONE

        }
        isLoadOutpassRequest(list)


    }

    private fun filter(text: String) {
        val filteredList = if (text.isBlank()) {
            currentFilteredList
        } else {
            val searchWords = text.trim().lowercase().split("\\s+".toRegex())
            currentFilteredList.filter { outpass ->
                val fieldsToSearch = listOf(
                    outpass.reason.lowercase(),
                    outpass.request_time.lowercase(),
                    outpass.fromdate_todate.lowercase(),
                    outpass.status.lowercase(),
                )
                // Check if ALL search words are found in ANY of the fields(feildTosearch List ie name,email...etc)
                searchWords.all { word ->
                    fieldsToSearch.any { field ->
                        field.contains(word)
                    }
                }
            }

        }

        if (filteredList.isNotEmpty()) {
            ShowData()
            nAdapter.updateData(filteredList)
        } else {
            ErrorMessage(Constant.NO_DATA_FOUND)
        }
    }

    fun ErrorMessage(ErrorMessage: String) {
        binding.rcHostelOutpassRequest.visibility = View.GONE
        binding.lblErrorMessage.visibility = View.VISIBLE
        binding.imgNoDataFound.visibility = View.VISIBLE
        binding.lblErrorMessage.text = ErrorMessage
    }

    fun ShowData() {
        binding.rcHostelOutpassRequest.visibility = View.VISIBLE
        binding.lblErrorMessage.visibility = View.GONE
        binding.imgNoDataFound.visibility = View.GONE
    }



    private fun isLoadOutpassRequest(newData: List<OutpassRequestData>) {
        ShowData()
        currentFilteredList=newData
        binding.rcHostelOutpassRequest.visibility = View.VISIBLE
        nAdapter = OutpassRequestList(
            newData,this,this, Constant.isShimmerViewDisable
        )
        binding.rcHostelOutpassRequest.layoutManager = LinearLayoutManager(this)
        binding.rcHostelOutpassRequest.adapter = nAdapter
    }

    fun showGatepassDialog(
        activity: Activity,
        gatePass: OutpassRequestData
    ) {

        if (isDialogShowing || activity.isFinishing || activity.isDestroyed) return
        isDialogShowing = true

        val dialogView =
            LayoutInflater.from(activity).inflate(R.layout.parent_hostel_gate_pass, null)

        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()

        val gatePassLayout = dialogView.findViewById<View>(R.id.hostel_gate_pass)

        val lblSessionNo = gatePassLayout.findViewById<TextView>(R.id.lblSessionNo)
        val lblPersonName = gatePassLayout.findViewById<TextView>(R.id.lblPersonName)
        val lblStudentRollNumber = gatePassLayout.findViewById<TextView>(R.id.lblStudentRollNumber)
        val tvExitTime = gatePassLayout.findViewById<TextView>(R.id.tvExitTime)
        val lblReason = gatePassLayout.findViewById<TextView>(R.id.lblReason)
        val tvRoomId = gatePassLayout.findViewById<TextView>(R.id.tvRoomId)
        val tvBlockName = gatePassLayout.findViewById<TextView>(R.id.tvBlockName)
        val tvValidFrom = gatePassLayout.findViewById<TextView>(R.id.tvValidFrom)
        val tvValidTo = gatePassLayout.findViewById<TextView>(R.id.tvValidTo)
        val txtDashLine = gatePassLayout.findViewById<TextView>(R.id.txtDashLine)
        val tvAuthorizedBy = gatePassLayout.findViewById<TextView>(R.id.tvlblAuthorizedBy)


        val input = gatePass?.fromdate_todate?:" - "
        val parts = input.split(" - ")
        val from = parts.getOrNull(0)?.trim() ?: ""
        val to = parts.getOrNull(1)?.trim() ?: ""

        lblSessionNo.text = Constant.getInitials( isParentHostelDetails.firstOrNull()?.student_name?:"")
        lblPersonName.text = isParentHostelDetails.firstOrNull()?.student_name?:""
        lblStudentRollNumber.text = isParentHostelDetails.firstOrNull()?.admission_no?:""
        tvExitTime.text =Constant.getOnlyTime(from)
        lblReason.text = gatePass.reason
        tvRoomId.text =isParentHostelDetails.firstOrNull()?.room_id?:""
        tvValidFrom.text =  Constant.convertDateFormatType2(from)
        tvValidTo.text = Constant.convertDateFormatType2(to)
        tvAuthorizedBy.text = gatePass.action_by?:""
        tvBlockName.text=isParentHostelDetails.firstOrNull()?.floor_name?:""

        setSingleLineDashes(txtDashLine)


        dialogView.setOnClickListener {
            alertDialog.dismiss()
            isDialogShowing = false
        }

        gatePassLayout.setOnClickListener {
            alertDialog.dismiss()
            isDialogShowing = false

        }

    }

    fun setSingleLineDashes(textView: TextView) {

        textView.post {

            val totalWidth = textView.width
            val dash = "— "
            val dashWidth = textView.paint.measureText(dash)
            val count = (totalWidth / dashWidth).toInt()
            val builder = StringBuilder()
            for (i in 0 until count) {
                builder.append(dash)
            }
            textView.text = builder.toString()
            textView.setTextColor(
                ContextCompat.getColor(textView.context, R.color.light_gray_15)
            )
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

        }
    }

    override fun onGatePassClick(data: OutpassRequestData) {
        showGatepassDialog(this,data)
    }
}