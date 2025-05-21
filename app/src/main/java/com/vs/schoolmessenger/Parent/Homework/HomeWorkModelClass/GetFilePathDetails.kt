package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class GetFilePathDetails(
    @SerializedName(APIKeyNames.type)
    val type: String,
    @SerializedName(APIKeyNames.path)
    val path: String,
    val subject_name: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(path)
        parcel.writeString(subject_name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<GetFilePathDetails> {
        override fun createFromParcel(parcel: Parcel): GetFilePathDetails {
            return GetFilePathDetails(parcel)
        }

        override fun newArray(size: Int): Array<GetFilePathDetails?> {
            return arrayOfNulls(size)
        }
    }
}
