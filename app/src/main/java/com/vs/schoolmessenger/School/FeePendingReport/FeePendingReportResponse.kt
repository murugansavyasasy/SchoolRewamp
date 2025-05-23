package com.vs.schoolmessenger.School.FeePendingReport

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Parent.Noticeboard.Notice
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionItem

data class FeePendingReportResponse (
    @SerializedName (APIKeyNames.status)val status: Boolean,
    @SerializedName (APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<FeePendingCollectionItem>
)

