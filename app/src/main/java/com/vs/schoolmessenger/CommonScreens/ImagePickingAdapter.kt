package com.vs.schoolmessenger.CommonScreens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.FileProvider
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

class ImagePickingAdapter(
    private val context: Context,
    private val items: MutableList<FileItem>,
    private val listener: OnImageClickListener
) : RecyclerView.Adapter<ImagePickingAdapter.FileViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingItem: FileItem? = null
    private val handler = Handler(Looper.getMainLooper())

    private val playIcon = R.drawable.video_play
    private val pauseIcon = R.drawable.pause_icon

    // NEW: Cache dimension values for efficiency (avoids repeated resource lookups)
    private val defaultStartEndMargin: Int = context.resources.getDimensionPixelSize(R.dimen.twenty)
    private val defaultTopMargin: Int = context.resources.getDimensionPixelSize(R.dimen.ten)

    inner class FileViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgPicking)
        val del: ImageView = v.findViewById(R.id.imgDelete)
        val delete: ImageView = v.findViewById(R.id.imgaudiodelete)
        val audioBubble: RelativeLayout = v.findViewById(R.id.audioBubble)

        // Audio UI
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

        // NEW: Dynamically set root margins for full width on audio items
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (isAudio) {
            // Full width: 0 start/end margins, keep top margin
            layoutParams.marginStart = 0
            layoutParams.marginEnd = 0
            layoutParams.topMargin = defaultTopMargin
        } else {
            // Original constrained width: restore margins
            layoutParams.marginStart = defaultStartEndMargin
            layoutParams.marginEnd = defaultStartEndMargin
            layoutParams.topMargin = defaultTopMargin
        }
        holder.itemView.layoutParams = layoutParams

        if (isAudio) {
            holder.lblTime.visibility = View.VISIBLE

            val durMs = getAudioDurationMs(item.path)
            holder.lblCurrentDuration.text = formatTime(durMs)
            holder.seekBar.max = durMs.toInt()

            val mp = mediaPlayer
            if (item == currentPlayingItem && mp != null) {
                // Current playing (or paused)
                holder.seekBar.progress = mp.currentPosition
                holder.lblTime.text = formatTime(mp.currentPosition.toLong())
                val isPlayingNow = mp.isPlaying
                holder.imgVideoPlay.setImageResource(if (isPlayingNow) pauseIcon else playIcon)

                // FIXED: Set SeekBar listener ONLY for the current item
                holder.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) mp.seekTo(progress)
                    }

                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })

                if (isPlayingNow) {
                    startProgressUpdate(holder, mp)
                }
            } else {
                // Not current: reset and clear listener
                holder.seekBar.progress = 0
                holder.lblTime.text = "00:00"
                holder.imgVideoPlay.setImageResource(playIcon)
                holder.seekBar.setOnSeekBarChangeListener(null)
            }

            holder.imgVideoPlay.setOnClickListener {
                playPauseAudio(item, holder)
            }
        }

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

        if (pos == 0) {
            holder.del.visibility = View.GONE
            holder.delete.visibility = View.GONE
        } else {
            if (isAudio) {
                holder.del.visibility = View.GONE
                holder.delete.visibility = View.VISIBLE
                holder.delete.setOnClickListener {
                    if (item == currentPlayingItem) {
                        stopAudioIfPlaying()
                    }
                    items.removeAt(pos)
                    notifyItemRemoved(pos)
                    notifyItemRangeChanged(pos, items.size)
                }
            } else {
                holder.delete.visibility = View.GONE
                holder.del.visibility = View.VISIBLE
                holder.del.setOnClickListener {
                    if (item == currentPlayingItem) {
                        stopAudioIfPlaying()
                    }
                    items.removeAt(pos)
                    notifyItemRemoved(pos)
                    notifyItemRangeChanged(pos, items.size)
                }
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
            } else {
                when (item.type.toString()) {
                    Constant.IMAGE, Constant.VIDEO, Constant.AUDIO -> {
                        val filtered = Constant.selectedFiles.filter {
                            it.type.toString() in listOf(
                                Constant.IMAGE,
                                Constant.VIDEO,
                                Constant.AUDIO
                            )
                        }
                        Constant.commonFileList = filtered.map {
                            CommonFileData(it.type.toString(), it.path)
                        }.toMutableList()

                        Constant.selectedFileIndex =
                            filtered.indexOfFirst { it.path == item.path }.coerceAtLeast(0)

                        context.startActivity(Intent(context, FilesViewActivity::class.java))
                    }

                    else -> {
                        val uri = if (item.path.startsWith("content://")) {
                            Uri.parse(item.path)
                        } else {
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                File(item.path)
                            )
                        }

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, context.contentResolver.getType(uri))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        try {
                            context.startActivity(Intent.createChooser(intent, "Open with"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "No app found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // FIXED: Use adapterPosition and check against current item reference
    override fun onViewRecycled(holder: FileViewHolder) {
        super.onViewRecycled(holder)
        val pos = holder.adapterPosition
        if (pos != RecyclerView.NO_POSITION) {
            val item = items.getOrNull(pos)
            if (item == currentPlayingItem) {
                stopAudioIfPlaying()
            }
        }
    }

    private fun startProgressUpdate(holder: FileViewHolder, mp: MediaPlayer) {
        val updateTask = object : Runnable {
            override fun run() {
                try {
                    holder.seekBar.progress = mp.currentPosition
                    holder.lblTime.text = formatTime(mp.currentPosition.toLong())
                    if (mp.isPlaying) {
                        handler.postDelayed(this, 200)
                    }
                } catch (_: Exception) {}
            }
        }
        handler.post(updateTask)
    }

    private fun playPauseAudio(item: FileItem, holder: FileViewHolder) {
        val mp = mediaPlayer
        if (item == currentPlayingItem) {
            mp?.let {
                if (it.isPlaying) {
                    // Pause
                    it.pause()
                    holder.imgVideoPlay.setImageResource(playIcon)
                    handler.removeCallbacksAndMessages(null)
                } else {
                    // Resume
                    it.start()
                    holder.imgVideoPlay.setImageResource(pauseIcon)
                    startProgressUpdate(holder, it)
                }
            }
            return
        }

        // Stop previous
        stopAudioIfPlaying()

        try {
            val uri = Uri.parse(item.path)
            mediaPlayer = MediaPlayer.create(context, uri) ?: run {
                Toast.makeText(context, "Cannot create player", Toast.LENGTH_SHORT).show()
                return
            }

            mediaPlayer?.let { newMp ->
                currentPlayingItem = item
                holder.imgVideoPlay.setImageResource(pauseIcon)
                holder.seekBar.max = newMp.duration
                holder.lblCurrentDuration.text = formatTime(newMp.duration.toLong())
                holder.lblTime.text = "00:00"
                holder.seekBar.progress = 0

                // FIXED: Set SeekBar listener for this new player
                holder.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) newMp.seekTo(progress)
                    }

                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {}
                })

                newMp.setOnCompletionListener {
                    holder.seekBar.progress = 0
                    holder.lblTime.text = "00:00"
                    holder.imgVideoPlay.setImageResource(playIcon)
                    mediaPlayer?.release()
                    mediaPlayer = null
                    currentPlayingItem = null
                    handler.removeCallbacksAndMessages(null)
                    // FIXED: Rebind the item to reset UI (safe now with item ref)
                    val pos = items.indexOf(item)
                    if (pos != -1) {
                        notifyItemChanged(pos)
                    }
                }

                newMp.start()
                startProgressUpdate(holder, newMp)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot play audio: ${e.message}", Toast.LENGTH_SHORT).show()
            currentPlayingItem = null
        }
    }

    private fun stopAudioIfPlaying() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.stop()
            }
            mp.release()
            mediaPlayer = null
            currentPlayingItem = null
            handler.removeCallbacksAndMessages(null)
        }
    }

    private fun getAudioDurationMs(filePath: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val ms = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLong() ?: 0L
            retriever.release()
            ms
        } catch (e: Exception) {
            0L
        }
    }

    private fun getAudioDuration(filePath: String): String {
        return formatTime(getAudioDurationMs(filePath))
    }

    private fun formatTime(ms: Long): String {
        val sec = ms / 1000
        val min = sec / 60
        val remaining = sec % 60
        return String.format("%d:%02d", min, remaining)
    }

    fun releaseMediaPlayer() {
        stopAudioIfPlaying()
    }

    override fun getItemCount() = items.size
}