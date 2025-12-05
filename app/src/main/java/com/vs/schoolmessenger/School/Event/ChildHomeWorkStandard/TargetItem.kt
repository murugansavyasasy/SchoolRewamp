package com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard

sealed class TargetItem {
    data class School(val name: String) : TargetItem()
    data class Staff(val name: String, val role: String) : TargetItem()
    data class Student(
        val name: String,
        val standard: String,
        val section: String,
        val mobile: String
    ) : TargetItem()

    data class Section(val stdSec: List<String>) : TargetItem()
    data class Standard(val className: String) : TargetItem()
    data class Group(val groupName: String) : TargetItem()
}
