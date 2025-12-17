package com.vs.schoolmessenger.School.SchoolStrength.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.SchoolStrength.Model.Standard
import com.vs.schoolmessenger.Utils.ShimmerUtil


class SchoolStrengthAdapter(
    private var itemList: List<Standard>,
    private var context: Context,
    private var isLoading: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var expandedPosition = -1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.schoolstrength_report)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.schoolstrength_report, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position, this)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val boyslabel: TextView = itemView.findViewById(R.id.boyslabel)
        private val girlslabel: TextView = itemView.findViewById(R.id.girlslabel)
        private val totallabel: TextView = itemView.findViewById(R.id.totallabel)
        private val header1: TextView = itemView.findViewById(R.id.header1)
        private val btnViewDetails: TextView = itemView.findViewById(R.id.btnViewDetails)
        private val viewBoys: View = itemView.findViewById(R.id.viewBoys)

        private val boyslabel1: ImageView = itemView.findViewById(R.id.boyslabel1)
        private val boyslabel2: ImageView = itemView.findViewById(R.id.boyslabel2)
        private val girlslabel2: ImageView = itemView.findViewById(R.id.girlslabel2)
        private val girlslabel1: ImageView = itemView.findViewById(R.id.girlslabel1)
        private val unknownimage: ImageView = itemView.findViewById(R.id.unknownimage)
        private val unknownimage1: ImageView = itemView.findViewById(R.id.unknownimage1)
        private val viewGirls: View = itemView.findViewById(R.id.viewGirls)
        private val unspecifiedcount: TextView = itemView.findViewById(R.id.unspecified_count)
        private val viewUnspecified: View = itemView.findViewById(R.id.viewUnspecified)

        @SuppressLint("SetTextI18n")
        fun bind(
            data: Standard,
            position: Int,
            adapter: SchoolStrengthAdapter
        ) {
            boyslabel.text = "${context.getString(R.string.boys)} : ${data.boys_count}"
            girlslabel.text = "${context.getString(R.string.girls)} : ${data.girls_count}"
            unspecifiedcount.text = "${context.getString(R.string.not_specified)} : ${data.other_count}"
            totallabel.text =
                "${context.getString(R.string.total_students)} : ${data.total_students}"
            header1.text = "${context.getString(R.string.Standard)} - ${data.name}"

            val greyColor = ContextCompat.getColor(context, android.R.color.darker_gray)

            // Set default backgrounds
            viewBoys.setBackgroundResource(R.color.PrimaryColor)
            viewGirls.setBackgroundResource(R.color.pink)
            viewUnspecified.setBackgroundColor(greyColor)

            val boysCount = data.boys_count.toIntOrNull() ?: 0
            val girlsCount = data.girls_count.toIntOrNull() ?: 0
            val otherCount = data.other_count.toIntOrNull() ?: 0
            val total = boysCount + girlsCount + otherCount

            // Handle icons visibility based on counts
            val showBoysIcon = boysCount > 0
            val showGirlsIcon = girlsCount > 0
            val showOtherIcon = otherCount > 0

            val numCategories = (if (showBoysIcon) 1 else 0) + (if (showGirlsIcon) 1 else 0) + (if (showOtherIcon) 1 else 0)

            if (numCategories == 0) {
                // All zero → hide all icons
                boyslabel1.visibility = View.GONE
                boyslabel2.visibility = View.GONE
                girlslabel1.visibility = View.GONE
                girlslabel2.visibility = View.GONE
                unknownimage.visibility = View.GONE
                unknownimage1.visibility = View.GONE
            } else if (numCategories == 1) {
                // Only one category non-zero → show two icons for that category
                if (showBoysIcon) {
                    // Only boys
                    boyslabel1.visibility = View.VISIBLE
                    boyslabel2.visibility = View.VISIBLE
                    girlslabel1.visibility = View.GONE
                    girlslabel2.visibility = View.GONE
                    unknownimage.visibility = View.GONE
                    unknownimage1.visibility = View.GONE
                } else if (showGirlsIcon) {
                    // Only girls
                    boyslabel1.visibility = View.GONE
                    boyslabel2.visibility = View.GONE
                    girlslabel1.visibility = View.VISIBLE
                    girlslabel2.visibility = View.VISIBLE
                    unknownimage.visibility = View.GONE
                    unknownimage1.visibility = View.GONE
                } else {
                    // Only other
                    boyslabel1.visibility = View.GONE
                    boyslabel2.visibility = View.GONE
                    girlslabel1.visibility = View.GONE
                    girlslabel2.visibility = View.GONE
                    unknownimage.visibility = View.VISIBLE
                    unknownimage1.visibility = View.VISIBLE
                }
            } else {
                // 2 or 3 categories non-zero → show one icon for each present category
                boyslabel1.visibility = if (showBoysIcon) View.VISIBLE else View.GONE
                boyslabel2.visibility = View.GONE
                girlslabel1.visibility = if (showGirlsIcon) View.VISIBLE else View.GONE
                girlslabel2.visibility = View.GONE
                unknownimage.visibility = if (showOtherIcon) View.VISIBLE else View.GONE
                unknownimage1.visibility = View.GONE
            }

            // Always show labels (including :0 for zero counts); unspecified treated same as others
            boyslabel.visibility = View.VISIBLE
            girlslabel.visibility = View.VISIBLE
            unspecifiedcount.visibility = View.VISIBLE

            // Set weights proportionally (or equal grey split if total == 0)
            val layoutBoys = viewBoys.layoutParams as LinearLayout.LayoutParams
            val layoutGirls = viewGirls.layoutParams as LinearLayout.LayoutParams
            val layoutUnspecified = viewUnspecified.layoutParams as LinearLayout.LayoutParams

            if (total > 0) {
                layoutBoys.weight = boysCount.toFloat() / total
                layoutGirls.weight = girlsCount.toFloat() / total
                layoutUnspecified.weight = otherCount.toFloat() / total
            } else {
                // All zero → grey bar split equally across all three views
                viewBoys.setBackgroundColor(greyColor)
                viewGirls.setBackgroundColor(greyColor)
                viewUnspecified.setBackgroundColor(greyColor)
                layoutBoys.weight = 1f
                layoutGirls.weight = 1f
                layoutUnspecified.weight = 1f
            }

            viewBoys.layoutParams = layoutBoys
            viewGirls.layoutParams = layoutGirls
            viewUnspecified.layoutParams = layoutUnspecified

            val detailRecyclerView: RecyclerView = itemView.findViewById(R.id.rlaabsenteesreport3)

            detailRecyclerView.layoutManager = LinearLayoutManager(context)

            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            detailRecyclerView.addItemDecoration(dividerItemDecoration)

            val detailAdapter = SchoolStrengthDetailAdapter(data.sections, context, false)
            detailRecyclerView.adapter = detailAdapter

            detailRecyclerView.visibility =
                if (adapter.expandedPosition == position) View.VISIBLE else View.GONE

            btnViewDetails.setOnClickListener {
                val previousExpandedPosition = adapter.expandedPosition

                if (adapter.expandedPosition == position) {
                    adapter.expandedPosition = -1
                    adapter.notifyItemChanged(position)
                } else {
                    adapter.expandedPosition = position
                    adapter.notifyItemChanged(previousExpandedPosition)
                    adapter.notifyItemChanged(position)
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