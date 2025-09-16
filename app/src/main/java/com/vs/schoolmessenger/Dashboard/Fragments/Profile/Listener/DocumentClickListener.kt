package com.vs.schoolmessenger.Dashboard.Fragments.Profile.Listener

import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileField


interface DocumentClickListener {
    fun onDocumentClicked(field: ProfileField, position: Int)

}
