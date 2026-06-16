package com.vs.schoolmessenger.Parent.LSRW

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.SubmittedstudentListViewBinding

class MySubmissionAdapter(
    private val context: Context,
    private val filePathDetails: List<GetFilePathDetails>,
    private val subjectName: String,
    private val selectedSchoolMenu: Int,
    private val isParentAssignment: Boolean
) : RecyclerView.Adapter<MySubmissionAdapter.DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding = SubmittedstudentListViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(filePathDetails[position], position, context, filePathDetails, subjectName)
    }

    override fun getItemCount(): Int = filePathDetails.size

    class DataViewHolder(private val binding: SubmittedstudentListViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: GetFilePathDetails,
            position: Int,
            context: Context,
            fullList: List<GetFilePathDetails>,
            subjectName: String
        ) {
            binding.relativelayoutHeader.visibility = View.VISIBLE
            binding.imgView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE


            when (item.type.uppercase()) {
                Constant.IMAGE -> {
                    Glide.with(binding.root.context)
                        .load(item.url)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                binding.progressBar.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable?>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                binding.progressBar.visibility = View.GONE
                                return false
                            }
                        })
                        .into(binding.imgView)
                }

                Constant.VIDEO -> binding.imgView.setImageResource(R.drawable.video_play)
                Constant.PDF -> binding.imgView.setImageResource(R.drawable.hw_pdf_img)
                Constant.DOC, Constant.DOCX -> binding.imgView.setImageResource(R.drawable.microsoft_word_img)
                Constant.TXT -> binding.imgView.setImageResource(R.drawable.txt_file_img)
                Constant.PPT, Constant.PPTX -> binding.imgView.setImageResource(R.drawable.ppt_icon)
                Constant.EXCEL -> binding.imgView.setImageResource(R.drawable.excel_icon)
                else -> binding.imgView.setImageResource(R.drawable.questionmark)
            }

            binding.progressBar.visibility = View.GONE

            // Open File Viewer
            binding.relativelayoutHeader.setOnClickListener {
                val commonList = fullList.map {
                    CommonFileData(type = it.type, path = it.url)
                }.toMutableList()

                Constant.commonFileList = commonList
                Constant.selectedFileIndex = position

                val intent = Intent(context, FilesViewActivity::class.java)
                intent.putExtra(Constant.subjectName, subjectName)
                context.startActivity(intent)
            }
        }
    }
}
