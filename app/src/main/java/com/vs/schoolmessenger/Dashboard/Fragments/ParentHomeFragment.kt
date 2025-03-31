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
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Combination.PrioritySelection
import com.vs.schoolmessenger.Dashboard.Model.AdItem
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.Dashboard.Parent.ChildMenuAdapter
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.Parent.Attendance.AttendanceReport
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ParentHomeFragmentBinding
import java.util.Locale

class ParentHomeFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: ParentHomeFragmentBinding // Automatically generated binding class
    lateinit var isMenuAdapter: ChildMenuAdapter
    private lateinit var items: ArrayList<GridItem>
    private lateinit var aditems: List<AdItem>
    private var isMenuItems: MutableList<GridItem> = mutableListOf()
    private var isSearchVisible = false
    var userDetails: UserDetails? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ParentHomeFragmentBinding.inflate(layoutInflater)
        binding.imgNotification.setOnClickListener(this)
        binding.imgSearchClick.setOnClickListener(this)
        binding.lblChangeRoll.setOnClickListener(this)
        userDetails = SharedPreference.getUserDetails(requireActivity())

        binding.lblStudentName.text = "Hello, " + userDetails!!.child_details.get(0).name
        binding.lblSchoolName.text = userDetails!!.child_details.get(0).school_name
        binding.lblSchoolAddress.text = userDetails!!.child_details.get(0).student_address

        if (userDetails!!.is_staff) {
            binding.lblChangeRoll.visibility = View.VISIBLE
        } else {
            binding.lblChangeRoll.visibility = View.GONE
        }

        // Sample data
        items = arrayListOf(
            GridItem(
                R.drawable.communication_icon_dashboard,
                "Communication"
            ),
            GridItem(R.drawable.image_pdf_icon, "Image/Pdf"),
            GridItem(R.drawable.video_dashboard_icon, "Video"),
            GridItem(R.drawable.home_work_icon, "Homework"),
            GridItem(R.drawable.exam_icon, "Schedule Exam/Test"),
            GridItem(R.drawable.event_icon, "Event / Holidays"),
            GridItem(R.drawable.timetable_icon, "Class Timetable"),
        )

        binding.lblViewDetails.paintFlags =
            binding.lblViewDetails.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        binding.lblViewDetails.setOnClickListener {
            this.startActivity(
                Intent(
                    requireActivity(),
                    AttendanceReport::class.java
                )
            )
        }

        aditems = listOf(
            AdItem(R.drawable.ad_1),
            AdItem(R.drawable.ad_2),
            AdItem(R.drawable.sample_ad),
            AdItem(R.drawable.ad_3),
        )

        binding.lblGif.playAnimation()
        binding.lblGif.setAnimation(R.raw.mathematics)

        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
        })

        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun filter(text: String) {
        isMenuItems.clear()
        if (text.isEmpty()) {
            isMenuItems.addAll(items)  // If search is empty, show all items
        } else {
            for (item in items) {
                if (item.title.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                    isMenuItems.add(item)  // Add the matching GridItem to filteredList
                }
            }
        }
        isMenuAdapter.notifyDataSetChanged()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgNotification -> {
                val intent = Intent(requireActivity(), Notification::class.java)
                startActivity(intent)
            }

            R.id.lblChangeRoll -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
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

    override fun onResume() {
        super.onResume()

        val adapter = ChildMenuAdapter(requireActivity(), null, null, Constant.isShimmerViewShow)
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
                ChildMenuAdapter(requireActivity(), items, aditems, Constant.isShimmerViewDisable)
            Log.d("aditems", aditems.size.toString())
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
    }

    override fun onPause() {
        super.onPause()
        Log.d("Status", "onPause")

    }
}