package com.vs.schoolmessenger.Parent.Communication

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.WaveformSeekBar
import kotlin.math.max

class VoiceAdapter(
    private var itemList: ArrayList<VoiceData>?,
    private var listener: VoiceClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var currentlyPlayingHolder: DataViewHolder? = null // Track currently playing holder

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }


    fun updateData() {
        itemList!!.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_from_voice_message, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this) // Pass adapter reference
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val waveformSeekBar: WaveformSeekBar = itemView.findViewById(R.id.waveformSeekBar)
        private val imgVoicePlay: ImageView = itemView.findViewById(R.id.imgVoicePlay)
        private val lblStartDuration: TextView = itemView.findViewById(R.id.lblStartDuration)
        private val lblEndDuration: TextView = itemView.findViewById(R.id.lblEndDuration)
        private val rlaSendVoice: RelativeLayout = itemView.findViewById(R.id.rlaSendVoice)


        private lateinit var mediaPlayer: MediaPlayer
        private var isPrepared = false
        private var isPlayingVoice = false
        private var lastPosition: Int = 0
        private val handler = Handler(Looper.getMainLooper())

        // Progress updater for audio playback
        private val progressUpdater = object : Runnable {
            override fun run() {
                if (isPrepared && mediaPlayer.isPlaying) {
                    val normalizedPower = max(1f, (1f + 160) / 160)
                    waveformSeekBar.updateWithLevel(normalizedPower)
                    lblStartDuration.text = formatTime(mediaPlayer.currentPosition)
                    handler.postDelayed(this, 100)
                }
            }
        }


        fun bind(
            data: VoiceData,
            position: Int,
            listener: VoiceClickListener,
            adapter: VoiceAdapter
        ) {
            lblTitle.text = data.title
            rlaSendVoice.visibility = View.GONE
            getAudioDuration(data.url) { duration ->
                lblEndDuration.text =
                    formatTime(duration) // Update the TextView with formatted duration
            }


            imgVoicePlay.setOnClickListener {
                listener.onItemClick(data, this@DataViewHolder)

                imgVoicePlay.setOnClickListener {
                    if (adapter.currentlyPlayingHolder != null && adapter.currentlyPlayingHolder != this) {
                        adapter.currentlyPlayingHolder?.stopAudioPlayback()
                    }

                    if (isPlayingVoice) {
                        pauseAudio()
                    } else {
                        if (!isPrepared) {
                            initializeMediaPlayer(data.url)
                        } else {
                            resumeAudio()
                        }
                    }
                    adapter.currentlyPlayingHolder = this
                }
            }
        }

        // Initialize MediaPlayer and prepare audio
        private fun initializeMediaPlayer(audioUrl: String) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()
                setOnPreparedListener {
                    isPrepared = true
                    startAudioProgressUpdate()
                    start()
                    isPlayingVoice = true
                    updatePlayPauseIcon(isPlaying = true)
                }
                setOnCompletionListener {
                    resetPlaybackState()
                }
            }
        }

        // Pause audio playback
        private fun pauseAudio() {
            mediaPlayer.pause()
            lastPosition = mediaPlayer.currentPosition
            isPlayingVoice = false
            updatePlayPauseIcon(isPlaying = false)
            waveformSeekBar.updateWithLevel(0f)

        }

        // Resume audio playback
        private fun resumeAudio() {
            mediaPlayer.seekTo(lastPosition)
            mediaPlayer.start()
            isPlayingVoice = true
            startAudioProgressUpdate()
            updatePlayPauseIcon(isPlaying = true)
        }

        // Stop audio playback
        fun stopAudioPlayback() {
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.reset()
                resetPlaybackState()
            }
        }

        // Update the play/pause icon
        private fun updatePlayPauseIcon(isPlaying: Boolean) {
            val icon = if (isPlaying) R.drawable.pause_icon else R.drawable.video_play
            imgVoicePlay.setImageDrawable(ContextCompat.getDrawable(context, icon))
        }

        // Reset playback state
        private fun resetPlaybackState() {
            stopAudioProgressUpdate()
            isPrepared = false
            isPlayingVoice = false
            lastPosition = 0
            waveformSeekBar.updateWithLevel(0f)
            updatePlayPauseIcon(isPlaying = false)
        }

        // Start updating audio progress
        private fun startAudioProgressUpdate() {
            handler.post(progressUpdater)
        }

        // Stop updating audio progress
        private fun stopAudioProgressUpdate() {
            handler.removeCallbacks(progressUpdater)
        }

        // Format milliseconds to "mm:ss"
        private fun formatTime(milliseconds: Int): String {
            val seconds = (milliseconds / 1000) % 60
            val minutes = (milliseconds / (1000 * 60)) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        // Get audio duration asynchronously
        private fun getAudioDuration(audioUrl: String, callback: (Int) -> Unit) {
            val tempMediaPlayer = MediaPlayer()
            try {
                tempMediaPlayer.setDataSource(audioUrl)
                tempMediaPlayer.prepareAsync()
                tempMediaPlayer.setOnPreparedListener {
                    callback(tempMediaPlayer.duration)
                    tempMediaPlayer.release()
                }
                tempMediaPlayer.setOnErrorListener { mp, _, _ ->
                    mp.release()
                    false
                }
            } catch (e: Exception) {
                tempMediaPlayer.release()
            }
        }
    }

    fun releaseMediaPlayer() {
        currentlyPlayingHolder?.stopAudioPlayback()
        currentlyPlayingHolder = null
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}
