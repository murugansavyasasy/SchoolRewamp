package com.vs.schoolmessenger.School.SchoolStrength.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.SchoolStrength.Model.Standard
import com.vs.schoolmessenger.Utils.ShimmerUtil


class SchoolStrengthAdapter(
    private var itemList: List<Standard>,
    private var context: Context,
    private var isLoading: Boolean,


    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var expandedPosition = -1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.schoolstrength_report)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.schoolstrength_report, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position, this)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val boyslabel: TextView = itemView.findViewById(R.id.boyslabel)
        private val girlslabel: TextView = itemView.findViewById(R.id.girlslabel)
        private val totallabel: TextView = itemView.findViewById(R.id.totallabel)
        private val header1: TextView = itemView.findViewById(R.id.header1)
        private val btnViewDetails: TextView = itemView.findViewById(R.id.btnViewDetails)
        private val viewBoys: View = itemView.findViewById(R.id.viewBoys)
        private val boysl1abel1: ImageView = itemView.findViewById(R.id.boysl1abel1)
        private val viewGirls: View = itemView.findViewById(R.id.viewGirls)


        @SuppressLint("SetTextI18n")
        fun bind(
            data: Standard,
            position: Int,
            adapter: SchoolStrengthAdapter
        ) {
            boyslabel.text = "Boys : ${data.boys_count}"
            girlslabel.text = "Girls : ${data.girls_count}"
            totallabel.text = "Total Students : ${data.total_students}"
            header1.text = "Standard" + " " + data.name

            if (data.girls_count == "0") {
                boysl1abel1.visibility = View.GONE
            } else {
                boysl1abel1.visibility = View.VISIBLE
            }

            val boysCount = data.boys_count.toIntOrNull() ?: 0
            val girlsCount = data.girls_count.toIntOrNull() ?: 0
            val totalStudents = data.total_students.toIntOrNull() ?: 0

            if (totalStudents > 0) {
                val boysWeight = boysCount.toFloat() / totalStudents
                val girlsWeight = girlsCount.toFloat() / totalStudents

                val layoutBoys = viewBoys.layoutParams as LinearLayout.LayoutParams
                val layoutGirls = viewGirls.layoutParams as LinearLayout.LayoutParams

                layoutBoys.weight = boysWeight
                layoutGirls.weight = girlsWeight

                viewBoys.layoutParams = layoutBoys
                viewGirls.layoutParams = layoutGirls
            } else {
                viewBoys.layoutParams.width = 0
                viewGirls.layoutParams.width = 0
            }

            val detailRecyclerView: RecyclerView = itemView.findViewById(R.id.rlaabsenteesreport3)
            val expandableLayout: LinearLayout = itemView.findViewById(R.id.linear_layout2)

            detailRecyclerView.layoutManager = LinearLayoutManager(context)


            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            detailRecyclerView.addItemDecoration(dividerItemDecoration)

            val detailAdapter = SchoolStrengthDetailAdapter(data.sections, context, false)
            detailRecyclerView.adapter = detailAdapter

            detailRecyclerView.visibility =
                if (adapter.expandedPosition == position) View.VISIBLE else View.GONE

            btnViewDetails.setOnClickListener {
                val previousExpandedPosition = adapter.expandedPosition

                if (adapter.expandedPosition == position) {
                    adapter.expandedPosition = -1
                    adapter.notifyItemChanged(position)
                } else {
                    adapter.expandedPosition = position
                    adapter.notifyItemChanged(previousExpandedPosition)
                    adapter.notifyItemChanged(position)
                }
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}