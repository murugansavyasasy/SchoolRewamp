package com.vs.schoolmessenger.School.ClassTest.Report.Model

import com.google.gson.annotations.SerializedName


data class SectionModeldata(

        @SerializedName("section_id")
        val sectionId: String,

        @SerializedName("section_name")
        val sectionName: String,

        @SerializedName("subjects")
        val subjects: List<SubjectModeldata>
    )
