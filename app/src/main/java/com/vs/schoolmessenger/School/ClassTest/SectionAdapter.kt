package com.vs.schoolmessenger.School.ClassTest

import android.graphics.drawable.GradientDrawable
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

class SectionAdapter(
    private val sectionList: List<StandardSection>,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {

    private val selectedIds = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(sectionList[position])
    }

    override fun getItemCount() = sectionList.size

    fun getSelectedSections(): List<StandardSection> =
        sectionList.filter { it.sectionId in selectedIds }

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardSection: CardView = itemView.findViewById(R.id.cardSection)
        private val viewAvatar: View = itemView.findViewById(R.id.viewAvatar)
        private val lblAvatarLetter: TextView = itemView.findViewById(R.id.lblAvatarLetter)
        private val lblSectionName: TextView = itemView.findViewById(R.id.lblSectionName)
        private val lblSectionDetail: TextView = itemView.findViewById(R.id.lblSectionDetail)
        private val imgCheckMark: ImageView = itemView.findViewById(R.id.imgSectionCheck)

        fun bind(item: StandardSection) {
            lblAvatarLetter.text = item.sectionName?.firstOrNull()?.toString() ?: ""
            lblSectionName.text = "Section ${item.sectionName}"
            lblSectionDetail.text = "Standard ${item.standardName} — ${item.sectionName}"

            val isSelected = item.sectionId in selectedIds
            applySelectionState(isSelected, item)

            cardSection.setOnClickListener {
                val id = item.sectionId ?: return@setOnClickListener
                if (id in selectedIds) selectedIds.remove(id) else selectedIds.add(id)
                notifyItemChanged(adapterPosition)
                onSelectionChanged(selectedIds.size)
            }
        }

        private fun applySelectionState(isSelected: Boolean, item: StandardSection) {
            val context = itemView.context
            val primaryColor = ContextCompat.getColor(context, R.color.PrimaryColor)
            val lightPurple = ContextCompat.getColor(context, R.color.light_purple)
            val grey = ContextCompat.getColor(context, R.color.grey)
            val white = ContextCompat.getColor(context, R.color.white)

            if (isSelected) {
                val border = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = context.resources.getDimension(R.dimen.sixteen)
                    setStroke(4, primaryColor)
                    setColor(white)
                }
                cardSection.background = border
                cardSection.cardElevation = 0f

                (viewAvatar.background as? GradientDrawable)?.setColor(primaryColor)
                lblAvatarLetter.setTextColor(white)

                imgCheckMark.visibility = View.VISIBLE
                imgCheckMark.setColorFilter(primaryColor)
            } else {
                cardSection.setCardBackgroundColor(white)
                cardSection.cardElevation = context.resources.getDimension(R.dimen.two)

                (viewAvatar.background as? GradientDrawable)?.setColor(lightPurple)
                lblAvatarLetter.setTextColor(primaryColor)

                imgCheckMark.visibility = View.GONE
            }
        }
    }
}
