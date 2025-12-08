package com.vs.schoolmessenger.Parent.LSRW.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LsrwSubmitSkillDataClass(
    val id: String,
    val isDescription: String,
    val thumbnail: String
) : Parcelable