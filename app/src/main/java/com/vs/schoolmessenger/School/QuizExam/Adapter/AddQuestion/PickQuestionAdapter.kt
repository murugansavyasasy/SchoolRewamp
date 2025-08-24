package com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.QuizExam.Model.PickFromQuestionBank.GetPickFromQBankData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class PickQuestionAdapter(
    private var itemList: MutableList<GetPickFromQBankData>?,
    private var context: Context,
    private var isLoading: Boolean,
    private val onItemCheckedChange: ((Boolean) -> Unit)? = null

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    // 🔹 store only IDs of selected questions
    private val selectedIds = mutableSetOf<String>() // change to String if id is String

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

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList!!.size
    }

    fun getUpdatedList(): List<GetPickFromQBankData> = itemList!!

    // 🔹 Select/Deselect All
//    fun selectAll(isSelectAll: Boolean) {
//        selectedIds.clear()
//        if (isSelectAll) {
//            selectedIds.addAll(itemList!!.map { it.id }) // assumes id is Int
//        }
//        notifyDataSetChanged()
//    }

    fun selectAll(isSelectAll: Boolean) {
        selectedIds.clear()

        itemList?.forEach { item ->
            item.checked = isSelectAll
            if (isSelectAll) {
                selectedIds.add(item.id)
            }
        }

        notifyDataSetChanged()
    }


    fun getSelectedQuestions(): List<GetPickFromQBankData> {
        return itemList!!.filter { selectedIds.contains(it.id) }
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val edtChapterName: EditText = itemView.findViewById(R.id.edtChapterName)
        val edtQuestion: EditText = itemView.findViewById(R.id.edtQuestion)
        val edtOptionA: EditText = itemView.findViewById(R.id.edtOptionA)
        val edtOptionB: EditText = itemView.findViewById(R.id.edtOptionB)
        val edtOptionC: EditText = itemView.findViewById(R.id.edtOptionC)
        val edtOptionD: EditText = itemView.findViewById(R.id.edtOptionD)
        val edtCorrectAns: EditText = itemView.findViewById(R.id.edtCorrectAns)
        val edtMark: EditText = itemView.findViewById(R.id.edtMark)
        val lblremove: ImageView = itemView.findViewById(R.id.lblremove)
        val cbQuestion: android.widget.CheckBox = itemView.findViewById(R.id.cbQuestion)
        val lnrAttachment: LinearLayout = itemView.findViewById(R.id.lnrAttachment)

        fun bind(data: GetPickFromQBankData) {
            lblremove.visibility = View.GONE
            lnrAttachment.visibility=View.GONE
            cbQuestion.visibility=View.VISIBLE
            edtChapterName.setText(data.chapter)
            edtQuestion.setText(data.question)
            edtOptionA.setText(data.a_option)
            edtOptionB.setText(data.b_option)
            edtOptionC.setText(data.c_option)
            edtOptionD.setText(data.d_option)
            edtCorrectAns.setText(data.answer)
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

            // 🔹 Selection binding

            cbQuestion.setOnCheckedChangeListener(null)
            cbQuestion.isChecked = data.checked  // default is false

            cbQuestion.setOnCheckedChangeListener { _, isChecked ->
                data.checked = isChecked
                if (isChecked) selectedIds.add(data.id)
                else selectedIds.remove(data.id)
                onItemCheckedChange?.invoke(isChecked)
            }

        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
