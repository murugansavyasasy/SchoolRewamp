package com.vs.schoolmessenger.Parent.Homework

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R

class HomeworkImgPDFAdapter(
    private var GetFilePathDetailsData: ArrayList<GetFilePathDetails>?,
    private var context: Context,
    private var isLoading: Boolean):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_view_small_list, parent, false)
            DataViewHolder.ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.homework_img_pdf_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else GetFilePathDetailsData?.size ?: 0    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(GetFilePathDetailsData!![position],position, this)

        }

    }
    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {


        private val handler = Handler(Looper.getMainLooper())
        private val DefaultImage: ImageView = itemView.findViewById(R.id.ImgPDF)
        var mHomeworkImgPDFAdapter: HomeworkImgPDFAdapter? = null




        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: GetFilePathDetails?,
            position: Int,
            adapter: HomeworkImgPDFAdapter,

            ) {

            Log.d("GetFileDetails", data.toString())
            when (data?.type?.uppercase()) {
                "IMAGE" -> {

                    // Load the real image using Glide
                    Glide.with(context)
                        .load(data.path) // Your image URL
                        .placeholder(R.drawable.image_placeholder) // optional
//                        .error(R.drawable.) // if fail
                        .into(itemView.findViewById(R.id.ImgPDF)) // replace with your ImageView ID

                }
                        "PDF" -> DefaultImage.setBackgroundResource(R.drawable.hw_pdf_img)
                        "DOC", "DOCX" -> DefaultImage.setBackgroundResource(R.drawable.microsoft_word_img)
                        "TXT" -> DefaultImage.setBackgroundResource(R.drawable.txt_file_img)
                        else -> DefaultImage.setBackgroundResource(R.drawable.book)
                    }
            DefaultImage.setOnClickListener {
                val intent = Intent(context, FullScreenViewerActivity::class.java)
                val gson = Gson()
                val dataJson = gson.toJson(adapter.GetFilePathDetailsData) // send whole list
                intent.putExtra("dataList", dataJson)
                intent.putExtra("position", position)
                context.startActivity(intent)
            }







//            mHomeworkImgPDFAdapter =
//                HomeworkImgPDFAdapter(null, context, Constant.isShimmerViewShow)
//            homeworkImgPdf.layoutManager = LinearLayoutManager(context)
//            homeworkImgPdf.adapter = mHomeworkImgPDFAdapter
//
//            Constant.executeAfterDelay {
//                // Once data is loaded, stop shimmer and pass the actual data
//                mHomeworkImgPDFAdapter =
//                    HomeworkImgPDFAdapter(
//                        data.,
//                        context,
//                        Constant.isShimmerViewDisable,
//                    )
//                // Set GridLayoutManager (2 columns in this case)
//                homeworkImgPdf.adapter = mHomeworkImgPDFAdapter
//
//
//            }
        }


        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)

            init {
                shimmerLayout.startShimmer() // Start shimmer effect
            }
        }
    }
}



//working code
//package com.vs.schoolmessenger.Parent.Homework
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.RelativeLayout
//import android.widget.TextView
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.facebook.shimmer.ShimmerFrameLayout
//import com.vs.schoolmessenger.Parent.Homework.HomeWorkItemAdapter.DataViewHolder
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Utils.Constant
//
//class HomeworkImgPDFAdapter(
//    private var GetFilePathDetailsData: ArrayList<GetFilePathDetails>?,
//    private var context: Context,
//    private var isLoading: Boolean):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val view =
//                LayoutInflater.from(parent.context)
//                    .inflate(R.layout.shimmer_view_small_list, parent, false)
//            DataViewHolder.ShimmerViewHolder(view)
//        } else {
//            val view =
//                LayoutInflater.from(parent.context)
//                    .inflate(R.layout.homework_img_pdf_item, parent, false)
//            DataViewHolder(view, context) // Pass context to DataViewHolder
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 20 // Show shimmer items while loading
//        else GetFilePathDetailsData?.size ?: 0    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is DataViewHolder) {
//            // Bind actual data when loading is complete
//            holder.bind(GetFilePathDetailsData!![position],position, this)
//
//        }
//
//    }
//    class DataViewHolder(itemView: View, private val context: Context) :
//        RecyclerView.ViewHolder(itemView) {
//
//
//        private val handler = Handler(Looper.getMainLooper())
//        private val DefaultImage: ImageView = itemView.findViewById(R.id.ImgPDF)
//        var mHomeworkImgPDFAdapter: HomeworkImgPDFAdapter? = null
//
//
//
//
//        @SuppressLint("ClickableViewAccessibility")
//        fun bind(
//            data: GetFilePathDetails?,
//            position: Int,
//            adapter: HomeworkImgPDFAdapter,
//
//            ) {
//
//            Log.d("GetFileDetails", data.toString())
//            when (data?.type?.uppercase()) {
//                "IMAGE" -> {
//
//                    // Load the real image using Glide
//                    Glide.with(context)
//                        .load(data.path) // Your image URL
//                        .placeholder(R.drawable.image_placeholder) // optional
////                        .error(R.drawable.) // if fail
//                        .into(itemView.findViewById(R.id.ImgPDF)) // replace with your ImageView ID
//
//                    DefaultImage.setOnClickListener {
//                        val intent = Intent(Intent.ACTION_VIEW)
//                        intent.data = Uri.parse(data.path)
//                        context.startActivity(intent)
//                    }
//                }
//
//                "PDF", "DOC", "DOCX", "TXT" -> {
//
//                    // Show a default icon depending on type
//                    when (data.type.uppercase()) {
//                        "PDF" -> DefaultImage.setBackgroundResource(R.drawable.hw_pdf_img)
//                        "DOC", "DOCX" -> DefaultImage.setBackgroundResource(R.drawable.microsoft_word_img)
//                        "TXT" -> DefaultImage.setBackgroundResource(R.drawable.txt_file_img)
//                        else -> DefaultImage.setBackgroundResource(R.drawable.book)
//                    }
//
//                    DefaultImage.setOnClickListener {
//                        val intent = Intent(Intent.ACTION_VIEW)
//                        intent.data = Uri.parse(data.path)
//                        context.startActivity(intent)
//                    }
//                }
//
//                else -> {
//                    DefaultImage.setBackgroundResource(R.drawable.text_icon)
//                }
//
//
//            }
//
//
//
//
////            mHomeworkImgPDFAdapter =
////                HomeworkImgPDFAdapter(null, context, Constant.isShimmerViewShow)
////            homeworkImgPdf.layoutManager = LinearLayoutManager(context)
////            homeworkImgPdf.adapter = mHomeworkImgPDFAdapter
////
////            Constant.executeAfterDelay {
////                // Once data is loaded, stop shimmer and pass the actual data
////                mHomeworkImgPDFAdapter =
////                    HomeworkImgPDFAdapter(
////                        data.,
////                        context,
////                        Constant.isShimmerViewDisable,
////                    )
////                // Set GridLayoutManager (2 columns in this case)
////                homeworkImgPdf.adapter = mHomeworkImgPDFAdapter
////
////
////            }
//        }
//
//
//        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            private val shimmerLayout: ShimmerFrameLayout =
//                itemView.findViewById(R.id.shimmer_view_container)
//
//            init {
//                shimmerLayout.startShimmer() // Start shimmer effect
//            }
//        }
//    }
//}
//
//
