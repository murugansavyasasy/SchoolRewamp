package com.vs.schoolmessenger.Repository

object APIMethods {

    // AUTH
    const val isCountryList = "app/api/setup/countries"
    const val isVersionCheck = "app/api/setup/version-check"
    const val isValidateUser = "app/api/auth/validate-user"
    const val isValidateOtp = "app/api/auth/validate-otp"
    const val isPasswordChange = "app/api/cred/change-password"
    const val isForgetPassword = "app/api/cred/forgot-password"
    const val isResetPassword = "app/api/cred/reset-password"
    const val isCreateNewPassword = "app/api/cred/create-new-password"
    const val isDeviceToken = "app/api/auth/device-token"




    const val isGetDashBoard = "dashboard/dashboard/get-dashboard-details"
    const val isGetAds = "dashboard/dashboard/get-ads"
    const val getStaffList = "comm/api/recipient/get-staff-list"
    const val getSubjectList = "comm/api/recipient/get-subject-list"
    const val getStandard = "comm/api/recipient/get-standards"
    const val getStudentList = "comm/api/recipient/get-student-list"
    const val isGroupList = "comm/api/recipient/get-group-list"
    const val isGetCommmunicationlist = "comm/api/communication/list-archive"
    const val isUpdateStatusCommunication = "comm/api/communication/read-status-update"
    const val isGetCommmunicationlistload = "comm/api/communication/list"
    const val isGetVoiceHistory = "comm/api/voice/get-voice-history"
    const val isGetTextHistory = "comm/api/text-message/get-text-history"
    const val isGetHomeWorkReport = "comm/api/homework/report"
    const val isGetDailyCollectionReport = "admin/api/fee-report/daily-collection"
    const val isGetSchoolStrengthReport = "api/get-school-strength"
    const val isDetailedPendingReport = "admin/api/fee-report/detailed-pending-report"
    const val isSendText = "comm/api/text-message/send-text"
    const val isSendHomeWork = "comm/api/homework/send-homework"
    const val isSendVoice = "comm/api/voice/send-voice"
    const val isUpdateStatusArchive = "comm/api/communication/read-status-update-archive"
    const val isGetAcademicYear = "comm/api/recipient/get-academic-year-list"
    const val isHomeWorkDetails="comm/api/homework/list"

    const val send_homework = "comm/api/homework/send-homework"
    const val homework_reports = "comm/api/homework/get-homework-report"
    const val homework_list = "comm/api/homework/get-homework-list"
    const val homework_list_archive = "comm/homework/get-homework-list-archive"
    const val send_attachments = "comm/api/attachment/send-attachment"
    const val attachments_list = "comm/api/communication/attachment-list"
    const val attachments_list_archive = "comm/api/communication/attachment-list-archive"


    const val punch_giometric_attendance =  "staff-attd/api/geometric/entry-using-app"
    const val add_giometric_location =  "staff-attd/api/geometric/set-geometric-location"
    const val giometric_location_history =  "staff-attd/api/geometric/get-geometric-location-history"
    const val staff_locations =  "staff-attd/api/geometric/get-staff-geometric-location"
    const val remove_location =  "staff-attd/api/geometric/remove-geometric-location"
    const val update_location =  "staff-attd/api/geometric/update-geometric-location"
    const val giometric_staff_attendance_report =  "staff-attd/api/geometric/geometric-staff-attendance-report"
    const val giometric_principal_attendance_report =  "staff-attd/api/geometric/geometric-principal-attendance-report"
    const val punch_history =  "staff-attd/api/geometric/geometric-punch-history"

    const val student_report= "admin/api/get-student-report"


}