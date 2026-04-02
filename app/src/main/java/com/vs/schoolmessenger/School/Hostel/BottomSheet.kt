package com.vs.schoolmessenger.School.Hostel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Fragement.AdminRequestFragment
import com.vs.schoolmessenger.School.Hostel.Fragement.AttendanceHistoryFragment
import com.vs.schoolmessenger.School.Hostel.Fragement.BedOccupiedFragment
import com.vs.schoolmessenger.School.Hostel.Fragement.FeeManagementFragment
import com.vs.schoolmessenger.School.Hostel.Fragement.MessTimeTableFragment
import com.vs.schoolmessenger.School.Hostel.Fragement.OutPassRequestFragment
import com.vs.schoolmessenger.School.Hostel.Fragement.RoomAttendanceFragment
import com.vs.schoolmessenger.School.Hostel.Fragement.TotalStudentFragment
import com.vs.schoolmessenger.School.Hostel.Model.FragmentType

class BottomSheet : BottomSheetDialogFragment() {

    private var type: String? = null

    companion object {

        fun newInstance(type: String): BottomSheet {
            val fragment = BottomSheet()
            val bundle = Bundle()
            bundle.putString("TYPE", type)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("TYPE")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.hostel_bottom_sheet, container, false)

        if (savedInstanceState == null) {
            loadFragment()
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )

        bottomSheet?.let {

            it.layoutParams.height = LayoutParams.MATCH_PARENT

            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    fun closeSheet() {
        dismiss()
    }

    private fun loadFragment() {
        val fragment = when (type) {

            FragmentType.BEDOCCUPIED.toString() -> BedOccupiedFragment()
            FragmentType.FEEMANAGEMENT.toString() -> FeeManagementFragment()
            FragmentType.TOTALSTUDENT.toString() -> TotalStudentFragment()
            FragmentType.PENDINGISSUES.toString() -> AdminRequestFragment()
            FragmentType.OUTPASSREQUESTS.toString() -> OutPassRequestFragment()
            FragmentType.MESSTIMETABLE.toString() -> MessTimeTableFragment()
            FragmentType.ATTENDANCEHISTORYHOSTEL.toString() -> AttendanceHistoryFragment()
            FragmentType.ROOMATTENDANCE.toString() -> RoomAttendanceFragment()

            else -> BedOccupiedFragment()
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.bottomFragmentContainer, fragment)
            .commitNowAllowingStateLoss()
    }
}