package com.vs.schoolmessenger.Parent.Homework

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R

class FullScreenViewerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewerPagerAdapter
    private lateinit var fileList: ArrayList<GetFilePathDetails>
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homework_view_image_document)

        // Retrieve the file list and position passed from the adapter
        val dataJson = intent.getStringExtra("dataList")
        position = intent.getIntExtra("position", 0)

        // Convert the JSON back to the file list using Gson
        val gson = Gson()
        val type = object : TypeToken<ArrayList<GetFilePathDetails>>() {}.type
        fileList = gson.fromJson(dataJson, type)

        // Initialize the ViewPager2
        viewPager = findViewById(R.id.viewPager)
        adapter = ViewerPagerAdapter(fileList, this)
        viewPager.adapter = adapter

        // Set the current position of the ViewPager
        viewPager.setCurrentItem(position, false)
    }
}
