package com.vs.schoolmessenger.Parent.LSRW

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.AudioItemBinding

class AudioAdapter(
    private val audioList: List<String>
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var currentlyPlayingPos: Int = -1

    inner class AudioViewHolder(val binding: AudioItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = AudioItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val url = audioList[position]
        Log.d("isComingAdapter", "isComingAdapter")
        resetUI(holder)


        getAudioDuration(url) { duration ->
            holder.binding.lblTime.text = duration
            holder.binding.audioSeekBar.max = parseToSeconds(duration)
        }


        holder.binding.imgVoicePlay.setOnClickListener {
            if (currentlyPlayingPos == position) {
                pauseAudio(holder)
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

    private fun playAudio(url: String, holder: AudioViewHolder, position: Int) {
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
        holder.binding.imgVoicePlay.setImageResource(R.drawable.pause_icon)

        runnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    val currentSec = it.currentPosition / 1000
                    holder.binding.audioSeekBar.progress = currentSec
                    holder.binding.lblCurrentDuration.text = formatDuration(currentSec)
                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(runnable!!)
    }

    private fun pauseAudio(holder: AudioViewHolder) {
        mediaPlayer?.pause()
        holder.binding.imgVoicePlay.setImageResource(R.drawable.video_play)
        currentlyPlayingPos = -1
        runnable?.let { handler.removeCallbacks(it) }
    }

    private fun releasePlayer() {
        runnable?.let { handler.removeCallbacks(it) }
        mediaPlayer?.release()
        mediaPlayer = null
        currentlyPlayingPos = -1
    }

    private fun getAudioDuration(url: String, callback: (String) -> Unit) {
        try {
            val tempPlayer = MediaPlayer()
            tempPlayer.setDataSource(url)
            tempPlayer.setOnPreparedListener {
                val durationInSec = it.duration / 1000
                callback(formatDuration(durationInSec))
                it.release()
            }
            tempPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            callback(Constant.time_zero)
        }
    }

    private fun formatDuration(seconds: Int): String {
        return String.format(Constant.dateForMate, seconds / 60, seconds % 60)
    }

    private fun parseToSeconds(duration: String): Int {
        val parts = duration.split(":")
        return if (parts.size == 2) parts[0].toInt() * 60 + parts[1].toInt() else 0
    }

    private fun resetUI(holder: AudioViewHolder) {
        holder.binding.imgVoicePlay.setImageResource(R.drawable.video_play)
        holder.binding.audioSeekBar.progress = 0
        holder.binding.lblCurrentDuration.text = "00:00"
    }

    fun release() {
        releasePlayer()
    }
}
