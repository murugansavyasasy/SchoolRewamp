package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableViewDetails.Adapter



import android.annotation.SuppressLint
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.cardview.widget.CardView

import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableRubric
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ExamTimeTableRubricsWise(
    private var itemList: List<ExamTimetableRubric>?,
    private var context: Context,
    private val listener: ExamMarkListener,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<ExamTimetableRubric> = itemList ?: listOf()
    private var filteredList: List<ExamTimetableRubric> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.rubrics_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.rubrics_item, parent, false)
            DataViewHolder(view, context, listener)
        }


    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position)

        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else filteredList?.size ?: 0
    }


    fun updateData(newList: List<ExamTimetableRubric>) {
        this.fullList = newList
        notifyDataSetChanged()
    }


    class DataViewHolder(
        itemView: View, private val context: Context, private val listener: ExamMarkListener
    ) :
        RecyclerView.ViewHolder(itemView) {

        private val rubricsName: TextView = itemView.findViewById(R.id.rubricsName)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val lblSession: TextView = itemView.findViewById(R.id.lblSession)
        private val header: CardView = itemView.findViewById(R.id.header)



        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: ExamTimetableRubric, position: Int) {

            rubricsName.text=data.rubricName
            lblTime.text="${context.getString(R.string.date)} - ${Constant.formatDate33(data.schedulingDetails?.date?:"")}"
            lblSession.text="${context.getString(R.string.session)} - ${data.schedulingDetails?.session}"

            header.setOnClickListener {
                listener.onRubricClick(data)
            }

        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}