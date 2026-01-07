package com.vs.schoolmessenger.Parent.LSRW

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.AudioItemBinding

class AudioAdapter(
    private val audioList: List<String>
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    private var currentlyPlayingPos = -1
    private var previousHolder: AudioViewHolder? = null

    class AudioViewHolder(val binding: AudioItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = AudioItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val url = audioList[position]

        resetUI(holder)

        getAudioDuration(url) { duration ->
            holder.binding.lblCurrentDuration.text = duration
            holder.binding.audioSeekBar.max = parseToSeconds(duration)
        }

        holder.binding.imgVoicePlay.setOnClickListener {
            if (currentlyPlayingPos == position) {
                pauseCurrent(holder)
            } else {
                playAudio(url, holder, position)
            }
        }

        holder.binding.audioSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer != null && currentlyPlayingPos == position) {
                    mediaPlayer?.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun getItemCount(): Int = audioList.size

    // ---------------- PLAYBACK --------------------

    private fun playAudio(url: String, holder: AudioViewHolder, position: Int) {

        // 🔥 STOP PREVIOUS PLAYING AUDIO + RESET UI
        if (previousHolder != null && currentlyPlayingPos != -1) {
            resetUI(previousHolder!!)
        }
        releasePlayer()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepare()
            start()
            setOnCompletionListener {
                resetUI(holder)
                currentlyPlayingPos = -1
                releasePlayer()
            }
        }

        currentlyPlayingPos = position
        previousHolder = holder

        holder.binding.imgVoicePlay.setImageResource(R.drawable.pause_icon)
        startProgressUpdater(holder)
    }

    private fun startProgressUpdater(holder: AudioViewHolder) {
        runnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    val sec = it.currentPosition / 1000
                    holder.binding.lblTime.text = formatDuration(sec)
                    holder.binding.audioSeekBar.progress = sec
                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(runnable!!)
    }

    private fun pauseCurrent(holder: AudioViewHolder) {
        mediaPlayer?.pause()
        holder.binding.imgVoicePlay.setImageResource(R.drawable.video_play)
        runnable?.let { handler.removeCallbacks(it) }
        currentlyPlayingPos = -1
    }

    private fun releasePlayer() {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = null

        mediaPlayer?.release()
        mediaPlayer = null
    }

    // ---------------- UTIL --------------------

    private fun getAudioDuration(url: String, callback: (String) -> Unit) {
        try {
            val temp = MediaPlayer()
            temp.setDataSource(url)
            temp.setOnPreparedListener {
                val sec = it.duration / 1000
                callback(formatDuration(sec))
                it.release()
            }
            temp.prepareAsync()
        } catch (e: Exception) {
            callback("00:00")
        }
    }

    private fun formatDuration(seconds: Int): String =
        String.format("%02d:%02d", seconds / 60, seconds % 60)

    private fun parseToSeconds(duration: String): Int {
        val parts = duration.split(":")
        return if (parts.size == 2) (parts[0].toInt() * 60) + parts[1].toInt() else 0
    }

    private fun resetUI(holder: AudioViewHolder) {
        holder.binding.imgVoicePlay.setImageResource(R.drawable.video_play)
        holder.binding.audioSeekBar.progress = 0
        holder.binding.lblTime.text = "00:00"
    }

    fun release() {
        releasePlayer()
    }
}