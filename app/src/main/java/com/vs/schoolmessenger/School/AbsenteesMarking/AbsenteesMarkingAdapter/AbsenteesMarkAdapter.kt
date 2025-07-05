package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingAdapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SpecificStudentData.SpecificStudentSelectClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesSelectionListener
import com.vs.schoolmessenger.School.StudentReport.StudentReportData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AbsenteesMarkAdapter(
    private var itemList: List<NameAndIds>?,
    private var context: Context,
    private var isLoading: Boolean,
    private val selectionListener: AbsenteesSelectionListener,
    private val listener: SpecificStudentSelectClickListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val studentIdList = mutableListOf<String>()
    private var isTextExpanded = false
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var allMarkedAbsent = false

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.attendance_student_list)
            ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.attendance_student_list, parent, false)
            DataViewHolder(view, context, studentIdList,selectionListener,listener) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position)

        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val studentIdList: MutableList<String>,
        private val selectionListener: AbsenteesSelectionListener,
        private val listener: SpecificStudentSelectClickListener,


        ) :
        RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblRollNo: TextView = itemView.findViewById(R.id.lblRollNo)
        private val lblAdmisNo: TextView = itemView.findViewById(R.id.lblAdmissionNoValue)
        private val lnrPresent: LinearLayout = itemView.findViewById(R.id.lnrPresent)
        private val lnrAbsent: LinearLayout = itemView.findViewById(R.id.lnrAbsent)
        private val lnrRollno: LinearLayout = itemView.findViewById(R.id.lnrRollNo)

        fun bind(data: NameAndIds, position: Int) {

            lblName.text = data.name
            if(data.roll_no!=""){
                lblRollNo.text = data.roll_no
                lnrRollno.setBackgroundResource(R.drawable.rect_light_blue)
            }
            else{
                lblRollNo.text = ""
                lnrRollno.setBackgroundResource(0)
            }
            lblAdmisNo.text = "ADMIS NO: "+data.admission_no

            val id = data.id.toString()

            // If ID is in list (absent), show Absent UI
            if (studentIdList.contains(id)) {
                lnrAbsent.visibility = View.VISIBLE
                lnrPresent.visibility = View.GONE
            } else {
                //  If ID is not in list (present), show Present UI
                lnrAbsent.visibility = View.GONE
                lnrPresent.visibility = View.VISIBLE
            }

            lnrPresent.setOnClickListener {
                // Now showing "Absent", so add to list
                lnrAbsent.visibility = View.VISIBLE
                lnrPresent.visibility = View.GONE

                if (!studentIdList.contains(id)) {
                    studentIdList.add(id)
                    listener.onIdCheck(data)
                }

                Log.d("StudentIDList", "After marking Absent: $studentIdList")
                selectionListener.onSelectionChanged(studentIdList.toList())
            }

            lnrAbsent.setOnClickListener {
                // Now showing "Present", so remove from list
                lnrAbsent.visibility = View.GONE
                lnrPresent.visibility = View.VISIBLE
                studentIdList.remove(id)
                listener.onIdUnchecked(data)
                Log.d("StudentIDList", "After marking Present: $studentIdList")
                selectionListener.onSelectionChanged(studentIdList.toList())
            }
        }
    }

    fun setAllAbsent(enable: Boolean) {
//        studentIdList.clear()
        if (enable) {
            itemList?.forEach {
                studentIdList.add(it.id.toString())
            }
        }
        else {
            studentIdList.clear()
        }

        notifyDataSetChanged()
        selectionListener.onSelectionChanged(studentIdList.toList())

    }

    fun updateData(newList: List<NameAndIds>) {
        itemList = newList
        notifyDataSetChanged()
    }



    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}