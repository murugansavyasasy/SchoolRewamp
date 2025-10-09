package com.vs.schoolmessenger.School.AbsenteesMarking

import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds

interface AbsenteesSelectionListener {
    fun onSelectionChanged(selectedIds: List<NameAndIds>)
}