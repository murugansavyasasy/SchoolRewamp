package com.vs.schoolmessenger.AWS

class PreSignedUrl {
    // Getters and Setters
    var status: Int = 0
    var message: String? = null
    var data: Data? = null

    class Data {
        // Getters and Setters
        var presignedUrl: String? = null
        var fileUrl: String? = null
    }
}
