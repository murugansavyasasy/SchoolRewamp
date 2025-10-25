package com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel.AnswerData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class InteractionWithStaffChatAdapter(
    private var itemList: List<AnswerData> = listOf(),
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.interaction_with_staff_chat)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.interaction_with_staff_chat, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val answerText: TextView = itemView.findViewById(R.id.answerText)
        private val user_name: TextView = itemView.findViewById(R.id.user_name)
        private val time_value: TextView = itemView.findViewById(R.id.time_value)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val linear_layo212ut: LinearLayout = itemView.findViewById(R.id.linear_layo212ut)
        private val linear_layout: LinearLayout = itemView.findViewById(R.id.linear_layout)
        private val header_relativelayout: RelativeLayout = itemView.findViewById(R.id.header_relativelayout)
        private val card_view: CardView = itemView.findViewById(R.id.card_view)
        private val txtQuestion: TextView = itemView.findViewById(R.id.txtQuestion)
        private val txtAskedBy: TextView = itemView.findViewById(R.id.txtAskedBy)
        private val txtAnswer: TextView = itemView.findViewById(R.id.txtAnswer)
        private val txtTimestamp: TextView = itemView.findViewById(R.id.txtTimestamp)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(chat: AnswerData, position: Int) {
            if (chat.my_question == true) {
                card_view.visibility = View.GONE
                linear_layo212ut.visibility = View.VISIBLE
                questionText.text = chat.question
                answerText.text = chat.answer
                user_name.text = chat.student_name
                time_value.text = Constant.formatChatDate(chat.asked_on)
                time.text = Constant.formatChatDate(chat.answered_on)
                linear_layo212ut.visibility =
                    if (chat.answer == Constant.Not_answered_yet) View.GONE else View.VISIBLE
            } else {
                card_view.visibility = View.VISIBLE
                linear_layo212ut.visibility = View.GONE
                linear_layout.visibility = View.GONE
                txtQuestion.text = chat.question
                txtAskedBy.text = "Asked by ~ " + " " +chat.student_name
                txtAnswer.text = chat.answer
                txtTimestamp.text = Constant.formatChatDate(chat.asked_on)
                card_view.visibility =
                    if (chat.answer == Constant.Not_answered_yet) View.GONE else View.VISIBLE
            }
        }

    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }

    fun updateData(newList: List<AnswerData>) {
        itemList = newList
        isLoading = false
        notifyDataSetChanged()
    }


}

