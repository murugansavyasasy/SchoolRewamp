package com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.QuestionSource
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter

class AddQuestionAdapter(
    private var itemList: MutableList<GetQuizQuestionReportData>?,
    private var context: Context,
    private var isLoading: Boolean,
    var onQBankItemRemoved: ((String) -> Unit)? = null


) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private val itemsCorrectAnswer = listOf(
        "Select correct answer", "Option A", "Option B", "Option C", "Option D"
    )

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
                question = "",
                chapter = "",
                answer = "",
                a_option = "",
                b_option = "",
                c_option = "",
                d_option = "",
                mark = 0,
                iframe="",
                file_size="",
                thumbnail="",
                sourceType = QuestionSource.USER,
                file_path = emptyList()
            )
        )
        Constant.isQuestionLimit -= 1
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

            if (item.chapter.isBlank()) {
                holder.edtChapterName.error = context.getString(R.string.this_is_required)
                if (isAllValid) holder.edtChapterName.requestFocus()
                isAllValid = false
            }
            if (item.question.isBlank()) {
                holder.edtQuestion.error = context.getString(R.string.this_is_required)
                if (isAllValid) holder.edtQuestion.requestFocus()
                isAllValid = false
            }
            if (item.a_option.isBlank()) {
                holder.edtOptionA.error = context.getString(R.string.this_is_required)
                if (isAllValid) holder.edtOptionA.requestFocus()
                isAllValid = false
            }
            if (item.b_option.isBlank()) {
                holder.edtOptionB.error =context.getString(R.string.this_is_required)
                if (isAllValid) holder.edtOptionB.requestFocus()
                isAllValid = false
            }
            if (item.c_option.isBlank()) {
                holder.edtOptionC.error = context.getString(R.string.this_is_required)
                if (isAllValid) holder.edtOptionC.requestFocus()
                isAllValid = false
            }
            if (item.d_option.isBlank()) {
                holder.edtOptionD.error = context.getString(R.string.this_is_required)
                if (isAllValid) holder.edtOptionD.requestFocus()
                isAllValid = false
            }

            if (item.answer.isBlank() || item.answer == "0") {
                Toast.makeText(context, "Please select correct answer", Toast.LENGTH_SHORT).show()
                isAllValid = false
            }

            if (item.mark == null) {
                holder.edtMark.error = context.getString(R.string.this_is_required)
                if (isAllValid) holder.edtMark.requestFocus()
                isAllValid = false
            } else if (item.mark <= 0) {
                holder.edtMark.error = context.getString(R.string.mark_should_be_greater_than_zero)
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

    private fun updateSpinnerOptions(holder: DataViewHolder, data: GetQuizQuestionReportData) {
        val optionsList = listOf(
            "Select correct answer",
            data.a_option.ifBlank { "Option A" },
            data.b_option.ifBlank { "Option B" },
            data.c_option.ifBlank { "Option C" },
            data.d_option.ifBlank { "Option D" }
        )

        val spinnerAdapter = SpinnerLoadingAdapter(context, optionsList)
        holder.spinnerCorrectAnswer.adapter = spinnerAdapter

        // restore previously selected answer
        val selectedIndex = data.answer.toIntOrNull() ?: 0
        holder.spinnerCorrectAnswer.setSelection(selectedIndex)
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val edtChapterName: EditText = itemView.findViewById(R.id.edtChapterName)
        val edtQuestion: EditText = itemView.findViewById(R.id.edtQuestion)
        val edtOptionA: EditText = itemView.findViewById(R.id.edtOptionA)
        val edtOptionB: EditText = itemView.findViewById(R.id.edtOptionB)
        val edtOptionC: EditText = itemView.findViewById(R.id.edtOptionC)
        val edtOptionD: EditText = itemView.findViewById(R.id.edtOptionD)
        val spinnerCorrectAnswer: Spinner = itemView.findViewById(R.id.spinnerCorrectAnswer)
        val rytSpinnerHeader: RelativeLayout = itemView.findViewById(R.id.rytSpinnerHeader)
        val edtCorrectAns: EditText = itemView.findViewById(R.id.edtCorrectAns)
        val edtMark: EditText = itemView.findViewById(R.id.edtMark)
        val lblremove: ImageView = itemView.findViewById(R.id.lblremove)


        fun bind(data: GetQuizQuestionReportData, position: Int) {

            rytSpinnerHeader.visibility=View.VISIBLE
            edtCorrectAns.visibility=View.GONE

            edtChapterName.setText(data.chapter)
            edtQuestion.setText(data.question)
            edtOptionA.setText(data.a_option)
            edtOptionB.setText(data.b_option)
            edtOptionC.setText(data.c_option)
            edtOptionD.setText(data.d_option)
//            edtCorrectAns.setText(data.answer)
            edtMark.setText(data.mark.toString())

            val optionsList = listOf(
                "Select correct answer",
                data.a_option.ifBlank { "Option A" },
                data.b_option.ifBlank { "Option B" },
                data.c_option.ifBlank { "Option C" },
                data.d_option.ifBlank { "Option D" }
            )

            val spinnerAdapter = SpinnerLoadingAdapter(context, optionsList)
            spinnerCorrectAnswer.adapter = spinnerAdapter

            // pre-select saved answer index
            val selectedIndex = data.answer.toIntOrNull() ?: 0
            spinnerCorrectAnswer.setSelection(selectedIndex)

            spinnerCorrectAnswer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        itemList!![adapterPosition].answer = pos.toString()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }


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
                    updateSpinnerOptions(this, itemList!![adapterPosition])
                }
            }
            edtOptionB.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].b_option = text.toString()
                    updateSpinnerOptions(this, itemList!![adapterPosition])

                }
            }
            edtOptionC.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].c_option = text.toString()
                    updateSpinnerOptions(this, itemList!![adapterPosition])

                }
            }
            edtOptionD.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].d_option = text.toString()
                    updateSpinnerOptions(this, itemList!![adapterPosition])

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


