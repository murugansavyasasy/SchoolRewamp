package com.vs.schoolmessenger.Repository

object APIMethods {

    const val isCountryList = "app/setup/countries"
    const val isVersionCheck = "app/setup/version-check"
    const val isValidateUser = "app/auth/validate-user"
    const val isValidateOtp = "app/auth/validate-otp"

    //    const val isValidateUser ="validate/get-user-details-with-validation"
    const val isPasswordChange = "app/cred/change-password"
    const val isForgetPassword = "app/cred/forgot-password"
    const val isResetPassword = "app/cred/reset-password"
    const val isCreateNewPassword = "app/cred/create-new-password"
    const val isDeviceToken = "app/auth/device-token"
    const val isGetDashBoard = "dashboard/dashboard/get-dashboard-details"
    const val isGetAds = "dashboard/dashboard/get-ads"
    const val getStaffList = "comm/recipient/get-staff-list"
    const val getSubjectList = "comm/recipient/get-subject-list"
    const val getStandard = "comm/recipient/get-standards"
    const val getStudentList = "comm/recipient/get-student-list"
    const val isGroupList = "comm/recipient/get-group-list"
    const val isGetCommmunicationlist = "comm/communication/list-archive"
    const val isUpdateStatusCommunication = "comm/communication/read-status-update"
    const val isGetCommmunicationlistload = "comm/communication/list"
    const val isGetVoiceHistory = "comm/voice/get-voice-history"
    const val isGetTextHistory = "comm/text-message/get-text-history"
    const val isGetHomeWorkReport = "comm/homework/get-homework-report"
    const val isSendText = "comm/text-message/send-text"
    const val isSendHomeWork = "comm/homework/send-homework"
    const val isSendVoice = "comm/voice/send-voice"
    const val isUpdateStatusArchive = "comm/communication/read-status-update-archive"
    const val isGetAcademicYear = "comm/recipient/get-academic-year-list"
    const val isHomeWorkDetails="comm/homework/get-homework-list"

    const val send_homework = "comm/homework/send-homework"
    const val homework_reports = "comm/homework/get-homework-report"
    const val homework_list = "comm/homework/get-homework-list"
    const val homework_list_archive = "comm/homework/get-homework-list-archive"
    const val send_attachments = "comm/attachment/send-attachment"
    const val attachments_list = "comm/communication/attachment-list"
    const val attachments_list_archive = "comm/communication/attachment-list-archive"


    const val punch_giometric_attendance =  "staff-attd/geometric/entry-using-app"
    const val add_giometric_location =  "staff-attd/geometric/set-geometric-location"
    const val giometric_location_history =  "staff-attd/geometric/get-geometric-location-history"
    const val staff_locations =  "staff-attd/geometric/get-staff-geometric-location"
    const val remove_location =  "staff-attd/geometric/remove-geometric-location"
    const val update_location =  "staff-attd/geometric/update-geometric-location"
    const val giometric_staff_attendance_report =  "staff-attd/geometric/geometric-staff-attendance-report"
    const val giometric_principal_attendance_report =  "staff-attd/geometric/geometric-principal-attendance-report"
    const val punch_history =  "staff-attd/geometric/geometric-punch-history"

    const val student_report= "api/get-student-report"


}