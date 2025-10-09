package com.vs.schoolmessenger.School.InteractionWithStudent

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.InteractionWithStudent.Listener.ReplyClickListener
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.QuestionData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class InteractionWithQuestionAdapter(
    private var itemList: List<QuestionData> = listOf(),
    private val context: Context,
    private var listener: ReplyClickListener,
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
                ShimmerUtil.wrapWithShimmer(parent, R.layout.interaction_with_question_staff_chat)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.interaction_with_question_staff_chat, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], listener, position)
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
        private val more_options: ImageView = itemView.findViewById(R.id.more_options)
        private val student_name: TextView = itemView.findViewById(R.id.aboveText)
        private val created_date: TextView = itemView.findViewById(R.id.belowText)
        private val reply_type: TextView = itemView.findViewById(R.id.reply_type)
        private val linear_layout: LinearLayout = itemView.findViewById(R.id.linear_layout)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(chat: QuestionData, listener: ReplyClickListener, position: Int) {
            questionText.text = chat.question
            answerText.text = chat.answer
            student_name.text = chat.student_name
            if(chat.reply_type == "1") {
                reply_type.text = "Public Reply"
            } else if (chat.reply_type == "2") {
                reply_type.text = "Private Reply"
            } else {
                reply_type.visibility = View.GONE
            }
            created_date.text = Constant.formatChatDate(chat.created_on)

            answerText.visibility =
                if (chat.answer == Constant.Not_answered_yet) View.GONE else View.VISIBLE


            val popupHandler = View.OnClickListener {
                showPopup(it, chat, listener, position)
            }
            questionText.setOnClickListener(popupHandler)
            questionText.setOnLongClickListener {
                popupHandler.onClick(it)
                true
            }
            more_options.setOnClickListener(popupHandler)
            linear_layout.setOnClickListener(popupHandler)
        }


        private fun showPopup(
            view: View,
            chat: QuestionData,
            listener: ReplyClickListener,
            position: Int
        ) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.question_popup_menu, popup.menu)

            if (chat.answer == Constant.Not_answered_yet) {
                popup.menu.findItem(R.id.menu_reply_all)?.isVisible = false
            } else {
                popup.menu.findItem(R.id.menu_reply)?.isVisible = false
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_reply -> {
                        listener.onAnswerClick(chat, position)
                        true
                    }

                    R.id.menu_reply_all -> {
                        listener.onUpdateAnswerClick(chat, position, true)
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }

    fun updateData(newList: List<QuestionData>) {
        itemList = newList
        isLoading = false
        notifyDataSetChanged()
    }


}

