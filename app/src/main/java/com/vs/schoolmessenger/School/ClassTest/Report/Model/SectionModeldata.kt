package com.vs.schoolmessenger.School.ClassTest.Report.Model

import com.google.gson.annotations.SerializedName


data class SectionModeldata(

        @SerializedName("class_name")
        val className: String,


        @SerializedName("section_id")
        val sectionId: String,

        @SerializedName("section_name")
        val sectionName: String,

        @SerializedName("subjects")
        val subjects: List<SubjectModeldata>
    )
