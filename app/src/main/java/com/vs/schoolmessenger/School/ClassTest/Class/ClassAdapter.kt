package com.vs.schoolmessenger.School.ClassTest.Class

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ClassTest.Class.Models.ClassTestItem
import com.vs.schoolmessenger.School.ClassTest.Class.Models.TestEntry
import java.util.Calendar
import java.util.Locale

class ClassAdapter(var context: Context,
    private val items: MutableList<ClassTestItem>
) : RecyclerView.Adapter<ClassAdapter.SubjectViewHolder>() {

    fun getAllItems(): List<ClassTestItem> = items

    fun syncItems(updated: List<ClassTestItem>) {
        items.clear()
        items.addAll(updated)
        notifyDataSetChanged()
    }

    inner class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lytHeader: View = view.findViewById(R.id.lytSubjectHeader)
        val txtSubjectName: TextView = view.findViewById(R.id.txtSubjectName)
        val txtSectionLabel: TextView = view.findViewById(R.id.txtSectionLabel)
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
        holder.txtSectionLabel.text = item.sectionLabel

        val isAllComplete = isItemFullyFilled(item)
        holder.imgCompleted.visibility = if (isAllComplete) View.VISIBLE else View.GONE

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
        val shouldShowBanner = sourceItem != null && item.isExpanded &&
                (!item.isMerged || snapshotOf(sourceItem) != item.mergedSourceSnapshot)

        if (shouldShowBanner && sourceItem != null) {
            val label = if (item.isMerged)
                "Source data changed — re-copy from ${sourceItem.subjectName} · ${sourceItem.sectionLabel}?"
            else
                "Copy data from ${sourceItem.subjectName} · ${sourceItem.sectionLabel}?"

            holder.txtMergeTitle.text = label
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
        holder.btnAddTest.setOnClickListener {
            val lastTest = item.tests.lastOrNull()
            if (lastTest != null && (lastTest.examName.isBlank())) {
                Toast.makeText(
                    holder.itemView.context,
                    "Please fill Activity Name  before adding another activity",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            item.tests.add(TestEntry())
            notifyItemChanged(position)
            notifyMergeDependents(position)
        }
    }

    private fun isItemFullyFilled(item: ClassTestItem): Boolean {
        if (item.tests.isEmpty()) return false
        return item.tests.all { t ->
            t.examName.isNotBlank() &&
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
        target.isMerged = true
        target.mergedSourceSnapshot = snapshotOf(source)
    }


    private fun snapshotOf(item: ClassTestItem): String {
        return item.tests.joinToString("||") { t ->
            "${t.examName}~${t.testDate}~${t.session}~${t.maxMarks}~${t.minMarks}~${t.syllabus}"
        }
    }


    private fun notifyMergeDependents(sourceItemPos: Int) {
        if (sourceItemPos !in items.indices) return
        val subjectId = items[sourceItemPos].subjectId
        items.indices
            .filter { it != sourceItemPos && items[it].subjectId == subjectId }
            .forEach { notifyItemChanged(it) }
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

        val focusScrollListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.post {
                    val recyclerView = v.rootView.findViewById<RecyclerView>(R.id.rcClassList)
                    recyclerView?.let { rv ->
                        val rect = Rect()
                        view.getDrawingRect(rect)
                        view.requestRectangleOnScreen(rect, true)
                    }
                }
            }
        }

        etExamName.onFocusChangeListener = focusScrollListener
        etMaxMarks.onFocusChangeListener = focusScrollListener
        etMinMarks.onFocusChangeListener = focusScrollListener
        etSyllabus.onFocusChangeListener = focusScrollListener

        txtTestNumber.text = "${testIndex + 1}"
        testlabel.text = "Activity ${testIndex + 1}"

        imgDelete.visibility = View.VISIBLE
        removetext.visibility = View.VISIBLE
        imgDelete.setOnClickListener {
            item.tests.removeAt(testIndex)
            if (item.tests.isEmpty()) {
                item.tests.add(TestEntry())
            }
            notifyItemChanged(itemPos)
            notifyMergeDependents(itemPos)
        }

        removetext.setOnClickListener {
            item.tests.removeAt(testIndex)
            if (item.tests.isEmpty()) {
                item.tests.add(TestEntry())
            }
            notifyItemChanged(itemPos)
            notifyMergeDependents(itemPos)
        }

        etExamName.setText(test.examName)
        etExamName.addTextChangedListener(watcher {
            test.examName = it
            refreshCompletedState(v, item, itemPos)
        })

        if (test.testDate.isNotEmpty()) tvTestDate.text = test.testDate
        tvTestDate.setOnClickListener {

            val originalLocale = Locale.getDefault()
            Locale.setDefault(Locale.ENGLISH)

            val config = Configuration(context.resources.configuration)
            config.setLocale(Locale.ENGLISH)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            val cal = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                ctx,
                { _, y, m, d ->

                    Locale.setDefault(originalLocale)
                    context.resources.updateConfiguration(
                        Configuration(context.resources.configuration).apply {
                            setLocale(originalLocale)
                        },
                        context.resources.displayMetrics
                    )

                    val s = String.format(Locale.ENGLISH, "%02d/%02d/%04d", d, m + 1, y)
                    test.testDate = s
                    tvTestDate.text = s
                    refreshCompletedState(v, item, itemPos)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

            datePickerDialog.show()
        }

        applySessionStyle(ctx, btnFN, btnAN, test.session)
        btnFN.setOnClickListener {
            test.session = "FN"
            applySessionStyle(ctx, btnFN, btnAN, "FN")
            notifyMergeDependents(itemPos)
        }
        btnAN.setOnClickListener {
            test.session = "AN"
            applySessionStyle(ctx, btnFN, btnAN, "AN")
            notifyMergeDependents(itemPos)
        }

        etMaxMarks.setText(test.maxMarks)
        etMaxMarks.addTextChangedListener(watcher {
            test.maxMarks = it
            refreshCompletedState(v, item, itemPos)
            validateMarksRange(ctx, etMaxMarks, etMinMarks)
        })

        etMinMarks.setText(test.minMarks)
        etMinMarks.addTextChangedListener(watcher {
            test.minMarks = it
            refreshCompletedState(v, item, itemPos)
            validateMarksRange(ctx, etMaxMarks, etMinMarks)
        })

        // Run once on bind so restored/saved invalid data is flagged immediately
        validateMarksRange(ctx, etMaxMarks, etMinMarks)

        etSyllabus.setText(test.syllabus)
        etSyllabus.addTextChangedListener(watcher { test.syllabus = it })
    }


    private fun validateMarksRange(
        ctx: Context,
        etMaxMarks: EditText,
        etMinMarks: EditText
    ): Boolean {
        val maxText = etMaxMarks.text?.toString()?.trim().orEmpty()
        val minText = etMinMarks.text?.toString()?.trim().orEmpty()

        if (maxText.isEmpty() || minText.isEmpty()) {
            etMinMarks.error = null
            etMinMarks.background = ContextCompat.getDrawable(ctx, R.drawable.input_field_bg)
            return true
        }

        val maxVal = maxText.toDoubleOrNull()
        val minVal = minText.toDoubleOrNull()

        val isValid = maxVal != null && minVal != null && minVal < maxVal

        if (isValid) {
            etMinMarks.error = null
            etMinMarks.background = ContextCompat.getDrawable(ctx, R.drawable.input_field_bg)
        } else {
            etMinMarks.background = ContextCompat.getDrawable(ctx, R.drawable.input_field_bg_error)
            etMinMarks.error = "Min marks must be less than Max marks"
        }

        return isValid
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

        notifyMergeDependents(itemPos)
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