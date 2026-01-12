package com.vs.schoolmessenger.Parent.Communication

import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
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
    var isSeeMoreClick = true

    var isFilterClick = false
    private var currentSearchQuery: String = ""

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    var userDetails: UserDetails? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaVoiceMessage.setOnClickListener(this)
        binding.seeMoreLabel.setOnClickListener(this)
        binding.imgFilter.setOnClickListener(this)

        userDetails = SharedPreference.getUserDetails(this)
        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)

        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)
            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )
            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }



        isFromArchive = intent.getBooleanExtra(Constant.fromArchive, false)
        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        showShimmer()

        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails.standard_name + " - " + isChildDetails.section_name

        binding.root.post {
            val finalName =
                Constant.isSelectedMenuName?.takeIf { it.isNotEmpty() } ?: menu_name ?: ""
            Log.d("lblHeaderTitle", "Setting headerview text: $finalName")
            binding.lblHeaderTitle.text = finalName
            binding.lblHeaderTitle.visibility = View.VISIBLE
        }

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.linearlayout1.visibility == View.VISIBLE) {
                binding.linearlayout1.visibility = View.GONE
                binding.rytFilter.visibility = View.GONE
                binding.txtSearchMenu.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)

            } else {
                binding.linearlayout1.visibility = View.VISIBLE
                binding.rytFilter.visibility = View.GONE
                binding.txtSearchMenu.text.clear()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.txtSearchMenu.windowToken, 0)

            }
        }

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter!!.pauseMediaPlayer()
                currentSearchQuery = s.toString()
                applyCombinedFilter()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        appViewModel?.isGetCommmunicationlist?.observe(this) { response ->
            if (response != null) {
                if (response?.status == true) {
                    if (response.data.isNotEmpty()) {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                        appendData(response.data, archiveFlag = true)
                        if (fromNotification) {
                            scrollToMessageId(headerId)
                        }
                    } else {
                        hasFetchedMore = true
                        if (allVoiceData.isNotEmpty()) {
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                        } else {
                            binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                        }
                        checkAndShowNoData(
                            filteredList = allVoiceData,
                            message = response.message
                        )
                    }
                } else {
                    if (allVoiceData.isNotEmpty()) {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    } else {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    }
                    checkAndShowNoData(
                        message = response?.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
            }
        }


        appViewModel?.isGetCommmunicationlistload?.observe(this) { response ->
            if (response != null) {
                if (response?.status == true) {
                    appendData(response.data, archiveFlag = false)
                    scrollToMessageId(headerId)
                } else {
                    if (allVoiceData.isNotEmpty()) {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                    } else {
                        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    }
                    checkAndShowNoData(message = response?.message)
                }
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


    private fun scrollToMessageId(headerId: String?) {
        if (msg_id == -1) return

        allVoiceData?.let { list ->
            val index = list.indexOfFirst { it.header_id == headerId }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index in ongoing")
                binding.recyclerInitial.post {
                    binding.recyclerInitial.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.recyclerInitial, index)
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
            if (binding.RdbAll.isChecked) {
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
            filteredList = filteredList.filter { item ->
                val contentMatch =
                    item.content.orEmpty().contains(currentSearchQuery, ignoreCase = true)
                val titleMatch =
                    item.title.orEmpty().contains(currentSearchQuery, ignoreCase = true)
                val typeMatch = item.type.orEmpty().contains(currentSearchQuery, ignoreCase = true)
                val timeMatch = item.time.orEmpty().contains(currentSearchQuery, ignoreCase = true)

                val dateToCheck = try {
                    Constant.convertDateTimeFormat(item.date.orEmpty())
                } catch (e: Exception) {
                    item.date.orEmpty()
                }
                val dateMatch = dateToCheck.contains(currentSearchQuery, ignoreCase = true)

                contentMatch || titleMatch || dateMatch || typeMatch || timeMatch
            }
        }

        adapter?.updateList(filteredList, isSeeMoreClick)
        checkAndShowNoData(filteredList)
    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.imgFilter -> {
                isFilterClick = true
                if (binding.rytFilter.isVisible) {
                    binding.rytFilter.visibility = View.GONE
                } else {
                    binding.rytFilter.visibility = View.VISIBLE
                }
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
                    isSeeMoreClick = false
                    binding.seeMoreLabel.visibility = View.GONE
                    binding.txtSearchMenu.text.clear()
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
        Log.d("isSeeMoreClick", isSeeMoreClick.toString())
        newData.let {
            val processedData = it!!.map { item -> item.copy(is_archive = archiveFlag) }
            allVoiceData.addAll(processedData)
            binding.recyclerInitial.visibility = View.VISIBLE
            if (adapter == null) {
                adapter = UnifiedVoiceAdapter(
                    allVoiceData as ArrayList<VoiceData>,
                    this,
                    this,
                    Constant.isShimmerViewDisable,
                    this,
                    isAccessToken.orEmpty(),
                    archiveFlag,
                    isSeeMoreClick
                )
                binding.recyclerInitial.layoutManager = LinearLayoutManager(this)
                binding.recyclerInitial.isNestedScrollingEnabled = false
                binding.recyclerInitial.adapter = adapter
            } else {
                adapter?.setIsFromArchive(archiveFlag)
                adapter?.updateList(allVoiceData, isSeeMoreClick)
            }

            if (allVoiceData.isNotEmpty()) {
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
            } else {
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            }
            applyCombinedFilter()
        }
        checkAndShowNoData()
    }

    private fun checkAndShowNoData(filteredList: List<VoiceData>? = null, message: String? = null) {
        val listToCheck = filteredList ?: allVoiceData
        val isEmpty = listToCheck.isEmpty()
        binding.txtNoData.visibility = if (isEmpty) View.VISIBLE else View.GONE

        if (!isFilterClick) {
            binding.seeMoreLabel.visibility =
                if (isEmpty && isSeeMoreClick) View.VISIBLE else View.GONE
        }
        binding.nomessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerInitial.visibility = if (isEmpty) View.GONE else View.VISIBLE

        if (isEmpty) {
            binding.txtNoData.text = message ?: getString(R.string.no_list_found)
        }
    }


    private fun showShimmer() {
        val shimmerAdapter = UnifiedVoiceAdapter(
            null,
            this,
            this,
            Constant.isShimmerViewShow,
            this,
            isAccessToken.orEmpty(),
            isFromArchive,
            isSeeMoreClick
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

    override fun onSeeMoreClick(
        data: VoiceData,
        holder: UnifiedVoiceAdapter.DataViewHolder
    ) {
        isSeeMoreClick = false
        if (!hasFetchedMore) {
            hasFetchedMore = true
            fetchMoreData()
        }
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

    override fun onPause() {
        super.onPause()
        adapter?.pauseMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.onDestroyMediaPlayer()
    }

    override fun onBackPressed() {
        if (adapter != null) {
            adapter!!.releaseMediaPlayer()
        }
        super.onBackPressed()
        val intent = Intent(this, ParentDashboard::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}