package com.vs.schoolmessenger.School.Assignment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Assignment.Model.AssignmentStudentListClickListener
import com.vs.schoolmessenger.School.Assignment.Model.StudentSubmission
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.util.Locale
import androidx.core.graphics.toColorInt
import com.vs.schoolmessenger.Utils.Constant

class AssignmentStudentListAdapter(
    private var itemList: List<StudentSubmission>?,
    private var listener: AssignmentStudentListClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private val noDataImage: ImageView? = null,
    private val noDataText: TextView? = null,
    private val createdDate: String? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList: List<StudentSubmission> = itemList ?: emptyList()

    private var filteredList: List<StudentSubmission> = itemList ?: emptyList()

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.assignment_student_list)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.assignment_student_list, parent, false)
            DataViewHolder(view, context, listener,createdDate)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isLoading && holder is DataViewHolder) {
            filteredList.getOrNull(position)?.let { student ->
                holder.bind(student, position, this)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else filteredList.size
    }

    fun updateList(newData: List<StudentSubmission>) {
        originalList = ArrayList(newData)
        filteredList = ArrayList(newData)
        isLoading = false
        notifyDataSetChanged()

        toggleNoDataUI()
    }

    private fun toggleNoDataUI() {
        if (filteredList.isEmpty()) {
            noDataImage?.visibility = View.VISIBLE
            noDataText?.visibility = View.VISIBLE
        } else {
            noDataImage?.visibility = View.GONE
            noDataText?.visibility = View.GONE
        }
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                Log.d("NoticeBoardFilter", "originalList size: ${originalList.size}")
                val charString =
                    constraint?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""

                val resultList = if (charString.isEmpty()) {
                    originalList
                } else {
                    originalList.filter { student ->
                        student.student_name?.lowercase(Locale.getDefault())
                            ?.contains(charString) == true ||
                                student.standard?.lowercase(Locale.getDefault())
                                    ?.contains(charString) == true ||
                                student.section?.lowercase(Locale.getDefault())
                                    ?.contains(charString) == true ||
                                student.submit_status?.lowercase(Locale.getDefault())
                                    ?.contains(charString) == true
                    }
                }
                Log.d("NoticeBoardFilter", "Filtered list size: ${resultList.size}")
                val filterResults = FilterResults()
                filterResults.values = resultList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = ArrayList(results?.values as? List<StudentSubmission> ?: emptyList())
                notifyDataSetChanged()
                toggleNoDataUI()

                if (filteredList.isEmpty()) {
                    noDataImage?.visibility = View.VISIBLE
                    noDataText?.visibility = View.VISIBLE
                } else {
                    noDataImage?.visibility = View.GONE
                    noDataText?.visibility = View.GONE
                }
            }
        }
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: AssignmentStudentListClickListener,
        private  val createdDate: String?
    ) : RecyclerView.ViewHolder(itemView) {
        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)

        private val sectionLabel: TextView = itemView.findViewById(R.id.sectionlabel)
        private val statusLabel: TextView = itemView.findViewById(R.id.statuslabel)
        private val layout: RelativeLayout = itemView.findViewById(R.id.rlarelativelayout)
//        private val arrowIcon: ImageView = itemView.findViewById(R.id.arrow_icon)

        private val sectionlabel: TextView = itemView.findViewById(R.id.sectionlabel)
        private val statuslabel: TextView = itemView.findViewById(R.id.statuslabel)
        private val submittedLabel: TextView = itemView.findViewById(R.id.submittedLabel)
        private val submittedDate: TextView = itemView.findViewById(R.id.submittedDate)
        private val cancelimage: ImageView = itemView.findViewById(R.id.cancelimage)
        private val statusButton: LinearLayout = itemView.findViewById(R.id.statusButton)
        private val avatarText: TextView = itemView.findViewById(R.id.avatarText)


        fun bind(data: StudentSubmission, position: Int, adapter: AssignmentStudentListAdapter) {

            sectionlabel.text = data.standard + " - " + data.section
            val submissiondetails = data.submissions_details.firstOrNull()

            if (data.submit_status == "NOTSUBMITTED") {
                submittedLabel.text = "Due Date" + " : "
                submittedDate.text = Constant.formatCreatedDate(createdDate)
                Log.d("created_date", Constant.formatCreatedDate(createdDate))

            } else {
                submittedLabel.text = data.submit_status + " : "
                submittedDate.text = Constant.convertSubmittedDateAssignment(submissiondetails?.submitted_on)
            }

            lblStudentName.text = data.student_name

            val name = data.student_name
            avatarText.text = if (!name.isNullOrEmpty()) {
                name.first().toString().uppercase()
            } else {
                "-"
            }

            sectionLabel.text = data.standard
//            standardLabel.text = data.section
            statusLabel.text = data.submit_status

//            arrowIcon.visibility = if (data.submit_status.equals("SUBMITTED", true)) View.VISIBLE else View.GONE

            layout.setOnClickListener {
                if (data.submit_status.equals("SUBMITTED", true)) {
                    val intent = Intent(context, AssignmentStudentListDetail::class.java)
                    intent.putParcelableArrayListExtra(
                        "submission_list",
                        ArrayList(data.submissions_details)
                    )
                    context.startActivity(intent)
                } else {
                    Log.d("AssignmentAdapter", "No Redirection Available")
                }
            }

            if (data.submit_status == "SUBMITTED") {
                statuslabel.text = "Submitted"
                cancelimage.setBackgroundResource(R.drawable.correcticonsvg)
                statuslabel.setTextColor(
                    ContextCompat.getColor(context, R.color.clr_green)
                )
                statusButton.setBackgroundResource(R.drawable.completed_button_bg)
            } else {
                statuslabel.text = "Pending"
                cancelimage.setBackgroundResource(R.drawable.downloadsvgformat)
                statuslabel.setTextColor("#9e6e40".toColorInt())

                statusButton.setBackgroundResource(R.drawable.pending_button_bg)
            }


        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}

