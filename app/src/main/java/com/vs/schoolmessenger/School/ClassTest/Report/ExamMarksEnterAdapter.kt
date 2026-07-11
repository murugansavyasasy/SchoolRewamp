package com.vs.schoolmessenger.School.ClassTest.Report

import android.app.DatePickerDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ClassTest.Class.ClassAdapter
import com.vs.schoolmessenger.School.ClassTest.Class.Models.ClassTestItem
import com.vs.schoolmessenger.School.ClassTest.Class.Models.TestEntry
import com.vs.schoolmessenger.School.ClassTest.Report.Model.ExamlistModel
import java.util.Calendar

class ExamMarksEnterAdapter(
    private val items: MutableList<ClassTestItem>,
    private val onDeleteTestClick: (item: ClassTestItem, test: TestEntry, testIndex: Int, itemPos: Int) -> Unit
) : RecyclerView.Adapter<ExamMarksEnterAdapter.SubjectViewHolder>() {

    fun getAllItems(): List<ClassTestItem> = items

    fun syncItems(updated: List<ClassTestItem>) {
        items.clear()
        items.addAll(updated)
        notifyDataSetChanged()
    }


    fun removeTestAt(itemPos: Int, testIndex: Int) {
        val item = items.getOrNull(itemPos) ?: return
        if (testIndex in item.tests.indices) {
            item.tests.removeAt(testIndex)
            if (item.tests.isEmpty()) {
                item.tests.add(TestEntry())
            }
            notifyItemChanged(itemPos)
        }
    }


    inner class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lytHeader: View = view.findViewById(R.id.lytSubjectHeader)
        val txtSubjectName: TextView = view.findViewById(R.id.txtSubjectName)
        val txtSectionLabel: TextView = view.findViewById(R.id.txtSectionLabel)
        val txtActivityCount: TextView = view.findViewById(R.id.txtActivityCount)
        val imgChevron: ImageView = view.findViewById(R.id.imgChevron)
        val imgCompleted: ImageView = view.findViewById(R.id.imgCompleted)
        val lytExpanded: View = view.findViewById(R.id.lytExpanded)
        val lytTestsContainer: LinearLayout = view.findViewById(R.id.lytTestsContainer)
        val btnAddTest: TextView = view.findViewById(R.id.btnAddTest)
        val dividerExpanded: View = view.findViewById(R.id.dividerExpanded)
        val lytMergeBanner: LinearLayout = view.findViewById(R.id.lytMergeBanner)
        val txtMergeTitle: TextView = view.findViewById(R.id.txtMergeTitle)
        val txtMergeSubtitle: TextView = view.findViewById(R.id.txtMergeSubtitle)
        val btnMerge: TextView = view.findViewById(R.id.btnMerge)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SubjectViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_class_test_subject, parent, false)
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val item = items[position]

        holder.txtSubjectName.text = item.subjectName
        holder.txtSectionLabel.visibility = View.GONE

        val isAllComplete = isItemFullyFilled(item)
        holder.imgCompleted.visibility = if (isAllComplete) View.VISIBLE else View.GONE

        val activityCount = item.tests.count { it.examName.isNotBlank() }
        if (activityCount > 0) {
            holder.txtActivityCount.text =
                "$activityCount ${if (activityCount == 1) "Activity" else "Activities"}"
            holder.txtActivityCount.visibility = View.VISIBLE
        } else {
            holder.txtActivityCount.visibility = View.GONE
        }
        holder.imgCompleted.visibility = View.GONE

        holder.imgChevron.animate()
            .rotation(if (item.isExpanded) 90f else 0f)
            .setDuration(200)
            .start()

        holder.lytExpanded.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
        holder.dividerExpanded.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
        if (item.isExpanded && item.tests.isEmpty()) {
            item.tests.add(TestEntry())
        }
        val sourceItem = findPreviousFilledSameSubject(position)
        if (sourceItem != null && item.isExpanded) {
            holder.txtMergeTitle.text =
                "Copy data from ${sourceItem.subjectName} · ${sourceItem.sectionLabel}?"
            holder.txtMergeSubtitle.text =
                "Fills all tests from that subject into this section"
            holder.lytMergeBanner.visibility = View.VISIBLE

            holder.btnMerge.setOnClickListener {
                mergeFromSource(sourceItem, item)
                holder.lytMergeBanner.visibility = View.GONE
                notifyItemChanged(position)
            }
        } else {
            holder.lytMergeBanner.visibility = View.GONE
        }

        holder.lytHeader.setOnClickListener {
            item.isExpanded = !item.isExpanded
            if (item.isExpanded && item.tests.isEmpty()) {
                item.tests.add(TestEntry())
            }
            notifyItemChanged(position)
        }

        if (item.isExpanded) {
            renderTestForms(holder, item, position)
        }

        holder.btnAddTest.visibility = View.GONE

//        holder.btnAddTest.setOnClickListener {
//            item.tests.add(TestEntry())
//            notifyItemChanged(position)
//        }
    }

    private fun isItemFullyFilled(item: ClassTestItem): Boolean {
        if (item.tests.isEmpty()) return false
        return item.tests.all { t ->
            t.examName.isNotBlank() &&
                    t.testDate.isNotBlank() &&
                    t.maxMarks.isNotBlank() &&
                    t.minMarks.isNotBlank()
        }
    }

    private fun findPreviousFilledSameSubject(currentPos: Int): ClassTestItem? {
        val currentSubjectId = items[currentPos].subjectId
        for (i in 0 until currentPos) {
            val candidate = items[i]
            if (candidate.subjectId == currentSubjectId &&
                candidate.tests.isNotEmpty() &&
                candidate.tests.any { it.examName.isNotBlank() }
            ) {
                return candidate
            }
        }
        return null
    }

    private fun mergeFromSource(source: ClassTestItem, target: ClassTestItem) {
        target.tests.clear()
        source.tests.forEach { original ->
            target.tests.add(
                TestEntry(
                    examName = original.examName,
                    testDate = original.testDate,
                    session = original.session,
                    maxMarks = original.maxMarks,
                    minMarks = original.minMarks,
                    syllabus = original.syllabus
                )
            )
        }
    }

    private fun renderTestForms(
        holder: SubjectViewHolder,
        item: ClassTestItem,
        position: Int
    ) {
        val ctx = holder.itemView.context
        holder.lytTestsContainer.removeAllViews()

        item.tests.forEachIndexed { index, test ->
            val formView = LayoutInflater.from(ctx)
                .inflate(R.layout.item_test_form, holder.lytTestsContainer, false)
            bindTestForm(formView, ctx, test, index, item, position)
            holder.lytTestsContainer.addView(formView)
        }
    }

    private fun bindTestForm(
        v: View,
        ctx: Context,
        test: TestEntry,
        testIndex: Int,
        item: ClassTestItem,
        itemPos: Int
    ) {
        val txtTestNumber: TextView = v.findViewById(R.id.txtTestNumber)
        val testlabel: TextView = v.findViewById(R.id.testlabel)
        val etExamName: EditText = v.findViewById(R.id.etExamName)
        val tvTestDate: TextView = v.findViewById(R.id.tvTestDate)
        val btnFN: TextView = v.findViewById(R.id.btnSessionFN)
        val btnAN: TextView = v.findViewById(R.id.btnSessionAN)
        val etMaxMarks: EditText = v.findViewById(R.id.etMaxMarks)
        val etMinMarks: EditText = v.findViewById(R.id.etMinMarks)
        val etSyllabus: EditText = v.findViewById(R.id.etSyllabus)
        val imgDelete: ImageView = v.findViewById(R.id.imgDeleteTest)
        val removetext: TextView = v.findViewById(R.id.removetext)

        txtTestNumber.text = "${testIndex + 1}"
        testlabel.text = "Activity ${testIndex + 1}"

        imgDelete.visibility = if (test.canDelete) View.VISIBLE else View.GONE
        removetext.visibility = if (test.canDelete) View.VISIBLE else View.GONE

        etExamName.isEnabled = false
        tvTestDate.isEnabled = false
        etMaxMarks.isEnabled = false
        etMinMarks.isEnabled = false
        etSyllabus.isEnabled = false
        btnFN.isEnabled = false
        btnAN.isEnabled = false


        imgDelete.setOnClickListener {
            onDeleteTestClick(item, test, testIndex, itemPos)
        }

        removetext.setOnClickListener {
            onDeleteTestClick(item, test, testIndex, itemPos)
        }



        etExamName.setText(test.examName)
        etExamName.addTextChangedListener(watcher {
            test.examName = it
            refreshCompletedState(v, item, itemPos)
        })

        if (test.testDate.isNotEmpty()) tvTestDate.text = test.testDate
        tvTestDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                ctx,
                { _, y, m, d ->
                    val s = "%02d/%02d/%04d".format(d, m + 1, y)
                    test.testDate = s
                    tvTestDate.text = s
                    // Refresh tick after date is picked
                    refreshCompletedState(v, item, itemPos)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        applySessionStyle(ctx, btnFN, btnAN, test.session)
        btnFN.setOnClickListener {
            test.session = "FN"
            applySessionStyle(ctx, btnFN, btnAN, "FN")
        }
        btnAN.setOnClickListener {
            test.session = "AN"
            applySessionStyle(ctx, btnFN, btnAN, "AN")
        }

        etMaxMarks.setText(test.maxMarks)
        etMaxMarks.addTextChangedListener(watcher {
            test.maxMarks = it
            refreshCompletedState(v, item, itemPos)
        })

        etMinMarks.setText(test.minMarks)
        etMinMarks.addTextChangedListener(watcher {
            test.minMarks = it
            refreshCompletedState(v, item, itemPos)
        })

        etSyllabus.setText(test.syllabus)
        etSyllabus.addTextChangedListener(watcher { test.syllabus = it })
    }

    private fun refreshCompletedState(
        formView: View,
        item: ClassTestItem,
        itemPos: Int
    ) {
        val recyclerView = formView.rootView
            ?.findViewById<RecyclerView>(R.id.rcClassList)
        val holder = recyclerView
            ?.findViewHolderForAdapterPosition(itemPos) as? SubjectViewHolder

        if (holder != null) {
            holder.imgCompleted.visibility =
                if (isItemFullyFilled(item)) View.VISIBLE else View.GONE
        } else {
            notifyItemChanged(itemPos)
        }
    }

    private fun applySessionStyle(
        ctx: Context,
        btnFN: TextView,
        btnAN: TextView,
        active: String
    ) {
        val white = ContextCompat.getColor(ctx, R.color.white)
        val black = ContextCompat.getColor(ctx, R.color.black)
        val grey = ContextCompat.getColor(ctx, R.color.clr_grey_dark)

        if (active == "FN") {
            btnFN.background = ContextCompat.getDrawable(ctx, R.drawable.session_toggle_bg_primary)
            btnFN.setTextColor(white)
            btnAN.background = ContextCompat.getDrawable(ctx, R.drawable.session_toggle_bg)
            btnAN.setTextColor(grey)
        } else {
            btnAN.background = ContextCompat.getDrawable(ctx, R.drawable.session_toggle_bg_primary)
            btnAN.setTextColor(white)
            btnFN.background = ContextCompat.getDrawable(ctx, R.drawable.session_toggle_bg)
            btnFN.setTextColor(black)
        }
    }

    private fun watcher(onChanged: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
        override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        override fun afterTextChanged(s: Editable?) {
            onChanged(s?.toString() ?: "")
        }
    }
}