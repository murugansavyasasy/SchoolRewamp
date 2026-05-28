package com.vs.schoolmessenger.Parent.QuizExam.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.QuizExam.Model.MySubmission.QuizDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.AttachmentMediaAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator2

class QuizCompletedAdapter(
    private var itemList: List<QuizDetails>?,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int) =
        if (isLoading) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            ShimmerViewHolder(
                ShimmerUtil.wrapWithShimmer(parent, R.layout.submitted_quiz_preview_item)
            )
        } else {
            DataViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.submitted_quiz_preview_item, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount() =
        if (isLoading) 20 else itemList?.size ?: 0


    private fun applyDefaultOptionState(
        layout: LinearLayout,
        card: CardView,
        textView: TextView
    ) {
        layout.background = null
        card.cardElevation = context.resources.getDimension(R.dimen.five)
        card.radius = context.resources.getDimension(R.dimen.five)

        val primaryColor = ContextCompat.getColor(context, R.color.PrimaryColor)
        textView.setTextColor(primaryColor)
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


    private fun applyOptionState(
        layout: LinearLayout,
        card: CardView,
        textView: TextView,
        colorRes: Int
    ) {
        val drawable = ContextCompat
            .getDrawable(context, R.drawable.quiz_option_stroke_bg)
            ?.mutate() as GradientDrawable

        val color = ContextCompat.getColor(context, colorRes)

        // 50% alpha fill
        drawable.setColor(color and 0x50FFFFFF.toInt())

        drawable.setStroke(2, color)

        layout.background = drawable

        // Remove card shadow for selected
        card.cardElevation = 0f

        val selected_colour = ContextCompat.getColor(context, R.color.white)

        // Set selected text color
        textView.setTextColor(selected_colour)
    }


    private fun CircleIndicator2.attachToRecyclerView(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return
        this.createIndicators(adapter.itemCount, 0)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                this@attachToRecyclerView.animatePageSelected(firstVisible)
            }
        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                this@attachToRecyclerView.createIndicators(adapter.itemCount, 0)
            }
        })
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val rcAttachement: RecyclerView = itemView.findViewById(R.id.rcAttachement)
        private val indicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)

        private val option1: TextView = itemView.findViewById(R.id.option1)
        private val option2: TextView = itemView.findViewById(R.id.option2)
        private val option3: TextView = itemView.findViewById(R.id.option3)
        private val option4: TextView = itemView.findViewById(R.id.option4)

        private val imgA: ImageView = itemView.findViewById(R.id.imgOptionA)
        private val imgB: ImageView = itemView.findViewById(R.id.imgOptionB)
        private val imgC: ImageView = itemView.findViewById(R.id.imgOptionC)
        private val imgD: ImageView = itemView.findViewById(R.id.imgOptionD)

        private val lnrBgA: LinearLayout = itemView.findViewById(R.id.lnrChangeBgOptionA)
        private val lnrBgB: LinearLayout = itemView.findViewById(R.id.lnrChangeBgOptionB)
        private val lnrBgC: LinearLayout = itemView.findViewById(R.id.lnrChangeBgOptionC)
        private val lnrBgD: LinearLayout = itemView.findViewById(R.id.lnrChangeBgOptionD)

        private val cardA: CardView = itemView.findViewById(R.id.CardOption1)
        private val cardB: CardView = itemView.findViewById(R.id.CardOption2)
        private val cardC: CardView = itemView.findViewById(R.id.CardOption3)
        private val cardD: CardView = itemView.findViewById(R.id.CardOption4)

        private val lblYourResponse: TextView = itemView.findViewById(R.id.lblYourResponse)
        private val lblCorrectAnswer: TextView = itemView.findViewById(R.id.lblCorrectanswer)
        private val lnrAnswerDetails: LinearLayout = itemView.findViewById(R.id.lnrAnswerDetails)

        fun bind(data: QuizDetails, position: Int) {
            data class OptionUI(val bg: LinearLayout, val card: CardView, val text: TextView)

            //we are are matching the answer with ID and handling the UI Colour change behaviour
            val uiMap = mapOf(
                data.options[0].value.trim() to OptionUI(lnrBgA, cardA, option1),
                data.options[1].value.trim() to OptionUI(lnrBgB, cardB, option2),
                data.options[2].value.trim() to OptionUI(lnrBgC, cardC, option3),
                data.options[3].value.trim() to OptionUI(lnrBgD, cardD, option4)
            )

            //            At initial we are reseting all to have the card
            uiMap.values.forEach {
                applyDefaultOptionState(it.bg, it.card, it.text)
            }


            questionText.text = "${position + 1}) ${data.question}"

            if (!data.q_file_path.isNullOrEmpty()) {
                if (data.q_file_path.size == 1) {
                    indicator.visibility = View.GONE
                    rcAttachement.visibility = View.VISIBLE
                } else {
                    indicator.visibility = View.VISIBLE
                    rcAttachement.visibility = View.VISIBLE
                }

                rcAttachement.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rcAttachement.adapter = AttachmentMediaAdapter(
                    data.q_file_path,
                    context,
                    Constant.isShimmerViewDisable
                )
                indicator.attachToRecyclerView(rcAttachement)

            } else {
                indicator.visibility = View.GONE
                rcAttachement.visibility = View.GONE
            }

            option1.text = data.options[0].value
            option2.text = data.options[1].value
            option3.text = data.options[2].value
            option4.text = data.options[3].value

            val images = listOf(imgA, imgB, imgC, imgD)
            data.options.forEachIndexed { index, opt ->
                val imgView = images[index]
                if (!opt.image.isNullOrBlank()) {
                    imgView.visibility = View.VISIBLE
                    Glide.with(context).load(opt.image).into(imgView)
                    imgView.setOnClickListener {
                        openImagePreview(context, opt.image!!)
                    }
                } else imgView.visibility = View.GONE
            }


            val studentAns = data.student_answer?.trim()
            val correctAns = data.correct_answer?.trim()

            //See we here just checking whether the studentAns and correctAns is match based on that we are handling the UI
            when {

                studentAns.equals(Constant.N_A, true) -> {
                    uiMap[correctAns]?.let {
                        applyOptionState(it.bg, it.card, it.text, R.color.green)

                    }
                    lnrAnswerDetails.visibility = View.VISIBLE
                    lblYourResponse.text = context.getString(R.string.not_answered1)
                    lblCorrectAnswer.text = correctAns
                }

                studentAns.equals(correctAns, true) -> {
                    uiMap[correctAns]?.let {
                        applyOptionState(it.bg, it.card, it.text, R.color.green)

                    }
                    lnrAnswerDetails.visibility = View.GONE
                }

                else -> {
                    //we are are matching the answer with ID and handling the UI Colour change behaviour
                    uiMap[studentAns]?.let {
                        applyOptionState(it.bg, it.card, it.text, R.color.light_red_2)
                    }
                    uiMap[correctAns]?.let {
                        applyOptionState(it.bg, it.card, it.text, R.color.green)

                    }
                    lnrAnswerDetails.visibility = View.VISIBLE
                    lblYourResponse.text = studentAns
                    lblCorrectAnswer.text = correctAns
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() = ShimmerUtil.startShimmer(itemView)
    }
}

