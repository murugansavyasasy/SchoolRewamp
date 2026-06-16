package com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard

import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.DetailedAttendanceRecords.getHostelDetailedAttendance

class getParentHostelDasboardData (
    val gate_pass: List<GatePass>,
    val today_attendance: List<String>,
    val out_pass_requests: List<OutpassRequestData>,
    val hostel_info: List<HostelInfo>,
    val attendance_details: List<getHostelDetailedAttendance>,
    val fee_details: List<FeeDetails>
)