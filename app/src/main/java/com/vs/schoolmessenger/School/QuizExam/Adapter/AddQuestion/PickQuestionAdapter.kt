package com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.QuizExam.Model.PickFromQuestionBank.GetPickFromQBankData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class PickQuestionAdapter(
    private var itemList: MutableList<GetPickFromQBankData>?,
    private var context: Context,
    private var isLoading: Boolean,
    var onSelectionChanged: ((allSelected: Boolean) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val tempSelection = mutableMapOf<String, Boolean>()

    // PickQuestionAdapter
    val currentTempSelection: Map<String, Boolean>
        get() = tempSelection.toMap() // return a copy to keep encapsulation

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.add_question_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.add_question_item, parent, false)
            DataViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList!![position].let { holder.bind(it) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    fun getAllNotImported(): List<GetPickFromQBankData> {
        return itemList?.filter { !it.checked } ?: emptyList()
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList!!.size
    }

    fun getUpdatedList(): List<GetPickFromQBankData> = itemList!!


    // Select/Deselect All (temporary only)
    fun selectAll(isChecked: Boolean) {
        if (isChecked) {
            // Mark all as temporarily selected
            itemList?.forEach { tempSelection[it.id] = true }
        } else {
            // Clear all temporary selections
            itemList?.forEach { tempSelection[it.id] = false }
        }
        notifyDataSetChanged()
    }


    fun uncheckItemById(id: String) {
        val index = itemList?.indexOfFirst { it.id == id } ?: -1
        if (index != -1) {
            itemList!![index].checked = false
            notifyItemChanged(index)
        }
        notifySelectionChanged()
    }


    fun markAsImported(imported: List<GetPickFromQBankData>) {
        imported.forEach { imp ->
            val index = itemList!!.indexOfFirst { it.id == imp.id }
            if (index != -1) {
                itemList!![index].checked = true   // permanent
                tempSelection.remove(imp.id)       // clear temp
                notifyItemChanged(index)
            }
        }
        notifySelectionChanged()
    }


    fun notifySelectionChanged() {
        val allSelected = itemList!!.all { item ->
            // If user explicitly interacted, respect tempSelection
            tempSelection[item.id] ?: item.checked
        }
        onSelectionChanged?.invoke(allSelected)
    }


    fun getSelected(): List<GetPickFromQBankData> {
        return itemList!!.filter { tempSelection[it.id] ?: it.checked }
    }

    // Reset only temporary selections (revert to permanent imported state)
    fun resetTemporarySelections() {
        tempSelection.clear()
        notifyDataSetChanged()
        notifySelectionChanged()
    }

    // Check if all items are imported (checked permanently)
    fun isAllImported(): Boolean {
        return itemList?.all { it.checked } == true
    }


    fun clearSelections() {
        tempSelection.clear()
        itemList!!.forEachIndexed { index, item ->
            if (!item.checked) notifyItemChanged(index)
        }
        notifySelectionChanged()
    }


    fun hasAnySelected(): Boolean {
        return itemList?.any { tempSelection[it.id] == true || it.checked } == true
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbQuestion: CheckBox = itemView.findViewById(R.id.cbQuestion)
        private val lnrEntireQuestion: LinearLayout = itemView.findViewById(R.id.lnrEntireQuestion)
        val edtChapterName: EditText = itemView.findViewById(R.id.edtChapterName)
        val edtQuestion: EditText = itemView.findViewById(R.id.edtQuestion)
        val edtOptionA: EditText = itemView.findViewById(R.id.edtOptionA)
        val edtOptionB: EditText = itemView.findViewById(R.id.edtOptionB)
        val edtOptionC: EditText = itemView.findViewById(R.id.edtOptionC)
        val edtOptionD: EditText = itemView.findViewById(R.id.edtOptionD)
        val edtCorrectAns: EditText = itemView.findViewById(R.id.edtCorrectAns)
        val rytSpinnerHeader: RelativeLayout = itemView.findViewById(R.id.rytSpinnerHeader)
        val edtMark: EditText = itemView.findViewById(R.id.edtMark)
        val lblremove: ImageView = itemView.findViewById(R.id.lblremove)
        val lnrAttachment: LinearLayout = itemView.findViewById(R.id.lnrAttachment)


        fun bind(data: GetPickFromQBankData) {
            rytSpinnerHeader.visibility = View.GONE
            edtCorrectAns.visibility = View.VISIBLE
            cbQuestion.setOnCheckedChangeListener(null)

            // show from tempSelection first, else permanent checked
            cbQuestion.isChecked = tempSelection[data.id] ?: data.checked

            cbQuestion.setOnCheckedChangeListener { _, isChecked ->
                tempSelection[data.id] = isChecked
                notifySelectionChanged()
            }

            lblremove.visibility = View.GONE
            lnrAttachment.visibility = View.GONE
            cbQuestion.visibility = View.VISIBLE
            edtChapterName.setText(data.chapter)
            edtQuestion.setText(data.question)
            edtOptionA.setText(data.a_option)
            edtOptionB.setText(data.b_option)
            edtOptionC.setText(data.c_option)
            edtOptionD.setText(data.d_option)
            edtCorrectAns.setText(data.correct_answer_text)
            edtMark.setText(data.mark.toString())

            edtChapterName.isFocusable = false
            edtChapterName.isClickable = false

            edtQuestion.isFocusable = false
            edtQuestion.isClickable = false

            edtOptionA.isFocusable = false
            edtOptionA.isClickable = false

            edtOptionB.isFocusable = false
            edtOptionB.isClickable = false

            edtOptionC.isFocusable = false
            edtOptionC.isClickable = false

            edtOptionD.isFocusable = false
            edtOptionD.isClickable = false

            edtCorrectAns.isFocusable = false
            edtCorrectAns.isClickable = false

            edtMark.isFocusable = false
            edtMark.isClickable = false
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
