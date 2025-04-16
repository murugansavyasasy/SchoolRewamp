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
    const val isSendText = "comm/text-message/send-text"
    const val isSendVoice = "comm/voice/send-voice"
    const val isUpdateStatusArchive = "comm/communication/read-status-update-archive"
    const val isGetAcademicYear = "comm/recipient/get-academic-year-list"

}