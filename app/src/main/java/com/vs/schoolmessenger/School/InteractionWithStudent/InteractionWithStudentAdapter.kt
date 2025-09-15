package com.vs.schoolmessenger.School.InteractionWithStudent

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class InteractionWithStudentAdapter(
    private var itemList: List<StudentChatData>?,
    private var listener: InteractionWithStudentListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<StudentChatData> = itemList ?: listOf()
    private var filteredList: List<StudentChatData> = itemList ?: listOf()

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
                ShimmerUtil.wrapWithShimmer(parent, R.layout.interaction_student_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.interaction_student_item, parent, false)
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
                filteredList = results?.values as? List<StudentChatData> ?: listOf()
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
        private val lblLogo: TextView = itemView.findViewById(R.id.lblLogo)
        private val relative_layout: RelativeLayout = itemView.findViewById(R.id.relative_layout)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(student: StudentChatData, position: Int, adapter: InteractionWithStudentAdapter) {
            nameheader.text = student.name
            subjectheader.text = student.subject_name
//            unreadcount.text = student.unread_count
            lblLogo.text = Constant.getNameInitials(student.name)


            relative_layout.setOnClickListener {
                listener.onClickItem(student)
            }

        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
