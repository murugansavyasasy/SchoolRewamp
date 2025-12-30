package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivitySubjectNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.OnActivityExamSelectListener
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ActivityExamListAdapter(
    private var examList: List<getActivitySubjectNameData>,
    private val context: Context,
    private val listener: OnActivityExamSelectListener,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.activity_exam_list_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_exam_list_item, parent, false)
            ExamViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 6 else examList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ExamViewHolder)
            holder.bind(examList[position], position)
        else if (holder is ShimmerViewHolder)
            holder.startShimmer()
    }

    fun updateData(newList: List<getActivitySubjectNameData>) {
        examList = newList
        isLoading = false
        notifyDataSetChanged()
    }


    inner class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.examTitle)
        private val lblMap: TextView = itemView.findViewById(R.id.lblMap)
        private val arrow: ImageView = itemView.findViewById(R.id.arrow)
        private val subjectsRv: RecyclerView = itemView.findViewById(R.id.rcSubject)

        //        private val header: RelativeLayout = itemView.findViewById(R.id.Header)
        private val lnrUploadImage: LinearLayout = itemView.findViewById(R.id.lnrUploadImage)

        private val cardUploadImage: CardView = itemView.findViewById(R.id.cardUploadImage)
        private val viewDiv: View = itemView.findViewById(R.id.viewDiv)

        fun bind(item: getActivitySubjectNameData, position: Int) {

            title.text = item.subject
            val total = item.paper.size
            val selectedCount = item.paper.count { !it.selectedValue.isNullOrEmpty() }
            applyParentColor(this, selectedCount, total)

            subjectsRv.layoutManager = LinearLayoutManager(context)
            subjectsRv.adapter = ActivitySubjectListAdapter(item.paper, context) {
                val total = item.paper.size
                val selectedCount = item.paper.count { !it.selectedValue.isNullOrEmpty() }
                applyParentColor(this, selectedCount, total) // update UI instantly without notify
            }

            // ---------- EXPAND STATE ----------
            val isExpanded = position == expandedPosition

            subjectsRv.visibility = if (isExpanded) View.VISIBLE else View.GONE
            viewDiv.visibility = if (isExpanded) View.VISIBLE else View.GONE
            arrow.rotation = if (isExpanded) 180f else 0f



            cardUploadImage.setOnClickListener {

                val previouslyExpanded = expandedPosition
                expandedPosition = if (expandedPosition == position) -1 else position

                notifyItemChanged(position)
                if (previouslyExpanded != -1 && previouslyExpanded != position) {
                    notifyItemChanged(previouslyExpanded)
                }
                listener.onActivityExamSelected(item)
            }

        }

        fun Context.dp(value: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                this.resources.displayMetrics
            ).toInt()
        }


        private fun applyParentColor(holder: ExamViewHolder, selectedCount: Int, total: Int) {
            val bg = lnrUploadImage.background as GradientDrawable
            bg.mutate()
            when {
                selectedCount == 0 -> { // ORIGINAL

                    bg.setStroke(
                        context.dp(2),
                        ContextCompat.getColor(context, android.R.color.white)
                    )
                    bg.setColor(ContextCompat.getColor(context, R.color.white))
                    cardUploadImage.cardElevation = context.dp(5).toFloat()

                    holder.lblMap.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.very_dark_gray2
                        )
                    )
//                    holder.header.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    lblMap.text = context.getString(R.string.not_started)


                }

                selectedCount < total -> {           // YELLOW
                    bg.setStroke(
                        context.dp(2),
                        ContextCompat.getColor(context, R.color.light_bg_orange_6)
                    )
                    bg.setColor(ContextCompat.getColor(context, R.color.light_bg_orange_5))

                    cardUploadImage.cardElevation = 0f

                    holder.lblMap.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.light_bg_orange_6
                        )
                    )
//                    holder.header.setBackgroundColor(ContextCompat.getColor(context, R.color.light_bg_orange_5))
                    lblMap.text = "${selectedCount} ${context.getString(R.string.of)} ${total} ${
                        context.getString(R.string.activities_mapped)
                    }"

                }

                selectedCount == total -> {          // GREEN
                    bg.setStroke(
                        context.dp(2),
                        ContextCompat.getColor(context, R.color.dark_green_3)
                    )
                    bg.setColor(ContextCompat.getColor(context, R.color.light_pale_green_1))

                    cardUploadImage.cardElevation = 0f


//                    holder.header.setBackgroundColor(ContextCompat.getColor(context, R.color.light_pale_green_1))
                    holder.lblMap.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.dark_green_3
                        )
                    )
                    lblMap.text =
                        "${context.getString(R.string.all)} ${total} ${context.getString(R.string.activities_mapped)}"

                }
            }
        }


    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}


//package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.RelativeLayout
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivitySubjectNameData
//import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.OnActivityExamSelectListener
//import com.vs.schoolmessenger.Utils.ShimmerUtil
//class ActivityExamListAdapter(
//    private var examList: List<getActivitySubjectNameData>,
//    private val context: Context,
//    private val listener: OnActivityExamSelectListener,
//    private var isLoading: Boolean
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//
//    private var selectedPosition = -1
//    private var expandedPosition = -1
//
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.exam_list_item)
//            ShimmerViewHolder(shimmerView)
//        } else {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.exam_list_item, parent, false)
//            ExamViewHolder(view)
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 6 else examList.size
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is ExamViewHolder)
//            holder.bind(examList[position], position)
//        else if (holder is ShimmerViewHolder)
//            holder.startShimmer()
//    }
//
//    fun updateData(newList: List<getActivitySubjectNameData>) {
//        examList = newList
//        isLoading = false
//        notifyDataSetChanged()
//    }
//
//
//
//    inner class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val title: TextView = itemView.findViewById(R.id.examTitle)
//        private val month: TextView = itemView.findViewById(R.id.examMonth)
//        private val arrow: ImageView = itemView.findViewById(R.id.arrow)
//        private val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
//        private val subjectsRv: RecyclerView = itemView.findViewById(R.id.rcSubject)
//        private val header: RelativeLayout = itemView.findViewById(R.id.Header)
//        private val viewDiv: View = itemView.findViewById(R.id.viewDiv)
//        private val leftRibbon: View = itemView.findViewById(R.id.leftRibbon)
//
//        fun bind(item: getActivitySubjectNameData, position: Int) {
//
//            title.text = item.subject
//
//            subjectsRv.layoutManager = LinearLayoutManager(context)
//            subjectsRv.adapter = ActivitySubjectListAdapter(item.paper, context){
//                notifyItemChanged(position)
//            }
//
//            val total = item.paper.size
//            val selectedCount = item.paper.count { !it.selectedValue.isNullOrEmpty() }
//            applyParentColor(this, selectedCount, total)
//
//
//            // ---------- EXPAND STATE ----------
//            val isExpanded = position == expandedPosition
//
//            subjectsRv.visibility = if (isExpanded) View.VISIBLE else View.GONE
//            viewDiv.visibility = if (isExpanded) View.VISIBLE else View.GONE
//            arrow.rotation = if (isExpanded) 180f else 0f
//
//
//            // ---------- EXPAND CLICK ----------
//            arrow.setOnClickListener {
//
//                val previouslyExpanded = expandedPosition
//
//                // toggle
//                expandedPosition = if (expandedPosition == position) -1 else position
//
//                notifyItemChanged(position)
//
//                // collapse previously expanded
//                if (previouslyExpanded != -1 && previouslyExpanded != position) {
//                    notifyItemChanged(previouslyExpanded)
//                }
//            }
//
//
////            val isSelected = position == selectedPosition
//
////            if (isSelected) {
////                imgCheck.setImageResource(R.drawable.double_circle)
////                title.setTextColor(ContextCompat.getColor(context, R.color.dark_bg_orange_2))
////                leftRibbon.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_bg_orange_2))
////                header.setBackgroundColor(ContextCompat.getColor(context, R.color.light_bg_orange_3))
////            } else {
////                imgCheck.setImageResource(R.drawable.circle_icon)
////                title.setTextColor(ContextCompat.getColor(context, R.color.black))
////                leftRibbon.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
////                header.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
////            }
//
//            header.setOnClickListener {
//
//                val previousSelected = selectedPosition
//
//                if (previousSelected == position) {
//                    // user clicked same selected item → unselect
//                    selectedPosition = -1
//                    notifyItemChanged(previousSelected)
//                    listener.onActivityExamSelected(null)   // send null to main activity
//                    return@setOnClickListener
//                }
//
//                // new item selected
//                selectedPosition = position
//                notifyItemChanged(position)
//
//                if (previousSelected != -1) {
//                    notifyItemChanged(previousSelected)
//                }
//
//                listener.onActivityExamSelected(item)
//            }
//
//        }
//        private fun applyParentColor(holder: ExamViewHolder, selectedCount: Int, total: Int) {
//
//            when {
//                selectedCount == 0 -> {              // ORIGINAL
//                    holder.imgCheck.setImageResource(R.drawable.circle_icon)
//                    holder.title.setTextColor(ContextCompat.getColor(context, R.color.black))
//                    holder.leftRibbon.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
//                    holder.header.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
//                }
//                selectedCount < total -> {           // YELLOW / ONGOING
//                    holder.imgCheck.setImageResource(R.drawable.double_circle)
//                    holder.title.setTextColor(ContextCompat.getColor(context, R.color.dark_bg_orange_2))
//                    holder.leftRibbon.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_bg_orange_2))
//                    holder.header.setBackgroundColor(ContextCompat.getColor(context, R.color.light_bg_orange_3))
//                }
//                selectedCount == total -> {          // GREEN / ALL COMPLETED
//                    holder.imgCheck.setImageResource(R.drawable.double_circle)
//                    holder.title.setTextColor(ContextCompat.getColor(context, R.color.dark_green))
//                    holder.leftRibbon.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_green))
//                    holder.header.setBackgroundColor(ContextCompat.getColor(context, R.color.light_green))
//                }
//            }
//        }
//
//
//
//    }
//
//    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun startShimmer() {
//            ShimmerUtil.startShimmer(itemView)
//        }
//    }
//}
