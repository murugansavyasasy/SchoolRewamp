package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model

import com.google.gson.annotations.SerializedName

data class Record(
    @SerializedName("S.no")
    val sNo: Int?,

    @SerializedName("Reg No")
    val regNo: Int?,

    @SerializedName("Student Name")
    val studentName: String?,

    @SerializedName("TERM I SCI")
    val termISci: Any?,

    @SerializedName("TERM II SCI")
    val termIISci: Any?,

    @SerializedName("TERM III SCI")
    val termIIISci: Any?,

    @SerializedName("TERM I ENG")
    val termIEng: Any?,

    @SerializedName("TERM II ENG")
    val termIIEng: Any?,

    @SerializedName("Term III English")
    val termIIIEnglish: Any?,

    @SerializedName("Term I Social")
    val termISocial: Any?,

    @SerializedName("Term II Soc")
    val termIISoc: Any?,

    @SerializedName("Term III Social")
    val termIIISocial: Any?,

    @SerializedName("Term I Art")
    val termIArt: Any?,

    @SerializedName("Terrn III Art")
    val terrnIIIArt: Any?
)