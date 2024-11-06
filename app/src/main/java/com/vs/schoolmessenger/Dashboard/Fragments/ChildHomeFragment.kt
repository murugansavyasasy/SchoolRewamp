package com.vs.schoolmessenger.Dashboard.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.Dashboard.Parent.ChildMenuAdapter
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ChildHomeFragmentBinding

class ChildHomeFragment : Fragment() {
    private lateinit var binding: ChildHomeFragmentBinding // Automatically generated binding class

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChildHomeFragmentBinding.inflate(layoutInflater)

        // Sample data
        val items = listOf(
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
            GridItem(R.drawable.splash_icon5, "Events"),
            // Add more items
        )

        val adapter = ChildMenuAdapter(items)
        // Set GridLayoutManager (2 columns in this case)
        binding.recyclerViewMenu.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerViewMenu.adapter = adapter


        return binding.root
    }
}