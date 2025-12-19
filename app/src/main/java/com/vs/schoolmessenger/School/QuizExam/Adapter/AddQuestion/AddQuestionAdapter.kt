package com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.QuizExam.AddQuestionListner
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.QuestionSource
import com.vs.schoolmessenger.School.QuizExam.OnAttachmentListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter

class AddQuestionAdapter(
    private var itemList: MutableList<GetQuizQuestionReportData>?,
    private var context: Context,
    var listener: AddQuestionListner,
    var isListener: OnAttachmentListener,
    private var isLoading: Boolean,
    var onQBankItemRemoved: ((String) -> Unit)? = null

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    var isLastAnswerIndex = 0

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
        itemList!!.size
        itemList!!.addAll(newItems)
//        notifyItemRangeInserted(startPosition, newItems.size)
        notifyItemRangeChanged(0, itemList!!.size)//refresh to update remove visibility on all items

    }


    fun addItem(recyclerView: RecyclerView) {
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
                iframe = "",
                file_size = "",
                thumbnail = "",
                sourceType = QuestionSource.USER,
                file_path = mutableListOf()
            )
        )
        Constant.isQuestionLimit -= 1
        notifyItemInserted(itemList!!.size - 1)


        notifyItemRangeChanged(0, itemList!!.size)//refresh to update remove visibility on all items

        recyclerView.post {
            recyclerView.smoothScrollToPosition(itemList!!.size - 1)
        }
    }


    fun removeItem(position: Int) {
        if (position >= 0 && position < itemList!!.size) {
            val removed = itemList!![position]

            // If it's a QBANK question → notify PickQuestionAdapter
            if (removed.sourceType == QuestionSource.QBANK && removed.id.isNotEmpty()) {
                onQBankItemRemoved?.invoke(removed.id)
//                Constant.isQuestionLimit += 1
            }

            itemList!!.removeAt(position)
            notifyItemRemoved(position)
//            notifyItemRangeChanged(position, itemList!!.size)

            notifyItemRangeChanged(0, itemList!!.size)// Rebind all items so lblremove visibility updates correctly
            Constant.isQuestionLimit += 1
            listener?.onCountUpdated()
            listener?.onUICheck(itemList!!)

        }
    }

    fun showValidationErrors(recyclerView: RecyclerView): Boolean {
        var isAllValid = true
        var firstInvalidIndex: Int? = null

        itemList!!.forEachIndexed { index, item ->
            val holder = recyclerView.findViewHolderForAdapterPosition(index) as? DataViewHolder

            when {
                item.chapter.isBlank() -> {
                    holder?.edtChapterName?.error = context.getString(R.string.this_is_required)
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.question.isBlank() -> {
                    holder?.edtQuestion?.error = context.getString(R.string.this_is_required)
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.a_option.isBlank() -> {
                    holder?.edtOptionA?.error = context.getString(R.string.this_is_required)
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.b_option.isBlank() -> {
                    holder?.edtOptionB?.error = context.getString(R.string.this_is_required)
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.c_option.isBlank() -> {
                    holder?.edtOptionC?.error = context.getString(R.string.this_is_required)
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.d_option.isBlank() -> {
                    holder?.edtOptionD?.error = context.getString(R.string.this_is_required)
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                //here "0" means means option if option is 0 need to show Please select correct option
                item.answer.isBlank() || item.answer == "0" -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_select_correct_option), Toast.LENGTH_SHORT
                    ).show()
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.mark == null -> {
                    holder?.edtMark?.error = context.getString(R.string.this_is_required)
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.mark!! <= 0 -> {
                    holder?.edtMark?.error =
                        context.getString(R.string.mark_should_be_greater_than_zero)
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }
            }
        }

        // scroll & focus on first invalid field
        firstInvalidIndex?.let { invalidIndex ->
            recyclerView.smoothScrollToPosition(invalidIndex)

            recyclerView.post {
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            rv.removeOnScrollListener(this)

                            val holder =
                                rv.findViewHolderForAdapterPosition(invalidIndex) as? DataViewHolder
                            holder?.let {
                                when {
                                    itemList!![invalidIndex].chapter.isBlank() -> it.edtChapterName.requestFocus()
                                    itemList!![invalidIndex].question.isBlank() -> it.edtQuestion.requestFocus()
                                    itemList!![invalidIndex].a_option.isBlank() -> it.edtOptionA.requestFocus()
                                    itemList!![invalidIndex].b_option.isBlank() -> it.edtOptionB.requestFocus()
                                    itemList!![invalidIndex].c_option.isBlank() -> it.edtOptionC.requestFocus()
                                    itemList!![invalidIndex].d_option.isBlank() -> it.edtOptionD.requestFocus()
                                    itemList!![invalidIndex].mark == null || itemList!![invalidIndex].mark!! <= 0 -> it.edtMark.requestFocus()
                                    else -> {}
                                }
                            }
                        }
                    }
                })
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
        holder.spinnerCorrectAnswer.setSelection(isLastAnswerIndex)
        spinnerAdapter.selectedPosition = isLastAnswerIndex
        spinnerAdapter.notifyDataSetChanged()
        Log.d("pos", isLastAnswerIndex.toString())

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
        val lnrAttachmentPick: LinearLayout = itemView.findViewById(R.id.lnrAttachmentPick)


        fun bind(data: GetQuizQuestionReportData, position: Int) {

            rytSpinnerHeader.visibility = View.VISIBLE
            edtCorrectAns.visibility = View.GONE

            edtChapterName.setText(data.chapter)
            edtQuestion.setText(data.question)
            edtOptionA.setText(data.a_option)
            edtOptionB.setText(data.b_option)
            edtOptionC.setText(data.c_option)
            edtOptionD.setText(data.d_option)
//            edtCorrectAns.setText(data.answer)
//            edtMark.setText(data.mark.toString())
            edtMark.setText(if (data.mark == 0) "" else data.mark.toString())

//            val optionsList = listOf(
//                "Select correct option",
//                data.a_option.ifBlank { "Option A" },
//                data.b_option.ifBlank { "Option B" },
//                data.c_option.ifBlank { "Option C" },
//                data.d_option.ifBlank { "Option D" }
//            )

            val optionsList = listOf(
                "Select correct option",
                "Option A",
                "Option B",
                "Option C",
                "Option D"
            )

            val spinnerAdapter = SpinnerLoadingAdapter(context, optionsList)
            spinnerCorrectAnswer.adapter = spinnerAdapter

            // pre-select saved answer index
            val selectedIndex = data.answer.toIntOrNull() ?: 0
            spinnerCorrectAnswer.setSelection(selectedIndex)
            spinnerAdapter.selectedPosition = selectedIndex

            spinnerCorrectAnswer.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        pos: Int,
                        id: Long
                    ) {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            itemList!![adapterPosition].answer = pos.toString()
                            isLastAnswerIndex = pos
                            spinnerAdapter.selectedPosition = pos

                            spinnerAdapter.notifyDataSetChanged()
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
//                    updateSpinnerOptions(this, itemList!![adapterPosition])
                }
            }
            edtOptionB.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].b_option = text.toString()
//                    updateSpinnerOptions(this, itemList!![adapterPosition])

                }
            }
            edtOptionC.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].c_option = text.toString()
//                    updateSpinnerOptions(this, itemList!![adapterPosition])

                }
            }
            edtOptionD.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].d_option = text.toString()
//                    updateSpinnerOptions(this, itemList!![adapterPosition])
                }
            }

            edtMark.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].mark = text.toString().toIntOrNull() ?: 0
                }
            }

//            lnrAttachmentPick.setOnClickListener {
//                isListener.onAttachmentPick(adapterPosition, itemList)
//            }

//            //we are just hiding the lblremove if the itemList size is one to avoid last item to not be removed
//            if (itemList!!.size == 1) {
//                lblremove.visibility = View.GONE
//            } else {
//                lblremove.visibility = View.VISIBLE
//            }

            lblremove.setOnClickListener {
                removeItem(position)
            }

            edtQuestion.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {

                    val drawableEnd = 2 // index for drawableEnd

                    edtQuestion.compoundDrawables[drawableEnd]?.let { drawable ->
                        if (event.rawX >= (edtQuestion.right - drawable.bounds.width() - edtQuestion.paddingEnd)) {
                            isListener.onAttachmentPick(adapterPosition, itemList, true, edtOptionA)
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }

            edtOptionA.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {

                    val drawableEnd = 2 // index for drawableEnd

                    edtOptionA.compoundDrawables[drawableEnd]?.let { drawable ->
                        if (event.rawX >= (edtOptionA.right - drawable.bounds.width() - edtOptionA.paddingEnd)) {
                            isListener.onAttachmentPick(adapterPosition, itemList,false,edtOptionA)
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }

            edtOptionB.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {

                    val drawableEnd = 2 // index for drawableEnd

                    edtOptionB.compoundDrawables[drawableEnd]?.let { drawable ->
                        if (event.rawX >= (edtOptionB.right - drawable.bounds.width() - edtOptionB.paddingEnd)) {
                            isListener.onAttachmentPick(adapterPosition, itemList,false,edtOptionB)
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }

            edtOptionC.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {

                    val drawableEnd = 2 // index for drawableEnd

                    edtOptionC.compoundDrawables[drawableEnd]?.let { drawable ->
                        if (event.rawX >= (edtOptionC.right - drawable.bounds.width() - edtOptionC.paddingEnd)) {
                            isListener.onAttachmentPick(adapterPosition, itemList,false,edtOptionC)
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }

            edtOptionD.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {

                    val drawableEnd = 2 // index for drawableEnd

                    edtOptionD.compoundDrawables[drawableEnd]?.let { drawable ->
                        if (event.rawX >= (edtOptionD.right - drawable.bounds.width() - edtOptionD.paddingEnd)) {
                            isListener.onAttachmentPick(adapterPosition, itemList,false,edtOptionD)
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }
        }
    }


    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}


