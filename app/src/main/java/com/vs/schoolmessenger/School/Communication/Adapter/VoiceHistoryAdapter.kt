package com.vs.schoolmessenger.School.Communication.Adapter

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceHistoryDetails
import com.vs.schoolmessenger.School.Communication.Interface.VoiceHistoryClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import com.vs.schoolmessenger.Utils.WaveformSeekBar

class VoiceHistoryAdapter(
    private var itemList: List<VoiceHistoryDetails>?,
    private var listener: VoiceHistoryClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    var currentlyPlayingHolder: DataViewHolder? = null

    override fun getItemViewType(position: Int) =
        if (isLoading) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            ShimmerViewHolder(
                ShimmerUtil.wrapWithShimmer(parent, R.layout.history_from_voice_message)
            )
        } else {
            DataViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_from_voice_message, parent, false),
                context
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], listener, this)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount() = if (isLoading) 20 else itemList?.size ?: 0

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val waveformSeekBar: WaveformSeekBar =
            itemView.findViewById(R.id.waveformSeekBar)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val imgVoicePlay: ImageView =
            itemView.findViewById(R.id.imgVoicePlay)
        private val lblStartDuration: TextView =
            itemView.findViewById(R.id.lblStartDuration)
        private val lblEndDuration: TextView = itemView.findViewById(R.id.lblEndDuration)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblSeeMoreClick: TextView = itemView.findViewById(R.id.lblSeeMoreClick)

        private val rlaSendVoice: RelativeLayout =
            itemView.findViewById(R.id.rlaSendVoice)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)


        private lateinit var mediaPlayer: MediaPlayer
        private var isPrepared = false
        private var isPlayingVoice = false
        private var lastPosition = 0

        private val handler = Handler(Looper.getMainLooper())
        private var isUserSeeking = false


        private val progressRunnable = object : Runnable {
            override fun run() {
                if (::mediaPlayer.isInitialized && isPrepared && !isUserSeeking) {

                    val duration = mediaPlayer.duration
                    if (duration > 0) {
                        val progress =
                            mediaPlayer.currentPosition.toFloat() / duration.toFloat()

                        waveformSeekBar.updateWithLevel(progress.coerceIn(0f, 1f))
                        lblStartDuration.text = formatTime(mediaPlayer.currentPosition)
                    }

                    handler.postDelayed(this, 40)
                }
            }
        }

        fun bind(
            data: VoiceHistoryDetails,
            listener: VoiceHistoryClickListener,
            adapter: VoiceHistoryAdapter
        ) {

            lblTitle.text = data.title
            lblSeeMoreClick.visibility = View.GONE

            val parts = data.sentOn.split(" ")
            if (parts.size >= 3) {
                val date = parts[0]
                val time = "${parts[1]} ${parts[2]}"
                lblTime.text = time
                lblDate.text = Constant.convertDateTimeFormat(date)
            }

            lblEndDuration.text = formatTime(data.duration.toInt() * 1000)
            lblStartDuration.text = "00:00"
            waveformSeekBar.updateWithLevel(0f)
            rlaSendVoice.visibility = View.VISIBLE
            lblEndDuration.text = String.format(
                Constant.dateForMate,
                data.duration.toInt() / 60,
                data.duration.toInt() % 60
            )

            lblEndDuration.text = formatTime(data.duration.toInt() * 1000)
            lblStartDuration.text = "00:00"
            waveformSeekBar.updateWithLevel(0f)
            rlaSendVoice.visibility = View.VISIBLE


            rlaSendVoice.setOnClickListener {
                if (adapter.currentlyPlayingHolder != null &&
                    adapter.currentlyPlayingHolder != this
                ) {
                    adapter.currentlyPlayingHolder?.stopPlayback()
                    adapter.currentlyPlayingHolder = null
                }

                stopPlayback() // stop this holder if needed
                listener.onItemClick(data, this)
//                listener.onItemClick(data, this)
            }

            waveformSeekBar.setOnSeekChangeListener { progress ->

                if (!::mediaPlayer.isInitialized || !isPrepared) return@setOnSeekChangeListener

                isUserSeeking = true

                val newPosition = (progress * mediaPlayer.duration).toInt()
                mediaPlayer.seekTo(newPosition)
                lastPosition = newPosition
                lblStartDuration.text = formatTime(newPosition)

                isUserSeeking = false

                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    startProgress()
                    updateIcon(true)
                    isPlayingVoice = true
                }
            }


            imgVoicePlay.setOnClickListener {

                if (adapter.currentlyPlayingHolder != null &&
                    adapter.currentlyPlayingHolder != this
                ) {
                    adapter.currentlyPlayingHolder?.pauseOnly()
                }

                if (isPlayingVoice) {
                    pause()
                } else {
                    if (!::mediaPlayer.isInitialized || !isPrepared) {
                        initPlayer(data.url)
                    } else {
                        resume()
                    }
                }

                adapter.currentlyPlayingHolder = this
            }
        }

        private fun initPlayer(url: String) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()

                setOnPreparedListener {
                    isPrepared = true
                    seekTo(lastPosition)
                    start()
                    isPlayingVoice = true
                    startProgress()
                    updateIcon(true)
                    keepScreenOn()
                }

                setOnCompletionListener {
                    stopProgress()
                    isPlayingVoice = false
                    lastPosition = 0
                    waveformSeekBar.updateWithLevel(0f)
                    lblStartDuration.text = "00:00"
                    updateIcon(false)
                }
            }
        }

        private fun resume() {
            mediaPlayer.seekTo(lastPosition)
            mediaPlayer.start()
            isPlayingVoice = true
            startProgress()
            updateIcon(true)
        }

        private fun pause() {
            mediaPlayer.pause()
            lastPosition = mediaPlayer.currentPosition
            isPlayingVoice = false
            stopProgress()
            updateIcon(false)
        }

        fun pauseOnly() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                lastPosition = mediaPlayer.currentPosition
                mediaPlayer.pause()
                isPlayingVoice = false
                stopProgress()
                updateIcon(false)
            }
        }

        fun stopPlayback() {
            stopProgress()
            if (::mediaPlayer.isInitialized) {
                try {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                } catch (_: Exception) {
                }
            }
            isPrepared = false
            isPlayingVoice = false
            lastPosition = 0
            waveformSeekBar.updateWithLevel(0f)
            lblStartDuration.text = "00:00"
            updateIcon(false)
        }

        private fun startProgress() {
            handler.removeCallbacks(progressRunnable)
            handler.post(progressRunnable)
        }

        private fun stopProgress() {
            handler.removeCallbacks(progressRunnable)
        }

        private fun updateIcon(playing: Boolean) {
            imgVoicePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    if (playing) R.drawable.pause_icon else R.drawable.video_play
                )
            )
        }

        private fun keepScreenOn() {
            if (context is Activity) {
                context.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        private fun formatTime(ms: Int): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Constant.dateForMate, minutes, seconds)
        }
    }

    fun releaseMediaPlayer() {
        currentlyPlayingHolder?.stopPlayback()
        currentlyPlayingHolder = null
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}