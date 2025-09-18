package com.vs.schoolmessenger.School.AbsenteesReport.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesStudents
import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesReportAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesDetailClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil


class AbsenteesReportDetailAdapter(
    private var itemList: List<ClassWise>?,
    private var listener: AbsenteesDetailClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private val selectedDate: String,
    private var class_name: String? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectedPosition = 0

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.absentees_detail_list)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.absentees_detail_list, parent, false)
            DataViewHolder(view, context)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener, this, selectedDate)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20
        else itemList?.size ?: 0
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val classvalue: TextView = itemView.findViewById(R.id.classvalue)
        private val sectionvalue: TextView = itemView.findViewById(R.id.sectionvalue)
        private val absentvalue: TextView = itemView.findViewById(R.id.absentvalue)

        fun bind(
            data: ClassWise,
            position: Int,
            listener: AbsenteesDetailClickListener,
            adapter: AbsenteesReportDetailAdapter,
            selectedDate: String
        ) {
            classvalue.text = "Class : "+data.class_name
            sectionvalue.text = data.section_wise[0].section_name
            absentvalue.text = data.total_absentees + " / " + data.student_counts

        }


        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}
