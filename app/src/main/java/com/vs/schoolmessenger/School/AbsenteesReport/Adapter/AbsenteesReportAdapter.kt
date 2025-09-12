package com.vs.schoolmessenger.School.AbsenteesReport.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AbsenteesReportAdapter(
    private var itemList: List<AbsenteeData>,
    private var listener: AbsenteesClickListener,
    private var context: Context,
    private var isLoading: Boolean,

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectedPosition = 0

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.absentees_date_list)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.absentees_date_list, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position, listener, this)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val daytextview: TextView = itemView.findViewById(R.id.daytextview)
        private val datetextview: TextView = itemView.findViewById(R.id.datetextview)
        private val monthtextview: TextView = itemView.findViewById(R.id.monthtextview)
        private val linearLayout1: LinearLayout = itemView.findViewById(R.id.linear_layout1)
        private val totalcount: TextView = itemView.findViewById(R.id.totalcount)

        fun bind(
            data: AbsenteeData,
            position: Int,
            listener: AbsenteesClickListener,
            adapter: AbsenteesReportAdapter
        ) {
            daytextview.text = data.day

            val inputFormat = SimpleDateFormat(Constant.ddMMyyyy, Locale.getDefault())
            val dateObject: Date? = inputFormat.parse(data.date)

            dateObject?.let {
                val dayFormat = SimpleDateFormat(Constant.dd, Locale.getDefault())
                val monthFormat = SimpleDateFormat(Constant.MMMM, Locale.getDefault())

                datetextview.text = dayFormat.format(it)
                monthtextview.text = monthFormat.format(it)
            }
            totalcount.text = data.total_absentees

            if (adapter.selectedPosition == position) {
                linearLayout1.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.custom_blue
                    )
                )
                datetextview.setTextColor(ContextCompat.getColor(context, R.color.black))
                daytextview.setTextColor(ContextCompat.getColor(context, R.color.black))
            } else {
                linearLayout1.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                datetextview.setTextColor(ContextCompat.getColor(context, R.color.grey))
                daytextview.setTextColor(ContextCompat.getColor(context, R.color.grey))
            }

            itemView.setOnClickListener {
                adapter.setSelectedPosition(position)
                listener.onDateSelected(data)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}