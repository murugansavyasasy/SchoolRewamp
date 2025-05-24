package com.vs.schoolmessenger.School.AbsenteesMarking

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AbsenteesMarkAdapter(
    private var itemList: List<NameAndIds>?,
    private var listener: AbsenteesClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private val selectionListener: AbsenteesSelectionListener
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
            com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.attendance_student_list, parent, false)
            DataViewHolder(view, context, studentIdList,selectionListener) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener)

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
        private val selectionListener: AbsenteesSelectionListener

    ) :
        RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblRollNo: TextView = itemView.findViewById(R.id.lblRollNo)
        private val lblAdmisNo: TextView = itemView.findViewById(R.id.lblAdmissionNoValue)
        private val lnrPresent: LinearLayout = itemView.findViewById(R.id.lnrPresent)
        private val lnrAbsent: RelativeLayout = itemView.findViewById(R.id.lnrAbsent)

        fun bind(data: NameAndIds, position: Int, listener: AbsenteesClickListener) {

            lblName.text = data.name
            lblRollNo.text = data.roll_no
            lblAdmisNo.text = data.admission_no

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
                }

                Log.d("StudentIDList", "After marking Absent: $studentIdList")
                selectionListener.onSelectionChanged(studentIdList.toList())
                listener.onItemClick(data)
            }

            lnrAbsent.setOnClickListener {
                // Now showing "Present", so remove from list
                lnrAbsent.visibility = View.GONE
                lnrPresent.visibility = View.VISIBLE

                studentIdList.remove(id)

                Log.d("StudentIDList", "After marking Present: $studentIdList")
                selectionListener.onSelectionChanged(studentIdList.toList())
                listener.onItemClick(data)
            }
        }
    }

    fun setAllAbsent(enable: Boolean) {
        studentIdList.clear()

        if (enable) {
            itemList?.forEach {
                studentIdList.add(it.id.toString())
            }
        }
        notifyDataSetChanged()
        selectionListener.onSelectionChanged(studentIdList.toList())

    }

    fun getSelectedStudentIds(): List<String> = studentIdList.toList()





    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}