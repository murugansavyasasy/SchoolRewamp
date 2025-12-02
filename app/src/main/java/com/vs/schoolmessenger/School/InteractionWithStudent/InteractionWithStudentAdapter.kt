package com.vs.schoolmessenger.School.InteractionWithStudent

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
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData
import com.vs.schoolmessenger.School.InteractionWithStudent.Response.InteractionWithStudentListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.interaction_student_item)
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
                        it.subject_name.lowercase().contains(query) || it.name.lowercase()
                            .contains(query) || it.section_name.lowercase().contains(query)
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
        private val lblDesc: TextView = itemView.findViewById(R.id.lblDesc)
        private val yesterdayheader: TextView = itemView.findViewById(R.id.yesterdayheader)
        private val relative_layout: RelativeLayout = itemView.findViewById(R.id.relative_layout)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(student: StudentChatData, position: Int, adapter: InteractionWithStudentAdapter) {
            nameheader.text = student.subject_name
            subjectheader.text =
                "${context.getString(R.string.Class_1)} - ${student.name} (${student.section_name})"

            unreadcount.text = student.unread_count.toString()
            lblLogo.text = Constant.getNameInitials(student.subject_name)

            if (student.last_msg.isNullOrBlank()) {
                lblDesc.text = context.getString(R.string.no_messages_yet)
            } else {
                lblDesc.text = student.last_msg
            }

            yesterdayheader.text = getRelativeTime(student.last_msg_time)

            val isVisible = student.unread_count > Constant.zero__
            unreadcount.visibility = if (isVisible) View.VISIBLE else View.GONE
            yesterdayheader.visibility = if (isVisible) View.VISIBLE else View.GONE


            relative_layout.setOnClickListener {
                if (student.unread_count > Constant.zero__) {
                    listener.onReadStatusClick(student, adapterPosition)
                } else {
                    Log.d("Message Read Status", "Message")
                }
                listener.onClickItem(student)
            }

        }

        fun getRelativeTime(apiTime: String): String {
            return try {
                val format = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
                val date = format.parse(apiTime) ?: return apiTime

                val now = Date()
                val diffInMillis = now.time - date.time

                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                when {
                    minutes < 1 -> context.getString(R.string.just_now)
                    minutes < 60 -> "$minutes ${context.getString(R.string.min_ago)}"
                    hours < 24 -> "$hours ${context.getString(R.string.hr_ago)}"
                    days < 7 -> "$days ${context.getString(R.string.day_)}${
                        if (days > 1) "${
                            context.getString(
                                R.string.s_
                            )
                        }" else ""
                    } ${context.getString(R.string.ago)}"

                    else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                apiTime
            }
        }

    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}