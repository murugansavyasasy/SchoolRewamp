package com.vs.schoolmessenger.School.Attachment.AttachmentTargetDetails

import com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard.TargetData




data class AttachmentTargetDetailResponse (
    val status: Boolean,
    val message: String,
    val data: List<TargetData>
)