package com.vs.schoolmessenger.Parent.LSRW

import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.LSRW.Model.SkillData
import com.vs.schoolmessenger.Parent.LSRW.Model.lsrwitemclicklistener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LsrwBinding


class LSRW : BaseActivity<LsrwBinding>(), View.OnClickListener, lsrwitemclicklistener {

    private lateinit var adapter: LSRWAdapter
    private lateinit var appViewModel: App
    private var isAccessToken: String? = null
    private var allItems: List<SkillData> = emptyList()

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    var userDetails: UserDetails? = null


    override fun getViewBinding(): LsrwBinding {
        return LsrwBinding.inflate(layoutInflater)
    }

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

            Log.d("NoticeBoard_EXTRAS", "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name")

            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }



        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.lsrw)

        binding.root.post {
            val finalName =
                Constant.isSelectedMenuName?.takeIf { it.isNotEmpty() } ?: menu_name ?: ""
            Log.d("NoticeBoard_HeaderFinal", "Setting headerview text: $finalName")
            binding.lblHeaderTitle.text = finalName
            binding.lblHeaderTitle.visibility = View.VISIBLE
        }
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.imgSearchToolBar.setOnClickListener(this)

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
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
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token
        binding.toolbarLayout.lblStudentName.text = childDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            "${childDetails?.standard_name ?: ""} - ${childDetails?.section_name ?: ""}"

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        binding.rcyrecyclerview.layoutManager = LinearLayoutManager(this)
        adapter = LSRWAdapter(emptyList(), this, this)
        binding.rcyrecyclerview.adapter = adapter

        fetchLsrwSkillReportData()

        binding.toolbarLayout.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        appViewModel.islsrwSkilllist?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response != null) {
                if (response?.status == true && !response.data.isNullOrEmpty()) {
                    binding.rcyrecyclerview.visibility = View.VISIBLE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    binding.rlNoDataContainer.visibility = View.GONE
                    allItems = response.data
                    adapter.updateList(allItems)
                    if (fromNotification) {
                        scrollToMessageId(headerId)
                    }
                } else {
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.rcyrecyclerview.visibility = View.GONE
                    binding.toolbarLayout.rytSearch.visibility = View.GONE
                    binding.rlNoDataContainer.visibility = View.VISIBLE
                    binding.noDataFound.text = response.message ?: getString(R.string.no_data_found)
                }
            }
        }

    }


    private fun scrollToMessageId(headerId: String?) {
        if (msg_id == -1) return

        allItems?.let { list ->
            val index = list.indexOfFirst { it.id == headerId }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in ongoing")
                binding.rcyrecyclerview.post {
                    binding.rcyrecyclerview.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rcyrecyclerview, index)
                }
            } else {
                Log.d("ScrollDebug", "No item found with headerId: $headerId")
            }
        }
        Log.d("ScrollDebug", "No index found for headerId $headerId")
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


    private fun fetchLsrwSkillReportData() {
        Constant.showLoading(this)
        binding.rcyrecyclerview.visibility = View.VISIBLE
        binding.rcyrecyclerview.isNestedScrollingEnabled = false
        appViewModel.islsrwSkilllist(isAccessToken ?: "",this)
    }

    private fun filterList(query: String) {
        val filteredList = if (query.isEmpty()) {
            allItems
        } else {
            allItems.filter { item ->
                item.subject?.contains(query, ignoreCase = true) == true ||
                        item.activity_type?.contains(query, ignoreCase = true) == true ||
                        item.title?.contains(query, ignoreCase = true) == true ||
                        item.description?.contains(query, ignoreCase = true) == true ||
                        item.sent_by?.contains(query, ignoreCase = true) == true
            }
        }

        adapter.updateList(filteredList)

        if (filteredList.isNotEmpty()) {
            binding.rcyrecyclerview.visibility = View.VISIBLE
            binding.rlRecyclerContainer.visibility = View.VISIBLE
            binding.rlNoDataContainer.visibility = View.GONE
        } else {
            binding.rcyrecyclerview.visibility = View.GONE
            binding.rlRecyclerContainer.visibility = View.GONE
            binding.rlNoDataContainer.visibility = View.VISIBLE
            binding.noDataFound.text = getString(R.string.no_data_found)
        }

        binding.rcyrecyclerview.scrollToPosition(0)
    }

    override fun onBackPressed() {
        Constant.selectedFiles.clear()
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBack -> {
                Constant.selectedFiles.clear()
                onBackPressed()
            }


        }
    }

    override fun onReadStatusClick(
        isData: SkillData,
        isPosition: Int
    ) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, "LSRW")
            addProperty(APIKeyNames.detail_id, isData.detail_id)
        }
        appViewModel?.isUpdateStatusCommunication(isAccessToken!!, jsonObject, this)
    }

}