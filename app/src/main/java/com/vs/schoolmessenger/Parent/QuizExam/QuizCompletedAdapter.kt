package com.vs.schoolmessenger.Parent.QuizExam

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class QuizCompletedAdapter (
    private var itemList: List<QuizCompletedData>?,
    private var listener: QuizCompletedListener,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectedPosition = RecyclerView.NO_POSITION


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            DataViewHolder.ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.quiz_completedlist, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this) // Pass adapter reference
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }




    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.questionText)
        private val option1: TextView = itemView.findViewById(R.id.option1)
        private val option2: TextView = itemView.findViewById(R.id.option2)
        private val option3: TextView = itemView.findViewById(R.id.option3)
        private val option4: TextView = itemView.findViewById(R.id.option4)



        fun bind(
            data: QuizCompletedData,
            position: Int,
            listener: QuizCompletedListener,
            adapter: QuizCompletedAdapter
        ) {
            questionText.text = data.questionvalue
            option1.text = data.option1
            option2.text = data.option2
            option3.text = data.option3
            option4.text = data.option4


        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)
            init {
                shimmerLayout.startShimmer() // Start shimmer effect
            }
        }
    }
}
