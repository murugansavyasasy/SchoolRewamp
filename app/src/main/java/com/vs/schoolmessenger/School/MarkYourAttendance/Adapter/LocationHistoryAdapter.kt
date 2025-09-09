package com.vs.schoolmessenger.School.MarkYourAttendance.Adapter

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.Adapter.TextHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.LocationHistoryData
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.LocationHistoryClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.util.Locale

class LocationHistoryAdapter(
    private var itemList: List<LocationHistoryData>?,
    private var listener: LocationHistoryClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.locations_list_items)
            TextHistoryAdapter.DataViewHolder.ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.locations_list_items, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener, this) // Pass adapter reference
        } else if (holder is TextHistoryAdapter.DataViewHolder.ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    fun updateList(newList: List<LocationHistoryData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    fun removeItemById(id: Int) {
        val updatedList = itemList!!.filter { it.id != id }
        updateList(updatedList)
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblPlaceName: TextView = itemView.findViewById(R.id.lblPlace)
        private val lblAddress: TextView = itemView.findViewById(R.id.lblAddress)
        private val lblDistance: TextView = itemView.findViewById(R.id.lblDistance)
        private val lblLatLang: TextView = itemView.findViewById(R.id.lblLatLang)
        private val rytImgEdit: RelativeLayout = itemView.findViewById(R.id.rytImgEdit)
        private val rytImgDelete: RelativeLayout = itemView.findViewById(R.id.rytImgDelete)



        private fun getAddressFromLocation(latitude: String, longitude: String): String {
            val geocoder = Geocoder(context, Locale.getDefault())
            val lat: Double? = latitude.toDoubleOrNull()
            val lang: Double? = longitude.toDoubleOrNull()

            return try {
                val addresses = geocoder.getFromLocation(lat!!, lang!!, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Address not found"
                } else "No address found"
            } catch (e: Exception) {
                e.printStackTrace()
                "Geocoder error"
            }
        }


        fun bind(
            data: LocationHistoryData,
            position: Int,
            listener: LocationHistoryClickListener,
            adapter: LocationHistoryAdapter
        ) {
            lblPlaceName.text = data.location
            lblAddress.text = getAddressFromLocation(data.latitude,data.longitude)
            lblLatLang.text = data.latitude + " - " + data.longitude
            lblDistance.text = data.distance + " "+context.getString(R.string.Meters)


            rytImgDelete.setOnClickListener {
                listener.onItemClick(data, Constant.isDelete)
            }
            rytImgEdit.setOnClickListener {
                listener.onItemClick(data, Constant.isEdit)
            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}