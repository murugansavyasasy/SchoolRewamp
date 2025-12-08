package com.vs.schoolmessenger.School.LSRW.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.SubmittedstudentListViewBinding

class StudentSubmittedListRemarkAdapter(
    private var context: Context,
    private var filePathDetails: List<GetFilePathDetails>,
    var isSubjectName: String,
    var selectedSchoolMenu: Int,
    var isParentAssignment: Boolean,
) : RecyclerView.Adapter<StudentSubmittedListRemarkAdapter.DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding =
            SubmittedstudentListViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(
            filePathDetails[position],
            position,
            context,
            filePathDetails,
            isSubjectName,
            selectedSchoolMenu,
            isParentAssignment,
        )
    }

    override fun getItemCount(): Int = filePathDetails.size

    class DataViewHolder(private val binding: SubmittedstudentListViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: GetFilePathDetails,
            position: Int,
            context: Context,
            fullList: List<GetFilePathDetails>,
            isSubjectName: String,
            selectedSchoolMenu: Int,
            isParentAssignment: Boolean
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
                                dataSource: com.bumptech.glide.load.DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                binding.progressBar.visibility = View.GONE
                                return false
                            }
                        })
                        .into(binding.imgView)
                }

                Constant.VIDEO -> {
                    binding.imgView.setBackgroundColor(Color.BLACK)
                    binding.imgView.setImageResource(R.drawable.videoplay_svgformatstyle)
                    binding.progressBar.visibility = View.GONE
                }

                Constant.PDF -> {
                    binding.imgView.setImageResource(R.drawable.hw_pdf_img)
                    binding.progressBar.visibility = View.GONE
                }

                Constant.DOC, Constant.DOCX -> {
                    binding.imgView.setImageResource(R.drawable.microsoft_word_img)
                    binding.progressBar.visibility = View.GONE
                }

                Constant.TXT -> {
                    binding.imgView.setImageResource(R.drawable.txt_file_img)
                    binding.progressBar.visibility = View.GONE
                }

                Constant.PPT, Constant.PPTX -> {
                    binding.imgView.setImageResource(R.drawable.ppt_icon)
                    binding.progressBar.visibility = View.GONE
                }

                Constant.EXCEL -> {
                    binding.imgView.setImageResource(R.drawable.excel_icon)
                    binding.progressBar.visibility = View.GONE
                }

                else -> {
                    binding.imgView.setImageResource(R.drawable.excel_icon)
                    binding.progressBar.visibility = View.GONE
                }
            }


            binding.relativelayoutHeader.setOnClickListener {
                openFileViewer(fullList, position, context, isSubjectName)
            }


        }

        private fun openFileViewer(
            fullList: List<GetFilePathDetails>,
            position: Int,
            context: Context,
            isSubjectName: String
        ) {
            val commonList = fullList.map {
                CommonFileData(
                    type = it.type,
                    path = it.url
                )
            }.toMutableList()

            Constant.commonFileList = commonList
            Constant.selectedFileIndex = position

            val intent = Intent(context, FilesViewActivity::class.java)
            intent.putExtra(Constant.subjectName, isSubjectName)
            context.startActivity(intent)
        }
    }
}

