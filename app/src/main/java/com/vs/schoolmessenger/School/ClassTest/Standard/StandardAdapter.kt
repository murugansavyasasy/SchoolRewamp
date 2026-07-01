package com.vs.schoolmessenger.School.ClassTest.Standard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StandardAdapter(
    private var itemList: List<StandardSection>?,
    private val context: Context,
    private var isLoading: Boolean,
    private val fullSectionList: List<StandardSection> = emptyList()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA    = 1

    private var selectedStandardId: String? =
        itemList?.firstOrNull()?.standardId

    fun getSelectedStandardId(): String? = selectedStandardId

    override fun getItemViewType(position: Int) =
        if (isLoading) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.item_class_standard)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_class_standard, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount() = if (isLoading) 8 else itemList?.size ?: 0

    fun updateData(newList: List<StandardSection>) {
        isLoading = false
        itemList  = newList
        notifyDataSetChanged()
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardStandard: CardView = itemView.findViewById(R.id.cardStandard)
        private val lblStandardName: TextView = itemView.findViewById(R.id.lblStandardName)
        private val lblSectionCount: TextView = itemView.findViewById(R.id.lblSectionCount)
        private val imgCheckMark: ImageView = itemView.findViewById(R.id.imgCheckMark)

        fun bind(item: StandardSection) {
            lblStandardName.text = item.standardName

            val count = (fullSectionList.ifEmpty { itemList.orEmpty() })
                .count { it.standardId == item.standardId }
            lblSectionCount.text = if (count == 1) "1 section" else "$count sections"

            applySelectionState(item.standardId == selectedStandardId)

            cardStandard.setOnClickListener {
                val previousId   = selectedStandardId
                selectedStandardId = item.standardId

                Constant.isSelectedStandardId   = item.standardId   ?: ""
                Constant.isSelectedStandardName = item.standardName ?: ""

                itemList?.forEachIndexed { index, section ->
                    if (section.standardId == previousId ||
                        section.standardId == item.standardId) {
                        notifyItemChanged(index)
                    }
                }
            }
        }

        private fun applySelectionState(isSelected: Boolean) {
            val primaryColor = ContextCompat.getColor(context, R.color.PrimaryColor)
            val white        = ContextCompat.getColor(context, R.color.white)
            val black        = ContextCompat.getColor(context, R.color.black)
            val grey         = ContextCompat.getColor(context, R.color.grey)

            if (isSelected) {
                cardStandard.setCardBackgroundColor(primaryColor)
                lblStandardName.setTextColor(white)
                lblSectionCount.setTextColor(white)
                imgCheckMark.visibility = View.VISIBLE
            } else {
                cardStandard.setCardBackgroundColor(white)
                lblStandardName.setTextColor(black)
                lblSectionCount.setTextColor(grey)
                imgCheckMark.visibility = View.GONE
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() = ShimmerUtil.startShimmer(itemView)
    }
}