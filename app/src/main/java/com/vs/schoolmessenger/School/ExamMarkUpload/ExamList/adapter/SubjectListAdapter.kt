package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getSubjectWiseACtivitiesData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SubjectListAdapter(
    private var subjects: List<getSubjectWiseACtivitiesData>?,   // 🔥 supports null for shimmer
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var expandedIndex = -1

    override fun getItemViewType(position: Int): Int {
        return if (subjects == null) TYPE_SHIMMER else TYPE_DATA
    }

    override fun getItemCount(): Int {
        return subjects?.size ?: 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.subject_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.subject_item, parent, false)
            SubjectViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ShimmerViewHolder)
            holder.startShimmer()
        else if (holder is SubjectViewHolder)
            subjects?.get(position)?.let { holder.bind(it, position) }
    }

    //  Called by ExamListAdapter when API result arrives
    fun updateData(newList: List<getSubjectWiseACtivitiesData>?) {
        subjects = newList
        notifyDataSetChanged()
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() = ShimmerUtil.startShimmer(itemView)
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
        private val flexActivities: FlexboxLayout = itemView.findViewById(R.id.flexActivities)
        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
        private val subjectHeader: LinearLayout = itemView.findViewById(R.id.subHeader)
        private val lblNoData: TextView = itemView.findViewById(R.id.lblNoData)
        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)

        fun bind(item: getSubjectWiseACtivitiesData, position: Int) {

            subjectName.text = item.subject_name
            flexActivities.removeAllViews()

            if (item.splitup_details.isNotEmpty()) {
                item.splitup_details?.forEach { act ->
                    val chip = LayoutInflater.from(context)
                        .inflate(R.layout.activity_item, flexActivities, false) as TextView
                    chip.text = act.name
                    flexActivities.addView(chip)
                }
                lnrFlexContainer.visibility = View.VISIBLE
                lblNoData.visibility = View.GONE
            } else {
                lnrFlexContainer.visibility = View.GONE
                lblNoData.visibility = View.VISIBLE
            }

            val isExpanded = expandedIndex == position
            lnrFlexContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            subArrow.rotation = if (isExpanded) 90f else 0f

            subjectHeader.setOnClickListener {
                val prev = expandedIndex
                expandedIndex = if (expandedIndex == position) -1 else position

                if (prev != -1) notifyItemChanged(prev)
                notifyItemChanged(position)
            }
        }
    }
}