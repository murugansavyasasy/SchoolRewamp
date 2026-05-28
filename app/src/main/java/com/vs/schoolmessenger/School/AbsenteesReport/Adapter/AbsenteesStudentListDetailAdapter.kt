package com.vs.schoolmessenger.School.AbsenteesReport.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.AbsenteesStudentDetailClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Model.Student
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
                        // Safe null checks with Elvis (default to empty string)
                        val studentName = it.student_name?.lowercase() ?: ""
                        val admissionNo = it.admission_no?.lowercase() ?: ""
                        val primaryMobile = it.primary_mobile?.lowercase() ?: ""
                        val studentId = it.student_id?.lowercase() ?: ""
                        val rollNo = it.roll_no?.lowercase() ?: ""

                        studentName.contains(query) ||
                                admissionNo.contains(query) ||
                                primaryMobile.contains(query) ||
                                studentId.contains(query) ||
                                rollNo.contains(query)
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

        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvAdmissionNo: TextView = itemView.findViewById(R.id.tvAdmissionNo)
        private val tvRollNo: TextView = itemView.findViewById(R.id.tvRollNo)

        private val statusFN: TextView = itemView.findViewById(R.id.statusFN)
        private val statusAN: TextView = itemView.findViewById(R.id.statusAN)
        private val imageView: ShapeableImageView = itemView.findViewById(R.id.Image_value)

        fun bind(data: Student, position: Int, listener: AbsenteesStudentDetailClickListener) {
            tvStudentName.text = data.student_name

            if (data.admission_no.isEmpty()) {
                tvAdmissionNo.visibility = View.GONE
            } else {
                tvAdmissionNo.visibility = View.VISIBLE
                tvAdmissionNo.text =
                    "${context.getString(R.string.admission_no)}: ${data.admission_no}"
            }

            if (data.roll_no.isEmpty()) {
                tvRollNo.visibility = View.GONE
            } else {
                tvRollNo.visibility = View.VISIBLE
                tvRollNo.text = "${context.getString(R.string.roll_no)}${data.roll_no}"
            }

            val attStatus = data.att_status ?: ""  // Default to empty string if null
            val statusParts = attStatus.split("/")
            val fnStatus = statusParts.getOrNull(0)?.trim() ?: "-"
            val anStatus = statusParts.getOrNull(1)?.trim() ?: "-"

            setStatusView(statusFN, fnStatus)
            setStatusView(statusAN, anStatus)






            if (data.photo_path.isNullOrEmpty()) {
                when (data.gender.lowercase()) {
                    "male" -> {
                        imageView.setImageResource(R.drawable.malesvgformatstyle)
                    }

                    "female" -> {
                        imageView.setImageResource(R.drawable.femalesvgformatstyle)
                    }

                    else -> {
                        imageView.setImageResource(R.drawable.default_profile)
                    }
                }
            } else {
                Glide.with(imageView.context).load(data.photo_path)
                    .placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                    .into(imageView)
            }

            itemView.setOnClickListener {
                listener.onFooterItemClicked(position, data)
            }

        }


        private fun setStatusView(view: TextView, status: String) {
            val drawableRes = when (status.uppercase()) {
                "P" -> R.drawable.report_present_icon
                "A" -> R.drawable.report_absent_icon
                "P~" -> R.drawable.report_latercomer_icon
                "OD" -> R.drawable.report_od_icon
                else -> R.drawable.report_nottaken_icon // or "-"
            }

            // Set background drawable
            view.background = ContextCompat.getDrawable(context, drawableRes)

            // set the status text (P, A, etc.)
            view.text = if (status == "-") "-"
            else if (status == "P~") "LA"
            else status

        }


    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
