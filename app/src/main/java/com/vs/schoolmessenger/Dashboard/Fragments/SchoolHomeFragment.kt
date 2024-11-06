package com.vs.schoolmessenger.Dashboard.Fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.SchoolHomeFragmentBinding


class SchoolHomeFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: SchoolHomeFragmentBinding // Automatically generated binding class

    lateinit var isMenuAdapter: SchoolMenuAdapter
    private lateinit var items: List<GridItem>
    private var isMenuItems: MutableList<GridItem> = mutableListOf()
    private var isSearchVisible = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = SchoolHomeFragmentBinding.inflate(layoutInflater)
        binding.imgNotification.setOnClickListener(this)
        binding.imgSearchClick.setOnClickListener(this)
        // Sample data
        items = listOf(
            GridItem(R.drawable.splash_icon1, "Text Message"),
            GridItem(R.drawable.splash_icon2, "Voice Message"),
            GridItem(R.drawable.splash_icon3, "Images"),
            GridItem(R.drawable.splash_icon4, "Circulars"),
            GridItem(R.drawable.splash_icon5, "Notice Board"),
            GridItem(R.drawable.splash_icon6, "Assignment"),
            GridItem(R.drawable.splash_icon7, "Communication"),
            GridItem(R.drawable.splash_icon8, "Biometric"),
            GridItem(R.drawable.splash_icon1, "Exams"),
            GridItem(R.drawable.splash_icon2, "Homework"),
            GridItem(R.drawable.splash_icon3, "Attendance"),
            GridItem(R.drawable.splash_icon4, "Time table"),
            GridItem(R.drawable.splash_icon5, "Events")
        )

        /**
        // Data for the pie chart (percentage, color)
        val chartData = listOf(
        Pair(20f, ContextCompat.getColor(requireActivity(), R.color.yellow)),       // Girls 20%
        Pair(
        45f,
        ContextCompat.getColor(requireActivity(), R.color.light_green_bg1)
        ), // Boys 50%
        Pair(
        35f,
        ContextCompat.getColor(requireActivity(), R.color.light_orange0)
        )    // Staff 30%
        )
        // Set the data to the chart
        binding.customPieChart.setData(chartData)
        binding.lblTotalStrength.text = "10,000"
        binding.lblGirlsCount.text = "3,500 (35%)"
        binding.lblBoysCount.text = "4,500 (45%)"
        binding.lblTeachersCount.text = "2,000 (20%)"
         */


        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
        })

        val adapter = SchoolMenuAdapter(null, Constant.isShimmerViewShow)
        binding.recyclerViewMenu.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerViewMenu.adapter = adapter

        // Simulate loading data with a delay (e.g., fetch from server or database)
        binding.recyclerViewMenu.postDelayed({
            // Once data is loaded, stop shimmer and pass the actual data
            val adapter = SchoolMenuAdapter(items, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.recyclerViewMenu.layoutManager = GridLayoutManager(requireActivity(), 3)
            binding.recyclerViewMenu.adapter = adapter
        }, 500) // Simulate 2 seconds loading time

        return binding.root
    }

    private fun filter(text: String) {
        isMenuItems.clear()
        if (text.isEmpty()) {
            isMenuItems.addAll(items)  // If search is empty, show all items
        } else {
            for (item in items) {
                if (item.title.toLowerCase().contains(text.toLowerCase())) {
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
}