package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

data class UserValidationResponse(  val status: Boolean,
                                    val message: String,
                                    val data: List<UserValidationData>)
