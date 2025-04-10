package com.vs.schoolmessenger.Parent.Communication

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CommunicationBinding

class Communication : BaseActivity<CommunicationBinding>(), View.OnClickListener,
    VoiceClickListener {

    override fun getViewBinding(): CommunicationBinding {
        return CommunicationBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var appViewModel: App? = null
    var isGetCommmunicationlistData: List<VoiceData>? = null
    var mAdapter: VoiceAdapter? = null

    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.rlaTextMessage.setOnClickListener(this)
        binding.rlaVoiceMessage.setOnClickListener(this)

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel!!.init()

        binding.toolbarLayout.lblParentToolBar.text = resources.getText(R.string.Communication)
        binding.toolbarLayout.lblStudentName.text = "Sathish Ganesan"
        binding.toolbarLayout.lblStudentSection.text = "XII - B"
        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails!!.access_token

        appViewModel?.isGetCommmunicationlist?.observe(this) { response ->
            if (response != null && response.status) {
                isGetCommmunicationlistData = response.data
                isLoadGroupData(isGetCommmunicationlistData)
            }
        }

        isGetCommmunicationlist()



    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.rlaTextMessage -> {

                mAdapter!!.updateData()
                mAdapter!!.notifyDataSetChanged()
                isChangeBackRoundCommunicationType(
                    binding.rlaTextMessage,
                    binding.imgTextMessage,
                    binding.lblTextMessage
                )
            }


        }
    }


    private fun isGetCommmunicationlist() {
        appViewModel!!.isGetCommmunicationlist(isAccessToken!!, this)
    }

    private fun isChangeBackRoundCommunicationType(
        isTypeCommunication: RelativeLayout,
        imgTypeCommunication: ImageView,
        lblTypeCommunication: TextView
    ) {
        // Reset backgrounds and colors
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



        // Update icons based on selection
        when (imgTypeCommunication) {
            binding.imgVoiceMessage -> {
                binding.imgVoiceMessage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.mic_icon
                    )
                )
            }

            binding.imgTextMessage -> {
                binding.imgTextMessage.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.text_icon
                    )
                )
            }
        }
    }



    private fun isLoadGroupData(isGetCommmunicationlistData: List<VoiceData>?) {
        mAdapter = VoiceAdapter(
            null,
            this,
            this,
            Constant.isShimmerViewShow,
            this, // <-- LifecycleOwner (your activity must implement LifecycleOwner)
            isAccessToken.toString() // <-- make sure this is defined
        )
        binding.rlaCommunicationData.layoutManager = LinearLayoutManager(this)
        binding.rlaCommunicationData.adapter = mAdapter

        Constant.executeAfterDelay {
            mAdapter = VoiceAdapter(
                ArrayList(isGetCommmunicationlistData ?: emptyList()),
                this@Communication,
                this@Communication,
                Constant.isShimmerViewDisable,
                this@Communication, // <-- LifecycleOwner
                isAccessToken.toString() // <-- pass the token here too
            )

            binding.rlaCommunicationData.adapter = mAdapter
        }
    }

    override fun onUpdateArchiveStatus(type: String?, detailId: String?) {
        val jsonObject = JsonObject().apply {
            addProperty("type", type)
            addProperty("detail_id", detailId)
        }

        if (isAccessToken != null) {
            appViewModel?.isUpdateStatusArchive(
                isAccessToken!!,
                jsonObject,
                this
            )
        }
    }






    override fun onItemClick(data: VoiceData, holder: VoiceAdapter.DataViewHolder) {

    }
}
