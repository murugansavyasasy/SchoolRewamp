package com.vs.schoolmessenger.School.PTM.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Locale
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotClickListener
import com.vs.schoolmessenger.Utils.ShimmerUtil

class UpComingSlotAdapter(
    private var itemList: ArrayList<SlotDetail>? = null,
    private var listener: StaffSlotClickListener,
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
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.slots_item_staff_side)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.slots_item_staff_side, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && itemList != null) {
            holder.bind(itemList!![position], position, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun updateData(newList: ArrayList<SlotDetail>, loading: Boolean) {
        this.itemList = newList
        this.isLoading = loading
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblMode: TextView = itemView.findViewById(R.id.lblMode)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val rytSlots: RelativeLayout = itemView.findViewById(R.id.rytSlots)
        private val rltJoinNow: RelativeLayout = itemView.findViewById(R.id.rltJoinNow)


        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: SlotDetail, position: Int, listener: StaffSlotClickListener) {
            lblTitle.text = data.event_name
            lblMode.text = "Mode - ${data.event_mode}"

            val formattedDate = try {
                val possibleFormats = listOf("yyyy-MM-dd", "dd-MM-yyyy", "MM/dd/yyyy", "yyyy/MM/dd")
                var parsedDate: java.util.Date? = null
                for (format in possibleFormats) {
                    try {
                        val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                        parsedDate = inputFormat.parse(data.date)
                        if (parsedDate != null) break
                    } catch (_: Exception) {
                    }
                }

                if (parsedDate != null) {
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    outputFormat.format(parsedDate)
                } else {
                    data.date
                }
            } catch (e: Exception) {
                data.date
            }

            lblDate.text = formattedDate

            lblDate.text = formattedDate

            lblTime.text = "${data.start_time} - ${data.end_time}"

            rytSlots.background = context.resources.getDrawable(R.drawable.bg_gradient)

            rytSlots.setOnClickListener {
                listener.onClickListener(data)
            }

            rltJoinNow.setOnClickListener {
                val url = data.join_url ?: "https://www.google.com"
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse(url)
                context.startActivity(intent)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
