package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.vs.schoolmessenger.Parent.Homework.FullScreenViewerActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import javax.security.auth.Subject
import kotlin.math.log

class HomeworkImgPDFAdapter(
    private var SubjectName:String ,
    private var GetFilePathDetailsData: List<GetFilePathDetails>?,
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
        else GetFilePathDetailsData?.size ?: 0

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(SubjectName,GetFilePathDetailsData!![position],position, this)
        }
    }
    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val DefaultImage: ImageView = itemView.findViewById(R.id.ImgPDF)
        private val ImgOrDocumentType:ImageView=itemView.findViewById(R.id.imageOrDocumentType)
        private val WebViewThumbnail:WebView=itemView.findViewById(R.id.WVThumbnaildocument)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            SubjectName: String,
            data: GetFilePathDetails?,
            position: Int,
            adapter: HomeworkImgPDFAdapter, ) {

            Log.d("GetFileDetails", data.toString())
            when (data?.type?.uppercase()) {
                Constant.IMAGE -> {
                    // Load the real image using Glide
                    Glide.with(context)
                        .load(data.path) // Your image URL
                        .placeholder(R.drawable.image_placeholder) // optional
//                        .error(R.drawable.) // if fail
                        .into(itemView.findViewById(R.id.ImgPDF)) // replace with your ImageView ID

                    ImgOrDocumentType.setBackgroundResource(R.drawable.default_image_icon)
                    WebViewThumbnail.visibility=View.GONE

                }
                Constant.PDF -> {ImgOrDocumentType.setBackgroundResource(R.drawable.hw_pdf_img)
                    openDocumentInWebView(data.path)

                }

                Constant.DOC, Constant.DOCX ->{
                    ImgOrDocumentType.setBackgroundResource(R.drawable.microsoft_word_img)
                    openDocumentInWebView(data.path)

                }
                Constant.TXT -> {
                    ImgOrDocumentType.setBackgroundResource(R.drawable.txt_file_img)
                    openDocumentInWebView(data.path)

                }
            }
            DefaultImage.setOnClickListener {
                val selectedItem = adapter.GetFilePathDetailsData!![position]
                val context = itemView.context



                if (selectedItem.type.equals(Constant.IMAGE, ignoreCase = true)) {
                    // Filter only image items
                    val imageList = adapter.GetFilePathDetailsData!!.filter {
                        it.type.equals(Constant.IMAGE, ignoreCase = true)
                    }

                    val selectedImageIndex = imageList.indexOfFirst { it.path == selectedItem.path }
                    Log.d("HomeworkPDFAdapter,Imaged Clicked!", "HomeworkPDFAdapter,Image Clicked!")

                    val intent = Intent(context, FullScreenViewerActivity::class.java)
                    val dataJson = Gson().toJson(imageList)
                    Log.d("JsonImageList",dataJson.toString())
//                    intent.putExtra("SelectedSubjectName", item?.subject_name)
                    intent.putExtra("SelectedSubjectName", SubjectName)
                    Log.d("SelectedSubjectName",SubjectName)
                    intent.putExtra(Constant.data, dataJson)
                    intent.putExtra(Constant.position, selectedImageIndex)
                    context.startActivity(intent)
                }
            }


            WebViewThumbnail.setOnClickListener{
                val selectedItem = adapter.GetFilePathDetailsData!![position]
                val context = itemView.context
                Log.d("HomeworkPDFAdapter,Document Clicked!","HomeworkPDFAdapter,Document Clicked!")
                    // Open document viewer (PDF, DOCX, etc.)
                    val intent = Intent(context, FullScreenViewerActivity::class.java)
                intent.putExtra("SelectedSubjectName", SubjectName)
                Log.d("SelectedSubjectName",SubjectName)
//                intent.putExtra("SelectedSubjectName", item?.subject_name)
                intent.putExtra(Constant.SelectedDocumentPath, selectedItem.path)
                Log.d("SelectedDocumentpath",selectedItem.path)
                intent.putExtra(Constant.SelectedDocumentType, selectedItem.type)
                    context.startActivity(intent)

            }



//            DefaultImage.setOnClickListener {
//                val selectedItem = adapter.GetFilePathDetailsData!![position]
//                val context = itemView.context
//
//                if (selectedItem.type.equals("IMAGE", ignoreCase = true)) {
//                    // Filter only image items
//                    val imageList = adapter.GetFilePathDetailsData!!.filter {
//                        it.type.equals("IMAGE", ignoreCase = true)
//                    }
//
//                    val selectedImageIndex = imageList.indexOfFirst { it.path == selectedItem.path }
//                    Log.d("HomeworkPDFAdapter,Imaged Clicked!","HomeworkPDFAdapter,Image Clicked!")
//
//                    val intent = Intent(context, FullScreenViewerActivity::class.java)
//                    val dataJson = Gson().toJson(imageList)
//                    intent.putExtra("data", dataJson)
//                    intent.putExtra("position", selectedImageIndex)
//                    context.startActivity(intent)
//
//                } else {
//                    Log.d("HomeworkPDFAdapter,Document Clicked!","HomeworkPDFAdapter,Document Clicked!")
//                    // Open document viewer (PDF, DOCX, etc.)
//                    val intent = Intent(context, FullScreenViewerActivity::class.java)
//                    intent.putExtra("SelectedDocumentPath", selectedItem.path)
//                    intent.putExtra("SelectedDocumentType", selectedItem.type)
//                    context.startActivity(intent)
//                }
//            }
        }
        private fun openDocumentInWebView(urlpath: String) {
            DefaultImage.visibility=View.GONE
            WebViewThumbnail.isClickable = true
            WebViewThumbnail.isFocusable = true
            WebViewThumbnail.isFocusableInTouchMode = true
            WebViewThumbnail.visibility=View.VISIBLE
            val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$urlpath"
            WebViewThumbnail.loadUrl(googleDocsUrl);
            Log.d("After Loading FilePath", googleDocsUrl)

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

