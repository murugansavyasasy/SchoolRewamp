package com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary

import android.os.Parcel
import android.os.Parcelable

class TicketSummary : Parcelable {
    var id: Int = 0
    var merchant_name: String? = null
    var campaign_name: String? = null
    var merchant_id: Int = 0
    var merchant_logo: String? = null
    var about_merchant: String? = null
    var campaign_type: String? = null
    var offer_type: String? = null
    var discount: Int = 0
    var template_files: MutableList<String?>? = null
    var threshold_amount: String? = null
    var expiry_date: String? = null
    var expiry_type: String? = null
    var coupon_valid_for: String? = null
    var how_to_use: String? = null
    var terms_and_conditions: String? = null
    var cover_image: String? = null
    var cta_url: String? = null
    var offer_text: String? = null
    var location_list: MutableList<Location?>? = null
    var redeemed_on: String? = null
    var qr_code: String? = null
    var category_name: String? = null
    var source_link: String? = null
    var industry_name: String? = null
    var coupon_code: String? = null
    var coupon_status: String? = null
    var expires_in: Int = 0
    var isCTAvalid: Boolean = false
    var cTAname: String? = null
    var cTAredirect: String? = null
    var offer_to_show: String? = null

    constructor()

    protected constructor(`in`: Parcel) {
        id = `in`.readInt()
        merchant_name = `in`.readString()
        campaign_name = `in`.readString()
        merchant_id = `in`.readInt()
        merchant_logo = `in`.readString()
        about_merchant = `in`.readString()
        campaign_type = `in`.readString()
        offer_type = `in`.readString()
        discount = `in`.readInt()
        template_files = `in`.createStringArrayList()
        threshold_amount = `in`.readString()
        expiry_date = `in`.readString()
        expiry_type = `in`.readString()
        coupon_valid_for = `in`.readString()
        how_to_use = `in`.readString()
        terms_and_conditions = `in`.readString()
        cover_image = `in`.readString()
        cta_url = `in`.readString()
        offer_text = `in`.readString()
        location_list =
            `in`.createTypedArrayList<Location?>(
                Location.CREATOR
            )
        redeemed_on = `in`.readString()
        qr_code = `in`.readString()
        category_name = `in`.readString()
        source_link = `in`.readString()
        industry_name = `in`.readString()
        coupon_code = `in`.readString()
        coupon_status = `in`.readString()
        expires_in = `in`.readInt()
        isCTAvalid = `in`.readByte().toInt() != 0
        this.cTAname = `in`.readString()
        this.cTAredirect = `in`.readString()
        offer_to_show = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(merchant_name)
        dest.writeString(campaign_name)
        dest.writeInt(merchant_id)
        dest.writeString(merchant_logo)
        dest.writeString(about_merchant)
        dest.writeString(campaign_type)
        dest.writeString(offer_type)
        dest.writeInt(discount)
        dest.writeStringList(template_files)
        dest.writeString(threshold_amount)
        dest.writeString(expiry_date)
        dest.writeString(expiry_type)
        dest.writeString(coupon_valid_for)
        dest.writeString(how_to_use)
        dest.writeString(terms_and_conditions)
        dest.writeString(cover_image)
        dest.writeString(cta_url)
        dest.writeString(offer_text)
        dest.writeTypedList<Location?>(
            location_list
        )
        dest.writeString(redeemed_on)
        dest.writeString(qr_code)
        dest.writeString(category_name)
        dest.writeString(source_link)
        dest.writeString(industry_name)
        dest.writeString(coupon_code)
        dest.writeString(coupon_status)
        dest.writeInt(expires_in)
        dest.writeByte((if (isCTAvalid) 1 else 0).toByte())
        dest.writeString(this.cTAname)
        dest.writeString(this.cTAredirect)
        dest.writeString(offer_to_show)
    }

    override fun describeContents(): Int {
        return 0
    }

    class Location : Parcelable {
        var location_name: String? = null
        var latitude: String? = null
        var longitude: String? = null

        constructor()

        protected constructor(`in`: Parcel) {
            location_name = `in`.readString()
            latitude = `in`.readString()
            longitude = `in`.readString()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(location_name)
            dest.writeString(latitude)
            dest.writeString(longitude)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<Location?> = object : Parcelable.Creator<Location?> {
                override fun createFromParcel(`in`: Parcel): Location {
                    return Location(`in`)
                }

                override fun newArray(size: Int): Array<Location?> {
                    return arrayOfNulls<Location>(size)
                }
            }
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TicketSummary?> =
            object : Parcelable.Creator<TicketSummary?> {
                override fun createFromParcel(`in`: Parcel): TicketSummary {
                    return TicketSummary(`in`)
                }

                override fun newArray(size: Int): Array<TicketSummary?> {
                    return arrayOfNulls<TicketSummary>(size)
                }
            }
    }
}