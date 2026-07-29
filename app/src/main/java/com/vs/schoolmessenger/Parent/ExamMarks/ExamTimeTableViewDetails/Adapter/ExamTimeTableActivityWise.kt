package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableViewDetails.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableActivity
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableRubric
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamTimeTableActivityWise(
    private var itemList: List<ExamTimetableActivity>?,
    private val context: Context,
    private val listener: ExamMarkListener,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(
                parent,
                R.layout.exam_time_table_activity_item
            )
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.exam_time_table_activity_item, parent, false)
            DataViewHolder(view, context, listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList?.get(position))
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun updateData(newList: List<ExamTimetableActivity>) {
        itemList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: ExamMarkListener
    ) : RecyclerView.ViewHolder(itemView) {


//        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
//        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
//        private val lblSession: TextView = itemView.findViewById(R.id.lblSession)
//        private val lblVenue: TextView = itemView.findViewById(R.id.lblVenue)
//        private val lblSyllabus: TextView = itemView.findViewById(R.id.lblSyllabus)
        private val rlaNoDataFound: RelativeLayout = itemView.findViewById(R.id.rlaNoDataFound)
        private val lnrScheduleDetails: LinearLayout = itemView.findViewById(R.id.lnrScheduleDetails)
        private val subject_name: TextView = itemView.findViewById(R.id.subject_name)
        private val total_marks: TextView = itemView.findViewById(R.id.total_marks)
//        private val lblTotalMarks: TextView = itemView.findViewById(R.id.lblTotalMarks)
//        private val lblPassMarks: TextView = itemView.findViewById(R.id.lblPassMarks)
        private val div1: View = itemView.findViewById(R.id.div1)
        private val lblRubrics: TextView = itemView.findViewById(R.id.lblRubrics)
        private val lnrRubrics: LinearLayout = itemView.findViewById(R.id.lnrRubrics)
        private val NonRubriclblSession: TextView = itemView.findViewById(R.id.NonRubriclblSession)
        private val NonRubiclblTime: TextView = itemView.findViewById(R.id.NonRubiclblTime)
        private val rlaHeaderForNonRubic: RelativeLayout = itemView.findViewById(R.id.rlaHeaderForNonRubic)
        private val imgArrow: ImageView = itemView.findViewById(R.id.imgArrow)


        private val rcRubrics: RecyclerView =
            itemView.findViewById(R.id.rcRubrics)

        fun bind(data: ExamTimetableActivity?) {

            if (data == null) return
            subject_name.text=data.activityName
            total_marks.visibility= View.GONE

            val rubrics = data.rubrics ?: emptyList()



            if (rubrics.isEmpty()) {
                rcRubrics.visibility = View.GONE
                lnrScheduleDetails.visibility = View.GONE
                lnrRubrics.visibility = View.GONE
                rlaNoDataFound.visibility = View.GONE
                lblRubrics.visibility = View.GONE
                div1.visibility = View.GONE

                NonRubriclblSession.visibility= View.VISIBLE
                NonRubiclblTime.visibility= View.VISIBLE
                imgArrow.visibility= View.VISIBLE

                NonRubiclblTime.text="${context.getString(R.string.date)} : ${Constant.formatDate33(data.schedulingDetails?.date?:"")}"
                NonRubriclblSession.text="${context.getString(R.string.session)} : ${data.schedulingDetails?.session}"

                rlaHeaderForNonRubic.setOnClickListener {

                    val activityRubric = ExamTimetableRubric(
                        rubricId = data.activityId,
                        rubricName = data.activityName,
                        max_mark = data.max_mark,
                        pass_mark = data.pass_mark,
                        schedulingDetails = data.schedulingDetails
                    )
                    listener.onRubricClick(activityRubric)
                }

            } else {
                lnrScheduleDetails.visibility = View.GONE
                lblRubrics.visibility = View.GONE
                lnrRubrics.visibility = View.VISIBLE
                div1.visibility = View.VISIBLE
                NonRubriclblSession.visibility= View.GONE
                NonRubiclblTime.visibility= View.GONE
                imgArrow.visibility= View.GONE


                rlaNoDataFound.visibility = View.GONE
                rcRubrics.visibility = View.VISIBLE
                rcRubrics.layoutManager = LinearLayoutManager(context)
                rcRubrics.isNestedScrollingEnabled = false
                rcRubrics.adapter = ExamTimeTableRubricsWise(
                    rubrics,
                    context,
                    listener,
                    false

                )
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}