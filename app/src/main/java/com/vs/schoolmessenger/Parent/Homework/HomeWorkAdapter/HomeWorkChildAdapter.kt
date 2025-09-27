package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.Parent.LSRW.AudioAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_LSRW
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_NEEDS
import com.vs.schoolmessenger.databinding.FileviewItemBinding
import com.vs.schoolmessenger.databinding.LsrwBinding
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

class HomeWorkChildAdapter(
    private var context: Context,
    private var filePathDetails: List<GetFilePathDetails>,
    var isSubjectName: String,
    var selectedSchoolMenu: Int,
    var isParentAssignment: Boolean
) : RecyclerView.Adapter<HomeWorkChildAdapter.DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val binding =
            FileviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            isParentAssignment
        )
    }

    override fun getItemCount(): Int = filePathDetails.size




    class DataViewHolder(private val binding: FileviewItemBinding) :
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
            binding.progressBar.visibility = View.VISIBLE
            binding.imgView.visibility = View.VISIBLE
            binding.imgView.setBackgroundColor(Color.TRANSPARENT)

            Log.d("ParentAssigmentValue",isParentAssignment.toString())
            Log.d("selectedSchoolMenuvalue",selectedSchoolMenu.toString())
            if (selectedSchoolMenu == M_ASSIGNMENT) {
                Log.d("selectedschoolmenu adaptervalue", selectedSchoolMenu.toString())
                binding.imgView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.relativelayoutHeader.visibility = View.GONE
                binding.childrelativeLayout.visibility = View.VISIBLE
                when (item.type.uppercase()) {
                    Constant.IMAGE -> {
                        binding.imgFileType.setText(context.getString(R.string.img))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.VIDEO -> {
                        binding.imgFileType.setText(context.getString(R.string.vid))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PDF -> {
                        binding.imgFileType.setText(context.getString(R.string.pdf))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.DOC, Constant.DOCX -> {
                        binding.imgFileType.setText(context.getString(R.string.docx))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.TXT -> {
                        binding.imgFileType.setText(context.getString(R.string.txt))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PPT, Constant.PPTX -> {
                        binding.imgFileType.setText(context.getString(R.string.ppt))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.EXCEL -> {
                        binding.imgFileType.setText(context.getString(R.string.exc))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    else -> {
                        binding.imgFileType.setText(context.getString(R.string.exc))
                        binding.progressBar.visibility = View.GONE
                    }
                }
            } else if (selectedSchoolMenu == M_LSRW && isParentAssignment == true) {
                Log.d("selectedschoolmenu adaptervalue", selectedSchoolMenu.toString())
                binding.imgView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.relativelayoutHeader.visibility = View.GONE
                binding.childrelativeLayout.visibility = View.VISIBLE
                when (item.type.uppercase()) {
                    Constant.IMAGE -> {
                        binding.imgFileType.setText(context.getString(R.string.img))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }
                    Constant.VIDEO -> {
                        binding.imgFileType.setText(context.getString(R.string.vid))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PDF -> {
                        binding.imgFileType.setText(context.getString(R.string.pdf))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.DOC, Constant.DOCX -> {
                        binding.imgFileType.setText(context.getString(R.string.docx))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.TXT -> {
                        binding.imgFileType.setText(context.getString(R.string.txt))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PPT, Constant.PPTX -> {
                        binding.imgFileType.setText(context.getString(R.string.pptx))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.EXCEL -> {
                        binding.imgFileType.setText(context.getString(R.string.exc))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    else -> {
                        binding.imgFileType.setText(context.getString(R.string.exc))
                        binding.progressBar.visibility = View.GONE
                    }
                }
            } else if (selectedSchoolMenu == M_LSRW && isParentAssignment == false) {
                Log.d("selectedschoolmenu adaptervalue", selectedSchoolMenu.toString())
                binding.imgView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.relativelayoutHeader.visibility = View.GONE
                binding.childrelativeLayout.visibility = View.VISIBLE
                when (item.type.uppercase()) {
                    Constant.IMAGE -> {
                        binding.imgFileType.setText(context.getString(R.string.img))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.VIDEO -> {
                        binding.imgFileType.setText(context.getString(R.string.vid))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PDF -> {
                        binding.imgFileType.setText(context.getString(R.string.pdf))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.DOC, Constant.DOCX -> {
                        binding.imgFileType.setText(context.getString(R.string.docx))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.TXT -> {
                        binding.imgFileType.setText(context.getString(R.string.txt))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PPT, Constant.PPTX -> {
                        binding.imgFileType.setText(context.getString(R.string.pptx))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.EXCEL -> {
                        binding.imgFileType.setText(context.getString(R.string.exc))
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    else -> {
                        binding.imgFileType.setText(context.getString(R.string.exc))
                        binding.progressBar.visibility = View.GONE
                    }
                }
            } else {
                Log.d("else part condition", selectedSchoolMenu.toString())

                binding.relativelayoutHeader.visibility = View.VISIBLE
                binding.imgView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.childrelativeLayout.visibility = View.GONE
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
                        binding.imgView.setImageResource(R.drawable.video_play)
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

            }

            binding.relativelayoutHeader.setOnClickListener {
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

            binding.childrelativeLayout.setOnClickListener {
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


        private fun getFileSize(url: String, callback: (String) -> Unit) {
            Thread {
                try {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = Constant.HEAD
                    val fileSize = connection.contentLengthLong
                    connection.disconnect()

                    val sizeText = if (fileSize > 0) {
                        val sizeInKb = fileSize / 1024
                        if (sizeInKb > 1024) {
                            String.format("%.2f MB", sizeInKb / 1024.0)
                        } else {
                            "$sizeInKb KB"
                        }
                    } else {
                        "Unknown size"
                    }


                    Handler(Looper.getMainLooper()).post {
                        callback(sizeText)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Handler(Looper.getMainLooper()).post {
                        callback("Unknown size")
                    }
                }
            }.start()
        }



    }
}
