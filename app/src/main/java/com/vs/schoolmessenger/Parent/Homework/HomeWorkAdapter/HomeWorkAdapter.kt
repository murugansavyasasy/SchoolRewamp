package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.Parent.Homework.HomeWorkDateClickListener
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class HomeWorkAdapter(
    private var DateWiseHomeworkData: List<GetDateWiseHomeworkData>?,
    private var listener: HomeWorkDateClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private var isSeeMoreClick: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var expandedPosition = -1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.home_work_date_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.home_work_date_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(DateWiseHomeworkData!![position], position, this, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else DateWiseHomeworkData?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val rlaDateItem: RelativeLayout = itemView.findViewById(R.id.rlaDateItem)
        private val rcyHomeWorkItem: RecyclerView = itemView.findViewById(R.id.rcyHomeWorkItem)
        private val imgDown: ImageView = itemView.findViewById(R.id.imgDown)
        private val lblSeeMore: TextView = itemView.findViewById(R.id.lblSeeMore)
        var mHomeWorkItemAdapter: HomeWorkItemAdapter? = null

        fun bind(
            item: GetDateWiseHomeworkData,
            position: Int,
            adapter: HomeWorkAdapter,
            listener: HomeWorkDateClickListener
        ) {
            lblDate.text = Constant.convertDateTimeFormat(item.date)

            val isExpanded = position == adapter.expandedPosition
            rcyHomeWorkItem.visibility = if (isExpanded) View.VISIBLE else View.GONE
            imgDown.setImageResource(
                if (isExpanded) R.drawable.arrow_up_round else R.drawable.arrow_down_round
            )
            if (isExpanded) {
                loadData(item.homework, item)
            }
            if (position == adapter.itemCount - 1) {
                if (adapter.isSeeMoreClick){
                    lblSeeMore.visibility = View.VISIBLE
                }else{
                    lblSeeMore.visibility = View.GONE
                }
            } else {
                lblSeeMore.visibility = View.GONE
            }

            lblSeeMore.setOnClickListener {
                listener.onItemClick(item, this@DataViewHolder)
            }

            rlaDateItem.setOnClickListener {
                val previouslyExpanded = adapter.expandedPosition
                if (previouslyExpanded == position) {
                    adapter.expandedPosition = -1
                    adapter.notifyItemChanged(previouslyExpanded)
                } else {
                    adapter.expandedPosition = position
                    adapter.notifyItemChanged(previouslyExpanded)
                    adapter.notifyItemChanged(position)
                }
            }
        }

        private fun loadData(
            homeworkDetails: List<GetHomeworkDetails>, DateWiseHomeWorkdata: GetDateWiseHomeworkData
        ) {
            mHomeWorkItemAdapter =
                HomeWorkItemAdapter(null, null, context, Constant.isShimmerViewShow)
            rcyHomeWorkItem.layoutManager = LinearLayoutManager(context)
            rcyHomeWorkItem.adapter = mHomeWorkItemAdapter
            mHomeWorkItemAdapter = HomeWorkItemAdapter(
                DateWiseHomeWorkdata,
                homeworkDetails,
                context,
                Constant.isShimmerViewDisable,
            )
            rcyHomeWorkItem.adapter = mHomeWorkItemAdapter
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)

            init {
                shimmerLayout.startShimmer()
            }
        }
    }
}