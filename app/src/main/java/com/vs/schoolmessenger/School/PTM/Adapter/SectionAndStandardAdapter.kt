package com.vs.schoolmessenger.School.PTM.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.ShimmerUtil
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedClassSection(
    val class_id: String,
    val section_id: String
) : Parcelable


class SectionAndStandardAdapter(
    private var itemList: MutableList<StandardSection>,
    private var context: Context,
    private var isLoading: Boolean,
    private val onSelectionChanged: (List<SelectedClassSection>) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    // Maintain selected items
    private val selectedItems = mutableSetOf<SelectedClassSection>()

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.class_load_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.class_load_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }

    inner class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblClasses: TextView = itemView.findViewById(R.id.lblClasses)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: StandardSection, position: Int) {
            val itemKey = SelectedClassSection(
                section_id = data.sectionId,
                class_id = data.standardId
            )

            if (selectedItems.contains(itemKey)) {
                lblClasses.background = context.getDrawable(R.drawable.bg_button_blue_color)
                lblClasses.setTextColor(context.getColor(R.color.white))
            } else {
                lblClasses.background = context.getDrawable(R.drawable.gray_bg_radius)
                lblClasses.setTextColor(context.getColor(R.color.black))
            }

            lblClasses.text = "${data.standardName} - ${data.sectionName}"

            lblClasses.setOnClickListener {
                if (selectedItems.contains(itemKey)) {
                    selectedItems.remove(itemKey)
                    lblClasses.background = context.getDrawable(R.drawable.gray_bg_radius)
                    lblClasses.setTextColor(context.getColor(R.color.black))
                } else {
                    selectedItems.add(itemKey)
                    lblClasses.background = context.getDrawable(R.drawable.bg_button_blue_color)
                    lblClasses.setTextColor(context.getColor(R.color.white))
                }
                onSelectionChanged(selectedItems.toList())
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
