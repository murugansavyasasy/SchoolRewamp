package com.vs.schoolmessenger.Parent.Communication

import android.text.Editable
import android.util.Log
import android. text. TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CommunicationBinding

class Communication : BaseActivity<CommunicationBinding>(), View.OnClickListener, VoiceClickListener {

    override fun getViewBinding() = CommunicationBinding.inflate(layoutInflater)

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var allVoiceData = mutableListOf<VoiceData>()
    private var adapter: UnifiedVoiceAdapter? = null
    private var isInitialLoad = true
    private var isFromArchive = false
    private var hasFetchedMore = false
    private var currentFilter: String = "ALL"
    private var currentSearchQuery: String = ""

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaVoiceMessage.setOnClickListener(this)
        binding.seeMoreLabel.setOnClickListener(this)
        binding.imgFilter.setOnClickListener(this)

        isFromArchive = intent.getBooleanExtra("fromArchive", false)
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        showShimmer()

        binding.recyclerMore.post {
            binding.recyclerMore.requestFocus()
            binding.recyclerMore.layoutManager?.let { layoutManager ->
                val itemCount = adapter?.itemCount ?: 0
                if (itemCount > 0 && layoutManager is LinearLayoutManager) {
                    layoutManager.scrollToPositionWithOffset(itemCount - 1, 0)
                }
            }
        }

        binding.txtSearchMenu.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                applyCombinedFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        appViewModel?.isGetCommmunicationlist?.observe(this) { response ->
            if (response?.status == true) {
                appendData(response.data, archiveFlag = true)
            } else {
                checkAndShowNoData()
            }
        }

        appViewModel?.isGetCommmunicationlistload?.observe(this) { response ->
            if (response?.status == true) {
                appendData(response.data, archiveFlag = false)
            } else {
                checkAndShowNoData()
            }
        }

        fetchInitialData()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.imgFilter -> {
                val popupMenu = PopupMenu(this, v)
                popupMenu.menu.add("TEXT")
                popupMenu.menu.add("VOICE")
                popupMenu.menu.add("READ")
                popupMenu.menu.add("UNREAD")
                popupMenu.menu.add("ALL")

                popupMenu.setOnMenuItemClickListener { item ->
                    currentFilter = item.title.toString()
                    applyCombinedFilter()
                    true
                }

                popupMenu.show()
            }

            R.id.rlaTextMessage -> {
                adapter?.updateData()
                adapter?.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(binding.rlaTextMessage, binding.imgTextMessage, binding.lblTextMessage)
            }

            R.id.rlaVoiceMessage -> {
                adapter?.updateData()
                adapter?.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(binding.rlaVoiceMessage, binding.imgVoiceMessage, binding.lblVoiceMessage)
            }

            R.id.seeMoreLabel -> {
                if (!hasFetchedMore) {
                    hasFetchedMore = true
                    binding.seeMoreLabel.visibility = View.GONE
                    fetchMoreData()
                }
            }
        }
    }

    private fun applyCombinedFilter() {
        var filteredList = when (currentFilter) {
            "TEXT" -> allVoiceData.filter { it.type.equals("TEXT", ignoreCase = true) }
            "VOICE" -> allVoiceData.filter { it.type.equals("VOICE", ignoreCase = true) }
            "READ" -> allVoiceData.filter { it.is_unread == false }
            "UNREAD" -> allVoiceData.filter { it.is_unread == true }
            else -> allVoiceData
        }

        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.description?.contains(currentSearchQuery, ignoreCase = true) == true ||
                        it.content?.contains(currentSearchQuery, ignoreCase = true) == true
            }
        }

        adapter?.updateList(filteredList)
        checkAndShowNoData(filteredList)
    }

    private fun fetchInitialData() {
        isInitialLoad = true
        appViewModel?.isGetCommmunicationlistload(isAccessToken.orEmpty(), this)
    }

    private fun fetchMoreData() {
        isInitialLoad = false
        appViewModel?.isGetCommmunicationlist(isAccessToken.orEmpty(), this)
    }

    private fun appendData(newData: List<VoiceData>?, archiveFlag: Boolean) {
        if (isInitialLoad) allVoiceData.clear()

        newData?.let {
            val processedData = it.map { item -> item.copy(is_archive = archiveFlag) }
            allVoiceData.addAll(processedData)

            if (adapter == null) {
                adapter = UnifiedVoiceAdapter(
                    allVoiceData as ArrayList<VoiceData>,
                    this,
                    this,
                    Constant.isShimmerViewDisable,
                    this,
                    isAccessToken.orEmpty(),
                    archiveFlag
                )
                binding.recyclerInitial.layoutManager = LinearLayoutManager(this)
                binding.recyclerInitial.adapter = adapter
            } else {
                adapter?.setIsFromArchive(archiveFlag)
                adapter?.updateList(allVoiceData)
            }

            applyCombinedFilter() // Reapply filters after appending data
        }

        checkAndShowNoData()
    }

    private fun checkAndShowNoData(filteredList: List<VoiceData>? = null) {
        val listToCheck = filteredList ?: allVoiceData
        val isEmpty = listToCheck.isEmpty()

        binding.txtNoData.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.nomessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerInitial.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showShimmer() {
        val shimmerAdapter = UnifiedVoiceAdapter(
            null,
            this,
            this,
            Constant.isShimmerViewShow,
            this,
            isAccessToken.orEmpty(),
            isFromArchive
        )
        binding.recyclerInitial.layoutManager = LinearLayoutManager(this)
        binding.recyclerInitial.adapter = shimmerAdapter
    }

    override fun onUpdateArchiveStatus(type: String?, id: String?) {
        val jsonObject = JsonObject().apply {
            addProperty("type", type)
            addProperty("detail_id", id)
        }

        isAccessToken?.let {
            appViewModel?.isUpdateStatusArchive(it, jsonObject, this)
        }
    }

    override fun onUpdateCommunicationStatus(type: String?, id: String?) {
        val jsonObject = JsonObject().apply {
            addProperty("type", type)
            addProperty("detail_id", id)
        }

        isAccessToken?.let {
            appViewModel?.isUpdateStatusCommunication(it, jsonObject, this)
        }
    }

    override fun onItemClick(data: VoiceData, holder: UnifiedVoiceAdapter.DataViewHolder) {
        // Handle item click here
    }

    private fun isChangeBackRoundCommunicationType(
        isTypeCommunication: RelativeLayout,
        imgTypeCommunication: ImageView,
        lblTypeCommunication: TextView
    ) {
        binding.rlaVoiceMessage.background = null
        binding.rlaTextMessage.background = null
        isTypeCommunication.background = ContextCompat.getDrawable(this, R.drawable.bg_gradient_redious_parent)

        binding.lblVoiceMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.lblTextMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        lblTypeCommunication.setTextColor(ContextCompat.getColor(this, R.color.white))

        binding.imgVoiceMessage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mic_icon_black))
        binding.imgTextMessage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.text_icon_black))

        when (imgTypeCommunication) {
            binding.imgVoiceMessage -> binding.imgVoiceMessage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mic_icon))
            binding.imgTextMessage -> binding.imgTextMessage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.text_icon))
        }
    }
}


