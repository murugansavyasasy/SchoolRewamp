package com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard

import com.google.gson.annotations.SerializedName

data class SchoolNameTarget(

    @SerializedName("institute")
    val institute: List<String>? = null,

    @SerializedName("standard")
    val standardList: List<String>? = null,

    @SerializedName("group")
    val group: List<String>? = null,

    @SerializedName("sections")
    val sections: List<String>? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("std")
    val std: String? = null,

    @SerializedName("section")
    val section: String? = null,

    @SerializedName("mobile")
    val mobile: String? = null
) {
    fun getDisplayText(): String {
        return when {
            !institute.isNullOrEmpty() ->
                "🏛 " + institute.joinToString(" , ")

            !standardList.isNullOrEmpty() ->
                "📘 Standard: ${standardList.joinToString(" , ")}"

            !group.isNullOrEmpty() ->
                "👥 ${group.joinToString(" , ")}"

            !sections.isNullOrEmpty() ->
                "🏷 ${sections.joinToString(" , ")}"


            !name.isNullOrEmpty() && !role.isNullOrEmpty() ->
                "👨‍🏫 $name ($role)"


            !name.isNullOrEmpty() && !std.isNullOrEmpty() && !section.isNullOrEmpty() ->
                "🧒 $name - ${std}-${section}"

            else -> name ?: ""
        }
    }
}
