package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingAdapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SpecificStudentData.SpecificStudentSelectClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails.GetAttendanceStudentListData
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesSelectionListener
import com.vs.schoolmessenger.School.AbsenteesMarking.ODCustomSwitch
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AbsenteesMarkAdapter(
    private var itemList: MutableList<GetAttendanceStudentListData>? = mutableListOf(),
    private var isCurrentAttendanceType: String,
    private var context: Context,
    private var isLoading: Boolean,
    private val selectionListener: AbsenteesSelectionListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedStudents = mutableListOf<GetAttendanceStudentListData>()
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
            DataViewHolder(
                view,
                context,
                itemList!!,
                selectionListener
            ) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position],isCurrentAttendanceType, position)

        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val itemList: MutableList<GetAttendanceStudentListData>,
        private val selectionListener: AbsenteesSelectionListener,
        ) :
        RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblRollNo: TextView = itemView.findViewById(R.id.lblRollNo)
        private val lblAdmisNo: TextView = itemView.findViewById(R.id.lblAdmissionNoValue)
        private val lnrEntirePresent: LinearLayout = itemView.findViewById(R.id.lnrEntirePresent)
        private val lnrPresent: LinearLayout = itemView.findViewById(R.id.lnrPresent)
        private val lnrAbsent: LinearLayout = itemView.findViewById(R.id.lnrAbsent)
        private val lnrOD: LinearLayout = itemView.findViewById(R.id.lnrOD)
        private val switchOD: ODCustomSwitch = itemView.findViewById(R.id.switchOD)
        private val cbLaterComer: CheckBox = itemView.findViewById(R.id.cbLaterComer)

        fun bind(data: GetAttendanceStudentListData, isCurrentAttendanceType: String, position: Int) {

            if (data.roll_no.isNotEmpty()) {
                lblRollNo.text = data.roll_no
                lblRollNo.visibility = View.VISIBLE
            } else lblRollNo.visibility = View.GONE

            if (data.name.isNullOrEmpty()) {
                lblName.visibility = View.GONE
            } else {
                lblName.visibility = View.VISIBLE
                lblName.text = data.name
            }

            if (data.name.isNullOrEmpty()) {
                lblAdmisNo.visibility = View.GONE
            } else {
                lblAdmisNo.visibility = View.VISIBLE
                lblAdmisNo.text = context.getString(R.string.ADMIS_NO_) + data.admission_no
            }

            // -------- Helper functions --------
            fun getCurrentHalfStatus(): String {
                val parts = data.att_status.split("/")
                return when (isCurrentAttendanceType) {
                    "SH" -> parts.getOrNull(1) ?: "P" // second half
                    else -> parts.getOrNull(0) ?: "P" // first half or full day
                }
            }

            fun setCurrentHalfStatus(newValue: String) {
                val parts = data.att_status.split("/")
                val first = parts.getOrNull(0) ?: "P"
                val second = parts.getOrNull(1) ?: "P"

                data.att_status = when (isCurrentAttendanceType) {
                    "SH" -> "$first/$newValue"
                    else -> "$newValue/$second"
                }

                itemList[position] = data
                selectionListener.onSelectionChanged(itemList)
            }

            // -------- UI update based on att_status --------
            fun updateUI() {
                Log.d("CurrentAttendanceType",isCurrentAttendanceType)
                when (getCurrentHalfStatus()) {
                    "P" -> {
                        lnrEntirePresent.visibility = View.VISIBLE
                        lnrAbsent.visibility = View.GONE
                        lnrOD.visibility = View.GONE
                        switchOD.setChecked(false)
                        cbLaterComer.isChecked = false
                    }
                    "A" -> {
                        lnrEntirePresent.visibility = View.GONE
                        lnrAbsent.visibility = View.VISIBLE
                        lnrOD.visibility = View.GONE
                        switchOD.setChecked(false)
                        cbLaterComer.isChecked = false
                    }
                    "OD" -> {
                        lnrEntirePresent.visibility = View.GONE
                        lnrAbsent.visibility = View.GONE
                        lnrOD.visibility = View.VISIBLE
                        switchOD.setChecked(true)
                        cbLaterComer.isChecked = false
                    }
                    "P~" -> {
                        lnrEntirePresent.visibility = View.VISIBLE
                        lnrAbsent.visibility = View.GONE
                        lnrOD.visibility = View.GONE
                        switchOD.setChecked(false)
                        cbLaterComer.isChecked = true
                    }
                }
                switchOD.isEnabled = true
            }

            // -------- Listeners --------
            cbLaterComer.setOnCheckedChangeListener(null)
            cbLaterComer.isChecked = getCurrentHalfStatus() == "P~"

            val lateComerListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
                setCurrentHalfStatus(if (isChecked) "P~" else "P")
                updateUI()
            }
            cbLaterComer.setOnCheckedChangeListener(lateComerListener)

            // --- Present click ---
            lnrPresent.setOnClickListener {
                cbLaterComer.setOnCheckedChangeListener(null)
                setCurrentHalfStatus("A")
                cbLaterComer.isChecked = false
                updateUI()
                cbLaterComer.setOnCheckedChangeListener(lateComerListener)
            }

            // --- Absent click ---
            lnrAbsent.setOnClickListener {
                cbLaterComer.setOnCheckedChangeListener(null)
                setCurrentHalfStatus("P")
                cbLaterComer.isChecked = false
                updateUI()
                cbLaterComer.setOnCheckedChangeListener(lateComerListener)
            }

            // --- OD switch --- (kept your exact pattern)
            switchOD.setChecked(false)
            switchOD.setOnCheckedChangeListener { isChecked ->
                cbLaterComer.setOnCheckedChangeListener(null)

                // Determine the new value for OD or Present
                val newValue = if (isChecked) "OD" else "P"

                // Update only the relevant half of att_status
                val parts = data.att_status.split("/")
                val first = parts.getOrNull(0) ?: "P"
                val second = parts.getOrNull(1) ?: "P"

                data.att_status = when (isCurrentAttendanceType) {
                    "SH" -> "$first/$newValue"  // update second half
                    else -> "$newValue/$second" // update first half (for F or FH)
                }

                cbLaterComer.isChecked = false
                updateUI()
                cbLaterComer.setOnCheckedChangeListener(lateComerListener)
                itemList[position] = data
                selectionListener.onSelectionChanged(itemList)
            }


            // Initial state
            updateUI()
        }

    }

    fun setAllAbsent(enable: Boolean, isCurrentAttendanceType: String) {
        itemList?.forEachIndexed { index, data ->
            val parts = data.att_status.split("/")
            val first = parts.getOrNull(0) ?: "P"
            val second = parts.getOrNull(1) ?: "P"

            val newValue = if (enable) "A" else "P"

            // Decide which half to update based on session type
            val updatedStatus = when (isCurrentAttendanceType) {
                "SH" -> "$first/$newValue" // Update second half
                else -> "$newValue/$second" // Update first half (FH/F)
            }

            data.att_status = updatedStatus
            itemList?.set(index, data)
        }

        notifyDataSetChanged()
        selectionListener.onSelectionChanged(itemList ?: emptyList())
    }


    fun updateData(newList: List<GetAttendanceStudentListData>) {
        Log.d("OldList",itemList.toString()+itemList!!.size.toString())
        itemList = newList.toMutableList()
        Log.d("newList",itemList.toString()+itemList!!.size.toString())
        notifyDataSetChanged()
    }

//    fun unselectStudent(data: GetAttendanceStudentListData) {
//        selectedStudents.removeAll { it.id == data.id }
//        val position = itemList?.indexOfFirst { it.id == data.id } ?: -1
//        if (position != -1) {
//            notifyItemChanged(position)
//        }
//        selectionListener.onSelectionChanged(selectedStudents.toList())
//        selectionListener.onIdUnchecked(data)
//    }

    fun unselectStudents(dataList: List<GetAttendanceStudentListData>) {
        dataList.forEach { data ->
            val position = itemList?.indexOfFirst { it.id == data.id } ?: -1
            if (position != -1) {
                val student = itemList!![position]
                val parts = student.att_status.split("/")
                val first = parts.getOrNull(0) ?: "P"
                val second = parts.getOrNull(1) ?: "P"

                // Make status "P" based on current attendance type
                student.att_status = when (isCurrentAttendanceType) {
                    "SH" -> "$first/P"   // update second half
                    else -> "P/$second"  // update first half or full day
                }

                itemList!![position] = student
                notifyItemChanged(position)
            }
        }

        selectionListener.onSelectionChanged(itemList ?: emptyList())
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
//package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingAdapter
//
//import android.content.Context
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
//import com.vs.schoolmessenger.CommonScreens.SpecificStudentData.SpecificStudentSelectClickListener
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesSelectionListener
//import com.vs.schoolmessenger.Utils.ShimmerUtil
//
//class AbsenteesMarkAdapter(
//    private var itemList: List<NameAndIds>?,
//    private var context: Context,
//    private var isLoading: Boolean,
//    private val selectionListener: AbsenteesSelectionListener,
//    private val listener: SpecificStudentSelectClickListener,
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private val studentIdList = mutableListOf<String>()
//    private var isTextExpanded = false
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//    private var allMarkedAbsent = false
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.attendance_student_list)
//            ShimmerViewHolder(
//                shimmerView
//            )
//        } else {
//            val view =
//                LayoutInflater.from(parent.context)
//                    .inflate(R.layout.attendance_student_list, parent, false)
//            DataViewHolder(
//                view,
//                context,
//                studentIdList,
//                selectionListener,
//                listener
//            ) // Pass context to DataViewHolder
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is DataViewHolder) {
//            // Bind actual data when loading is complete
//            holder.bind(itemList!![position], position)
//
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 20 // Show shimmer items while loading
//        else itemList?.size ?: 0
//    }
//
//    class DataViewHolder(
//        itemView: View,
//        private val context: Context,
//        private val studentIdList: MutableList<String>,
//        private val selectionListener: AbsenteesSelectionListener,
//        private val listener: SpecificStudentSelectClickListener,
//
//
//        ) :
//        RecyclerView.ViewHolder(itemView) {
//        private val lblName: TextView = itemView.findViewById(R.id.lblName)
//        private val lblRollNo: TextView = itemView.findViewById(R.id.lblRollNo)
//        private val lblAdmisNo: TextView = itemView.findViewById(R.id.lblAdmissionNoValue)
//        private val lnrPresent: LinearLayout = itemView.findViewById(R.id.lnrPresent)
//        private val lnrAbsent: LinearLayout = itemView.findViewById(R.id.lnrAbsent)
//        private val lnrRollno: LinearLayout = itemView.findViewById(R.id.lnrRollNo)
//
//        fun bind(data: NameAndIds, position: Int) {
//
//            lblName.text = data.name
//            if (data.roll_no != "") {
//                lblRollNo.text = data.roll_no
//                lnrRollno.setBackgroundResource(R.drawable.rect_light_blue)
//            } else {
//                lblRollNo.text = ""
//                lnrRollno.setBackgroundResource(0)
//            }
//            lblAdmisNo.text = context.getString(R.string.ADMIS_NO_) + data.admission_no
//
//            val id = data.id.toString()
//
//            // If ID is in list (absent), show Absent UI
//            if (studentIdList.contains(id)) {
//                lnrAbsent.visibility = View.VISIBLE
//                lnrPresent.visibility = View.GONE
//            } else {
//                //  If ID is not in list (present), show Present UI
//                lnrAbsent.visibility = View.GONE
//                lnrPresent.visibility = View.VISIBLE
//            }
//
//            lnrPresent.setOnClickListener {
//                // Now showing "Absent", so add to list
//                lnrAbsent.visibility = View.VISIBLE
//                lnrPresent.visibility = View.GONE
//
//                if (!studentIdList.contains(id)) {
//                    studentIdList.add(id)
//                    listener.onIdCheck(data)
//                }
//
//                Log.d("StudentIDList", "After marking Absent: $studentIdList")
//                selectionListener.onSelectionChanged(studentIdList.toList())
//            }
//
//            lnrAbsent.setOnClickListener {
//                // Now showing "Present", so remove from list
//                lnrAbsent.visibility = View.GONE
//                lnrPresent.visibility = View.VISIBLE
//                studentIdList.remove(id)
//                listener.onIdUnchecked(data)
//                Log.d("StudentIDList", "After marking Present: $studentIdList")
//                selectionListener.onSelectionChanged(studentIdList.toList())
//            }
//        }
//    }
//
//    fun setAllAbsent(enable: Boolean) {
////        studentIdList.clear()
//        if (enable) {
//            itemList?.forEach {
//                studentIdList.add(it.id.toString())
//            }
//        } else {
//            studentIdList.clear()
//        }
//
//        notifyDataSetChanged()
//        selectionListener.onSelectionChanged(studentIdList.toList())
//
//    }
//
//    fun updateData(newList: List<NameAndIds>) {
//        itemList = newList
//        notifyDataSetChanged()
//    }
//
//
//    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun startShimmer() {
//            ShimmerUtil.startShimmer(itemView)
//        }
//    }
//}