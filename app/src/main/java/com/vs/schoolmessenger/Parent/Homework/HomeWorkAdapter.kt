package com.vs.schoolmessenger.Parent.Homework

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
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class HomeWorkAdapter (
    private var itemList: ArrayList<HomeWorkDateData>?,
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
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, this) // Pass adapter reference
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val rlaDateItem: RelativeLayout = itemView.findViewById(R.id.rlaDateItem)
        private val rcyHomeWorkItem: RecyclerView = itemView.findViewById(R.id.rcyHomeWorkItem)
        private val imgDown: ImageView = itemView.findViewById(R.id.imgDown)
        var mHomeWorkItemAdapter: HomeWorkItemAdapter? = null
        private lateinit var isHomeWorkReportList: List<HomeWorkList>
        var isPosition = -1
        private fun getRecyclerView(): RecyclerView {
            return rcyHomeWorkItem
        }

        fun bind(
            data: HomeWorkDateData,
            position: Int,
            adapter: HomeWorkAdapter
        ) {
            lblDate.text = data.isDate

            rlaDateItem.setOnClickListener {
                if (isPosition != position) {
                    rcyHomeWorkItem.visibility = View.VISIBLE
                    loadData()
                    isPosition = position
                    imgDown.setImageResource(R.drawable.arrow_up_round)
                } else {
                    isPosition = -1
                    rcyHomeWorkItem.visibility = View.GONE
                    imgDown.setImageResource(R.drawable.arrow_down_round)
                }
            }
        }

        private fun loadData() {
            isHomeWorkReportList = listOf(
                HomeWorkList(
                    "Annual Day celebrartions",
                    "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                    "15 Nov 2024",
                    "isVoice", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    ""
                ),
                HomeWorkList(
                    "Annual Day celebrartions",
                    "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                    "15 Nov 2024",
                    "isPDF",
                    "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    ""
                ),
                HomeWorkList(
                    "Annual Day celebrartions",
                    "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                    "15 Nov 2024",
                    "isImage", "",
                    ""
                ),
                HomeWorkList(
                    "Annual Day celebrartions",
                    "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                    "15 Nov 2024",
                    "isVoice", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    ""
                ),
                HomeWorkList(
                    "Annual Day celebrartions",
                    "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                    "15 Nov 2024",
                    "isVideo", "https://vimeo.com/76979871", "76979871"
                ),
                HomeWorkList(
                    "Annual Day celebrartions",
                    "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                    "15 Nov 2024",
                    "isText", "", ""
                ),
                HomeWorkList(
                    "Annual Day celebrartions",
                    "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                    "15 Nov 2024",
                    "isImage", "", ""
                ),
                HomeWorkList(
                    "Annual Day celebrartions",
                    "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                    "15 Nov 2024",
                    "isVideo", "https://vimeo.com/76979871", "76979871"
                ),
                HomeWorkList(
                    "Annual Day celebrartions",
                    "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                    "15 Nov 2024",
                    "isText", "", ""
                )
            )

            val rcyView = getRecyclerView()

            mHomeWorkItemAdapter =
                HomeWorkItemAdapter(null, context, Constant.isShimmerViewShow)
            rcyView.layoutManager = LinearLayoutManager(context)
            rcyView.adapter = mHomeWorkItemAdapter

            Constant.executeAfterDelay {
                // Once data is loaded, stop shimmer and pass the actual data
                mHomeWorkItemAdapter =
                    HomeWorkItemAdapter(
                        isHomeWorkReportList,
                        context,
                        Constant.isShimmerViewDisable
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
