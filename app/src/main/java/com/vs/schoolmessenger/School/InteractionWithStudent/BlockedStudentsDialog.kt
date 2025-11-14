package com.vs.schoolmessenger.School.InteractionWithStudent

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.AbsenteesReport.Model.Student
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.BlockedStudent
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.BlockListstudentBinding

class BlockedStudentsDialog : DialogFragment(), InteractionWithStudentListener {

    private lateinit var binding: BlockListstudentBinding
    private var isAccessToken: String? = null
    private lateinit var viewModel: App
    private lateinit var mAdapter: BlockListStudentAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = BlockListstudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                (resources.displayMetrics.heightPixels * 0.90).toInt()
            )
            attributes = attributes.apply { gravity = Gravity.CENTER }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val staffDetails = SharedPreference.getStaffDetails(requireContext())
        isAccessToken = staffDetails?.access_token

        viewModel = ViewModelProvider(this).get(App::class.java)
        viewModel.init()

        binding.imgBack.setOnClickListener {
            dismiss()
        }


        viewModel?.isblockstudent?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    showDataValidation(
                        resources.getString(R.string.success),
                        response.message ?: getString(R.string.updated_successfully), requireActivity()
                    )
                } else {
                    showDataValidation(
                        resources.getString(R.string.fail),
                        response.message, requireActivity()
                    )
                }
            }
        }



        fetchStudentData()


        viewModel.isblockstudentlist!!.observe(viewLifecycleOwner) { response ->
            if (response?.status == true) {
                isLoadStaffData(response.data)
                binding.lytList.visibility = View.GONE
            } else {

                showErrorUI(response?.message ?: getString(R.string.no_blocked_data_available))
            }
        }
    }


    private fun showDataValidation(title: String, message: String, activity: Activity) {
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
            closePopup()
            dismiss()
        }
    }

    private fun fetchStudentData() {
        viewModel.isblockstudentlist(isAccessToken ?: "")
    }

    private fun showErrorUI(message: String) {
        binding.nestedScrollview.visibility = View.GONE
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.rcystudentdata.visibility = View.GONE
    }

    private fun isLoadStaffData(data: List<BlockedStudent>?) {
        if (data.isNullOrEmpty()) {
            showErrorUI("No blocked students found")
            return
        }

        binding.nestedScrollview.visibility = View.VISIBLE

        binding.rcystudentdata.layoutManager = LinearLayoutManager(requireContext())
        mAdapter = BlockListStudentAdapter(data, this, requireContext(), false)
        binding.rcystudentdata.adapter = mAdapter
    }

    override fun onBlockedSearchResultEmpty(isEmpty: Boolean) {
//        binding.nomessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
//        binding.txtNoData.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rcystudentdata.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onUnblockClick(
        data: BlockedStudent,
        isPosition: Int
    ) {
        if (data.id.isNullOrEmpty()) {
            Constant.showDataValidation(
                getString(R.string.error),
                "Invalid student id",
                requireActivity()
            )
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty("student_id", data.id)
            addProperty("is_block", false)
            addProperty("reason", "")
        }
        viewModel.isblockstudent(isAccessToken!!, jsonObject)
        dismiss()
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {}
    override fun onClickItem(data: StudentChatData) {}
    override fun onReadStatusClick(data: StudentChatData, isPosition: Int) {}
}
