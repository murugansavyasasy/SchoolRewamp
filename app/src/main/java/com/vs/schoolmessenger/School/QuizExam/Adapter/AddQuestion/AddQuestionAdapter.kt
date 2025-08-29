package com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.QuestionSource
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AddQuestionAdapter(
    private var itemList: MutableList<GetQuizQuestionReportData>?,
    private var context: Context,
    private var isLoading: Boolean,
    var onQBankItemRemoved: ((String) -> Unit)? = null

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


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
            itemList!![position].let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList!!.size
    }

    fun removeItemsByIds(ids: List<String>) {
        if (ids.isEmpty()) return
        val iterator = itemList!!.iterator()
        while (iterator.hasNext()) {
            val q = iterator.next()
            if (ids.contains(q.id)) {
                iterator.remove()
            }
        }
        notifyDataSetChanged()
    }


    fun addItems(newItems: List<GetQuizQuestionReportData>) {
        val startPosition = itemList!!.size
        itemList!!.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }


    fun addItem() {
        itemList!!.add(
            GetQuizQuestionReportData(
                id = "",
                quiz_id = "",
                question = "11",
                chapter = "11",
                answer = "11",
                a_option = "11",
                b_option = "11",
                c_option = "11",
                d_option = "11",
                mark = 1,
                option_a_counts = 0,
                option_b_counts = 0,
                option_c_counts = 0,
                option_d_counts = 0,
                correct_answer_counts = 0,
                incorrect_answer_counts = 0,
                correct_answer = "",
                iframe="",
                file_size="",
                thumbnail="",
                sourceType = QuestionSource.USER,
                file_path = emptyList()
            )
        )
        notifyItemInserted(itemList!!.size - 1)
    }


    fun removeItem(position: Int) {
        if (position >= 0 && position < itemList!!.size) {
            val removed = itemList!![position]

            // If it's a QBANK question → notify PickQuestionAdapter
            if (removed.sourceType == QuestionSource.QBANK && removed.id.isNotEmpty()) {
                onQBankItemRemoved?.invoke(removed.id)
                Constant.isQuestionLimit += 1
            }

            itemList!!.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemList!!.size)
            Constant.isQuestionLimit += 1
        }
    }

    fun showValidationErrors(recyclerView: RecyclerView): Boolean {
        var isAllValid = true

        itemList!!.forEachIndexed { index, item ->
            val holder =
                recyclerView.findViewHolderForAdapterPosition(index) as? DataViewHolder
                    ?: return@forEachIndexed

            if (item.question.isBlank()) {
                holder.edtChapterName.error = "This is required!"
                if (isAllValid) holder.edtChapterName.requestFocus()
                isAllValid = false
            }
            if (item.question.isBlank()) {
                holder.edtQuestion.error = "This is required!"
                if (isAllValid) holder.edtQuestion.requestFocus()
                isAllValid = false
            }
            if (item.a_option.isBlank()) {
                holder.edtOptionA.error = "This is required!"
                if (isAllValid) holder.edtOptionA.requestFocus()
                isAllValid = false
            }
            if (item.b_option.isBlank()) {
                holder.edtOptionB.error = "This is required!"
                if (isAllValid) holder.edtOptionB.requestFocus()
                isAllValid = false
            }
            if (item.c_option.isBlank()) {
                holder.edtOptionC.error = "This is required!"
                if (isAllValid) holder.edtOptionC.requestFocus()
                isAllValid = false
            }
            if (item.d_option.isBlank()) {
                holder.edtOptionD.error = "This is required!"
                if (isAllValid) holder.edtOptionD.requestFocus()
                isAllValid = false
            }
            if (item.answer.isBlank()) {
                holder.edtCorrectAns.error = "This is required!"
                if (isAllValid) holder.edtCorrectAns.requestFocus()
                isAllValid = false
            }
            if (item.mark == null) {
                holder.edtMark.error = "This is required!"
                if (isAllValid) holder.edtMark.requestFocus()
                isAllValid = false
            } else if (item.mark <= 0) {
                holder.edtMark.error = "Mark should be greater than zero!"
                if (isAllValid) holder.edtMark.requestFocus()
                isAllValid = false
            }

        }

        return isAllValid
    }

    fun updateList(newList: MutableList<GetQuizQuestionReportData>) {
        itemList!!.clear()
        itemList!!.addAll(newList)
        notifyDataSetChanged()
    }


    fun getUpdatedList(): List<GetQuizQuestionReportData> = itemList!!

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


        fun bind(data: GetQuizQuestionReportData, position: Int) {

            edtChapterName.setText(data.chapter)
            edtQuestion.setText(data.question)
            edtOptionA.setText(data.a_option)
            edtOptionB.setText(data.b_option)
            edtOptionC.setText(data.c_option)
            edtOptionD.setText(data.d_option)
            edtCorrectAns.setText(data.answer)
            edtMark.setText(data.mark.toString())

            edtChapterName.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].chapter = text.toString()
                }
            }
            edtQuestion.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].question = text.toString()
                }
            }
            edtOptionA.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].a_option = text.toString()
                }
            }
            edtOptionB.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].b_option = text.toString()
                }
            }
            edtOptionC.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].c_option = text.toString()
                }
            }
            edtOptionD.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].d_option = text.toString()
                }
            }
            edtCorrectAns.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].answer = text.toString()
                }
            }
            edtMark.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].mark = text.toString().toIntOrNull() ?: 0
                }
            }


            // remove item
            lblremove.setOnClickListener {
                removeItem(position)
            }
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}


