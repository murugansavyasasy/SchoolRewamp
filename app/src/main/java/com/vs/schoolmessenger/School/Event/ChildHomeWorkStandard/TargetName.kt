package com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard


sealed class TargetName {
    data class StaffName(
        val name: String,
        val role: String
    ) : TargetName()

    data class StudentName(
        val name: String,
        val standard: String,
        val section: String,
        val mobile: String
    ) : TargetName()

    data class SectionName(
        val std_sec: List<String>
    ) : TargetName()

    data class StandardName(
        val className: String
    ) : TargetName()

    data class GroupName(
        val groupName: String
    ) : TargetName()

    data class SchoolName(
        val institudeName: String
    ) : TargetName()
}
