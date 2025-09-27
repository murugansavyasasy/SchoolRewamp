package com.vs.schoolmessenger.Parent.InteractionWithStaff

import android.content.Intent
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter.InteractionWithStaffAdapter
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Listener.InteractionWithStaffListener
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.Staff
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.StaffDataSending
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.IntectionWithStaffBinding

class InteractionWithStaff : BaseActivity<IntectionWithStaffBinding>(), View.OnClickListener,
    InteractionWithStaffListener {

    private var isChildDetails: ChildDetails? = null

    private var isAccessToken: String? = null
    private var appViewModel: App? = null

    private lateinit var interactionWithStaffAdapter: InteractionWithStaffAdapter

    override fun getViewBinding(): IntectionWithStaffBinding {
        return IntectionWithStaffBinding.inflate(layoutInflater)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener{onBackPressed()}

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener{
            if (binding.rytsearch.isVisible) {
                binding.rytsearch.visibility = View.GONE
                binding.txtVideoMenuBox.setText("")
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtVideoMenuBox.windowToken, 0)
            } else {
                binding.rytsearch.visibility = View.VISIBLE
                binding.txtVideoMenuBox.setText("")
                binding.txtVideoMenuBox.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.txtVideoMenuBox, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name

        isAccessToken = isChildDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        binding.lblHeaderTitle.text=Constant.isParentMenuName

        fetchstaffdata()

        binding.txtVideoMenuBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::interactionWithStaffAdapter.isInitialized) {
                    interactionWithStaffAdapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        appViewModel?.getdetailsforchat?.observe(this) { response ->
            Log.d("response++", response.toString())
            if (response == null) {
                showErrorUI(getString(R.string.Something_went_wrong_Please_try_again))
                binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
                return@observe
            }
            if (response.status) {
                isLoadStaffData(response.data)
            } else {
                showErrorUI(response.message ?: "No data available")
                binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
            }
        }
    }


    private fun fetchstaffdata() {
        appViewModel?.getdetailsforchat(
            isAccessToken ?: "", this
        )
    }

    private fun showErrorUI(message: String) {
        binding.nomessage.visibility = View.VISIBLE
        binding.txtNoData.text = message
        binding.txtNoData.visibility = View.VISIBLE
        binding.rcystaffdata.visibility = View.GONE
    }


    private fun isLoadStaffData(data: List<Staff>?) {
        if (data.isNullOrEmpty()) {
            binding.toolbarLayout.imgSearchToolBar.visibility=View.GONE
            showErrorUI(getString(R.string.no_staff_data_available))
            return
        }
        binding.toolbarLayout.imgSearchToolBar.visibility=View.VISIBLE
        binding.nomessage.visibility = View.GONE
        binding.txtNoData.visibility = View.GONE
        binding.rcystaffdata.visibility = View.VISIBLE
        binding.rcystaffdata.layoutManager = LinearLayoutManager(this)
        binding.rcystaffdata.layoutManager = GridLayoutManager(this, 1)
        interactionWithStaffAdapter = InteractionWithStaffAdapter(data, this, this, false)
        binding.rcystaffdata.adapter = interactionWithStaffAdapter
    }


    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }

    override fun onSearchResultEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.nomessage.visibility = View.VISIBLE
            binding.txtNoData.visibility = View.VISIBLE
            binding.txtNoData.text = (getString(R.string.no_matching_list_found))
            binding.rcystaffdata.visibility = View.GONE
        } else {
            binding.nomessage.visibility = View.GONE
            binding.txtNoData.visibility = View.GONE
            binding.rcystaffdata.visibility = View.VISIBLE
        }
    }

    override fun onClickItem(data: Staff) {
        val intent = Intent(this@InteractionWithStaff, InteractionwithStaffChatScreen::class.java)
        val saveStaffData = StaffDataSending(
            name = data.name,
            subject_id = data.subject_id!!,
            unread_count = data.unread_count!!,
            subject_name = data.subject_name,
            is_class_teacher = data.is_class_teacher,
            id = data.id,
            is_assigned = data.is_assigned
        )
        Constant.StaffDataSending = saveStaffData
        startActivity(intent)
    }
}