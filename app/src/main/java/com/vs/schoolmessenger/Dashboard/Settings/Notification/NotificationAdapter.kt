package com.vs.schoolmessenger.Dashboard.Settings.Notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(private val itemList: List<NotificationDataClass>) :
    RecyclerView.Adapter<NotificationAdapter.GridViewHolder>() {

    inner class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgRoundCard: CircleImageView = itemView.findViewById(R.id.imgRoundCard)
        val lblSendBy: TextView = itemView.findViewById(R.id.lblSendBy)
        val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        val lblContent: TextView = itemView.findViewById(R.id.lblContent)
        val lblView: TextView = itemView.findViewById(R.id.lblView)
        val lblNotification: TextView = itemView.findViewById(R.id.lblNotification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.resent_notifications, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = itemList[position]

        when (position) {
            1 -> {
                holder.lblNotification.visibility=View.VISIBLE
                holder.imgRoundCard.setImageResource(R.drawable.voice)
            }
            2 -> {
                holder.lblNotification.visibility=View.VISIBLE
                holder.imgRoundCard.setImageResource(R.drawable.phone_icon)

            }
            3 -> {
                holder.lblNotification.visibility=View.GONE
                holder.imgRoundCard.setImageResource(R.drawable.mail_icon)

            }
            4 -> {
                holder.lblNotification.visibility=View.VISIBLE
                holder.imgRoundCard.setImageResource(R.drawable.text_notification)

            }
            5 -> {
                holder.lblNotification.visibility=View.GONE
                holder.imgRoundCard.setImageResource(R.drawable.voice)

            }
            6 -> {
                holder.lblNotification.visibility=View.GONE
                holder.imgRoundCard.setImageResource(R.drawable.phone_icon)

            }
            7 -> {
                holder.lblNotification.visibility=View.VISIBLE
                holder.imgRoundCard.setImageResource(R.drawable.mail_icon)

            }
            8 -> {
                holder.lblNotification.visibility=View.GONE
                holder.imgRoundCard.setImageResource(R.drawable.text_notification)

            }
            9 -> {
                holder.lblNotification.visibility=View.VISIBLE
                holder.imgRoundCard.setImageResource(R.drawable.voice)

            }

        }



        holder.lblSendBy.text = item.sendBy
        holder.lblTitle.text = item.title
        holder.lblContent.text = item.content
    }

    override fun getItemCount(): Int = itemList.size
}