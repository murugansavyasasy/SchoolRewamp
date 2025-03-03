package com.vs.schoolmessenger.CommonScreens

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class SpecificStudentAdapter(
    private var itemList: List<SpecificStudentData>?,
    private var listener: SpecificStudentSelectClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectAll = false

    private var expandedPosition: Int = RecyclerView.NO_POSITION // Track expanded item

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.specific_student_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        if (holder is DataViewHolder) {
            holder.bind(
                itemList!![position],
                position,
                position == expandedPosition,
                listener,
                selectAll
            )

            holder.ivArrow.setOnClickListener {
                val previousExpanded = expandedPosition
                if (expandedPosition == position) {
                    expandedPosition = RecyclerView.NO_POSITION // Collapse current item
                } else {
                    expandedPosition = position // Expand new item
                }

                notifyItemChanged(previousExpanded) // Collapse previous item
                notifyItemChanged(position) // Expand new item
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun selectAll(select: Boolean) {
        selectAll = select
        notifyDataSetChanged() // Update all items
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblFirstLetter: TextView = itemView.findViewById(R.id.lblFirstLetter)
        private val fytFirstLetter: RelativeLayout = itemView.findViewById(R.id.fytFirstLetter)
        val ivArrow: View = itemView.findViewById(R.id.ivArrow)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)
        private val rcyStudentRoleNumber: RecyclerView =
            itemView.findViewById(R.id.rcyStudentRoleNumber)
        private val selectedItems = HashSet<Int>() // Track selected items

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(
            data: SpecificStudentData,
            position: Int,
            isExpanded: Boolean,
            listener: SpecificStudentSelectClickListener,
            selectAll: Boolean
        ) {
            lblName.text = data.isName
            lblFirstLetter.text = data.isName.firstOrNull()?.uppercase() ?: "?"
            cbSelect.isChecked = selectAll

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

            // Update arrow rotation
            ivArrow.rotation = if (isExpanded) 180f else 0f

            // Expand/collapse child RecyclerView
            if (isExpanded) {
                rcyStudentRoleNumber.visibility = View.VISIBLE
                rcyStudentRoleNumber.layoutManager = LinearLayoutManager(context)
                rcyStudentRoleNumber.adapter = StudentRoleNumberAdapter(data.isStudentAddNoData)
            } else {
                rcyStudentRoleNumber.visibility = View.GONE
            }

            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (selectAll) {
                    listener.onItemClick(data)
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }
}
