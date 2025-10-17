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

        fun bind(data: GetAttendanceStudentListData, position: Int) {

            if (data.roll_no != "") {
                lblRollNo.text = data.roll_no
                lblRollNo.visibility=View.VISIBLE
            } else {
                lblRollNo.visibility=View.GONE
            }

            if (data.name.isNullOrEmpty()){
                lblName.visibility=View.GONE
            }
            else{
                lblName.visibility=View.VISIBLE
                lblName.text = data.name
            }

            if (data.name.isNullOrEmpty()){
                lblAdmisNo.visibility=View.GONE
            }
            else{
                lblAdmisNo.visibility=View.VISIBLE
                lblAdmisNo.text = context.getString(R.string.ADMIS_NO_) + data.admission_no
            }
            // --- Function to update UI safely ---
            fun updateUI() {
                when (data.att_type) {
                    "PRESENT" -> {
                        lnrEntirePresent.visibility = View.VISIBLE
                        lnrAbsent.visibility = View.GONE
                        lnrOD.visibility = View.GONE
                        switchOD.setChecked(false)
                        cbLaterComer.isChecked = false
                    }
                    "ABSENT" -> {
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
                    "LATECOMER" -> {
                        lnrEntirePresent.visibility = View.VISIBLE
                        lnrAbsent.visibility = View.GONE
                        lnrOD.visibility = View.GONE
                        switchOD.setChecked(false)
                        cbLaterComer.isChecked = true
                    }
                }
                switchOD.isEnabled = true // OD always enabled
            }

            // ✅ Temporarily remove listener before changing checked state
            cbLaterComer.setOnCheckedChangeListener(null)
            cbLaterComer.isChecked = data.att_type == "LATECOMER"

            // ✅ Then reattach the listener AFTER UI sync
            val lateComerListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
                Log.d("Clicking", "cbLaterComer")
                data.att_type = if (isChecked) "LATECOMER" else "PRESENT"
                updateUI()
                itemList[position] = data
                selectionListener.onSelectionChanged(itemList)
            }
            cbLaterComer.setOnCheckedChangeListener(lateComerListener)

            // --- Present Click ---
            lnrPresent.setOnClickListener {
                cbLaterComer.setOnCheckedChangeListener(null)
                data.att_type = "ABSENT"
                cbLaterComer.isChecked = false
                updateUI()
                cbLaterComer.setOnCheckedChangeListener(lateComerListener)
                itemList[position] = data
                selectionListener.onSelectionChanged(itemList)
            }

            // --- Absent Click ---
            lnrAbsent.setOnClickListener {
                cbLaterComer.setOnCheckedChangeListener(null)
                data.att_type = "PRESENT"
                cbLaterComer.isChecked = false
                updateUI()
                cbLaterComer.setOnCheckedChangeListener(lateComerListener)
                itemList[position] = data
                selectionListener.onSelectionChanged(itemList)
            }

            // --- OD Switch ---
            switchOD.setOnCheckedChangeListener { isChecked ->
                cbLaterComer.setOnCheckedChangeListener(null)
                data.att_type = if (isChecked) "OD" else "PRESENT"
                cbLaterComer.isChecked = false
                updateUI()
                cbLaterComer.setOnCheckedChangeListener(lateComerListener)
                itemList[position] = data
                selectionListener.onSelectionChanged(itemList)
            }

            // Initial state sync
            updateUI()
        }
    }

    fun setAllAbsent(enable: Boolean) {
        itemList?.forEachIndexed { index, data ->
            data.att_type = if (enable) "ABSENT" else "PRESENT"
        }
        notifyDataSetChanged()
        selectionListener.onSelectionChanged(itemList ?: emptyList())
    }

    fun updateData(newList: List<GetAttendanceStudentListData>) {
        itemList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun unselectStudent(data: GetAttendanceStudentListData) {
        selectedStudents.removeAll { it.id == data.id }
        val position = itemList?.indexOfFirst { it.id == data.id } ?: -1
        if (position != -1) {
            notifyItemChanged(position)
        }
        selectionListener.onSelectionChanged(selectedStudents.toList())
        selectionListener.onIdUnchecked(data)
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