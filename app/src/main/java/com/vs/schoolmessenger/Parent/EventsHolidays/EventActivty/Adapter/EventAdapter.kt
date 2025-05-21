package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventDataClass
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Adapter.ShimmerViewHolder
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class EventAdapter (
    private var itemList: List<EventDataClass>?,
    private var listener: EventClickListener,
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
                    .inflate(R.layout.homeword_report_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)

        private val RcyImgPdf: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        var meventAdapter: EventFilePathAdapter? = null

        private fun getRecyclerView(): RecyclerView {
            return RcyImgPdf
        }



        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: EventDataClass,
            position: Int,
            listener: EventClickListener,
            adapter: EventAdapter
        ) {

            val eventImgPdf = getRecyclerView()
            lblTitleImage.text = data.title
            lblContentImage.text = data.content
            lblDateImage.text = data.date

            if (data.file_path.size > 0) {
                RcyImgPdf.visibility = View.VISIBLE
            } else {
                RcyImgPdf.visibility = View.GONE
            }

            meventAdapter =
                EventFilePathAdapter(null, context, Constant.isShimmerViewShow)
            eventImgPdf.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            eventImgPdf.adapter = meventAdapter


            meventAdapter =
                EventFilePathAdapter(
                    data.file_path,
                    context,
                    Constant.isShimmerViewDisable

                )
            eventImgPdf.adapter = meventAdapter
        }
    }
}