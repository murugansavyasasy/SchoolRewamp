package com.vs.schoolmessenger.Parent.Communication

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import com.vs.schoolmessenger.Utils.WaveformSeekBar


class UnifiedVoiceAdapter(
    private var itemList: ArrayList<VoiceData>? = null,
    private var listener: VoiceClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private var lifecycleOwner: LifecycleOwner,
    private var isAccessToken: String,
    private var isFromArchive: Boolean,
    private var isSeeMoreClick: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var currentlyPlayingHolder: DataViewHolder? = null
    private var appViewModel: App =
        ViewModelProvider(context as ViewModelStoreOwner)[App::class.java]

    init {
        appViewModel.init()
        appViewModel.isUpdateStatusArchive?.observe(lifecycleOwner) { response ->
            Log.d(
                "UnifiedVoiceAdapter",
                if (response?.status == true) "Archive API successful" else "Archive API failed or empty"
            )
        }
        appViewModel.isUpdateStatusCommunication?.observe(lifecycleOwner) { response ->
            Log.d(
                "UnifiedVoiceAdapter",
                if (response?.status == true) "API successful" else "API failed or empty"
            )
        }
    }

    fun setIsFromArchive(value: Boolean) {
        this.isFromArchive = value
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    fun updateData() {
        itemList?.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.history_from_voice_message)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_from_voice_message, parent, false)
            DataViewHolder(view, context, appViewModel, isAccessToken, isFromArchive)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener, this)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }

    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val appViewModel: App,
        private val isAccessToken: String,
        private val isFromArchive: Boolean
    ) : RecyclerView.ViewHolder(itemView) {

        private val lblSeeMore: TextView = itemView.findViewById(R.id.lblSeeMore)
        private val lblSeeMoreClick: TextView = itemView.findViewById(R.id.lblSeeMoreClick)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val lblStartDuration: TextView = itemView.findViewById(R.id.lblStartDuration)
        private val lblEndDuration: TextView = itemView.findViewById(R.id.lblEndDuration)
        private val lblContentText: TextView = itemView.findViewById(R.id.lblContentText)
        private val lblTitleText: TextView = itemView.findViewById(R.id.lblTitleText)
        private val lblDateText: TextView = itemView.findViewById(R.id.lblDateText)
        private val lblTimeText: TextView = itemView.findViewById(R.id.lblTimeText)
        private val rlaVoice: View = itemView.findViewById(R.id.rlaVoice)
        private val rlaText: RelativeLayout = itemView.findViewById(R.id.rlaText)
        private val imgVoicePlay: ImageView = itemView.findViewById(R.id.imgVoicePlay)
        private val waveformSeekBar: WaveformSeekBar = itemView.findViewById(R.id.waveformSeekBar)
        private val lblnewiconVoice: ImageView = itemView.findViewById(R.id.lblnewiconVoice)
        private val lblnewiconText: ImageView = itemView.findViewById(R.id.lblnewiconText)
        private val rlaSendVoice: View = itemView.findViewById(R.id.rlaSendVoice)
        private val rlaSelectText: View = itemView.findViewById(R.id.rlaSelectText)
        private var isExpanded = false
        private var mediaPlayer: MediaPlayer? = null

        private var isPrepared = false
        private var isPlayingVoice = false
        private var lastPosition: Int = 0
        private val handler = Handler(Looper.getMainLooper())

        private val progressUpdater = object : Runnable {
            override fun run() {
                if (isPrepared && mediaPlayer!!.isPlaying) {
                    waveformSeekBar.updateWithLevel(1f)
                    lblStartDuration.text = formatTime(mediaPlayer!!.currentPosition)
                    handler.postDelayed(this, 100)
                }
            }
        }

        @SuppressLint("DefaultLocale")
        fun bind(
            data: VoiceData,
            position: Int,
            listener: VoiceClickListener,
            adapter: UnifiedVoiceAdapter
        ) {

            if (position == adapter.itemCount - 1) {
                if (adapter.isSeeMoreClick) {
                    lblSeeMoreClick.visibility = View.VISIBLE
                } else {
                    lblSeeMoreClick.visibility = View.GONE
                }
            } else {
                lblSeeMoreClick.visibility = View.GONE
            }

            lblSeeMoreClick.setOnClickListener {
                lblSeeMoreClick.visibility = View.GONE
                listener.onSeeMoreClick(data, this@DataViewHolder)
            }

            if (data.type.equals(Constant.VOICE)) {
                rlaVoice.visibility = View.VISIBLE
                rlaText.visibility = View.GONE
                lblTitle.text = data.title ?: ""
                lblDate.text = Constant.convertDateTimeFormat(data.date.toString())
                lblTime.text = data.time ?: ""
                lblnewiconVoice.visibility = if (data.is_unread!!) View.VISIBLE else View.GONE
                lblnewiconText.visibility = View.GONE
                rlaSendVoice.visibility = View.GONE
                lblContentText.text = data.content ?: ""
                lblEndDuration.text = String.format(
                   Constant.dateForMate, data.duration!!.toInt() / 60, data.duration!!.toInt() % 60
                )

                imgVoicePlay.setOnClickListener {
                    listener.onItemClick(data, this@DataViewHolder)
                    lblnewiconVoice.visibility = View.GONE
                    if (data.is_unread!!) {
                        if (data.is_archive!!) {
                            listener.onUpdateArchiveStatus(data.type, data.id)
                        } else {
                            listener.onUpdateCommunicationStatus(data.type, data.id)
                        }
                        data.is_unread = false
                    }

                    if (adapter.currentlyPlayingHolder != null && adapter.currentlyPlayingHolder != this) {
                        adapter.currentlyPlayingHolder?.stopAudioPlayback()
                    }

                    if (isPlayingVoice) {
                        pauseAudio()
                    } else {
                        if (!isPrepared) {
                            initializeMediaPlayer(data.content ?: "")
                        } else {
                            resumeAudio()
                        }
                    }
                    adapter.currentlyPlayingHolder = this
                }
            } else {
                rlaVoice.visibility = View.GONE
                rlaText.visibility = View.VISIBLE
                lblTitleText.text = data.title ?: ""
                lblContentText.text = data.content ?: ""
                lblDateText.text = Constant.convertDateTimeFormat(data.date.toString())
                lblTimeText.text = data.time ?: ""
                rlaSelectText.visibility = View.GONE
                rlaSendVoice.visibility = View.GONE

                if (data.is_unread!!) {
                    lblnewiconText.visibility = View.VISIBLE
                    lblSeeMore.visibility = View.VISIBLE
                } else {
                    lblnewiconText.visibility = View.GONE
                    if (lblContentText.lineCount > 3) {
                        lblSeeMore.visibility = View.VISIBLE
                        lblnewiconText.visibility = View.GONE
                        lblContentText.maxLines = 3
                        lblContentText.ellipsize = TextUtils.TruncateAt.END
                        lblSeeMore.text = context.getString(R.string.see_more_2)
                    } else {
                        lblSeeMore.visibility = View.GONE
                    }
                }
                isSeeMoreVisibility(lblContentText, lblSeeMore)


                rlaText.setOnClickListener {
                    isExpanded = !isExpanded

                    if (data.is_unread == true) {
                        if (data.is_archive == true) {
                            listener.onUpdateArchiveStatus(data.type, data.id)
                        } else {
                            listener.onUpdateCommunicationStatus(data.type, data.id)
                        }
                        data.is_unread = false
                    }

                    listener.onItemClick(data, this@DataViewHolder)
                }

                isSeeMoreVisibility(lblContentText, lblSeeMore)


                lblSeeMore.setOnClickListener {
                    isExpanded = !isExpanded
                    lblSeeMore.visibility = View.GONE
                    if (isExpanded) {
                        lblContentText.maxLines = Int.MAX_VALUE
//                        lblSeeMore.text = context.getString(R.string.see_less)
                    } else {
                        lblContentText.maxLines = 3
//                        lblSeeMore.text = context.getString(R.string.see_more)
                    }

                    if (data.is_unread == true) {
                        if (data.is_archive == true) {
                            listener.onUpdateArchiveStatus(data.type, data.id)
                        } else {
                            listener.onUpdateCommunicationStatus(data.type, data.id)
                        }
                        data.is_unread = false
                    }
                    listener.onItemClick(data, this@DataViewHolder)
                }
            }
        }

        private fun initializeMediaPlayer(audioUrl: String) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()
                setOnPreparedListener {
                    isPrepared = true
                    startAudioProgressUpdate()
                    start()
                    isPlayingVoice = true
                    updatePlayPauseIcon(true)
                }
                setOnCompletionListener {
                    resetPlaybackState()
                    lblStartDuration.text = "00:00"
                }
            }
        }

        private fun isSeeMoreVisibility(lblContent: TextView, tvSeeMore: TextView) {
            lblContent.post {
                if (lblContent.lineCount > 3) {
                    tvSeeMore.visibility = View.VISIBLE
                    lblnewiconText.visibility = View.GONE
                    lblContent.maxLines = 3
                    lblContent.ellipsize = TextUtils.TruncateAt.END
                }
            }
        }

        private fun pauseAudio() {
            mediaPlayer!!.pause()
            lastPosition = mediaPlayer!!.currentPosition
            isPlayingVoice = false
            updatePlayPauseIcon(false)
            waveformSeekBar.updateWithLevel(0f)
        }

        private fun resumeAudio() {
            mediaPlayer!!.seekTo(lastPosition)
            mediaPlayer!!.start()
            isPlayingVoice = true
            startAudioProgressUpdate()
            updatePlayPauseIcon(true)
        }

        fun releaseMediaPlayer() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        }

        fun stopAudioPlayback() {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
                resetPlaybackState()
            }
            mediaPlayer = null
        }

        private fun resetPlaybackState() {
            stopAudioProgressUpdate()
            isPrepared = false
            isPlayingVoice = false
            lastPosition = 0
            waveformSeekBar.updateWithLevel(0f)
            updatePlayPauseIcon(false)
        }

        private fun updatePlayPauseIcon(isPlaying: Boolean) {
            val icon: Int
            if (isPlaying) {
                icon = R.drawable.pause_icon
            } else {
                icon = R.drawable.video_play
            }
            imgVoicePlay.setImageDrawable(ContextCompat.getDrawable(context, icon))
        }


        private fun startAudioProgressUpdate() {
            handler.post(progressUpdater)
        }

        private fun stopAudioProgressUpdate() {
            handler.removeCallbacks(progressUpdater)
        }

        private fun formatTime(milliseconds: Int): String {
            val seconds = (milliseconds / 1000) % 60
            val minutes = (milliseconds / (1000 * 60)) % 60
            return String.format(Constant.dateForMate, minutes, seconds)
        }
    }


    fun releaseMediaPlayer() {
        currentlyPlayingHolder?.stopAudioPlayback()
        currentlyPlayingHolder = null
    }

    fun updateList(newList: List<VoiceData>, isSeeMoreData: Boolean) {
        this.itemList = ArrayList(newList)
        isSeeMoreClick = isSeeMoreData
        notifyDataSetChanged()
    }

    fun setLoadingState(loading: Boolean) {
        isLoading = loading
        notifyDataSetChanged()
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
