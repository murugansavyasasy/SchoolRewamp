package com.vs.schoolmessenger.School.Communication.Adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.Adapter.TextHistoryAdapter.DataViewHolder.ShimmerViewHolder
import com.vs.schoolmessenger.School.Communication.DataClass.TextDetail
import com.vs.schoolmessenger.School.Communication.Interface.TextHistoryClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class TextHistoryAdapter(
    private var itemList: List<TextDetail>?,
    private var listener: TextHistoryClickListener,
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
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.history_from_text_message)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_from_text_message, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this) // Pass adapter reference
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val lblContent: TextView = itemView.findViewById(R.id.lblContent)
        private val lblSeeMore: TextView = itemView.findViewById(R.id.lblSeeMore)
        private val rlaSelectText: RelativeLayout = itemView.findViewById(R.id.rlaSelectText)
        private var isExpanded = false


        fun bind(
            data: TextDetail,
            position: Int,
            listener: TextHistoryClickListener,
            adapter: TextHistoryAdapter
        ) {
            lblTitle.text = data.title
            val parts = data.date.split(" ")
            val date = parts[0]
            val time = parts[1] + " " + parts[2]

            lblTime.text = time
            lblDate.text = Constant.convertDateTimeFormat(date)
            lblContent.text = data.content
            isSeeMoreVisibility(lblContent, lblSeeMore)
            lblSeeMore.setOnClickListener {
                isExpanded = !isExpanded
                updateTextView()
            }

            rlaSelectText.setOnClickListener {
                listener.onItemClick(data, this@DataViewHolder)
            }

        }

        private fun isSeeMoreVisibility(lblContent: TextView, tvSeeMore: TextView) {
            lblContent.post {
                if (lblContent.lineCount > 3) {
                    tvSeeMore.visibility = View.VISIBLE
                    lblContent.maxLines = 3
                    lblContent.ellipsize = TextUtils.TruncateAt.END
                } else {
                    tvSeeMore.visibility = View.GONE
                }
            }
        }


        private fun updateTextView() {
            if (isExpanded) {
                lblContent.maxLines = Int.MAX_VALUE
                lblSeeMore.text = context.getString(R.string.see_less)
            } else {
                lblContent.maxLines = 3
                lblSeeMore.text = context.getString(R.string.see_more)
            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}
