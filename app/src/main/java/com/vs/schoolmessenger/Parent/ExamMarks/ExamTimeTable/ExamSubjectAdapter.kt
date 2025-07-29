package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamSubjectDetail
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.content

class ExamSubjectAdapter(private var subjectList: List<ExamSubjectDetail>) :
    RecyclerView.Adapter<ExamSubjectAdapter.SubjectViewHolder>() {

    fun updateData(newList: List<ExamSubjectDetail>) {
        subjectList = newList
        notifyDataSetChanged()
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subjectname: TextView = itemView.findViewById(R.id.subjectname)
        private val datevalue: TextView = itemView.findViewById(R.id.datevalue)
        private val syllabusvalue: TextView = itemView.findViewById(R.id.syllabusvalue)
        private val maxmarkvalue: TextView = itemView.findViewById(R.id.maxmarkvalue)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val rootHeader: LinearLayout = itemView.findViewById(R.id.rootHeader)
        private val layoutAlarm: LinearLayout = itemView.findViewById(R.id.layoutAlarm)

        fun bind(subject: ExamSubjectDetail) {
            subjectname.text = subject.subject_name
            datevalue.text =  Constant.convertToReadableDate(subject.exam_date)
            syllabusvalue.text = subject.syllabus
            lblTime.text = subject.start_time
            maxmarkvalue.text = "Marks : ${subject.max_mark}"

            val background = rootHeader.background?.mutate()

            val context = rootHeader.context
            val color = ContextCompat.getColor(
                context,
                if (position % 2 == 0) R.color.light_blue2 else R.color.light_lavender
            )
            background?.setTint(color)
            rootHeader.background = background

            layoutAlarm.setOnClickListener {
                try {
                    val context = rootHeader.context

                    // Parse the date: "dd-MM-yyyy"
                    val dateFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                    val date = dateFormat.parse(subject.exam_date)

                    // Parse start and end time: "hh:mm a"
                    val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                    val startTime = timeFormat.parse(subject.start_time)
                    val endTime = timeFormat.parse(subject.end_time)

                    if (date != null && startTime != null && endTime != null) {
                        val startCal = java.util.Calendar.getInstance()
                        val endCal = java.util.Calendar.getInstance()

                        // Set date + start time
                        startCal.time = date
                        val startHourMin = java.util.Calendar.getInstance().apply { time = startTime }
                        startCal.set(java.util.Calendar.HOUR_OF_DAY, startHourMin.get(java.util.Calendar.HOUR_OF_DAY))
                        startCal.set(java.util.Calendar.MINUTE, startHourMin.get(java.util.Calendar.MINUTE))

                        // Set date + end time
                        endCal.time = date
                        val endHourMin = java.util.Calendar.getInstance().apply { time = endTime }
                        endCal.set(java.util.Calendar.HOUR_OF_DAY, endHourMin.get(java.util.Calendar.HOUR_OF_DAY))
                        endCal.set(java.util.Calendar.MINUTE, endHourMin.get(java.util.Calendar.MINUTE))

                        // Launch Google Calendar intent
                        val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                            data = android.provider.CalendarContract.Events.CONTENT_URI
                            putExtra(android.provider.CalendarContract.Events.TITLE, subject.subject_name)
                            putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Exam Reminder: ${subject.subject_name}")
                            putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.timeInMillis)
                            putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, endCal.timeInMillis)
                        }

                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            android.widget.Toast.makeText(context, "No Calendar app found!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Invalid time/date", android.widget.Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(rootHeader.context, "Error creating calendar event", android.widget.Toast.LENGTH_SHORT).show()
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_exam_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjectList[position])
    }

    override fun getItemCount(): Int = subjectList.size
}
