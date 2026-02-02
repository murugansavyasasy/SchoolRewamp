package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.StaffWiseExam.getStaffWisExamData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities.getSubjectWiseACtivitiesData
import com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.OnExamSelectListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamListAdapter(
    private var examList: List<getStaffWisExamData?>?,
    private val context: Context,
    private val listener: OnExamSelectListener,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var getSubjectActivitiesList: List<getSubjectWiseACtivitiesData>? = null

    private var selectedPosition = -1
    var expandedPosition = -1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.exam_list_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.exam_list_item, parent, false)
            ExamViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 6 else examList!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ExamViewHolder)
            holder.bind(examList!![position]!!, position)
        else if (holder is ShimmerViewHolder)
            holder.startShimmer()
    }

    fun updateData(newList: List<getStaffWisExamData>) {
        examList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    fun updateSecondData(newList: List<getSubjectWiseACtivitiesData>?) {
        getSubjectActivitiesList = newList
        if (expandedPosition != -1) notifyItemChanged(expandedPosition)
    }

    inner class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.examTitle)
        private val month: TextView = itemView.findViewById(R.id.examMonth)
        private val arrow: ImageView = itemView.findViewById(R.id.arrow)
        private val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
        private val subjectsRv: RecyclerView = itemView.findViewById(R.id.rcSubject)
        private val header: RelativeLayout = itemView.findViewById(R.id.Header)
        private val viewDiv: View = itemView.findViewById(R.id.viewDiv)
        private val leftRibbon: View = itemView.findViewById(R.id.leftRibbon)
        private val lblNoDataFound: TextView = itemView.findViewById(R.id.lblNoDataFound)

        fun bind(item: getStaffWisExamData, position: Int) {

            title.text = item.name
            month.text = Constant.convertDateFormatType3(item.date)
            subjectsRv.layoutManager = LinearLayoutManager(context)

            val isExpanded = position == expandedPosition
            subjectsRv.visibility = if (isExpanded) View.VISIBLE else View.GONE
            viewDiv.visibility = if (isExpanded) View.VISIBLE else View.GONE
            arrow.rotation = if (isExpanded) 180f else 0f

            arrow.setOnClickListener {
                val prevExpanded = expandedPosition
                expandedPosition = if (expandedPosition == position) -1 else position

                notifyItemChanged(position)
                if (prevExpanded != -1 && prevExpanded != position) notifyItemChanged(prevExpanded)

                //Actually we are calling the api for the inner recyclerview here true means i want to do api call
                listener.onExamApiCall(item)

            }

            val isSelected = position == selectedPosition
            if (isSelected) {
                imgCheck.setImageResource(R.drawable.double_circle)
                title.setTextColor(ContextCompat.getColor(context, R.color.dark_bg_orange_2))
                leftRibbon.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.dark_bg_orange_2
                    )
                )
                header.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_bg_orange_3
                    )
                )
            } else {
                imgCheck.setImageResource(R.drawable.circle_icon)
                title.setTextColor(ContextCompat.getColor(context, R.color.black))
                leftRibbon.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                header.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            header.setOnClickListener {


                val previousSelected = selectedPosition

                if (previousSelected == position) {
                    // user clicked same selected item → unselect
                    selectedPosition = -1
                    notifyItemChanged(previousSelected)
                    listener.onExamSelected(null)   // send null to main activity
                    return@setOnClickListener
                }

                // new item selected
                selectedPosition = position
                notifyItemChanged(position)

                if (previousSelected != -1) {
                    notifyItemChanged(previousSelected)
                }

                listener.onExamSelected(item)

            }

            if (isExpanded) {
                when {
                    getSubjectActivitiesList == null -> {
                        subjectsRv.adapter = SubjectListAdapter(null, context)
                        subjectsRv.visibility = View.VISIBLE
                        lblNoDataFound.visibility = View.GONE
                    }

                    getSubjectActivitiesList!!.isNotEmpty() -> {
                        subjectsRv.adapter = SubjectListAdapter(getSubjectActivitiesList!!, context)
                        subjectsRv.visibility = View.VISIBLE
                        lblNoDataFound.visibility = View.GONE
                    }

                    else -> {
                        subjectsRv.visibility = View.GONE
                        lblNoDataFound.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() = ShimmerUtil.startShimmer(itemView)
    }
}
