package com.vs.schoolmessenger.Dashboard.Settings.Faq

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Dashboard.Settings.Faq.Model.FaqItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.SchoolStrength.Adapter.SchoolStrengthAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.Utils.ShimmerUtil

class FaqAdapter(
    private var itemList: List<FaqItem>,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var expandedPosition = -1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.faq_report)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.faq_report, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val question = itemView.findViewById<TextView>(R.id.question1)
        private val answer = itemView.findViewById<TextView>(R.id.answer1)
        private val arrow = itemView.findViewById<ImageView>(R.id.arrow_icon_1)

        @SuppressLint("SetTextI18n")
        fun bind(data: FaqItem, position: Int) {

            question.text = data.question
            val ans = if (data.answer.isNotEmpty()) data.answer[0] else ""
            answer.text = ans
            val isExpanded = position == expandedPosition
            answer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            arrow.rotation = if (isExpanded) 180f else 0f

            itemView.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else position
                notifyDataSetChanged()
            }

            arrow.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else position
                notifyDataSetChanged()
            }
        }
    }
}
