package com.vs.schoolmessenger.Dashboard.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserValidationData
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.Model.AdItem
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.Dashboard.School.ContactDetails
import com.vs.schoolmessenger.Dashboard.School.DashboardData
import com.vs.schoolmessenger.Dashboard.School.MenuDetail
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SchoolHomeFragmentBinding
import java.util.Locale


class SchoolHomeFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: SchoolHomeFragmentBinding // Automatically generated binding class
    lateinit var isMenuAdapter: SchoolMenuAdapter

    //    private lateinit var items: ArrayList<GridItem>
//    private var isMenuItems: MutableList<GridItem> = mutableListOf()
    private var isSearchVisible = false
    private var appViewModel: App? = null


        private lateinit var aditems: List<AdItem>
    var userDetails: UserDetails? = null
    var isDashBoardData: List<DashboardData>? = null
    var isContactDetails: ContactDetails? = null
    var isMenuDetails: List<MenuDetail>? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SchoolHomeFragmentBinding.inflate(layoutInflater)
        binding.imgNotification.setOnClickListener(this)
        binding.imgSearchClick.setOnClickListener(this)

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel!!.init()

        userDetails = SharedPreference.getUserDetails(requireActivity())
        isDashBoardData()
        binding.lblSchoolAddress.text = userDetails!!.staff_details[0].school_address
        Glide.with(requireActivity())
            .load(userDetails!!.staff_details[0].school_logo)
            .into(binding.imgSchoolLogo)

        if (userDetails!!.staff_details.size > 1) {
            binding.lblSchoolName.text = userDetails!!.role_name
        } else {
            binding.lblSchoolName.text = userDetails!!.staff_details[0].school_name
        }

        binding.lblViewDetails.paintFlags =
            binding.lblViewDetails.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        binding.lblGif.playAnimation()
        binding.lblGif.setAnimation(R.raw.mathematics)


        appViewModel!!.isDashBoardData?.observe(requireActivity()) { response ->
            if (response != null) {
                val status = response.status
                val message = response.message
                if (status) {
                    val isDashboardResponse = response.data
                    isDashBoardData = isDashboardResponse
                    isContactDetails = isDashBoardData!![0].contactDetails
                    isMenuDetails=isDashBoardData!!.get(0).menuDetails
                }
            }
        }

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //   filter(s.toString())
            }
        })
        return binding.root
    }

//    @SuppressLint("NotifyDataSetChanged")
//    private fun filter(text: String) {
//        isMenuItems.clear()
//        if (text.isEmpty()) {
//            isMenuItems.addAll(items)  // If search is empty, show all items
//        } else {
//            for (item in items) {
//                if (item.title.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
//                    isMenuItems.add(item)  // Add the matching GridItem to filteredList
//                }
//            }
//        }
//        isMenuAdapter.notifyDataSetChanged()
//    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgNotification -> {
                val intent = Intent(requireActivity(), Notification::class.java)
                startActivity(intent)
            }

            R.id.imgSearchClick -> {

                if (isSearchVisible) {
                    isSearchVisible = false
                    binding.rytSearch.visibility = View.GONE
                } else {
                    isSearchVisible = true
                    binding.rytSearch.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun isDashBoardData() {
        val isToken = SharedPreference.getToken(requireActivity())
        Log.d("isToken", isToken!!)
        appViewModel!!.isDashBoardData(
            isToken!!,
            "staff",
            requireActivity()
        )
    }


    override fun onResume() {
        super.onResume()
        val adapter = SchoolMenuAdapter(requireActivity(), null, Constant.isShimmerViewShow)
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)

        // Adjust span count for special layout
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    2 -> 3 // TYPE_AD: Span across all 3 columns
                    else -> 1 // Default: 1 span per item
                }
            }
        }

        binding.recyclerViewMenus.layoutManager = gridLayoutManager
        binding.recyclerViewMenus.adapter = adapter

        Constant.executeAfterDelay {
            val isAdapter =
                SchoolMenuAdapter(requireActivity(),
                    isMenuDetails, Constant.isShimmerViewDisable)
            // Adjust span count again for the updated adapter
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (isAdapter.getItemViewType(position)) {
                        2 -> 3 // TYPE_AD: Span across all 3 columns
                        else -> 1 // Default: 1 span per item
                    }
                }
            }
            binding.recyclerViewMenus.layoutManager = gridLayoutManager
            binding.recyclerViewMenus.adapter = isAdapter
        }
        Log.d("Status", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
        Log.d("Status", "onPause")
    }
}