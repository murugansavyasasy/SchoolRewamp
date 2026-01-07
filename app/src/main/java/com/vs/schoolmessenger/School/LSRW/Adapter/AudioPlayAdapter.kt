package com.vs.schoolmessenger.School.LSRW.Adapter

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class AudioPlayAdapter(
    private val context: Context,
    private val items: MutableList<AudioFile>
) : RecyclerView.Adapter<AudioPlayAdapter.AudioViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var lastPlayingPos = -1
    private var updateSeekBarHandler = Handler(Looper.getMainLooper())

    class AudioViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imgPlayPause: ImageView = v.findViewById(R.id.imgPlayPause)
        val seekBar: SeekBar = v.findViewById(R.id.seekBar)
        val txtDuration: TextView = v.findViewById(R.id.txtDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio_message, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val item = items[position]

        holder.seekBar.progress = 0
        holder.imgPlayPause.setImageResource(
            if (item.isPlaying) R.drawable.pause_icon else R.drawable.play_icon_voice
        )

        holder.imgPlayPause.setOnClickListener {
            togglePlay(position, holder)
        }
    }

    private fun togglePlay(position: Int, holder: AudioViewHolder) {
        val item = items[position]

        // Stop previous audio
        if (lastPlayingPos != -1 && lastPlayingPos != position) {
            stopAudio(lastPlayingPos)
            notifyItemChanged(lastPlayingPos)
        }

        // Play or pause current
        if (item.isPlaying) {
            pauseAudio(position)
            holder.imgPlayPause.setImageResource(R.drawable.play_icon_voice)
        } else {
            playAudio(position, holder)
            holder.imgPlayPause.setImageResource(R.drawable.pause_icon)
        }
    }

    private fun playAudio(position: Int, holder: AudioViewHolder) {
        val item = items[position]

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(item.filePath)
            prepare()
            start()
        }

        item.isPlaying = true
        lastPlayingPos = position
        holder.seekBar.max = mediaPlayer!!.duration

        updateSeekBar(position, holder)
        mediaPlayer!!.setOnCompletionListener {
            stopAudio(position)
            notifyItemChanged(position)
        }
    }

    private fun pauseAudio(position: Int) {
        mediaPlayer?.pause()
        items[position].isPlaying = false
    }

    private fun stopAudio(position: Int) {
        items[position].isPlaying = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun updateSeekBar(position: Int, holder: AudioViewHolder) {
        updateSeekBarHandler.post(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (items[position].isPlaying) {
                        holder.seekBar.progress = it.currentPosition
                        holder.txtDuration.text = formatTime(it.currentPosition)
                        updateSeekBarHandler.postDelayed(this, 300)
                    }
                }
            }
        })
    }

    private fun formatTime(ms: Int): String {
        val sec = ms / 1000
        val m = sec / 60
        val s = sec % 60
        return String.format("%02d:%02d", m, s)
    }

    override fun getItemCount(): Int = items.size
}
