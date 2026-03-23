package com.vs.schoolmessenger.School.Hostel.Adapter.RoomAvailability


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Listner.HostelClickListner
import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility.getFloorwiseAvailability
import com.vs.schoolmessenger.Utils.ShimmerUtil

class FloorWiseRoomAvailability(
    private var itemList: List<getFloorwiseAvailability>?,
    private val context: Context,
    private val hostelClickListner: HostelClickListner,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1


    var fullList: List<getFloorwiseAvailability> = itemList ?: emptyList()
    var filteredList: List<getFloorwiseAvailability> = fullList

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.floorwise_room_availability)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.floorwise_room_availability, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position],hostelClickListner)
        }
    }

    fun updateData(newList: List<getFloorwiseAvailability>) {
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }


    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val query = constraint?.toString()?.lowercase()?.trim() ?: ""

                val result = fullList.mapNotNull { floor_details ->

                    val filteredDetails = if (query.isEmpty()) {
                        floor_details.rooms
                    } else {
                        floor_details.rooms.filter { room ->
                            room.number.lowercase().contains(query) ||
                                    room.current_occupancy.toString().lowercase().contains(query) ||
                                    room.max_occupancy.toString().lowercase().contains(query)||
                                    room.id.lowercase().contains(query)||
                                    room.total_beds.toString().contains(query)
                        }
                    }

                    if (filteredDetails.isNotEmpty()) {
                        getFloorwiseAvailability(
                            id = floor_details.id,
                            floor_no = floor_details.floor_no,
                            floor_name = floor_details.floor_name,
                            rooms = filteredDetails
                        )
                    } else null
                }

                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                filteredList = results?.values as? List<getFloorwiseAvailability> ?: emptyList()

                hostelClickListner.onSearchResultEmpty(filteredList.isEmpty())

                notifyDataSetChanged()
            }
        }
    }




    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblFloorName: TextView = itemView.findViewById(R.id.lblFloorName)
        val rcRoomWiseAvailability: RecyclerView =
            itemView.findViewById(R.id.rcRoomWiseAvailability)


        fun bind(
            data: getFloorwiseAvailability,
            hostelClickListner: HostelClickListner
        ) {
            lblFloorName.text = data.floor_name

            if (data.rooms.isEmpty()) {
                rcRoomWiseAvailability.visibility = View.GONE
            } else {
                rcRoomWiseAvailability.visibility = View.VISIBLE
                rcRoomWiseAvailability.layoutManager = LinearLayoutManager(context)
                rcRoomWiseAvailability.isNestedScrollingEnabled = false
                rcRoomWiseAvailability.adapter = RoomWiseAvailability(
                    data.rooms,
                    context,
                    hostelClickListner,
                    false
                )
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}