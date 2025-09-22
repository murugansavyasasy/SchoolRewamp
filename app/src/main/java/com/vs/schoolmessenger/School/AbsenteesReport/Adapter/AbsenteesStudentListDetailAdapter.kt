package com.vs.schoolmessenger.School.AbsenteesReport.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesStudentDetailClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Model.Student
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AbsenteesStudentListDetailAdapter(
    private var itemList: List<Student>?,
    private val listener: AbsenteesStudentDetailClickListener,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<Student> = itemList ?: listOf()
    private var filteredList: List<Student> = itemList ?: listOf()

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
                ShimmerUtil.wrapWithShimmer(parent, R.layout.absentees_student_footerlist)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.absentees_student_footerlist, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position, listener)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else filteredList.size
    }

    fun updateData(newList: List<Student>) {
        isLoading = false
        itemList = newList
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.student_name.lowercase().contains(query) || it.admission_no.lowercase()
                            .contains(query) || it.primary_mobile.lowercase()
                            .contains(query) || it.student_id.lowercase()
                            .contains(query) || it.roll_no.lowercase().contains(query)
                    }
                }
                return FilterResults().apply { values = result }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<Student> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val studentName: TextView = itemView.findViewById(R.id.student_name)
        private val sectionValue: TextView = itemView.findViewById(R.id.section_value)
        private val registerNumber: TextView = itemView.findViewById(R.id.register_number)
        private val imageView: TextView = itemView.findViewById(R.id.Image_value)
        private val buttoncall: TextView = itemView.findViewById(R.id.buttoncall)
        private val linearlayout: LinearLayout = itemView.findViewById(R.id.relative_layout)

        fun bind(data: Student, position: Int, listener: AbsenteesStudentDetailClickListener) {
            studentName.text = data.student_name
            registerNumber.text = "Admission No : " + data.admission_no


            when (data.gender) {
                "male" -> {
                    imageView.setBackgroundResource(R.drawable.malesvgformatstyle)
                }
                "female" -> {
                    imageView.setBackgroundResource(R.drawable.femalesvgformatstyle)
                }
                else -> {
                    imageView.setBackgroundResource(R.drawable.default_profile)
                }
            }


            Constant.isAbsenteesReportDataSending?.let { report ->
                val sectionNamesCombined =
                    report.section_wise?.joinToString(", ") { it.section_name } ?: ""
                sectionValue.text = "${report.class_name ?: ""} - $sectionNamesCombined"
            }

            itemView.setOnClickListener {
                listener.onFooterItemClicked(position, data)
            }

            linearlayout.setOnClickListener {
                Constant.redirectToDialPad(context, data.primary_mobile)
            }

        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
