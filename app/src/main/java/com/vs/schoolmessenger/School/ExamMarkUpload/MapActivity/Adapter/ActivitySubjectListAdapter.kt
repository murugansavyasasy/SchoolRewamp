package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.getSubjectData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivitySubjectData

class ActivitySubjectListAdapter(
    private val subjects: List<getActivitySubjectData>,
    private val context: Context,
) : RecyclerView.Adapter<ActivitySubjectListAdapter.SubjectViewHolder>() {

    private var expandedSubjectPos = -1   // local expand index

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.subject_item, parent, false)
        return SubjectViewHolder(view)
    }

    override fun getItemCount(): Int = subjects.size

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjects[position], position)
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
        private val flexActivities: FlexboxLayout = itemView.findViewById(R.id.flexActivities)
        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
        private val subjectHeader: LinearLayout = itemView.findViewById(R.id.subHeader)
        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)

        fun bind(item: getActivitySubjectData, position: Int) {

            subjectName.text = item.name
            flexActivities.removeAllViews()

            item.activities.forEach { act ->
                val chip = LayoutInflater.from(context)
                    .inflate(R.layout.activity_item, flexActivities, false) as TextView
                chip.text = act
                flexActivities.addView(chip)
            }

            // --- Determine if this item should be expanded ---
            val isExpanded = position == expandedSubjectPos

            lnrFlexContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE

            subArrow.rotation = if (isExpanded) 90f else 0f

            // --- Click to expand/collapse ---
            subjectHeader.setOnClickListener {

                val prev = expandedSubjectPos

                expandedSubjectPos =
                    if (expandedSubjectPos == position) -1     // collapse current
                    else position                               // expand new

                // Refresh old expanded row
                if (prev != -1) notifyItemChanged(prev)

                // Refresh newly expanded row
                notifyItemChanged(position)
            }

            subArrow.setColorFilter(
                ContextCompat.getColor(context, R.color.dark_orange_2),
                PorterDuff.Mode.SRC_IN
            )
        }
    }
}
