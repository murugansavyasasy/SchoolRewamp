package com.vs.schoolmessenger.CommonScreens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import java.io.File

class ImagePickingAdapter(
    private val context: Context,
    private val items: MutableList<FileItem>,
    private val listener: OnImageClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_MEDIA = 1
        private const val TYPE_AUDIO = 2

        private var mediaPlayer: MediaPlayer? = null
        private var currentPlayingPath: String? = null
        private val handler = Handler(Looper.getMainLooper())

        // Path-based caches (survive add / remove / reorder)
        private val audioProgress = mutableMapOf<String, Int>()
        private val durationCache = mutableMapOf<String, Long>()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.post {
            val glm = GridLayoutManager(recyclerView.context, 3)
            glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (getItemViewType(position)) {
                        TYPE_AUDIO -> 3
                        else -> 1
                    }
                }
            }
            recyclerView.layoutManager = glm
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return TYPE_ADD
        return when (items[position].type) {
            FileType.AUDIO -> TYPE_AUDIO
            else -> TYPE_MEDIA
        }
    }

    inner class MediaViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgPicking)
        val del: ImageView = v.findViewById(R.id.imgDelete)
        val imgVideo: ImageView = v.findViewById(R.id.imgVideo)
    }

    inner class AudioViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imgPlayAudio: ImageView = v.findViewById(R.id.imgPlayAudio)
        val seekBarAudio: SeekBar = v.findViewById(R.id.seekBarAudio)
        val lblCurrentTime: TextView = v.findViewById(R.id.lblCurrentTime)
        val lblDuration: TextView = v.findViewById(R.id.lblDuration)
        val imgAudioDelete: ImageView = v.findViewById(R.id.imgAudioDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_AUDIO -> {
                val v = LayoutInflater.from(context)
                    .inflate(R.layout.audio_picking_item, parent, false)
                AudioViewHolder(v)
            }
            else -> {
                val v = LayoutInflater.from(context)
                    .inflate(R.layout.image_picking_item, parent, false)
                MediaViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        when (holder) {
            is MediaViewHolder -> bindMedia(holder, pos)
            is AudioViewHolder -> bindAudio(holder, pos)
        }
    }

    private fun bindMedia(holder: MediaViewHolder, pos: Int) {
        val item = items[pos]

        if (pos == 0) {
            holder.img.setImageResource(R.drawable.add_image)
            holder.imgVideo.visibility = View.GONE
            holder.del.visibility = View.GONE
            holder.itemView.setOnClickListener { listener.onImageClick(0) }
            return
        }

        val filePath = item.path
        val fileUri = when {
            filePath.startsWith("content://") || filePath.startsWith("file://") -> Uri.parse(filePath)
            filePath.startsWith("http://") || filePath.startsWith("https://") -> filePath
            else -> File(filePath)
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
            .apply(RequestOptions().dontTransform())
            .error(placeholderRes)
            .into(holder.img)

        holder.del.visibility = View.VISIBLE
        holder.imgVideo.visibility =
            if (item.type.toString() == Constant.VIDEO) View.VISIBLE else View.GONE

        holder.del.setOnClickListener {
            Constant.Remaining += 1
            items.removeAt(pos)
            notifyItemRemoved(pos)
            notifyItemRangeChanged(pos, items.size)
        }

        holder.itemView.setOnClickListener {
            if (item.path.contains("amazonaws.")) {
                Constant.commonFileList = Constant.selectedFiles.map {
                    CommonFileData(type = it.type.toString(), path = it.path)
                }.toMutableList()
                Constant.selectedFileIndex = pos - 1
                val intent = Intent(context, FilesViewActivity::class.java)
                intent.putExtra(Constant.subjectName, "Your Files")
                context.startActivity(intent)
                return@setOnClickListener
            }

            if (item.type.toString() == Constant.IMAGE || item.type.toString() == Constant.VIDEO) {
                val filtered = Constant.selectedFiles.filter {
                    it.type.toString() == Constant.IMAGE || it.type.toString() == Constant.VIDEO
                }
                Constant.commonFileList = filtered.map {
                    CommonFileData(type = it.type.toString(), path = it.path)
                }.toMutableList()
                val idx = filtered.indexOfFirst { it.path == item.path }.coerceAtLeast(0)
                Constant.selectedFileIndex = idx - 1
                val intent = Intent(context, FilesViewActivity::class.java)
                intent.putExtra(Constant.subjectName, "Your Files")
                context.startActivity(intent)
                return@setOnClickListener
            }

            openDocument(filePath)
        }
    }

    /* ───────────────────── AUDIO ───────────────────── */

    private fun bindAudio(holder: AudioViewHolder, pos: Int) {
        val item = items[pos]

        // 1. SeekBar — path-based progress, default 0 for brand-new files
        holder.seekBarAudio.max = 100
        holder.seekBarAudio.progress = audioProgress[item.path] ?: 0

        // 2. Total duration (RIGHT label) — safe null handling
        val fileDuration = item.durationMs ?: 0L
        val totalDuration = when {
            fileDuration > 0 -> fileDuration
            durationCache.containsKey(item.path) -> durationCache[item.path] ?: 0L
            else -> extractDuration(item.path).also { durationCache[item.path] = it }
        }
        holder.lblDuration.text = formatTime(totalDuration)

        // 3. Play state (path-based)
        val isThisPlaying = (item.path == currentPlayingPath && mediaPlayer?.isPlaying == true)
        val isThisPaused = (item.path == currentPlayingPath && mediaPlayer?.isPlaying == false && mediaPlayer != null)

        holder.imgPlayAudio.setImageResource(
            if (isThisPlaying) R.drawable.pause_icon else R.drawable.video_play
        )

        // 4. Running time (LEFT label)
        holder.lblCurrentTime.text = when {
            (isThisPlaying || isThisPaused) && mediaPlayer != null ->
                formatTime(mediaPlayer!!.currentPosition.toLong())
            else -> "0:00"
        }

        // 5. Clean old runner
        (holder.itemView.tag as? Runnable)?.let { handler.removeCallbacks(it) }
        holder.itemView.tag = null

        // 6. Attach runner only while actually playing
        if (isThisPlaying) {
            attachProgressRunner(holder, item)
        }

        // 7. Play / Pause / Resume
        holder.imgPlayAudio.setOnClickListener { togglePlay(item) }

        // 8. Seek drag
        holder.seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                audioProgress[item.path] = progress
                if (item.path == currentPlayingPath && mediaPlayer != null && mediaPlayer!!.duration > 0) {
                    val ms = (progress * mediaPlayer!!.duration) / 100
                    mediaPlayer!!.seekTo(ms)
                    holder.lblCurrentTime.text = formatTime(ms.toLong())
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // 9. Delete (live adapter position + clear caches)
        holder.imgAudioDelete.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos == RecyclerView.NO_POSITION) return@setOnClickListener

            if (currentPlayingPath == item.path) {
                try {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                } catch (_: Exception) {}
                mediaPlayer = null
                currentPlayingPath = null
            }

            audioProgress.remove(item.path)
            durationCache.remove(item.path)

            Constant.Remaining += 1
            items.removeAt(currentPos)
            notifyItemRemoved(currentPos)
            notifyItemRangeChanged(currentPos, items.size)
        }
    }

    private fun togglePlay(item: FileItem) {
        if (currentPlayingPath == item.path) {
            when {
                mediaPlayer?.isPlaying == true -> pauseAudio(item)
                mediaPlayer != null -> resumeAudio(item)
                else -> startAudio(item)
            }
            return
        }
        releaseAudio()
        startAudio(item)
    }

    private fun startAudio(item: FileItem) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(item.path)
            prepare()
            start()
            setOnCompletionListener {
                audioProgress[item.path] = 0
                currentPlayingPath = null
                val completedPos = items.indexOfFirst { it.path == item.path }
                if (completedPos >= 0) notifyItemChanged(completedPos)
            }
        }
        currentPlayingPath = item.path
        val pos = items.indexOfFirst { it.path == item.path }
        if (pos >= 0) notifyItemChanged(pos)
    }

    private fun pauseAudio(item: FileItem) {
        mediaPlayer?.pause()
        mediaPlayer?.currentPosition?.let { ms ->
            if ((mediaPlayer?.duration ?: 0) > 0) {
                audioProgress[item.path] = (ms * 100) / mediaPlayer!!.duration
            }
        }
        val pos = items.indexOfFirst { it.path == item.path }
        if (pos >= 0) notifyItemChanged(pos)
    }

    private fun resumeAudio(item: FileItem) {
        mediaPlayer?.start()
        val pos = items.indexOfFirst { it.path == item.path }
        if (pos >= 0) notifyItemChanged(pos)
    }

    private fun releaseAudio() {
        val prevPath = currentPlayingPath
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        currentPlayingPath = null
        prevPath?.let { path ->
            val prevPos = items.indexOfFirst { it.path == path }
            if (prevPos >= 0) notifyItemChanged(prevPos)
        }
    }

    private fun attachProgressRunner(holder: AudioViewHolder, item: FileItem) {
        val runnable = object : Runnable {
            override fun run() {
                if (currentPlayingPath != item.path) return
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying && mp.duration > 0) {
                        val pct = (mp.currentPosition * 100) / mp.duration
                        holder.seekBarAudio.progress = pct
                        audioProgress[item.path] = pct
                        holder.lblCurrentTime.text = formatTime(mp.currentPosition.toLong())
                        handler.postDelayed(this, 500)
                    }
                }
            }
        }
        holder.itemView.tag = runnable
        handler.post(runnable)
    }

    /* ───────────────────── HELPERS ───────────────────── */

    private fun extractDuration(path: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            when {
                path.startsWith("content://") || path.startsWith("file://") -> {
                    retriever.setDataSource(context, Uri.parse(path))
                }
                else -> retriever.setDataSource(path)
            }
            val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
            dur
        } catch (e: Exception) {
            0L
        }
    }

    private fun openDocument(filePath: String) {
        val uri: Uri = if (filePath.startsWith("content://")) {
            Uri.parse(filePath)
        } else {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(filePath)
            )
        }
        val mimeType = getMimeType(context, uri) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Please install an app to view this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(context: Context, uri: Uri): String? {
        var mimeType: String? = context.contentResolver.getType(uri)
        if (mimeType == null) {
            val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            if (!ext.isNullOrEmpty()) {
                mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(ext.lowercase())
            }
        }
        return mimeType
    }

    private fun formatTime(ms: Long?): String {
        if (ms == null || ms <= 0) return "0:00"
        val sec = (ms / 1000) % 60
        val min = (ms / 1000) / 60
        return String.format("%d:%02d", min, sec)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is AudioViewHolder) {
            // Stop runner
            (holder.itemView.tag as? Runnable)?.let { handler.removeCallbacks(it) }
            holder.itemView.tag = null

            // RESET visual state so recycled view doesn't show old data
            holder.seekBarAudio.progress = 0
            holder.lblCurrentTime.text = "0:00"
            holder.lblDuration.text = "0:00"
            holder.imgPlayAudio.setImageResource(R.drawable.video_play)
        }
    }

    override fun getItemCount() = items.size
}







//package com.vs.schoolmessenger.CommonScreens
//
//import android.content.ActivityNotFoundException
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.View.GONE
//import android.view.View.VISIBLE
//import android.view.ViewGroup
//import android.webkit.MimeTypeMap
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.core.content.FileProvider
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.FileItem
//import com.vs.schoolmessenger.Utils.FileType
//import java.io.File
//
//class ImagePickingAdapter(
//    private val context: Context,
//    private val items: MutableList<FileItem>,
//    private val listener: OnImageClickListener
//) : RecyclerView.Adapter<ImagePickingAdapter.FileViewHolder>() {
//    private val defaultStartEndMargin: Int = context.resources.getDimensionPixelSize(R.dimen.twenty)
//    private val defaultTopMargin: Int = context.resources.getDimensionPixelSize(R.dimen.ten)
//
//    class FileViewHolder(v: View) : RecyclerView.ViewHolder(v) {
//        val img: ImageView = v.findViewById(R.id.imgPicking)
//        val del: ImageView = v.findViewById(R.id.imgDelete)
//        val delete: ImageView = v.findViewById(R.id.imgaudiodelete)
//        val imgVideoPlay: ImageView = v.findViewById(R.id.imgVideoPlay)
//        val imgVideo: ImageView = v.findViewById(R.id.imgVideo)
//        val lblTime: TextView = v.findViewById(R.id.lblTime)
//
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.image_picking_item, parent, false)
//        return FileViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: FileViewHolder, pos: Int) {
//        val item = items[pos]
//        Log.d("isFileType", item.type.toString())
//        Log.d("isFilePath", item.path.toString())
//
//        // Layout margins
//        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
//
//        layoutParams.marginStart = defaultStartEndMargin
//        layoutParams.marginEnd = defaultStartEndMargin
//        layoutParams.topMargin = defaultTopMargin
//
//        holder.itemView.layoutParams = layoutParams
//
//        val filePath = item.path
//        val fileUri = when {
//            filePath.startsWith("content://") || filePath.startsWith("file://") -> Uri.parse(
//                filePath
//            )
//
//            filePath.startsWith("http://") || filePath.startsWith("https://") -> filePath
//            else -> File(filePath)
//        }
//
//        val placeholderRes = when (item.type) {
//            FileType.PDF -> R.drawable.pdf_icon
//            FileType.DOC, FileType.DOCX -> R.drawable.doc_icon
//            FileType.PPT -> R.drawable.ppt_icon
//            FileType.EXCEL -> R.drawable.excel_icon
//            FileType.TXT -> R.drawable.txt_icon
//            FileType.IMAGE -> R.drawable.image_placeholder
//            FileType.VIDEO -> R.drawable.black
//            FileType.AUDIO -> R.drawable.voice
//            else -> R.drawable.address_icon
//        }
//
//        Glide.with(context)
//            .load(fileUri)
//            .placeholder(placeholderRes)
//            .apply(RequestOptions().dontTransform())
//            .error(placeholderRes)
//            .into(holder.img)
//
//
//        holder.del.visibility = if (pos == 0) GONE else VISIBLE
//        holder.del.setOnClickListener {
//            Log.d("isPosition", pos.toString())
//            Constant.Remaining = Constant.Remaining + 1
//            items.removeAt(pos)
//            notifyItemRemoved(pos)
//            notifyItemRangeChanged(pos, items.size)
//        }
//
//        if (item.type.toString() == Constant.VIDEO) {
//            holder.imgVideo.visibility = VISIBLE
//        } else {
//            holder.imgVideo.visibility = GONE
//        }
//        holder.itemView.setOnClickListener {
//            if (pos != 0) {
//                if (!item.path.contains("amazonaws.")) {
//                    if (item.type.toString() == Constant.IMAGE || item.type.toString() == Constant.VIDEO) {
//                        val filteredFiles = Constant.selectedFiles.filter {
//                            it.type.toString() == Constant.IMAGE || it.type.toString() == Constant.VIDEO
//                        }
//                        Constant.commonFileList = filteredFiles.map {
//                            CommonFileData(
//                                type = it.type.toString(),
//                                path = it.path
//                            )
//                        }
//                            .toMutableList()
//                        val clickedPath = item.path
//                        val indexInFiltered = filteredFiles.indexOfFirst { it.path == clickedPath }
//                            .let { if (it >= 0) it else 0 }
//                        Constant.selectedFileIndex = indexInFiltered - 1
//                        val intent = Intent(context, FilesViewActivity::class.java)
//                        intent.putExtra(Constant.subjectName, "Your Files")
//                        context.startActivity(intent)
//                    } else {
//
//
//                        Log.d("item.path", filePath)
//
//                        // 1️⃣ Create URI safely
//                        val uri: Uri = if (filePath.startsWith("content://")) {
//                            Uri.parse(filePath)
//                        } else {
//                            FileProvider.getUriForFile(
//                                context,
//                                "${context.packageName}.fileprovider",
//                                File(filePath)
//                            )
//                        }
//
//                        // 2️⃣ Get MIME type (with fallback)
//                        val mimeType = getMimeType(context, uri) ?: "*/*"
//                        Log.d("FILE_DEBUG", "uri=$uri mime=$mimeType")
//
//                        // 3️⃣ Create intent
//                        val intent = Intent(Intent.ACTION_VIEW).apply {
//                            setDataAndType(uri, mimeType)
//                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        }
//
//                        // 4️⃣ Open chooser safely
//                        try {
//                            context.startActivity(Intent.createChooser(intent, "Open with"))
//                        } catch (e: ActivityNotFoundException) {
//                            Toast.makeText(
//                                context,
//                                "Please install an app to view this file",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                } else {
//                    Constant.commonFileList = Constant.selectedFiles.map {
//                        CommonFileData(type = it.type.toString(), path = it.path)
//                    }.toMutableList()
//                    Constant.selectedFileIndex = pos - 1
//                    val intent = Intent(context, FilesViewActivity::class.java)
//                    intent.putExtra(Constant.subjectName, "Your Files")
//                    context.startActivity(intent)
//                }
//            } else {
//                listener.onImageClick(pos)
//            }
//        }
//    }
//
//    private fun getMimeType(context: Context, uri: Uri): String? {
//        var mimeType: String? = context.contentResolver.getType(uri)
//
//        if (mimeType == null) {
//            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
//            if (!extension.isNullOrEmpty()) {
//                mimeType = MimeTypeMap.getSingleton()
//                    .getMimeTypeFromExtension(extension.lowercase())
//            }
//        }
//        return mimeType
//    }
//
//    override fun getItemCount() = items.size
//}
//
