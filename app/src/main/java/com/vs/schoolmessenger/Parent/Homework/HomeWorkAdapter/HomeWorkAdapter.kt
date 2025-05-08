

package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.Homework.HomeWorkDateClickListener
import com.vs.schoolmessenger.Parent.Homework.HomeWorkList
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class HomeWorkAdapter (
    private var DateWiseHomeworkData: List<GetDateWiseHomeworkData>?,
    private var listener: HomeWorkDateClickListener,
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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            DataViewHolder.ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.home_work_date_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(DateWiseHomeworkData!![position],position, this,) // Pass adapter reference
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else DateWiseHomeworkData?.size ?: 0
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val rlaDateItem: RelativeLayout = itemView.findViewById(R.id.rlaDateItem)
        private val rcyHomeWorkItem: RecyclerView = itemView.findViewById(R.id.rcyHomeWorkItem)
        private val imgDown: ImageView = itemView.findViewById(R.id.imgDown)
        var mHomeWorkItemAdapter: HomeWorkItemAdapter? = null
        var isPosition = -1

        private fun getRecyclerView(): RecyclerView {
            return rcyHomeWorkItem
        }

        fun bind(
            item: GetDateWiseHomeworkData,
            position: Int,
            adapter: HomeWorkAdapter,


            ) {
            lblDate.text = item.date

            rlaDateItem.setOnClickListener {
                if (isPosition != position) {
                    rcyHomeWorkItem.visibility = View.VISIBLE
                    loadData(item.homework,item)
                    isPosition = position
                    imgDown.setImageResource(R.drawable.arrow_up_round)
                } else {
                    isPosition = -1
                    rcyHomeWorkItem.visibility = View.GONE
                    imgDown.setImageResource(R.drawable.arrow_down_round)
                }
            }

        }


        private fun loadData(
            homeworkDetails: List<GetHomeworkDetails>,
            DateWiseHomeWorkdata: GetDateWiseHomeworkData
        ) {



            val rcyView = getRecyclerView()

            mHomeWorkItemAdapter =
                HomeWorkItemAdapter(null,null,context, Constant.isShimmerViewShow,)
            rcyView.layoutManager = LinearLayoutManager(context)
            rcyView.adapter = mHomeWorkItemAdapter

            Constant.executeAfterDelay {
                // Once data is loaded, stop shimmer and pass the actual data
                mHomeWorkItemAdapter =
                    HomeWorkItemAdapter(
                        DateWiseHomeWorkdata,
                        homeworkDetails,
                        context,
                        Constant.isShimmerViewDisable,
                    )
                // Set GridLayoutManager (2 columns in this case)
                rcyView.adapter = mHomeWorkItemAdapter
            }
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