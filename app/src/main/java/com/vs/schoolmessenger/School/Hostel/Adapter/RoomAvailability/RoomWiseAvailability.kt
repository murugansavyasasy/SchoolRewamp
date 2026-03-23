package com.vs.schoolmessenger.School.Hostel.Adapter.RoomAvailability



import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Listner.HostelClickListner
import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility.getRoomAvailability
import com.vs.schoolmessenger.Utils.ShimmerUtil
import kotlin.math.log
import kotlin.math.roundToInt

class RoomWiseAvailability(
    private var itemList: List<getRoomAvailability>?,
    private var context: Context,
    private val hostelClickListner: HostelClickListner,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<getRoomAvailability> = itemList ?: listOf()
    private var filteredList: List<getRoomAvailability> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.roomwise_availability)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.roomwise_availability, parent, false)
            DataViewHolder(view, context, hostelClickListner)
        }


    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position)

        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else filteredList?.size ?: 0
    }

    fun updateData(newList: List<getRoomAvailability>) {
        this.fullList = newList
        notifyDataSetChanged()
    }

    class DataViewHolder(
        itemView: View, private val context: Context, private val hostelClickListner: HostelClickListner
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val lblRoomNo: TextView = itemView.findViewById(R.id.lblRoomNo)
        private val GroupsRoomDetails: Group = itemView.findViewById(R.id.GroupsRoomDetails)
        private val ErrorMsg: TextView = itemView.findViewById(R.id.ErrorMsg)
        private val lblFirstName: TextView = itemView.findViewById(R.id.lblFirstName)
        private val lblSecondName: TextView = itemView.findViewById(R.id.lblSecondName)
        private val lblPersonStrength: TextView = itemView.findViewById(R.id.lblPersonStrength)
        private val lblBedCount: TextView = itemView.findViewById(R.id.lblBedCount)
        private val lblHostellarRemainingCount: TextView = itemView.findViewById(R.id.lblHostellarRemainingCount)
        private val lblOccupancyPercentage: TextView = itemView.findViewById(R.id.lblOccupancyPercentage)
        private val proRoomAvailProgressBar: ProgressBar = itemView.findViewById(R.id.proRoomAvailProgressBar)
        private val cardHeader: MaterialCardView = itemView.findViewById(R.id.cardHeader)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: getRoomAvailability, position: Int) {

            lblRoomNo.text = data.number
            lblBedCount.text = data.total_beds.toString()

            val current = data.current_occupancy
            val max = data.max_occupancy

            val percentage = if (max > 0) {
                ((current.toFloat() / max.toFloat()) * 100)
                    .roundToInt()
                    .coerceAtMost(100)
            } else {
                0
            }

            lblOccupancyPercentage.text = "$percentage%"
            Log.d("percentage",percentage.toString())
            proRoomAvailProgressBar.progress = percentage
            lblPersonStrength.text = "$current / $max"

            if (percentage == 100) {
                proRoomAvailProgressBar.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.progress_red)
            } else {
                proRoomAvailProgressBar.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.progress_blue)
            }



            if (data.students.isEmpty()){
                GroupsRoomDetails.visibility=View.GONE
                ErrorMsg.visibility=View.VISIBLE
            }else{
                cardHeader.setOnClickListener {
                    hostelClickListner.onRoomClick(data)
                }
                GroupsRoomDetails.visibility=View.VISIBLE
                ErrorMsg.visibility=View.GONE
            }

            // Reset visibility first so accordingly we can make it visible it and control UI
            lblFirstName.visibility = View.GONE
            lblSecondName.visibility = View.GONE
            lblHostellarRemainingCount.visibility = View.GONE

            when (data.students.size) {

                1 -> {
                    lblFirstName.visibility = View.VISIBLE
                    lblFirstName.text = "• ${data.students[0]}"
                }

                2 -> {
                    lblFirstName.visibility = View.VISIBLE
                    lblSecondName.visibility = View.VISIBLE

                    lblFirstName.text = "• ${data.students[0]}"
                    lblSecondName.text = "• ${data.students[1]}"
                }

                3 -> {
                    lblFirstName.visibility = View.VISIBLE
                    lblSecondName.visibility = View.VISIBLE
                    lblHostellarRemainingCount.visibility = View.VISIBLE

                    lblFirstName.text = "• ${data.students[0]}"
                    lblSecondName.text = "• ${data.students[1]}"
                    lblHostellarRemainingCount.text = "• ${data.students[2]}" // "+2 more"
                }
            }

        }





    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}