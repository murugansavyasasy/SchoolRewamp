package com.vs.schoolmessenger.CommonScreens.RecipientDataClasses

data class AcademicYearResponse( val status: Boolean,
                                 val message: String,
                                 val data: List<AcademicYear>)
