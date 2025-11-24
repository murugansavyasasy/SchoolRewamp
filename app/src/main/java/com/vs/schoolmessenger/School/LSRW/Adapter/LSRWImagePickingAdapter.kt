package com.vs.schoolmessenger.School.LSRW.Adapter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import java.io.File
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import kotlin.apply
import kotlin.text.toLong

class LSRWImagePickingAdapter(
    private val context: Context,
    private val items: MutableList<FileItem>,
    private val listener: OnImageClickListener
) : RecyclerView.Adapter<LSRWImagePickingAdapter.FileViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingItemIndex: Int? = null
    private val handler = Handler(Looper.getMainLooper())

    private val playIcon = R.drawable.video_play
    private val pauseIcon = R.drawable.pause_icon

    private val defaultStartEndMargin: Int = context.resources.getDimensionPixelSize(R.dimen.twenty)
    private val defaultTopMargin: Int = context.resources.getDimensionPixelSize(R.dimen.ten)

    inner class FileViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgPicking)
        val del: ImageView = v.findViewById(R.id.imgDelete)
        val delete: ImageView = v.findViewById(R.id.imgaudiodelete)
        val audioBubble: RelativeLayout = v.findViewById(R.id.audioBubble)

        val imgVideoPlay: ImageView = v.findViewById(R.id.imgVideoPlay)
        val seekBar: SeekBar = v.findViewById(R.id.tvDuration)
        val lblCurrentDuration: TextView = v.findViewById(R.id.lblCurrentDuration)
        val lblTime: TextView = v.findViewById(R.id.lblTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_picking_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, pos: Int) {
        val item = items[pos]

        val isAudio = item.type.toString() == Constant.AUDIO
        val isVideo = item.type.toString() == Constant.VIDEO
        val isMedia = isAudio || isVideo

        holder.img.visibility = if (isAudio) View.GONE else View.VISIBLE
        holder.audioBubble.visibility = if (isAudio) View.VISIBLE else View.GONE
        holder.imgVideoPlay.visibility = if (isMedia) View.VISIBLE else View.GONE

        // Layout margins
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (isAudio) {
            layoutParams.marginStart = 0
            layoutParams.marginEnd = 0
            layoutParams.topMargin = defaultTopMargin
        } else {
            layoutParams.marginStart = defaultStartEndMargin
            layoutParams.marginEnd = defaultStartEndMargin
            layoutParams.topMargin = defaultTopMargin
        }
        holder.itemView.layoutParams = layoutParams

        // AUDIO UI BEHAVIOR
        if (isAudio) {
            holder.lblTime.visibility = View.VISIBLE

            val durMs = getAudioDurationMs(item.path)
            holder.lblCurrentDuration.text = formatTime(durMs)
            holder.seekBar.max = durMs.toInt()

            val mp = mediaPlayer
            if (currentPlayingItemIndex == pos && mp != null) {

                val isPlayingNow = mp.isPlaying

                holder.seekBar.progress = mp.currentPosition
                holder.lblTime.text = formatTime(mp.currentPosition.toLong())
                holder.imgVideoPlay.setImageResource(if (isPlayingNow) pauseIcon else playIcon)

                holder.seekBar.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) mp.seekTo(progress)
                    }
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })

                if (isPlayingNow) startProgressUpdate(holder, mp)

            } else {
                // RESET UI
                holder.seekBar.progress = 0
                holder.lblTime.text = "00:00"
                holder.imgVideoPlay.setImageResource(playIcon)
                holder.seekBar.setOnSeekBarChangeListener(null)
            }

            holder.imgVideoPlay.setOnClickListener {
                playPauseAudio(item, holder, pos)
            }
        }

        // IMAGE / VIDEO LOADER
        if (!isAudio) {
            val fileUri = when {
                item.path.startsWith("content://") -> Uri.parse(item.path)
                item.path.startsWith("http") -> item.path
                else -> Uri.fromFile(File(item.path))
            }

            val placeholderRes = when (item.type) {
                FileType.PDF -> R.drawable.pdf_icon
                FileType.DOC, FileType.DOCX -> R.drawable.doc_icon
                FileType.PPT -> R.drawable.ppt_icon
                FileType.EXCEL -> R.drawable.excel_icon
                FileType.TXT -> R.drawable.txt_icon
                FileType.IMAGE -> R.drawable.image_placeholder
                FileType.VIDEO -> R.drawable.black
                else -> R.drawable.address_icon
            }

            Glide.with(context)
                .load(fileUri)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .apply(RequestOptions().dontTransform())
                .into(holder.img)
        }

        // DELETE BEHAVIOR
        if (pos == 0) {
            holder.del.visibility = View.GONE
            holder.delete.visibility = View.GONE
        } else if (isAudio) {

            holder.del.visibility = View.GONE
            holder.delete.visibility = View.VISIBLE

            holder.delete.setOnClickListener {
                handleDelete(pos)
            }

        } else {

            holder.delete.visibility = View.GONE
            holder.del.visibility = View.VISIBLE

            holder.del.setOnClickListener {
                handleDelete(pos)
            }
        }

        holder.itemView.setOnClickListener {

            if (pos == 0) {
                listener.onImageClick(pos)
                return@setOnClickListener
            }

            if (item.path.contains("amazonaws.", ignoreCase = true)) {

                Constant.commonFileList = Constant.selectedFiles.map {
                    CommonFileData(it.type.toString(), it.path)
                }.toMutableList()

                Constant.selectedFileIndex = pos - 1

                context.startActivity(Intent(context, FilesViewActivity::class.java))
                return@setOnClickListener
            }

            if (item.type.toString() == Constant.IMAGE || item.type.toString() == Constant.VIDEO) {

                val realFiles = Constant.selectedFiles.drop(1) // Exclude placeholder at index 0
                val filtered = realFiles.filter {
                    it.type.toString() in listOf(Constant.IMAGE, Constant.VIDEO)
                }

                Constant.commonFileList = filtered.map {
                    CommonFileData(it.type.toString(), it.path)
                }.toMutableList()

                Constant.selectedFileIndex =
                    filtered.indexOfFirst { it.path == item.path }.coerceAtLeast(0)

                context.startActivity(Intent(context, FilesViewActivity::class.java))
                return@setOnClickListener
            }

            // Case 3: All other docs → open using external app
            try {
                val uri = if (item.path.startsWith("content://")) {
                    Uri.parse(item.path)
                } else {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        File(item.path)
                    )
                }

                val mimeType = getMimeTypeFromUri(uri)

                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val apps = context.packageManager.queryIntentActivities(
                    openIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )

                if (apps.isNotEmpty()) {
                    context.startActivity(Intent.createChooser(openIntent, "Open with"))
                } else {
                    Toast.makeText(context, "No app found to open this file.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open file", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun handleDelete(pos: Int) {

        // If deleting the currently playing audio → stop it
        if (currentPlayingItemIndex == pos) {
            stopAudioIfPlaying()
        }

        // Maintain correct index shift
        if (currentPlayingItemIndex != null && currentPlayingItemIndex!! > pos) {
            currentPlayingItemIndex = currentPlayingItemIndex!! - 1
        }

        Constant.Remaining = Constant.Remaining + 1
        items.removeAt(pos)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, items.size)
    }

    override fun onViewRecycled(holder: FileViewHolder) {
        super.onViewRecycled(holder)

        val pos = holder.adapterPosition
        if (pos != RecyclerView.NO_POSITION && currentPlayingItemIndex == pos) {
            stopAudioIfPlaying()
        }
    }

    private fun playPauseAudio(item: FileItem, holder: FileViewHolder, pos: Int) {

        // IF SAME AUDIO → PLAY/PAUSE TOGGLE
        if (currentPlayingItemIndex == pos) {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    holder.imgVideoPlay.setImageResource(playIcon)
                    handler.removeCallbacksAndMessages(null)
                } else {
                    mp.start()
                    holder.imgVideoPlay.setImageResource(pauseIcon)
                    startProgressUpdate(holder, mp)
                }
            }
            return
        }

        // NEW AUDIO → STOP PREVIOUS
        stopAudioIfPlaying()

        try {
            val uri = Uri.parse(item.path)
            mediaPlayer = MediaPlayer.create(context, uri)
                ?: throw Exception("Audio Unsupported")

            val mp = mediaPlayer!!
            currentPlayingItemIndex = pos

            holder.imgVideoPlay.setImageResource(pauseIcon)
            holder.seekBar.max = mp.duration
            holder.lblCurrentDuration.text = formatTime(mp.duration.toLong())
            holder.seekBar.progress = 0
            holder.lblTime.text = "00:00"

            holder.seekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) mp.seekTo(progress)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

            mp.setOnCompletionListener {
                holder.seekBar.progress = 0
                holder.lblTime.text = "00:00"
                holder.imgVideoPlay.setImageResource(playIcon)
                stopAudioIfPlaying()
                notifyItemChanged(pos)
            }

            mp.start()
            startProgressUpdate(holder, mp)

        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            currentPlayingItemIndex = null
        }
    }

    private fun startProgressUpdate(holder: FileViewHolder, mp: MediaPlayer) {
        val updateTask = object : Runnable {
            override fun run() {
                try {
                    holder.seekBar.progress = mp.currentPosition
                    holder.lblTime.text = formatTime(mp.currentPosition.toLong())

                    if (mp.isPlaying)
                        handler.postDelayed(this, 200)

                } catch (_: Exception) {}
            }
        }
        handler.post(updateTask)
    }

    private fun stopAudioIfPlaying() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) mp.stop()
            } catch (_: Exception) {}

            mp.release()
        }
        mediaPlayer = null
        currentPlayingItemIndex = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun getAudioDurationMs(filePath: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()

            if (filePath.startsWith("content://")) {
                val uri = Uri.parse(filePath)
                val fd = context.contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor
                retriever.setDataSource(fd)
            } else {
                retriever.setDataSource(filePath)
            }

            val ms = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLong() ?: 0L

            retriever.release()
            ms
        } catch (e: Exception) {
            0L
        }
    }

    private fun formatTime(ms: Long): String {
        val s = ms / 1000
        val m = s / 60
        val sec = s % 60
        return String.format("%d:%02d", m, sec)
    }

    fun releaseMediaPlayer() {
        stopAudioIfPlaying()
    }

    private fun getMimeTypeFromUri(uri: Uri): String {
        val contentResolver = context.contentResolver
        return contentResolver.getType(uri) ?: "*/*"
    }

    override fun getItemCount() = items.size
}