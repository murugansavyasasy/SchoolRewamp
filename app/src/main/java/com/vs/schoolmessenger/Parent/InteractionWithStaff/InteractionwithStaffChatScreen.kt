package com.vs.schoolmessenger.Parent.InteractionWithStaff

import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter.InteractionWithStaffChatAdapter
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel.AnswerData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.StaffchatScreenBinding

class InteractionwithStaffChatScreen : BaseActivity<StaffchatScreenBinding>(),
    View.OnClickListener {

    private var isChildDetails: ChildDetails? = null

    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private lateinit var interactionWithStaffChatAdapter: InteractionWithStaffChatAdapter

    val staffData = Constant.StaffDataSending


    override fun getViewBinding(): StaffchatScreenBinding {
        return StaffchatScreenBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        binding.btnSend.setOnClickListener(this)
        isChildDetails = SharedPreference.getChildDetails(this)

        isAccessToken = isChildDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()

        fetchChatData()


        appViewModel?.getstaffanswers?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI("Something went wrong. Please try again.")
                return@observe
            }
            if (response.status) {
                isLoadChatData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
            }
        }


    }


    private fun fetchChatData() {
        appViewModel?.getstaffanswers(
            isAccessToken ?: "",
            staffData?.id ?: "",
            staffData?.subject_id ?: "",
            0,
            staffData?.is_class_teacher ?: false,
            this
        )
    }


    private fun isLoadChatData(data: List<AnswerData>) {

        if (data.isNullOrEmpty()) {
            showErrorUI("No staff data available")
            return
        }

        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcystaffchatdata.visibility = View.VISIBLE
        binding.rcystaffchatdata.layoutManager = LinearLayoutManager(this)

        interactionWithStaffChatAdapter =
            InteractionWithStaffChatAdapter(data ?: listOf(), this, false)
        binding.rcystaffchatdata.adapter = interactionWithStaffChatAdapter

    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcystaffchatdata.visibility = View.GONE
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSend -> {
                Toast.makeText(this, "Development Work In Progress", Toast.LENGTH_SHORT).show()
            }

        }
    }

}

