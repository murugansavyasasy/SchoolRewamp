package com.vs.schoolmessenger.School.Assignment

import android.content.Context
import android.content.Intent
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
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Assignment.Model.AssignmentStudentListClickListener
import com.vs.schoolmessenger.School.Assignment.Model.StudentSubmission
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.util.Locale

class AssignmentStudentListAdapter(
    private var itemList: List<StudentSubmission>?,
    private var listener: AssignmentStudentListClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private val noDataImage: ImageView? = null,
    private val noDataText: TextView? = null,
    private val createdDate: String? = null,
    private val title: String? = null,
    private val assignmentSubject: String? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList: MutableList<StudentSubmission> =
        (itemList ?: emptyList()).toMutableList()
    private var filteredList: MutableList<StudentSubmission> = originalList.toMutableList()

    var onDataChange: ((Boolean) -> Unit)? = null

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
            DataViewHolder(view, context, listener, createdDate, title, assignmentSubject)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isLoading && holder is DataViewHolder) {
            filteredList.getOrNull(position)?.let { student ->
                holder.bind(student, position)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else filteredList.size
    }

    fun updateData(newData: List<StudentSubmission>) {
        originalList.clear()
        originalList.addAll(newData)
        filteredList.clear()
        filteredList.addAll(newData)
        isLoading = false
        notifyDataSetChanged()
        onDataChange?.invoke(filteredList.isNotEmpty())
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
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

                val filterResults = FilterResults()
                filterResults.values = resultList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList.clear()
                filteredList.addAll(results?.values as? List<StudentSubmission> ?: emptyList())
                notifyDataSetChanged()
                onDataChange?.invoke(filteredList.isNotEmpty())
            }
        }
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: AssignmentStudentListClickListener,
        private val createdDate: String?,
        private val title: String?,
        private val assignmentSubject: String?
    ) : RecyclerView.ViewHolder(itemView) {

        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)
        private val sectionLabel: TextView = itemView.findViewById(R.id.sectionlabel)
        private val statusLabel: TextView = itemView.findViewById(R.id.statuslabel)
        private val layout: RelativeLayout = itemView.findViewById(R.id.rlarelativelayout)
        private val submittedLabel: TextView = itemView.findViewById(R.id.submittedLabel)
        private val submittedDate: TextView = itemView.findViewById(R.id.submittedDate)
        private val cancelImage: ImageView = itemView.findViewById(R.id.cancelimage)
        private val statusButton: LinearLayout = itemView.findViewById(R.id.statusButton)
        private val avatarText: TextView = itemView.findViewById(R.id.avatarText)
        private val statusText: TextView = itemView.findViewById(R.id.statuslabel)

        fun bind(data: StudentSubmission, position: Int) {
            val submissionDetails = data.submissions_details.firstOrNull()


            sectionLabel.text = buildString {
                append(data.standard?.trim() ?: "")
                if (!data.section.isNullOrEmpty()) {
                    append(" - ${data.section.trim()}")
                }
            }
            Log.d("FinalText", sectionLabel.text.toString())


            if (data.submit_status == Constant.NOTSUBMITTED) {
                submittedLabel.text = "${context.getString(R.string.Due_Date)} : "
                submittedDate.text = Constant.formatCreatedDate(createdDate)
                Log.d("created_date", Constant.formatCreatedDate(createdDate))
            } else {
                submittedLabel.text = "${data.submit_status} : "
                submittedDate.text =
                    Constant.convertSubmittedDateAssignment(submissionDetails?.submitted_on)
            }

            lblStudentName.text = data.student_name
            avatarText.text = data.student_name?.firstOrNull()?.uppercase()?.toString() ?: "-"

            statusLabel.text = data.submit_status

            layout.setOnClickListener {

                if (data.submit_status.equals(Constant.SUBMITTED, true)) {
                    val intent = Intent(context, AssignmentStudentListDetail::class.java).apply {
                        putParcelableArrayListExtra(
                            Constant.submission_list,
                            ArrayList(data.submissions_details)
                        )
                        putExtra("title", title)
                        putExtra(Constant.assignmentsubject, assignmentSubject)
                        Log.d("titleAssignmentStudentlist", title.toString())
                        Log.d("descriptionAssignmentStudentlist", assignmentSubject.toString())

                    }
                    context.startActivity(intent)
                } else {
                    Log.d("AssignmentAdapter", "No Redirection Available")
                }
            }


            if (data.submit_status == Constant.SUBMITTED) {
                statusText.text = context.getString(R.string.submitted)
                cancelImage.setBackgroundResource(R.drawable.correcticonsvg)
                statusText.setTextColor(ContextCompat.getColor(context, R.color.clr_green))
                statusButton.setBackgroundResource(R.drawable.completed_button_bg)
            } else {
                statusText.text = context.getString(R.string.pending)
                cancelImage.setBackgroundResource(R.drawable.downloadsvgformat)
                statusText.setTextColor("#9e6e40".toColorInt())
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
