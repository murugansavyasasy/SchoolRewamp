package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
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
    fun updateData(newList: List<getSubjectWiseACtivitiesData>) {
        subjects = newList
        notifyDataSetChanged()
    }


    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

            if(item.splitup_details.isNotEmpty()){
                item.splitup_details?.forEach { act ->
                    val chip = LayoutInflater.from(context)
                        .inflate(R.layout.activity_item, flexActivities, false) as TextView
                    chip.text = act.name
                    flexActivities.addView(chip)
                }
                lnrFlexContainer.visibility= View.VISIBLE
                lblNoData.visibility= View.GONE
            }
            else{
                lnrFlexContainer.visibility= View.GONE
                lblNoData.visibility= View.VISIBLE
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


//package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.adapter
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.flexbox.FlexboxLayout
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getSubjectWiseACtivitiesData
//
//class SubjectListAdapter(
//    private val subjects: List<getSubjectWiseACtivitiesData>,
//    private val context: Context
//) : RecyclerView.Adapter<SubjectListAdapter.SubjectViewHolder>() {
//
//    private var expandedIndex = -1
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.subject_item, parent, false)
//        return SubjectViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = subjects.size
//
//    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
//        holder.bind(subjects[position], position)
//    }
//
//    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
//        private val flexActivities: FlexboxLayout = itemView.findViewById(R.id.flexActivities)
//        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
//        private val subjectHeader: LinearLayout = itemView.findViewById(R.id.subHeader)
//        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)
//
//        fun bind(item: getSubjectWiseACtivitiesData, position: Int) {
//
//            subjectName.text = item.subject_name
//            flexActivities.removeAllViews()
//
//            item.split_details?.forEach { act ->
//                val chip = LayoutInflater.from(context)
//                    .inflate(R.layout.activity_item, flexActivities, false) as TextView
//                chip.text = act.name
//                flexActivities.addView(chip)
//            }
//
//            val isExpanded = position == expandedIndex
//            lnrFlexContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
//            subArrow.rotation = if (isExpanded) 90f else 0f
//
//            subjectHeader.setOnClickListener {
//                val prev = expandedIndex
//                expandedIndex = if (expandedIndex == position) -1 else position
//
//                if (prev != -1) notifyItemChanged(prev)
//                notifyItemChanged(position)
//            }
//
//            subArrow.setColorFilter(
//                ContextCompat.getColor(context, R.color.dark_orange_2),
//                android.graphics.PorterDuff.Mode.SRC_IN
//            )
//        }
//    }
//}

//package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.adapter
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.flexbox.FlexboxLayout
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getSubjectWiseACtivitiesData
//import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.getSubjectData
//
//class SubjectListAdapter(
//    private val subjects: List<getSubjectWiseACtivitiesData>,
//    private val context: Context
//) : RecyclerView.Adapter<SubjectListAdapter.SubjectViewHolder>() {
//
//    private var expandedSubjectPos = -1   // local expand index
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.subject_item, parent, false)
//        return SubjectViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = subjects.size
//
//    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
//        holder.bind(subjects[position], position)
//    }
//
//    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
//        private val flexActivities: FlexboxLayout = itemView.findViewById(R.id.flexActivities)
//        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
//        private val subjectHeader: LinearLayout = itemView.findViewById(R.id.subHeader)
//        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)
//
//        fun bind(item: getSubjectWiseACtivitiesData, position: Int) {
//
//            subjectName.text = item.subject_name
//            flexActivities.removeAllViews()
//
//            item.split_details.forEach { act ->
//                val chip = LayoutInflater.from(context)
//                    .inflate(R.layout.activity_item, flexActivities, false) as TextView
//                chip.text = act.name
//                flexActivities.addView(chip)
//            }
//
//            // --- Determine if this item should be expanded ---
//            val isExpanded = position == expandedSubjectPos
//
//            lnrFlexContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
//
//            subArrow.rotation = if (isExpanded) 90f else 0f
//
//            // --- Click to expand/collapse ---
//            subjectHeader.setOnClickListener {
//
//                val prev = expandedSubjectPos
//
//                expandedSubjectPos =
//                    if (expandedSubjectPos == position) -1     // collapse current
//                    else position                               // expand new
//
//                // Refresh old expanded row
//                if (prev != -1) notifyItemChanged(prev)
//
//                // Refresh newly expanded row
//                notifyItemChanged(position)
//            }
//
//            subArrow.setColorFilter(
//                ContextCompat.getColor(context, R.color.dark_orange_2),
//                android.graphics.PorterDuff.Mode.SRC_IN
//            )
//        }
//    }
//}

//class SubjectListAdapter(
//    private val subjects: List<getSubjectData>,
//    private val context: Context
//) : RecyclerView.Adapter<SubjectListAdapter.SubjectViewHolder>() {
//
//    private var expandedSubjectPos = -1
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.subject_item, parent, false)
//        return SubjectViewHolder(view)
//    }
//
//    override fun getItemCount(): Int = subjects.size
//
//    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
//        holder.bind(subjects[position], position)
//    }
//
//    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
//        private val flexActivities: FlexboxLayout = itemView.findViewById(R.id.flexActivities)
//        private val subArrow: ImageView = itemView.findViewById(R.id.subArrow)
//        private val subjectHeader: LinearLayout = itemView.findViewById(R.id.subHeader)
//        private val lnrFlexContainer: LinearLayout = itemView.findViewById(R.id.lnrFlexContainer)
//
//        fun bind(item: getSubjectData, position: Int) {
//
//            subjectName.text = item.name
//
//            flexActivities.removeAllViews()
//
//            // inflate activity chips
//            item.activities.forEach { act ->
//                val chip = LayoutInflater.from(context)
//                    .inflate(R.layout.activity_item, flexActivities, false) as TextView
//                chip.text = act
//                flexActivities.addView(chip)
//            }
//
//            lnrFlexContainer.visibility =
//                if (item.isExpanded) View.VISIBLE else View.GONE
//
//            subjectHeader.setOnClickListener {
//                if (expandedSubjectPos != -1 && expandedSubjectPos != position) {
//                    subjects[expandedSubjectPos].isExpanded = false
//                    notifyItemChanged(expandedSubjectPos)
//                }
//
//                item.isExpanded = !item.isExpanded
//                expandedSubjectPos = if (item.isExpanded) position else -1
//                notifyItemChanged(position)
//            }
//
//            subArrow.setColorFilter(
//                ContextCompat.getColor(context, R.color.dark_orange_2),
//                android.graphics.PorterDuff.Mode.SRC_IN
//            )
//
//            if (item.isExpanded) {
//                subArrow.animate().rotation(90f).setDuration(200).start()
//
//
////                subArrow.setImageResource(R.drawable.down_arrow_3)   // expanded
//
//            } else {
//                subArrow.animate().rotation(0f).setDuration(200).start()
////                subArrow.setImageResource(R.drawable.right_arrow)  // collapsed
//            }
//        }
//    }
//}
