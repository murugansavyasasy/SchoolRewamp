package com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Listener.InteractionWithStaffListener
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.Staff
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil


class InteractionWithStaffAdapter(
    private var itemList: List<Staff>?,
    private var listener: InteractionWithStaffListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<Staff> = itemList ?: listOf()
    private var filteredList: List<Staff> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.interaction_with_staff_report)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.interaction_with_staff_report, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position, this)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.subject_name.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<Staff> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }

    inner class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val nameheader: TextView = itemView.findViewById(R.id.nameheader)
        private val subjectheader: TextView = itemView.findViewById(R.id.subjectheader)
        private val unreadcount: TextView = itemView.findViewById(R.id.unreadcount)
        private val yesterdayheader: TextView = itemView.findViewById(R.id.yesterdayheader)
        private val lblDesc: TextView = itemView.findViewById(R.id.lblDesc)
        private val lblLogo: TextView = itemView.findViewById(R.id.lblLogo)
        private val relative_layout: RelativeLayout = itemView.findViewById(R.id.relative_layout)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(staff: Staff, position: Int, adapter: InteractionWithStaffAdapter) {


            nameheader.text = staff.name
            subjectheader.text = staff.subject_name
            unreadcount.text = staff.unread_count
            lblLogo.text = Constant.getNameInitials(staff.name)
            yesterdayheader.text = staff.last_msg_time

            if (staff.last_msg.isNullOrBlank()) {
                lblDesc.text = "No messages yet"
            } else {
                lblDesc.text = staff.last_msg

            }



            if (staff.unread_count > Constant.zero) {
                unreadcount.visibility = View.VISIBLE
                yesterdayheader.visibility = View.VISIBLE
            } else {
                unreadcount.visibility = View.GONE
                yesterdayheader.visibility = View.GONE
            }

            relative_layout.setOnClickListener {
                if (staff.is_assigned == true) {
                    listener.onClickItem(staff)
                } else {
                    Log.d("Listener Status", "The isAssigned Value is False")
                }
            }

        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
