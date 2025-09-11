package com.vs.schoolmessenger.Parent.InteractionWithStaff.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

        @SuppressLint("ClickableViewAccessibility")
        fun bind(chat: AnswerData, position: Int) {
            questionText.text = chat.question
            answerText.text = chat.answer
            if (chat.answer == Constant.Not_answered_yet) {
                answerText.visibility = View.GONE
            } else {
                answerText.visibility = View.VISIBLE
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

