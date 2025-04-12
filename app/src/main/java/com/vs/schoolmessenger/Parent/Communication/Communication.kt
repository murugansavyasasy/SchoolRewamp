package com.vs.schoolmessenger.Parent.Communication

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


    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaVoiceMessage.setOnClickListener(this)
        binding.seeMoreLabel.setOnClickListener(this)

        isFromArchive = intent.getBooleanExtra("fromArchive", false)

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        // Initial shimmer and data loading
        showShimmer()

        appViewModel?.isGetCommmunicationlist?.observe(this) { response ->
            Log.d("CommDebug", "SeeMore Observer triggered: $response")
            if (response?.status == true) {
                appendData(response.data)
                binding.seeMoreLabel.visibility = View.VISIBLE
            }
        }



        appViewModel?.isGetCommmunicationlistload?.observe(this) { response ->
            if (response?.status == true) {
                appendData(response.data)
            }
        }

        fetchInitialData()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.rlaTextMessage -> {
                adapter?.updateData()
                adapter?.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage,
                    binding.imgTextMessage,
                    binding.lblTextMessage
                )
            }
            R.id.seeMoreLabel -> fetchMoreData()
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

    private fun appendData(newData: List<VoiceData>?) {
        newData?.let {
            if (isInitialLoad) {
                allVoiceData.clear()
            }
            allVoiceData.addAll(it)

            if (adapter == null) {
                adapter = UnifiedVoiceAdapter(
                    allVoiceData as ArrayList<VoiceData>?,
                    this,
                    this,
                    Constant.isShimmerViewDisable,
                    this,
                    isAccessToken.orEmpty(),
                    isFromArchive
                )

                binding.recyclerInitial.layoutManager = LinearLayoutManager(this)
                binding.recyclerInitial.adapter = adapter
            } else {
                adapter?.updateList(allVoiceData)
            }
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

