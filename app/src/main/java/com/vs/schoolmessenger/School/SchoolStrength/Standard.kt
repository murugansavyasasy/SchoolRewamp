package com.vs.schoolmessenger.School.SchoolStrength

import com.google.gson.annotations.SerializedName

data class Standard(
    val id: String,
    val name: String,
    val level: Int,

    @SerializedName("boys_count")
    val boysCount: Int,

    @SerializedName("girls_count")
    val girlsCount: Int,

    @SerializedName("other_count")
    val otherCount: Int,

    @SerializedName("total_students")
    val totalStudents: String,

    val sections: List<Section>
)
