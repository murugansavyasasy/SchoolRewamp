package com.vs.schoolmessenger.School.StudentReport

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class StudentReportData (

    @SerializedName(APIKeyNames.id) val id: Int,
    @SerializedName(APIKeyNames.name) val name: String,
    @SerializedName(APIKeyNames.primary_mobile) val primary_mobile: String,
    @SerializedName(APIKeyNames.admission_no) val admission_no: String,
    @SerializedName(APIKeyNames.roll_no) val roll_no: String,
    @SerializedName(APIKeyNames.gender) val gender: String,
    @SerializedName(APIKeyNames.dob) val dob: String,
    @SerializedName(APIKeyNames.class_id) val class_id: String,
    @SerializedName(APIKeyNames.section_id) val section_id: String,
    @SerializedName(APIKeyNames.class_name) val class_name: String,
    @SerializedName(APIKeyNames.section_name) val section_name: String,
    @SerializedName(APIKeyNames.father_name) val father_name: String,
    @SerializedName(APIKeyNames.class_teacher) val class_teacher: String,




//existing code
//    val admissionNumber: String,
//    val gender: String,
//    val dob: String,
//    val studentName: String,
//    val fatherName: String,
//    val teacherName: String,
//    val mobileNumber: String,
//    val email: String

)