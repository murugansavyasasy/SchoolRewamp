package com.vs.schoolmessenger.Parent.BusTracking.Adapter



import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.vs.schoolmessenger.Parent.BusTracking.BusClickListner
import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.BusListData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class BusListAdapter(
    private var itemList: List<BusListData>?,
    private var context: Context,
    private val listener: BusClickListner,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.bus_list_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.bus_list_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun updateData(newList: List<BusListData>) {
        itemList = newList
        notifyDataSetChanged()
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblPlace: TextView = itemView.findViewById(R.id.lblPlace)
        private val lblRouteNo: TextView = itemView.findViewById(R.id.lblRouteNo)
        private val lblCurrentStop: TextView = itemView.findViewById(R.id.lblCurrentStop)
        private val lblPickUpTime: TextView = itemView.findViewById(R.id.lblPickUpTime)
        private val lblDropTime: TextView = itemView.findViewById(R.id.lblDropTime)
        private val lblBusRouteNumber: TextView = itemView.findViewById(R.id.lblBusRouteNumber)
        private val lblBusNo: MaterialButton = itemView.findViewById(R.id.lblBusNo)
        private val lnrTrackLive: LinearLayout = itemView.findViewById(R.id.lnrTrackLive)




        fun bind(data: BusListData, position: Int) {

            lblPlace.text=data.route_name
            lblRouteNo.text=data.route_id
            lblBusRouteNumber.text=data.route_id

            lblCurrentStop.text=data.stop_name
            lblPickUpTime.text=data.tentative_pickup_time
            lblDropTime.text=data.tentative_drop_time
            lblBusNo.text=data.vehicle_no


            lnrTrackLive.setOnClickListener {
                listener.OnBusClick(data)
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}