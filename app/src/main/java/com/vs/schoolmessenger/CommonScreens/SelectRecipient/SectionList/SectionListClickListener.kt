package com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList

import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section

interface SectionListClickListener {
    fun onSectionClick(data: Section, isChecked: Boolean)
}