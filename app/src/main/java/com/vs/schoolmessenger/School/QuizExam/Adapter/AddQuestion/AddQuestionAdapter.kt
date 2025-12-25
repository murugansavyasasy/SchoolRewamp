package com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.QuizExam.Adapter.QuestionAttachmentAdapter
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
        listener?.onUICheck(itemList!!)

    }


    fun removeItem(position: Int) {
        if (position >= 0 && position < itemList!!.size) {

            val removed = itemList!![position]

            // If it's a QBANK question → notify PickQuestionAdapter
            if (removed.sourceType == QuestionSource.QBANK && removed.id.isNotEmpty()) {
                onQBankItemRemoved?.invoke(removed.id)
            }

            itemList!!.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(
                0,
                itemList!!.size
            )// Rebind all items so lblremove visibility updates correctly
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

                item.question.isBlank()-> {
                    holder?.edtQuestion?.error = context.getString(R.string.this_is_required)
//                    Toast.makeText(
//                        context,
//                        context.getString(R.string.a_question_or_an_question_image_must_be_added), Toast.LENGTH_SHORT
//                    ).show()
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.a_option.isBlank()  -> {
                    holder?.edtOptionA?.error = context.getString(R.string.this_is_required)
//                    Toast.makeText(
//                        context,
//                        context.getString(R.string.an_option_a_or_an_image_must_be_added), Toast.LENGTH_SHORT
//                    ).show()
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.b_option.isBlank() -> {

                    holder?.edtOptionB?.error = context.getString(R.string.this_is_required)
//                    Toast.makeText(
//                        context,
//                        context.getString(R.string.an_option_b_or_an_image_must_be_added), Toast.LENGTH_SHORT
//                    ).show()
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.c_option.isBlank()-> {
//                    Toast.makeText(
//                        context,
//                        context.getString(R.string.an_option_c_or_an_image_must_be_added), Toast.LENGTH_SHORT
//                    ).show()
                    holder?.edtOptionC?.error = context.getString(R.string.this_is_required)
                    if (firstInvalidIndex == null) firstInvalidIndex = index
                    isAllValid = false
                }

                item.d_option.isBlank() -> {
//                    Toast.makeText(
//                        context,
//                        context.getString(R.string.an_option_d_or_an_image_must_be_added), Toast.LENGTH_SHORT
//                    ).show()
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

    private fun openImagePreview(context: Context, imageUrl: String) {
        if (imageUrl.isBlank()) return

        Constant.commonFileList.clear()
        Constant.commonFileList.add(
            CommonFileData(Constant.IMAGE, imageUrl)
        )
        Constant.selectedFileIndex = 0

        context.startActivity(Intent(context, FilesViewActivity::class.java))
    }

    fun getUpdatedList(): List<GetQuizQuestionReportData> = itemList!!

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val edtChapterName: EditText = itemView.findViewById(R.id.edtChapterName)
        val edtQuestion: EditText = itemView.findViewById(R.id.edtQuestion)
        val lblQuestionPick: TextView = itemView.findViewById(R.id.lblQuestionPick)
        val lblAddImageA: TextView = itemView.findViewById(R.id.lblAddImageA)
        val lblAddImageB: TextView = itemView.findViewById(R.id.lblAddImageB)
        val lblAddImageC: TextView = itemView.findViewById(R.id.lblAddImageC)
        val lblAddImageD: TextView = itemView.findViewById(R.id.lblAddImageD)
        val edtOptionA: EditText = itemView.findViewById(R.id.edtOptionA)
        val edtOptionB: EditText = itemView.findViewById(R.id.edtOptionB)
        val edtOptionC: EditText = itemView.findViewById(R.id.edtOptionC)
        val edtOptionD: EditText = itemView.findViewById(R.id.edtOptionD)
        val spinnerCorrectAnswer: Spinner = itemView.findViewById(R.id.spinnerCorrectAnswer)
        val rytSpinnerHeader: RelativeLayout = itemView.findViewById(R.id.rytSpinnerHeader)
        val edtCorrectAns: EditText = itemView.findViewById(R.id.edtCorrectAns)
        val edtMark: EditText = itemView.findViewById(R.id.edtMark)
        val lblremove: ImageView = itemView.findViewById(R.id.lblremove)
        val rcyQuestions: RecyclerView = itemView.findViewById(R.id.rcyQuestions)
        val imgOptionA: ImageView = itemView.findViewById(R.id.imgOptionA)
        val imgOptionB: ImageView = itemView.findViewById(R.id.imgOptionB)
        val imgOptionC: ImageView = itemView.findViewById(R.id.imgOptionC)
        val imgOptionD: ImageView = itemView.findViewById(R.id.imgOptionD)

        val fremOptionA: FrameLayout = itemView.findViewById(R.id.framOptionA)
        val fremOptionB: FrameLayout = itemView.findViewById(R.id.framOptionB)
        val fremOptionC: FrameLayout = itemView.findViewById(R.id.framOptionC)
        val fremOptionD: FrameLayout = itemView.findViewById(R.id.framOptionD)

        val progressOptionA: ProgressBar = itemView.findViewById(R.id.progressOptionA)
        val progressOptionB: ProgressBar = itemView.findViewById(R.id.progressOptionB)
        val progressOptionC: ProgressBar = itemView.findViewById(R.id.progressOptionC)
        val progressOptionD: ProgressBar = itemView.findViewById(R.id.progressOptionD)


        fun bind(data: GetQuizQuestionReportData, position: Int) {
            Log.d(
                "ATTACH_DEBUG",
                "Question $adapterPosition attachments = ${data.file_path?.size}"
            )

            lblAddImageD.visibility= View.VISIBLE
            lblAddImageC.visibility= View.VISIBLE
            lblAddImageB.visibility= View.VISIBLE
            lblAddImageA.visibility= View.VISIBLE
            lblQuestionPick.visibility= View.VISIBLE
            rcyQuestions.visibility= View.VISIBLE

            rytSpinnerHeader.visibility = View.VISIBLE
            edtCorrectAns.visibility = View.GONE
            bindOptionImages(data)
            setupAttachmentRecycler(data, adapterPosition)
            edtChapterName.setText(data.chapter)
            edtQuestion.setText(data.question)
            edtOptionA.setText(data.a_option)
            edtOptionB.setText(data.b_option)
            edtOptionC.setText(data.c_option)
            edtOptionD.setText(data.d_option)
            edtMark.setText(if (data.mark == 0) "" else data.mark.toString())
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

            edtMark.doAfterTextChanged { text ->
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemList!![adapterPosition].mark = text.toString().toIntOrNull() ?: 0
                }
            }

            rcyQuestions.post {
                rcyQuestions.requestLayout()
            }
            imgOptionA.setOnClickListener {
                openImagePreview(context,data.a_image.toString())
            }
            imgOptionB.setOnClickListener {
                openImagePreview(context,data.b_image.toString())
            }
            imgOptionC.setOnClickListener {
                openImagePreview(context,data.c_image.toString())
            }
            imgOptionD.setOnClickListener {
                openImagePreview(context,data.d_image.toString())
            }

            lblremove.setOnClickListener {

                //Local item (USER, QBANK)
                if (data.id.isNullOrEmpty() || data.sourceType!= QuestionSource.API) {
                    Log.d("QuestionType","USER & QUESTION BANK Question")
                    removeItem(position)
                    return@setOnClickListener
                }

                // Delete only for API Data
//                We ask for the Confirmation to delete API question
                Log.d("QuestionType","API Question")
                listener.onDeleteQuizQuestion(data.id,) { isSuccess ->
                    Log.d("QuestionType","API Question")
                    if (isSuccess) {
                        removeItem(position)
                    } else {
                        return@onDeleteQuizQuestion
                    }
                }
            }

            lblQuestionPick.setOnClickListener {
                isListener.onAttachmentPick(adapterPosition, itemList, true, lblQuestionPick)
            }
        }

        private fun setupOptionImageUI(
            imageUrl: String?,
            imageView: ImageView,
            labelView: TextView,
            isFremLayout: FrameLayout,
            isProgressBar: ProgressBar
        ) {
            if (imageUrl.isNullOrEmpty()) {
                imageView.visibility = View.GONE
                isFremLayout.visibility = View.GONE
                labelView.text = context.getString(R.string.add_image)
                labelView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.attachment_icon_2, 0, 0, 0
                )
            } else {
                imageView.visibility = View.VISIBLE
                isFremLayout.visibility = View.VISIBLE
                Glide.with(itemView.context).load(imageUrl).into(imageView)
                labelView.text = context.getString(R.string.remove_image)
                labelView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.trash_bin_red_icon, 0, 0, 0
                )
                loadImageWithProgress(imageUrl, imageView, isProgressBar, isFremLayout)

            }
        }

        private fun loadImageWithProgress(
            imageUrl: String,
            imageView: ImageView,
            progressBar: ProgressBar,
            isFremLayout: FrameLayout
        ) {
            progressBar.visibility = View.VISIBLE
            imageView.visibility = View.INVISIBLE
            isFremLayout.visibility = View.INVISIBLE

            Glide.with(itemView.context)
                .load(imageUrl)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        imageView.visibility = View.GONE
                        isFremLayout.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        imageView.visibility = View.VISIBLE
                        isFremLayout.visibility = View.VISIBLE
                        return false
                    }
                })
                .into(imageView)
        }


        private fun handleOptionImageClick(
            isFremLayout: FrameLayout,
            imageUrl: String?,
            labelView: TextView,
            imageView: ImageView,
            onPick: () -> Unit,
            onRemove: () -> Unit
        ) {
            labelView.setOnClickListener {
                if (imageUrl.isNullOrEmpty()) {
                    onPick()
                } else {
                    onRemove()
                    imageView.visibility = View.GONE
                    isFremLayout.visibility = View.GONE
                    imageView.setImageDrawable(null)
                }
            }
        }

        private fun bindOptionImages(data: GetQuizQuestionReportData) {

            setupOptionImageUI(
                data.a_image,
                imgOptionA,
                lblAddImageA, fremOptionA, progressOptionA
            )
            handleOptionImageClick(
                fremOptionA,
                data.a_image,
                lblAddImageA,
                imgOptionA,
                onPick = {
                    isListener.onAttachmentPick(adapterPosition, itemList, false, lblAddImageA)
                },
                onRemove = {
                    data.a_image = ""
                    notifyItemChanged(adapterPosition)
                }
            )

            setupOptionImageUI(
                data.b_image,
                imgOptionB,
                lblAddImageB, fremOptionB, progressOptionB
            )
            handleOptionImageClick(
                fremOptionB,
                data.b_image,
                lblAddImageB,
                imgOptionB,
                onPick = {
                    isListener.onAttachmentPick(adapterPosition, itemList, false, lblAddImageB)
                },
                onRemove = {
                    data.b_image = ""
                    notifyItemChanged(adapterPosition)
                }
            )

            setupOptionImageUI(
                data.c_image,
                imgOptionC,
                lblAddImageC, fremOptionC, progressOptionC
            )
            handleOptionImageClick(
                fremOptionC,
                data.c_image,
                lblAddImageC,
                imgOptionC,
                onPick = {
                    isListener.onAttachmentPick(adapterPosition, itemList, false, lblAddImageC)
                },
                onRemove = {
                    data.c_image = ""
                    notifyItemChanged(adapterPosition)
                }
            )

            setupOptionImageUI(
                data.d_image,
                imgOptionD,
                lblAddImageD, fremOptionD, progressOptionD
            )
            handleOptionImageClick(
                fremOptionD,
                data.d_image,
                lblAddImageD,
                imgOptionD,
                onPick = {
                    isListener.onAttachmentPick(adapterPosition, itemList, false, lblAddImageD)
                },
                onRemove = {
                    data.d_image = ""
                    notifyItemChanged(adapterPosition)
                }
            )
        }

        private fun setupAttachmentRecycler(
            data: GetQuizQuestionReportData,
            questionPos: Int
        ) {
            val attachments = data.file_path ?: return

            if (attachments.isEmpty()) {
                rcyQuestions.visibility = View.GONE
                rcyQuestions.adapter = null
                return
            }
            rcyQuestions.visibility = View.VISIBLE
            rcyQuestions.isNestedScrollingEnabled = false
            rcyQuestions.layoutManager = GridLayoutManager(itemView.context, 3)

            rcyQuestions.adapter = QuestionAttachmentAdapter(
                itemView.context,
                attachments
            ) { removePos ->
                attachments.removeAt(removePos)
                notifyItemChanged(questionPos)
            }
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}


