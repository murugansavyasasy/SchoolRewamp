package com.vs.schoolmessenger.Parent.Video

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.fetchVimeoThumbnail

class VideoListAdapter(
    private val itemList: List<VideoData>,
    private val listener: VideoOnItemClickListener,
    context: Context
) : RecyclerView.Adapter<VideoListAdapter.GridViewHolder>() {

    private var isTextExpanded = false

    inner class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        val imgVimeoThumbnail: ImageView = itemView.findViewById(R.id.imgVimeoThumbnail)
        val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        val lblVideoContent: TextView = itemView.findViewById(R.id.lblVideoContent)
        val tvSeeMore: TextView = itemView.findViewById(R.id.tvSeeMore)
        val customProgressBar: ProgressBar = itemView.findViewById(R.id.customProgressBar)
        val imgVideoPlay: ImageView = itemView.findViewById(R.id.imgVideoPlay)
        val rlaVimeoThumbnail: RelativeLayout = itemView.findViewById(R.id.rlaVimeoThumbnail)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(itemList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_list, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = itemList[position]

        holder.customProgressBar.visibility = View.VISIBLE
        holder.imgVideoPlay.visibility = View.GONE

        holder.lblDate.text = item.date
        holder.lblVideoContent.text = item.content
        holder.lblTitle.text = item.title

        holder.lblVideoContent.post {
            if (holder.lblVideoContent.lineCount > 3) {
                holder.tvSeeMore.visibility = View.VISIBLE
                holder.lblVideoContent.maxLines = 3
                holder.lblVideoContent.ellipsize = TextUtils.TruncateAt.END
            }
        }

        holder.tvSeeMore.setOnClickListener {
            if (isTextExpanded) {
                holder.lblVideoContent.maxLines = 3
                holder.lblVideoContent.ellipsize = TextUtils.TruncateAt.END
                holder.tvSeeMore.text = R.string.SeeMore.toString()
            } else {
                holder.lblVideoContent.maxLines = Integer.MAX_VALUE
                holder.lblVideoContent.ellipsize = null
                holder.tvSeeMore.text = R.string.SeeLess.toString()
            }
            isTextExpanded = !isTextExpanded
        }


        fetchVimeoThumbnail("https://vimeo.com/${item.videoId}") { thumbnailUrl ->
            if (thumbnailUrl != null) {
                Glide.with(holder.imgVimeoThumbnail.context)
                    .load(thumbnailUrl)
                    .into(holder.imgVimeoThumbnail)
                holder.imgVideoPlay.visibility = View.VISIBLE
                holder.customProgressBar.visibility = View.GONE
            }
        }
        holder.rlaVimeoThumbnail.setOnClickListener {
            listener.onItemClick(item)
        }
    }

    override fun getItemCount(): Int = itemList.size
}
