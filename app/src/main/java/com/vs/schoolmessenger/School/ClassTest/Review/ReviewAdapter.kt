package com.vs.schoolmessenger.School.ClassTest.Review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ClassTest.Class.Models.ClassTestItem

class ReviewAdapter(
    private val items: List<ClassTestItem>,
    private val onRemoveTest: (subjectIndex: Int, testIndex: Int) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewVH>() {

    inner class ReviewVH(view: View) : RecyclerView.ViewHolder(view) {
        val txtSubjectName: TextView = view.findViewById(R.id.txtReviewSubjectName)
        val txtSectionLabel: TextView = view.findViewById(R.id.txtReviewSectionLabel)
        val txtActivityCount: TextView = view.findViewById(R.id.txtActivityCount)
        val lytTestSummaries: LinearLayout = view.findViewById(R.id.lytReviewTests)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ReviewVH(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_subject, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ReviewVH, position: Int) {
        val item = items[position]

        holder.txtSubjectName.text = item.subjectName
        holder.txtSectionLabel.text = item.sectionLabel

        val testCount = item.tests.size
        if (testCount == 0) {
            holder.txtActivityCount.text = "No activity added"
            holder.txtActivityCount.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
            )
        } else {
            holder.txtActivityCount.text = "$testCount ${if (testCount == 1) "activity" else "activities"}"
            holder.txtActivityCount.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.clr_grey_dark)
            )
        }
        holder.txtActivityCount.visibility = View.VISIBLE

        holder.lytTestSummaries.removeAllViews()

        val testSnapshot = item.tests.toList()

        item.tests.forEachIndexed { testIndex, test ->
            val row = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_review_test_row, holder.lytTestSummaries, false)

            row.findViewById<TextView>(R.id.txtReviewTestNumber).text = "${testIndex + 1}"

            row.findViewById<TextView>(R.id.txtReviewExamName).text =
                test.examName.ifBlank { "—" }

            row.findViewById<TextView>(R.id.txtReviewSession).text = test.session

            val imgRemove = row.findViewById<ImageView>(R.id.imgReviewDelete)
            val txtRemove = row.findViewById<TextView>(R.id.txtReviewRemove)
            imgRemove.setOnClickListener { onRemoveTest(position, testIndex) }
            txtRemove.setOnClickListener { onRemoveTest(position, testIndex) }

            row.findViewById<TextView>(R.id.txtReviewDate).text =
                if (test.testDate.isBlank()) "No date" else test.testDate
            row.findViewById<TextView>(R.id.txtReviewMax).text = test.maxMarks
            row.findViewById<TextView>(R.id.txtReviewMin).text = test.minMarks

            val txtSyllabus = row.findViewById<TextView>(R.id.txtReviewSyllabus)
            val lytSyllabus = row.findViewById<View>(R.id.lytSyllabusSection)
            if (test.syllabus.isNotBlank()) {
                lytSyllabus.visibility = View.VISIBLE
                txtSyllabus.text = test.syllabus
            } else {
                lytSyllabus.visibility = View.GONE
            }

            row.findViewById<View>(R.id.dividerTestRow).visibility =
                if (testIndex < item.tests.size - 1) View.VISIBLE else View.GONE

            holder.lytTestSummaries.addView(row)
        }
    }
}