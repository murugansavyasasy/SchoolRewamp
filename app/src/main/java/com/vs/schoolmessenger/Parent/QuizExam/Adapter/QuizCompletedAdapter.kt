
package com.vs.schoolmessenger.Parent.QuizExam.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.submitted_quiz_preview_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.submitted_quiz_preview_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
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
        private val lblYourResponse: TextView = itemView.findViewById(R.id.lblYourResponse)
        private val lblCorrectanswer: TextView = itemView.findViewById(R.id.lblCorrectanswer)
        private val lnrAnswerDetails: LinearLayout = itemView.findViewById(R.id.lnrAnswerDetails)

        fun bind(data: QuizDetails, position: Int) {

            if (data.file_path.isNullOrEmpty()) {
                indicator.visibility = View.GONE
                rcAttachement.visibility = View.GONE
            } else {
                indicator.visibility = View.VISIBLE
                rcAttachement.visibility = View.VISIBLE
                rcAttachement.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rcAttachement.adapter = AttachmentMediaAdapter(
                    data.file_path,
                    context,
                    Constant.isShimmerViewDisable
                )
                indicator.attachToRecyclerView(rcAttachement)
            }


            questionText.text="${position+1}) ${data.question}"
            option1.text=data.a_option
            option2.text=data.b_option
            option3.text=data.c_cption
            option4.text=data.d_option

            val options = listOf(option1, option2,option3, option4)
            options.forEach { option ->
                val defaultDrawable = ContextCompat.getDrawable(context, R.drawable.quiz_option_bg)?.mutate()
                defaultDrawable?.setTint(ContextCompat.getColor(context, R.color.mild_grey3)) // default color
                option.background = defaultDrawable
                option.setTextColor(ContextCompat.getColor(context, R.color.azure_radiance))
            }

            val studentAns = data.student_answer?.trim()
            val correctAns = data.correct_answer?.trim()

            val optionMap = mapOf(
                data.a_option.trim() to option1,
                data.b_option.trim() to option2,
                data.c_cption.trim() to option3,
                data.d_option.trim() to option4
            )

            if (studentAns.equals("N/A", ignoreCase = true)) {
                //  No Answer Selected
                optionMap[correctAns]?.let { tv ->
                    val drawable = ContextCompat.getDrawable(context, R.drawable.quiz_option_bg)?.mutate()
                    drawable?.setTint(ContextCompat.getColor(context, R.color.green))
                    tv.background = drawable
                    tv.setTextColor(ContextCompat.getColor(context, R.color.white))
                }

                lnrAnswerDetails.visibility = View.VISIBLE
                lblYourResponse.text = "Not Answered"
                lblYourResponse.setTextColor(ContextCompat.getColor(context, R.color.orange))

                lblCorrectanswer.text = data.correct_answer

            } else if (studentAns.equals(correctAns, ignoreCase = true)) {
                // Correct
                optionMap[correctAns]?.let { tv ->
                    val drawable = ContextCompat.getDrawable(context, R.drawable.quiz_option_bg)?.mutate()
                    drawable?.setTint(ContextCompat.getColor(context, R.color.green))
                    tv.background = drawable
                    tv.setTextColor(ContextCompat.getColor(context, R.color.white))
                }

                lnrAnswerDetails.visibility = View.GONE

            } else {
                //  Wrong
                optionMap[studentAns]?.let { tv ->
                    val drawable = ContextCompat.getDrawable(context, R.drawable.quiz_option_bg)?.mutate()
                    drawable?.setTint(ContextCompat.getColor(context, R.color.red))
                    tv.background = drawable
                    tv.setTextColor(ContextCompat.getColor(context, R.color.white))
                }

                optionMap[correctAns]?.let { tv ->
                    val drawable = ContextCompat.getDrawable(context, R.drawable.quiz_option_bg)?.mutate()
                    drawable?.setTint(ContextCompat.getColor(context, R.color.green))
                    tv.background = drawable
                    tv.setTextColor(ContextCompat.getColor(context, R.color.white))
                }

                lnrAnswerDetails.visibility = View.VISIBLE
                lblYourResponse.text = data.student_answer
                lblCorrectanswer.text = data.correct_answer
            }
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}

