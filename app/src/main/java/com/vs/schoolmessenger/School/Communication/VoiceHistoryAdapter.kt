package com.vs.schoolmessenger.School.Communication

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.SeekBarOnProgressChanged
import com.vs.schoolmessenger.Utils.WaveExtractor
import com.vs.schoolmessenger.Utils.WaveformSeekBar
import java.util.Random

class VoiceHistoryAdapter(
    private var itemList: List<VoiceHistoryData>?,
    private var listener: VoiceHistoryClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_from_voice_message, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener)
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
        private lateinit var mediaPlayer: MediaPlayer
        private var isPrepared = false
        private var isPlayingVoice = false // Track the playback state
        private var lastPosition: Int = 0 // Variable to hold the last playback position
        private val handler = Handler(Looper.getMainLooper())
        private val progressUpdater = object : Runnable {
            override fun run() {
                if (isPrepared && mediaPlayer.isPlaying) {
                    val progress = mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration
                    waveformSeekBar.progress = progress // Ensure this updates correctly
                    handler.postDelayed(this, 100) // Update every 100ms
                }
            }
        }

        fun bind(data: VoiceHistoryData, position: Int, listener: VoiceHistoryClickListener) {

            lblTitle.text = data.title
            initializeMediaPlayer(data.url)


            waveformSeekBar.apply {
                progress = 0f // Start with zero progress
                waveProgressColor = ContextCompat.getColor(context, R.color.light_green_bg1)
                sample = getDummyWaveSample() // Initial setup
            }

            waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
                override fun onProgressChanged(
                    waveformSeekBar: WaveformSeekBar,
                    progress: Float,
                    fromUser: Boolean
                ) {
                    if (fromUser && isPrepared) {
                        val seekPosition = (mediaPlayer.duration * progress).toInt()
                        mediaPlayer.seekTo(seekPosition)
                    }
                    waveformSeekBar.invalidate() // Force redraw for user-driven updates
                }
            }

            imgVoicePlay.setOnClickListener {
                listener.onItemClick(data,this)
            }
        }

        private fun getDummyWaveSample(): IntArray {
            return IntArray(50) { Random().nextInt(50) }
        }

        private fun loadWaveform(audioUrl: String) {
            Thread {
                val waveExtractor = WaveExtractor()
                val waveformSamples =
                    waveExtractor.extractWaveHeights(audioUrl, 50) // Extract wave data
                waveformSeekBar.sample =
                    waveformSamples.map { it.toInt() }.toIntArray() // Set waveform samples

            }.start()
        }


        private fun initializeMediaPlayer(audioUrl: String) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync() // Prepare asynchronously
                setOnPreparedListener {
                    isPrepared = true
                    waveformSeekBar.maxProgress = 1f // Set the max for normalized progress
                    waveformSeekBar.progress = 0f // Reset progress to 0
                    loadWaveform(audioUrl)
                    startAudioProgressUpdate() // Start updating progress
                    start() // Start playback
                }
                setOnCompletionListener {
                    stopAudioProgressUpdate()
                    lastPosition = 0 // Reset last position on completion
                    waveformSeekBar.progress = 0f // Reset seek bar on completion
                    isPlayingVoice = false // Update playback state
//                    binding.imgVoicePlay.setImageDrawable(
//                        ContextCompat.getDrawable(
//                            context,
//                            R.drawable.play_icon_voice
//                        )
//                    ) // Change icon to play
                }
            }
        }

        private fun startAudioProgressUpdate() {
            handler.post(progressUpdater)
            waveformSeekBar.invalidate() // Force redraw
        }

        private fun stopAudioProgressUpdate() {
            handler.removeCallbacks(progressUpdater)
            waveformSeekBar.progress = 0f // Reset progress
//           imgVoicePlay.setImageDrawable(
//                ContextCompat.getDrawable(context, R.drawable.play_icon_voice)
//            )
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }

}