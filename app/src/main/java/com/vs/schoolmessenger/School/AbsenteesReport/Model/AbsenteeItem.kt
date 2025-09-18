package com.vs.schoolmessenger.School.AbsenteesReport.Model



sealed class AbsenteeItem {
    data class ClassItem(val classWise: ClassWise) : AbsenteeItem()
    data class SectionItem(val sectionWise: SectionWise) : AbsenteeItem()
}
