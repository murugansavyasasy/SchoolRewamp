package com.vs.schoolmessenger.School.InteractionWithStudent

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.BlockedStudent
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil


class BlockListStudentAdapter(
    private var itemList: List<BlockedStudent>?,
    private var listener: InteractionWithStudentListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<BlockedStudent> = itemList ?: listOf()
    private var filteredList: List<BlockedStudent> = itemList ?: listOf()

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
                ShimmerUtil.wrapWithShimmer(parent, R.layout.block_interaction_student_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.block_interaction_student_item, parent, false)
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
                        it.name.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<BlockedStudent> ?: listOf()
                listener.onBlockedSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }

    inner class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val nameheader: TextView = itemView.findViewById(R.id.nameheader)

        private val blocked_on: TextView = itemView.findViewById(R.id.blocked_on)
        private val reason: TextView = itemView.findViewById(R.id.reason)

        private val lblLogo: TextView = itemView.findViewById(R.id.lblLogo)
        private val lytunblock: LinearLayout = itemView.findViewById(R.id.lytunblock)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(student: BlockedStudent, position: Int, adapter: BlockListStudentAdapter) {
            nameheader.text = student.name
            blocked_on.text = "${context.getString(R.string.Blocked_on)} : " + student.blocked_on
            reason.text = "${context.getString(R.string.reason_2)} " + student.reason
            lblLogo.text = Constant.getNameInitials(student.name)

            lytunblock.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle(context.getString(R.string.unblock_student))
                    .setMessage(context.getString(R.string.are_you_sure_you_want_to_unblock_this_student))
                    .setPositiveButton(context.getString(R.string.Yes)) { dialog, _ ->
                        listener.onUnblockClick(student, adapterPosition)
                        dialog.dismiss()
                    }
                    .setNegativeButton(context.getString(R.string.No)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
