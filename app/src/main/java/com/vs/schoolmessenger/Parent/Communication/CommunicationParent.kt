package com.vs.schoolmessenger.Parent.Communication

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CommunicationBinding

class CommunicationParent : BaseActivity<CommunicationBinding>(), View.OnClickListener,
    VoiceClickListener {

    override fun getViewBinding() = CommunicationBinding.inflate(layoutInflater)

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var allVoiceData = mutableListOf<VoiceData>()
    private var adapter: UnifiedVoiceAdapter? = null
    private var isInitialLoad = true
    private var isFromArchive = false
    private var hasFetchedMore = false
    private var isFilterType: String = Constant.ALL
    private var isCommunicationType = 1

    private var currentSearchQuery: String = ""

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaVoiceMessage.setOnClickListener(this)
        binding.seeMoreLabel.setOnClickListener(this)
        binding.imgFilter.setOnClickListener(this)

        isFromArchive = intent.getBooleanExtra(Constant.fromArchive, false)
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        showShimmer()

        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.Communication)
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails.standard_name + " - " + isChildDetails.section_name

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
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

        binding.rdgCommunication.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.RdbAll -> {

                    if (isCommunicationType == 1) {
                        isFilterType = Constant.ALL
                    } else if (isCommunicationType == 2) {
                        isFilterType = Constant.UNREAD
                    } else if (isCommunicationType == 3) {
                        isFilterType = Constant.READ
                    }

                    applyCombinedFilter()
                }

                R.id.RdbVoice -> {
                    if (isCommunicationType == 1) {
                        isFilterType = Constant.VOICE
                    } else if (isCommunicationType == 2) {
                        isFilterType = Constant.VOICE_UNREAD
                    } else if (isCommunicationType == 3) {
                        isFilterType = Constant.VOICE_READ
                    }
                    applyCombinedFilter()
                }

                R.id.RdbText -> {
                    if (isCommunicationType == 1) {
                        isFilterType = Constant.TEXT
                    } else if (isCommunicationType == 2) {
                        isFilterType = Constant.TEXT_UNREAD
                    } else if (isCommunicationType == 3) {
                        isFilterType = Constant.TEXT_READ
                    }
                    applyCombinedFilter()
                }
            }
        }
        binding.lblAll.setOnClickListener {
            isChangeBackgroundFilter(binding.lblAll)
        }

        binding.lblUnread.setOnClickListener {
            isChangeBackgroundFilter(binding.lblUnread)
        }

        binding.lblRead.setOnClickListener {
            isChangeBackgroundFilter(binding.lblRead)
        }

        fetchInitialData()
    }

    private fun isChangeBackgroundFilter(isSelectedFilter: TextView) {
        binding.lblAll.setBackgroundResource(R.drawable.bg_gray_light_radiuos)
        binding.lblUnread.setBackgroundResource(R.drawable.bg_gray_light_radiuos)
        binding.lblRead.setBackgroundResource(R.drawable.bg_gray_light_radiuos)

        isSelectedFilter.setBackgroundResource(R.drawable.bg_light_green_radious)

        if (isSelectedFilter == binding.lblAll) {
            isCommunicationType = 1
            if (binding.RdbAll.isChecked == true) {
                isFilterType = Constant.ALL
            } else if (binding.RdbText.isChecked == true) {
                isFilterType = Constant.TEXT_ALL
            } else if (binding.RdbVoice.isChecked == true) {
                isFilterType = Constant.VOICE_ALL
            }
        } else if (isSelectedFilter == binding.lblUnread) {
            isCommunicationType = 2
            if (binding.RdbAll.isChecked == true) {
                isFilterType = Constant.UNREAD
            } else if (binding.RdbText.isChecked == true) {
                isFilterType = Constant.TEXT_UNREAD
            } else if (binding.RdbVoice.isChecked == true) {
                isFilterType = Constant.VOICE_UNREAD
            }
        } else if (isSelectedFilter == binding.lblRead) {
            isCommunicationType = 3
            if (binding.RdbAll.isChecked == true) {
                isFilterType = Constant.READ
            } else if (binding.RdbText.isChecked == true) {
                isFilterType = Constant.TEXT_READ
            } else if (binding.RdbVoice.isChecked == true) {
                isFilterType = Constant.VOICE_READ
            }
        }

        applyCombinedFilter()
    }


    private fun applyCombinedFilter() {
        Log.d("isFilterType", isFilterType)

        var filteredList = when (isFilterType) {

            Constant.TEXT, Constant.TEXT_ALL -> allVoiceData.filter { it.type == Constant.TEXT }
            Constant.VOICE, Constant.VOICE_ALL -> allVoiceData.filter { it.type == Constant.VOICE }

            Constant.READ -> allVoiceData.filter { !it.is_unread!! }
            Constant.UNREAD -> allVoiceData.filter { it.is_unread!! }

            Constant.TEXT_READ -> allVoiceData.filter { it.type == Constant.TEXT && !it.is_unread!! }
            Constant.VOICE_READ -> allVoiceData.filter { it.type == Constant.VOICE && !it.is_unread!! }

            Constant.TEXT_UNREAD -> allVoiceData.filter { it.type == Constant.TEXT && it.is_unread!! }
            Constant.VOICE_UNREAD -> allVoiceData.filter { it.type == Constant.VOICE && it.is_unread!! }

            else -> allVoiceData
        }

        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.content?.contains(
                    currentSearchQuery,
                    ignoreCase = true
                ) == true || it.content?.contains(currentSearchQuery, ignoreCase = true) == true
            }
        }

        adapter?.updateList(filteredList)
        checkAndShowNoData(filteredList)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.imgFilter -> {
                binding.rytFilter.visibility = View.VISIBLE
            }

            R.id.rlaTextMessage -> {
                adapter?.updateData()
                adapter?.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage,
                    binding.imgTextMessage,
                    binding.lblTextMessage
                )
            }

            R.id.rlaVoiceMessage -> {
                adapter?.updateData()
                adapter?.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(
                    binding.rlaVoiceMessage,
                    binding.imgVoiceMessage,
                    binding.lblVoiceMessage
                )
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

        newData.let {
            val processedData = it!!.map { item -> item.copy(is_archive = archiveFlag) }
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
                binding.recyclerInitial.isNestedScrollingEnabled = false
                binding.recyclerInitial.adapter = adapter
            } else {
                adapter?.setIsFromArchive(archiveFlag)
                adapter?.updateList(allVoiceData)
            }

            applyCombinedFilter()
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
        binding.recyclerInitial.isNestedScrollingEnabled = false
        binding.recyclerInitial.adapter = shimmerAdapter
    }

    override fun onUpdateArchiveStatus(type: String?, id: String?) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, type)
            addProperty(APIKeyNames.detail_id, id)
        }
        isAccessToken?.let {
            appViewModel?.isUpdateStatusArchive(it, jsonObject, this)
        }
    }

    override fun onUpdateCommunicationStatus(type: String?, id: String?) {
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.type, type)
            addProperty(APIKeyNames.detail_id, id)
        }

        isAccessToken?.let {
            appViewModel?.isUpdateStatusCommunication(it, jsonObject, this)
        }
    }

    override fun onItemClick(data: VoiceData, holder: UnifiedVoiceAdapter.DataViewHolder) {

    }

    private fun isChangeBackRoundCommunicationType(
        isTypeCommunication: RelativeLayout,
        imgTypeCommunication: ImageView,
        lblTypeCommunication: TextView
    ) {
        binding.rlaVoiceMessage.background = null
        binding.rlaTextMessage.background = null
        isTypeCommunication.background =
            ContextCompat.getDrawable(this, R.drawable.bg_gradient_redious_parent)

        binding.lblVoiceMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.lblTextMessage.setTextColor(ContextCompat.getColor(this, R.color.black))
        lblTypeCommunication.setTextColor(ContextCompat.getColor(this, R.color.white))

        binding.imgVoiceMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.mic_icon_black
            )
        )
        binding.imgTextMessage.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.text_icon_black
            )
        )

        when (imgTypeCommunication) {
            binding.imgVoiceMessage -> binding.imgVoiceMessage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.mic_icon
                )
            )

            binding.imgTextMessage -> binding.imgTextMessage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.text_icon
                )
            )
        }
    }

    override fun onBackPressed() {
        if (adapter != null) {
            adapter!!.releaseMediaPlayer()
        }
        super.onBackPressed()
    }
}


