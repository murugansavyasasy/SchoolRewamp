package com.vs.schoolmessenger.Parent.BusTracking.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.Parent.BusTracking.BusClickListner
import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.BusListData
import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.Stop
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class BusListAdapter(
    private var itemList: List<BusListData>?,
    private var context: Context,
    private val listener: BusClickListner,
    private var isLoading: Boolean,
    private val isVendor: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int =
        if (isLoading) TYPE_SHIMMER else TYPE_DATA

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

    override fun getItemCount(): Int =
        if (isLoading) 20 else itemList?.size ?: 0

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

        private val pickuproutelabel: LinearLayout = itemView.findViewById(R.id.pickuproutelabel)
        private val imgPickupArrow: ImageView = itemView.findViewById(R.id.imgPickupArrow)
        private val pickupDaysContainer: MaterialCardView =
            itemView.findViewById(R.id.pickupDaysContainer)
        private val lnrPickupDays: LinearLayout = itemView.findViewById(R.id.lnrPickupDays)

        private val dropuproutelabel: LinearLayout = itemView.findViewById(R.id.dropuproutelabel)
        private val imgDropArrow: ImageView = itemView.findViewById(R.id.imgDropArrow)
        private val dropDaysContainer: MaterialCardView =
            itemView.findViewById(R.id.dropDaysContainer)
        private val lnrDropDays: LinearLayout = itemView.findViewById(R.id.lnrDropDays)
        private val lnrPickupDropBus: LinearLayout = itemView.findViewById(R.id.lnrPickupDropBus)
        private val btnPickupBus: TextView = itemView.findViewById(R.id.btnPickupBus)
        private val btnDropBus: TextView = itemView.findViewById(R.id.btnDropBus)


        fun bind(data: BusListData, position: Int) {
            lblPlace.text = data.route_name ?: ""
            lblRouteNo.text = data.route_id ?: ""
            lblBusRouteNumber.text = data.vehicle_reg_no ?: ""
            lblCurrentStop.text = data.stop_name ?: ""
            lblPickUpTime.text = data.tentative_pickup_time ?: ""
            lblDropTime.text = data.tentative_drop_time ?: ""

            lnrTrackLive.setOnClickListener { listener.OnBusClick(data) }

            btnPickupBus.setOnClickListener {
                listener.onCustomClick(data,"PICKING")
            }
            btnDropBus.setOnClickListener {
                listener.onCustomClick(data,"DROPPING")
            }

            if (isVendor.equals("school_chimes", true)) {
                lnrTrackLive.visibility= View.GONE
                lnrPickupDropBus.visibility = View.VISIBLE
                dropuproutelabel.visibility = View.VISIBLE
                pickuproutelabel.visibility = View.VISIBLE
            } else {
                lnrTrackLive.visibility= View.VISIBLE
                lnrPickupDropBus.visibility = View.GONE
                dropuproutelabel.visibility = View.GONE
                pickuproutelabel.visibility = View.GONE
            }


            val pickupPoint = data.stopping_points
                .firstOrNull { it.journey_type.equals("PICKING", true) }

            val dropPoint = data.stopping_points
                .firstOrNull { it.journey_type.equals("DROPPING", true) }

            var pickupExpanded = false
            pickuproutelabel.setOnClickListener {
                pickupExpanded = !pickupExpanded
                rotateArrow(imgPickupArrow, pickupExpanded)
                if (pickupExpanded) {
                    if (lnrPickupDays.tag != "built") {
                        lnrPickupDays.orientation = LinearLayout.VERTICAL
                        pickupPoint?.let { sp ->
                            addDaysRow(lnrPickupDays, sp.working_days)
                            addStopsList(
                                container = lnrPickupDays,
                                stops = sp.stops,
                                userStopId = data.stop_id ?: "",
                                startTime = sp.start_time
                            )
                        }
                        lnrPickupDays.tag = "built"
                    }

                    pickupDaysContainer.visibility = View.VISIBLE
                } else {
                    pickupDaysContainer.visibility = View.GONE
                }
            }

            var dropExpanded = false
            dropuproutelabel.setOnClickListener {
                dropExpanded = !dropExpanded
                rotateArrow(imgDropArrow, dropExpanded)
                if (dropExpanded) {
                    if (lnrDropDays.tag != "built") {
                        lnrDropDays.orientation = LinearLayout.VERTICAL
                        dropPoint?.let { sp ->
                            addDaysRow(lnrDropDays, sp.working_days)
                            addStopsList(
                                container = lnrDropDays,
                                stops = sp.stops,
                                userStopId = data.stop_id ?: "",
                                startTime = sp.start_time
                            )
                        }
                        lnrDropDays.tag = "built"
                    }
                    dropDaysContainer.visibility = View.VISIBLE
                } else {
                    dropDaysContainer.visibility = View.GONE
                }
            }
        }

        private fun rotateArrow(arrow: ImageView, expanded: Boolean) {
            arrow.animate()
                .rotation(if (expanded) 180f else 0f)
                .setDuration(200)
                .start()
        }


        private fun addDaysRow(container: LinearLayout, days: List<String>) {
            val currentDay = java.text.SimpleDateFormat("EEEE", java.util.Locale.ENGLISH)
                .format(java.util.Date()).uppercase()

            val hScrollWrapper = HorizontalScrollView(container.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins(0, dpToPx(8), 0, dpToPx(8)) }
                isHorizontalScrollBarEnabled = false
            }

            val daysRow = LinearLayout(container.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            days.forEach { day ->
                val tv = LayoutInflater.from(container.context)
                    .inflate(R.layout.item_day_buslist, daysRow, false) as TextView
                tv.text = day.take(3)
                if (day.equals(currentDay, ignoreCase = true)) {
                    tv.setBackgroundResource(R.drawable.background_primary_radius_button)
                    tv.setTextColor(
                        androidx.core.content.ContextCompat.getColor(
                            container.context, R.color.white
                        )
                    )
                } else {
                    tv.background = null
                    tv.setTextColor(
                        androidx.core.content.ContextCompat.getColor(
                            container.context, R.color.PrimaryColor
                        )
                    )
                }
                daysRow.addView(tv)
            }

            hScrollWrapper.addView(daysRow)
            container.addView(hScrollWrapper)
        }

        private fun addStopsList(
            container: LinearLayout,
            stops: List<Stop>,
            userStopId: String,
            startTime: String
        ) {

            stops.forEachIndexed { index, stop ->

                val stopView = LayoutInflater.from(container.context)
                    .inflate(R.layout.item_route_stop, container, false)

                val tvNumber = stopView.findViewById<TextView>(R.id.tvStopNumber)
                val tvName = stopView.findViewById<TextView>(R.id.tvStopName)
                val tvTime = stopView.findViewById<TextView>(R.id.tvStopTime)
                val tvLandmark = stopView.findViewById<TextView>(R.id.tvLandmark)
                val tvYourStop = stopView.findViewById<TextView>(R.id.tvYourStop)
                val viewLine = stopView.findViewById<View>(R.id.viewLine)
                val cardStop = stopView.findViewById<MaterialCardView>(R.id.cardStop)

                tvNumber.text = (index + 1).toString()
                tvName.text = stop.stop_name
                tvTime.text = stop.stop_time
                tvLandmark.text = stop.landmark

                if (index == stops.lastIndex) {
                    viewLine.visibility = View.INVISIBLE
                }

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                params.bottomMargin = dpToPx(8)
                stopView.layoutParams = params

                val isYourStop = stop.stop_id == userStopId

                if (isYourStop) {

                    tvYourStop.visibility = View.VISIBLE

                    cardStop.strokeWidth = dpToPx(3)

                    cardStop.strokeColor =
                        androidx.core.content.ContextCompat.getColor(
                            container.context,
                            R.color.PrimaryColor
                        )

                    cardStop.cardElevation = dpToPx(8).toFloat()

                    cardStop.setCardBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(
                            container.context,
                            R.color.light_blue_14
                        )
                    )

                    tvNumber.backgroundTintList =
                        androidx.core.content.ContextCompat.getColorStateList(
                            container.context,
                            R.color.mild_green
                        )

                    tvNumber.setTextColor(
                        androidx.core.content.ContextCompat.getColor(
                            container.context,
                            R.color.PrimaryColor
                        )
                    )

                } else {

                    tvYourStop.visibility = View.GONE

                    cardStop.strokeWidth = dpToPx(1)

                    cardStop.strokeColor =
                        androidx.core.content.ContextCompat.getColor(
                            container.context,
                            R.color.light_gray
                        )
                }

                container.addView(stopView)
            }
        }

        private fun dpToPx(dp: Int): Int =
            (dp * itemView.context.resources.displayMetrics.density).toInt()
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
