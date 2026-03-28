package com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDetails

import java.io.Serializable


data class getParentHostelDetailsData (
    val room_allocation_id: String,
    val hostel_id: String,
    val hostel_name: String,
    val student_id: String,
    val student_name: String,
    val admission_no: String,
    val primary_mobile: String,
    val gender: String,
    val floor_id: String,
    val floor_no: String,
    val floor_name: String,
    val room_id: String,
    val room_no: String,
    val room_type_id: String,
    val room_type: String,
    val hostel_type: String,
    val no_of_floors: Int,
    val no_of_rooms: Int,
    val warden_type: String,
    val max_capacity: Int,
    val warden_name: List<String>,
    val institute_name: String,
    val institute_address: String
): Serializable