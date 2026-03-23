package com.vs.schoolmessenger.School.StudentDetails.Model

import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.AcademicPerformance.AcademicPerformance
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.AttendanceOverview.AttendanceOverview
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.EntireFeeStructure.EntireFeeStructure
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.ExamPerformance.ExamPerformance
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.PaymentHistory.PaymentHistory
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.SubjectWisePerformance.SubjectWisePerformance

data class StudentDashboardData(
    val paymentHistory: List<PaymentHistory>?,
    val Academic_Performance: AcademicPerformance?,
    val ExamPerformance: ExamPerformance?,
    val SubjectWisePerformance: SubjectWisePerformance?,
    val AttendanceOverview: AttendanceOverview?,
    val EntireFeeStructure: List<EntireFeeStructure>?
)