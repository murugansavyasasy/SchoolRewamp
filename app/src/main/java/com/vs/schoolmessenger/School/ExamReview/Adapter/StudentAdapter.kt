package com.vs.schoolmessenger.School.ExamReview.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.CommonScreens.SpecificStudent.SpecificStudentSelectClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.Adapter.VoiceHistoryAdapter
import com.vs.schoolmessenger.Utils.ShimmerUtil


class StudentAdapter(
    private var itemList: List<NameAndIds>?,
    private var listener: SpecificStudentSelectClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private var isSingleSelect: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectAll = false
    private val selectedIds = mutableSetOf<String>()
    private var fullItemList: List<NameAndIds>? = null

    private var expandedPosition: Int = RecyclerView.NO_POSITION

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.specific_student_item_examreview)
            VoiceHistoryAdapter.ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.specific_student_item_examreview, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && itemList != null) {
            holder.bind(
                itemList!![position],
                position,
                position == expandedPosition,
                listener,
                selectedIds,
                isSingleSelect
            ) {
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        notifyDataSetChanged()
    }

    fun selectAll(select: Boolean) {
        selectAll = select
        if (select) {
            itemList?.forEach { selectedIds.add(it.id.toString()) }
        } else {
            selectedIds.clear()
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<NameAndIds> {
        return itemList?.filter { selectedIds.contains(it.id.toString()) } ?: emptyList()
    }

    fun getSelectedItem(): NameAndIds? = getSelectedItems().firstOrNull()

    fun updateList(newList: List<NameAndIds>) {
        this.itemList = newList
        selectedIds.clear()
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val cardStudent: MaterialCardView = itemView.findViewById(R.id.cardStudent)
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblRegNo: TextView = itemView.findViewById(R.id.lblRegNo)
        private val lblFirstLetter: TextView = itemView.findViewById(R.id.lblFirstLetter)
        private val fytFirstLetter: View = itemView.findViewById(R.id.fytFirstLetter)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(
            data: NameAndIds,
            position: Int,
            isExpanded: Boolean,
            listener: SpecificStudentSelectClickListener,
            selectedIds: MutableSet<String>,
            isSingleSelect: Boolean,
            onSingleSelectChanged: () -> Unit
        ) {
            lblName.text = data.name
            lblRegNo.text = "Roll No: ${data.admission_no.ifBlank { "--" }}"
            lblFirstLetter.text = data.name.firstOrNull()?.uppercase() ?: "?"

            val backgrounds = arrayOf(
                R.drawable.bg_circle_1,
                R.drawable.bg_circle_2,
                R.drawable.bg_circle_3,
                R.drawable.bg_circle_4,
                R.drawable.bg_circle_5,
                R.drawable.bg_circle_6,
                R.drawable.bg_circle_7,
                R.drawable.bg_circle_8
            )
            fytFirstLetter.setBackgroundResource(backgrounds[position % backgrounds.size])

            val isChecked = selectedIds.contains(data.id.toString())
            applySelectionState(isChecked)

            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = isChecked
            cbSelect.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    if (isSingleSelect) {
                        selectedIds.clear()
                    }
                    selectedIds.add(data.id.toString())
                    listener.onIdCheck(data)
                    if (isSingleSelect) {
                        onSingleSelectChanged()
                    } else {
                        applySelectionState(true)
                    }
                } else {
                    selectedIds.remove(data.id.toString())
                    listener.onIdUnchecked(data)
                    applySelectionState(false)
                }
            }
            itemView.setOnClickListener {
                cbSelect.isChecked = !cbSelect.isChecked
            }
        }

        private fun applySelectionState(isSelected: Boolean) {
            val primaryColor = ContextCompat.getColor(context, R.color.PrimaryColor)
            val black = ContextCompat.getColor(context, R.color.black)

            if (isSelected) {
                cardStudent.strokeWidth = context.resources.getDimensionPixelSize(R.dimen.two)
                cardStudent.strokeColor = primaryColor
                cardStudent.cardElevation = 0f
                lblName.setTextColor(primaryColor)
            } else {
                cardStudent.strokeWidth = 0
                cardStudent.cardElevation = context.resources.getDimension(R.dimen.two)
                lblName.setTextColor(black)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout?.startShimmer()
        }
    }
}