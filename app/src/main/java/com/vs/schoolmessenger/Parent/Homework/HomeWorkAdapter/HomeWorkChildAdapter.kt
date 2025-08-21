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
    var selectedSchoolMenu: Int
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
            selectedSchoolMenu
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
            selectedSchoolMenu: Int
        ) {
            binding.relativelayoutHeader.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
            binding.imgView.visibility = View.VISIBLE
            binding.imgView.setBackgroundColor(Color.TRANSPARENT)
            if (selectedSchoolMenu == M_ASSIGNMENT) {
                Log.d("selectedschoolmenu adaptervalue", selectedSchoolMenu.toString())
                binding.imgView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.relativelayoutHeader.visibility = View.GONE
                binding.childrelativeLayout.visibility = View.VISIBLE
                when (item.type.uppercase()) {
                    Constant.IMAGE -> {
                        binding.imgFileType.setImageResource(R.drawable.imagesvgformar)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.VIDEO -> {
                        binding.imgFileType.setImageResource(R.drawable.videosvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PDF -> {
                        binding.imgFileType.setImageResource(R.drawable.pdfsvgformatter)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.DOC, Constant.DOCX -> {
                        binding.imgFileType.setImageResource(R.drawable.docxsvgformatter)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.TXT -> {
                        binding.imgFileType.setImageResource(R.drawable.txtsvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PPT, Constant.PPTX -> {
                        binding.imgFileType.setImageResource(R.drawable.pptsvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.EXCEL -> {
                        binding.imgFileType.setImageResource(R.drawable.excelsvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    else -> {
                        binding.imgView.setImageResource(R.drawable.excel_icon)
                        binding.progressBar.visibility = View.GONE
                    }
                }
            } else if (selectedSchoolMenu == M_LSRW) {
                Log.d("selectedschoolmenu adaptervalue", selectedSchoolMenu.toString())
                binding.imgView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.relativelayoutHeader.visibility = View.GONE
                binding.childrelativeLayout.visibility = View.VISIBLE
                binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
                when (item.type.uppercase()) {
                    Constant.IMAGE -> {
                        binding.imgFileType.setImageResource(R.drawable.imagesvgformar)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.M4A -> {
                        binding.rlaSeekBarAndTitle.visibility = View.VISIBLE
                        val audioUrl = item.url
                        binding.lblStartDuration.text = "00:00"
                        binding.lblEndDuration.text = "00:00"
                        binding.lblTime.text = "00:00"
                        getAudioDuration(audioUrl) { duration ->
                            binding.lblEndDuration.text = duration
                        }
                        binding.imgVoicePlay.setOnClickListener {
                            toggleAudioPlayer(audioUrl, binding)
                        }
                    }

                    Constant.VIDEO -> {
                        binding.imgFileType.setImageResource(R.drawable.videosvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PDF -> {
                        binding.imgFileType.setImageResource(R.drawable.pdfsvgformatter)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.DOC, Constant.DOCX -> {
                        binding.imgFileType.setImageResource(R.drawable.docxsvgformatter)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.TXT -> {
                        binding.imgFileType.setImageResource(R.drawable.txtsvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PPT, Constant.PPTX -> {
                        binding.imgFileType.setImageResource(R.drawable.pptsvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.EXCEL -> {
                        binding.imgFileType.setImageResource(R.drawable.excelsvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    else -> {
                        binding.imgView.setImageResource(R.drawable.excel_icon)
                        binding.progressBar.visibility = View.GONE
                    }
                }
            } else if (selectedSchoolMenu == M_SCHOOL_NEEDS) {
                Log.d("selectedschoolmenu adaptervalue", selectedSchoolMenu.toString())
                binding.imgView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.relativelayoutHeader.visibility = View.GONE
                binding.childrelativeLayout.visibility = View.VISIBLE
                when (item.type.uppercase()) {
                    Constant.IMAGE -> {
                        binding.imgFileType.setImageResource(R.drawable.imagesvgformar)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.VIDEO -> {
                        binding.imgFileType.setImageResource(R.drawable.videosvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PDF -> {
                        binding.imgFileType.setImageResource(R.drawable.pdfsvgformatter)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.DOC, Constant.DOCX -> {
                        binding.imgFileType.setImageResource(R.drawable.docxsvgformatter)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.TXT -> {
                        binding.imgFileType.setImageResource(R.drawable.txtsvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.PPT, Constant.PPTX -> {
                        binding.imgFileType.setImageResource(R.drawable.pptsvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    Constant.EXCEL -> {
                        binding.imgFileType.setImageResource(R.drawable.excelsvgformat)
                        val fileName = item.url.substringAfterLast("/")
                        binding.txtFileName.text = fileName
                        getFileSize(item.url) { size ->
                            binding.txtFileSize.text = size
                        }
                        binding.progressBar.visibility = View.GONE
                    }

                    else -> {
                        binding.imgView.setImageResource(R.drawable.excel_icon)
                        binding.progressBar.visibility = View.GONE
                    }
                }
            } else {
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



            binding.root.setOnClickListener {
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
                    connection.requestMethod = "HEAD"
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




        private fun getAudioDuration(url: String, callback: (String) -> Unit) {
            Thread {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(url, HashMap())
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val durationMs = durationStr?.toLong() ?: 0L
                    val total = String.format(
                        "%02d:%02d",
                        (durationMs / 1000) / 60,
                        (durationMs / 1000) % 60
                    )
                    retriever.release()
                    Handler(Looper.getMainLooper()).post {
                        callback(total)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }



        private var mediaPlayer: MediaPlayer? = null
        private var isPlaying = false




        private fun toggleAudioPlayer(url: String, binding: FileviewItemBinding) {
            if (isPlaying) {
                mediaPlayer?.pause()
                binding.imgVoicePlay.setImageResource(R.drawable.play_icon_voice)
                isPlaying = false
            } else {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(url)
                        prepareAsync()
                        setOnPreparedListener {
                            start()
                            binding.imgVoicePlay.setImageResource(R.drawable.pause_icon)
                            this@DataViewHolder.isPlaying = true
                            updateSeekbar(binding)
                        }
                        setOnCompletionListener {
                            binding.imgVoicePlay.setImageResource(R.drawable.play_icon_voice)
                            this@DataViewHolder.isPlaying = false
                        }
                    }
                } else {
                    mediaPlayer?.start()
                    binding.imgVoicePlay.setImageResource(R.drawable.pause_icon)
                    isPlaying = true
                }
            }
        }



        private fun updateSeekbar(binding: FileviewItemBinding) {
            val handler = Handler(Looper.getMainLooper())
            handler.post(object : Runnable {
                override fun run() {
                    mediaPlayer?.let {
                        val currentPos = it.currentPosition / 1000
                        binding.lblStartDuration.text =
                            String.format("%02d:%02d", currentPos / 60, currentPos % 60)


                        val normalizedPower = max(1f, (1f + 160) / 160)
                        binding.waveformSeekBar.updateWithLevel(normalizedPower)


                        if (isPlaying) handler.postDelayed(this, 500)
                    }
                }
            })
        }






    }
}
