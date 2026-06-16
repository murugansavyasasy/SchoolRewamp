package com.vs.schoolmessenger.Parent.Attachment.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentData
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile
import com.vs.schoolmessenger.Parent.Attachment.OnChildItemClickListener
import com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttachmentFilePathAdapter(

    private var GetFilePathDetailsData: List<AttachmentFile>?,
    private val parentData: AttachmentData,
    private var listener: OnChildItemClickListener,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.homework_img_pdf_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.homework_img_pdf_item, parent, false)
            DataViewHolder(view, context, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else GetFilePathDetailsData?.size ?: 0

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete

            holder.bind(GetFilePathDetailsData!![position], parentData, position, this)

        }
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: OnChildItemClickListener
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val DefaultImage: ImageView = itemView.findViewById(R.id.ImgPDF)
        private val fileItem: CardView = itemView.findViewById(R.id.fileItem)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: AttachmentFile?,
            item: AttachmentData,
            position: Int,
            adapter: AttachmentFilePathAdapter,
        ) {

            Log.d("GetFileDetails", data.toString())
            if (data?.url.isNullOrEmpty()) {
                Log.e("FilePathAdapter", "Invalid URL at position $position")
                return
            }
            when (data?.type?.uppercase()) {
                Constant.IMAGE -> {
                    Glide.with(context)
                        .load(data.url)
                        .placeholder(R.drawable.image_placeholder)
                        .into(DefaultImage)
                    DefaultImage.visibility = View.VISIBLE
                }

                Constant.PDF -> {
                    DefaultImage.setImageResource(R.drawable.hw_pdf_img)
                    openDocumentInWebView(data.url)
                }

                Constant.DOC, Constant.DOCX -> {
                    DefaultImage.setImageResource(R.drawable.microsoft_word_img)
                    openDocumentInWebView(data.url)
                }

                Constant.TXT -> {
                    DefaultImage.setImageResource(R.drawable.txt_file_img)
                    openDocumentInWebView(data.url)
                }

                Constant.PPT, Constant.PPTX -> {
                    DefaultImage.setImageResource(R.drawable.ppt_icon)
                    openDocumentInWebView(data.url)
                }

                Constant.EXCEL -> {
                    DefaultImage.setImageResource(R.drawable.excel_icon)
                    openDocumentInWebView(data.url)
                }
            }

            fileItem.setOnClickListener {
                data?.let {
                    if (item.is_unread) {
                        item.is_unread = false
                        listener.onChildItemClick(it, item)
                    }
                    Constant.commonFileList.isEmpty()
                    Constant.commonFileList = adapter.GetFilePathDetailsData?.map {
                        CommonFileData(type = it.type, path = it.url)
                    }?.toMutableList() ?: mutableListOf()
                    Constant.selectedFileIndex = position
                    val intent = Intent(context, FilesViewActivity::class.java)
                    intent.putExtra(Constant.subjectName, item.title)
                    context.startActivity(intent)
                }
            }

        }

        private fun openDocumentInWebView(urlPath: String) {

        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)

            init {
                shimmerLayout.startShimmer()
            }
        }
    }
}