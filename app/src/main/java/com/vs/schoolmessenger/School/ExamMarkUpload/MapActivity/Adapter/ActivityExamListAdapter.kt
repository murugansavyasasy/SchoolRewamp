package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
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
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivityPaperNameData
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getActivitySubjectNameData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ActivityExamListAdapter(
    private var examList: List<getActivitySubjectNameData>,
    private var isEntryType: Boolean,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TAG = "ActivityExamAdapter"
    }

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

    override fun getItemCount(): Int = if (isLoading) 6 else examList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ExamViewHolder) holder.bind(examList[position], position)
        else if (holder is ShimmerViewHolder) holder.startShimmer()
    }

    fun updateData(newList: List<getActivitySubjectNameData>) {
        examList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    fun getFinalList(): List<getActivitySubjectNameData> = examList

    /* ─── helper: is this single paper fully mapped/selected? ─── */
    private fun isPaperSelected(paper: getActivityPaperNameData): Boolean {
        val result = if (isEntryType) {
            if (paper.rubrics.isNotEmpty()) {
                // Image entry + rubrics: ALL rubrics must have a mapped column
                paper.rubrics.all { !it.selectedRubricesValue.isNullOrEmpty() }
            } else {
                // Image entry + no rubrics: subject-level spinner value
                !paper.selectedValue.isNullOrEmpty()
            }
        } else {
            if (paper.rubrics.isNotEmpty()) {
                // Manual entry + rubrics: ALL rubrics must be ticked
                paper.rubrics.all { it.isSelected }
            } else {
                // Manual entry + no rubrics
                !paper.selectedActivityID.isNullOrEmpty()
            }
        }
        Log.d(TAG, "isPaperSelected ${paper.name} rubrics=${paper.rubrics.size} → $result")
        return result
    }

    inner class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.examTitle)
        private val lblMap: TextView = itemView.findViewById(R.id.lblMap)
        private val arrow: ImageView = itemView.findViewById(R.id.arrow)
        private val subjectsRv: RecyclerView = itemView.findViewById(R.id.rcSubject)
        private val lnrUploadImage: LinearLayout = itemView.findViewById(R.id.lnrUploadImage)
        private val cardUploadImage: CardView = itemView.findViewById(R.id.cardUploadImage)
        private val viewDiv: View = itemView.findViewById(R.id.viewDiv)

        fun bind(item: getActivitySubjectNameData, position: Int) {
            title.text = item.subject

            val total = item.paper.size
            val selectedCount = item.paper.count { isPaperSelected(it) }

            Log.d(TAG, "bind ${item.subject} → selectedCount=$selectedCount total=$total isEntryType=$isEntryType")
            applyParentColor(selectedCount, total)

            subjectsRv.layoutManager = LinearLayoutManager(context)
            subjectsRv.adapter = ActivitySubjectListAdapter(item.paper, isEntryType, context) {
                val newSelected = item.paper.count { isPaperSelected(it) }
                Log.d(TAG, "callback ${item.subject} → newSelected=$newSelected total=$total")
                applyParentColor(newSelected, total)
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
            }
        }

        private fun Context.dp(value: Int): Int =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                this.resources.displayMetrics
            ).toInt()

        private fun applyParentColor(selectedCount: Int, total: Int) {
            val bg = lnrUploadImage.background as GradientDrawable
            bg.mutate()

            when {
                selectedCount == 0 -> {
                    // RESET → white / default stroke
                    bg.setStroke(context.dp(2), ContextCompat.getColor(context, android.R.color.white))
                    bg.setColor(ContextCompat.getColor(context, R.color.white))
                    cardUploadImage.cardElevation = context.dp(5).toFloat()

//                    lblMap.visibility = View.GONE
//                    lblMap.text = ""
                    lblMap.visibility = View.GONE
                    lblMap.text = "${total} Activities"

                    if (isEntryType) {
                        lblMap.setTextColor(ContextCompat.getColor(context, R.color.very_dark_gray2))
                    }
                }

                selectedCount < total -> {           // YELLOW / partial
                    bg.setStroke(
                        context.dp(2),
                        ContextCompat.getColor(context, R.color.light_bg_orange_6)
                    )
                    bg.setColor(ContextCompat.getColor(context, R.color.light_bg_orange_5))
                    cardUploadImage.cardElevation = 0f

                    lblMap.visibility = View.VISIBLE
                    lblMap.text = "$selectedCount ${context.getString(R.string.of)} $total ${
                        context.getString(R.string.activities_mapped)
                    }"

                    if (isEntryType) {
                        lblMap.setTextColor(ContextCompat.getColor(context, R.color.light_bg_orange_6))
                    }
                }

                selectedCount == total -> {          // GREEN / complete
                    bg.setStroke(
                        context.dp(2),
                        ContextCompat.getColor(context, R.color.dark_green_3)
                    )
                    bg.setColor(ContextCompat.getColor(context, R.color.light_pale_green_1))
                    cardUploadImage.cardElevation = 0f

                    lblMap.visibility = View.VISIBLE
                    lblMap.text =
                        "${context.getString(R.string.all)} $total ${context.getString(R.string.activities_mapped)}"

                    if (isEntryType) {
                        lblMap.setTextColor(ContextCompat.getColor(context, R.color.dark_green_3))
                    }
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() = ShimmerUtil.startShimmer(itemView)
    }
}

