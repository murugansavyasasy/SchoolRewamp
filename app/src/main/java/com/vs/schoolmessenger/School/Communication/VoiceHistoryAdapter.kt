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
import com.vs.schoolmessenger.Utils.WaveformSeekBar
import kotlin.math.max

class VoiceHistoryAdapter(
    private var itemList: List<VoiceHistoryData>?,
    private var listener: VoiceHistoryClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var currentlyPlayingHolder: DataViewHolder? = null // Track currently playing holder

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
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
        private lateinit var mediaPlayer: MediaPlayer
        private var isPrepared = false
        var isPlayingVoice = false // Track the playback state
        private var lastPosition: Int = 0 // Variable to hold the last playback position
        private val handler = Handler(Looper.getMainLooper())
        private val progressUpdater = object : Runnable {
            override fun run() {
                if (isPrepared && mediaPlayer.isPlaying) {
                    val progress = mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration
//                    waveformSeekBar.progress = progress // Ensure this updates correctly
                    val currentPosition = mediaPlayer.currentPosition
                    lblStartDuration.text = formatTime(currentPosition)
                    handler.postDelayed(this, 100) // Update every 100ms
                }
            }
        }

        fun bind(
            data: VoiceHistoryData,
            position: Int,
            listener: VoiceHistoryClickListener,
            adapter: VoiceHistoryAdapter
        ) {
            lblTitle.text = data.title

            getAudioDuration(data.url) { duration ->
                lblEndDuration.text = formatTime(duration) // Update the TextView with formatted duration
            }


            imgVoicePlay.setOnClickListener {
                listener.onItemClick(data, this@DataViewHolder)

                // Stop any currently playing audio before playing the new one
                if (adapter.currentlyPlayingHolder != null && adapter.currentlyPlayingHolder != this) {
                    adapter.currentlyPlayingHolder?.stopAudioPlayback() // Stop previous audio
                }

                if (isPlayingVoice) {
                    // Pause the media player
                    mediaPlayer.pause()
                    lastPosition = mediaPlayer.currentPosition // Save current position
                    isPlayingVoice = false
                    imgVoicePlay.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.video_play
                        )
                    ) // Change icon to play
                    waveformSeekBar.updateWithLevel(0f)
                } else {

                    if (!isPrepared) {
                        val normalizedPower = max(1f, (1f + 160) / 160)
                        waveformSeekBar.updateWithLevel(normalizedPower)
                        imgVoicePlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.pause_icon
                            )
                        ) // Change icon to pause
                        initializeMediaPlayer(data.url) // Prepare the media player for the first time
                    } else {
                        val normalizedPower = max(1f, (1f + 160) / 160)
                        waveformSeekBar.updateWithLevel(normalizedPower)
                        mediaPlayer.seekTo(lastPosition) // Seek to last position
                        mediaPlayer.start() // Start playing
                        isPlayingVoice = true
                        imgVoicePlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.pause_icon
                            )
                        ) // Change icon to pause
                        startAudioProgressUpdate() // Start updating progress again
                    }
                }
                adapter.currentlyPlayingHolder = this // Set the current holder to this
            }
        }

        private fun getAudioDuration(audioUrl: String, callback: (Int) -> Unit) {

            // Check if the mediaPlayer is already initialized
            if (!::mediaPlayer.isInitialized) {
                mediaPlayer = MediaPlayer() // Initialize if not already done
            } else {
                mediaPlayer.reset() // Reset the MediaPlayer before reusing
            }

            try {
                mediaPlayer.setDataSource(audioUrl) // Set the data source to the audio URL
                mediaPlayer.prepareAsync() // Prepare asynchronously

                mediaPlayer.setOnPreparedListener {
                    // Once prepared, get the duration
                    val duration = mediaPlayer.duration // Duration in milliseconds
                    callback(duration) // Return the duration using the callback
                    mediaPlayer.release() // Release the media player resources
                }

                mediaPlayer.setOnErrorListener { mp, what, extra ->
                    mp.release() // Release the media player resources on error
                    false // Return false to indicate the error was handled
                }
            } catch (e: Exception) {
                mediaPlayer.release() // Release resources on exception
            }
        }

        // Function to format milliseconds into a time string (mm:ss)
        private fun formatTime(milliseconds: Int): String {
            val seconds = (milliseconds / 1000) % 60
            val minutes = (milliseconds / (1000 * 60)) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        fun stopAudioPlayback() {
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    waveformSeekBar.updateWithLevel(0f)

                    mediaPlayer.stop() // Stop playback
                    mediaPlayer.reset() // Reset media player
                }
                isPlayingVoice = false
                lastPosition = 0 // Reset last position on stop
//                waveformSeekBar.progress = 0f // Reset seek bar on stop
                imgVoicePlay.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.video_play)
                ) // Change the icon back to play
            }
        }


        private fun initializeMediaPlayer(audioUrl: String) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync() // Prepare asynchronously
                setOnPreparedListener {
                    isPrepared = true
                    startAudioProgressUpdate() // Start updating progress
                    start() // Start playback
                }
                setOnCompletionListener {
                    waveformSeekBar.updateWithLevel(0f)
                    stopAudioProgressUpdate()
                    lastPosition = 0 // Reset last position on completion
                    isPlayingVoice = false // Update playback state
                    imgVoicePlay.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.video_play
                        )
                    ) // Change icon back to play on completion
                }
            }
        }

        private fun startAudioProgressUpdate() {
            handler.post(progressUpdater)
            waveformSeekBar.invalidate() // Force redraw
        }

        private fun stopAudioProgressUpdate() {
            waveformSeekBar.updateWithLevel(0f)

            mediaPlayer.stop() // Stop playback
            mediaPlayer.reset()
            handler.removeCallbacks(progressUpdater)
            imgVoicePlay.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.video_play)
            ) // Reset icon when stopping
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


// Don't delete below code

/**

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

private var currentlyPlayingHolder: DataViewHolder? = null // Track currently playing holder

override fun getItemViewType(position: Int): Int {
return if (isLoading) TYPE_SHIMMER else TYPE_DATA
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
private lateinit var mediaPlayer: MediaPlayer
private var isPrepared = false
var isPlayingVoice = false // Track the playback state
private var lastPosition: Int = 0 // Variable to hold the last playback position
private val handler = Handler(Looper.getMainLooper())
private val progressUpdater = object : Runnable {
override fun run() {
if (isPrepared && mediaPlayer.isPlaying) {
val progress = mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration
waveformSeekBar.progress = progress // Ensure this updates correctly
val currentPosition = mediaPlayer.currentPosition
lblStartDuration.text = formatTime(currentPosition)
handler.postDelayed(this, 100) // Update every 100ms
}
}
}

fun bind(
data: VoiceHistoryData,
position: Int,
listener: VoiceHistoryClickListener,
adapter: VoiceHistoryAdapter
) {
lblTitle.text = data.title

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

getAudioDuration(data.url) { duration ->
lblEndDuration.text = formatTime(duration) // Update the TextView with formatted duration
}


imgVoicePlay.setOnClickListener {
listener.onItemClick(data, this@DataViewHolder)

// Stop any currently playing audio before playing the new one
if (adapter.currentlyPlayingHolder != null && adapter.currentlyPlayingHolder != this) {
adapter.currentlyPlayingHolder?.stopAudioPlayback() // Stop previous audio
}

if (isPlayingVoice) {
// Pause the media player
mediaPlayer.pause()
lastPosition = mediaPlayer.currentPosition // Save current position
isPlayingVoice = false
imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(
context,
R.drawable.play_icon_voice
)
) // Change icon to play
} else {
// If the media player is not initialized, initialize it
if (!isPrepared) {
imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(
context,
R.drawable.pause_icon
)
) // Change icon to pause
initializeMediaPlayer(data.url) // Prepare the media player for the first time
} else {
mediaPlayer.seekTo(lastPosition) // Seek to last position
mediaPlayer.start() // Start playing
isPlayingVoice = true
imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(
context,
R.drawable.pause_icon
)
) // Change icon to pause
startAudioProgressUpdate() // Start updating progress again
}
}
adapter.currentlyPlayingHolder = this // Set the current holder to this
}
}

private fun getAudioDuration(audioUrl: String, callback: (Int) -> Unit) {

// Check if the mediaPlayer is already initialized
if (!::mediaPlayer.isInitialized) {
mediaPlayer = MediaPlayer() // Initialize if not already done
} else {
mediaPlayer.reset() // Reset the MediaPlayer before reusing
}

try {
mediaPlayer.setDataSource(audioUrl) // Set the data source to the audio URL
mediaPlayer.prepareAsync() // Prepare asynchronously

mediaPlayer.setOnPreparedListener {
// Once prepared, get the duration
val duration = mediaPlayer.duration // Duration in milliseconds
callback(duration) // Return the duration using the callback
mediaPlayer.release() // Release the media player resources
}

mediaPlayer.setOnErrorListener { mp, what, extra ->
mp.release() // Release the media player resources on error
false // Return false to indicate the error was handled
}
} catch (e: Exception) {
mediaPlayer.release() // Release resources on exception
}
}

// Function to format milliseconds into a time string (mm:ss)
private fun formatTime(milliseconds: Int): String {
val seconds = (milliseconds / 1000) % 60
val minutes = (milliseconds / (1000 * 60)) % 60
return String.format("%02d:%02d", minutes, seconds)
}

private fun stopAudioPlayback() {
if (::mediaPlayer.isInitialized) {
if (mediaPlayer.isPlaying) {
mediaPlayer.stop() // Stop playback
mediaPlayer.reset() // Reset media player
}
isPlayingVoice = false
lastPosition = 0 // Reset last position on stop
waveformSeekBar.progress = 0f // Reset seek bar on stop
imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(context, R.drawable.play_icon_voice)
) // Change the icon back to play
}
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
loadWaveform(audioUrl) // Load waveform samples
startAudioProgressUpdate() // Start updating progress
start() // Start playback
}
setOnCompletionListener {
stopAudioProgressUpdate()
lastPosition = 0 // Reset last position on completion
waveformSeekBar.progress = 0f // Reset seek bar on completion
isPlayingVoice = false // Update playback state
imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(
context,
R.drawable.play_icon_voice
)
) // Change icon back to play on completion
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
imgVoicePlay.setImageDrawable(
ContextCompat.getDrawable(context, R.drawable.play_icon_voice)
) // Reset icon when stopping
}

private fun getDummyWaveSample(): IntArray {
return IntArray(50) { Random().nextInt(50) }
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
 */
