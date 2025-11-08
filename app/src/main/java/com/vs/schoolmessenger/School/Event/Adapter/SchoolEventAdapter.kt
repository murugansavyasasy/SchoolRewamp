package com.vs.schoolmessenger.School.Event.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventFilePathAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Event.Listener.SchoolEventClickListener
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SchoolEventAdapter(
    private var itemList: List<SchoolEventItem>?,
    private val listener: SchoolEventClickListener,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<SchoolEventItem> = itemList ?: listOf()
    private var filteredList: List<SchoolEventItem> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.event_ongoing_recyclerview)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.event_ongoing_recyclerview, parent, false)
            DataViewHolder(view,context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            filteredList[position].let {
                holder.bind(it, listener, context)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    val filtered = fullList.filter {
                        (it.title?.lowercase()?.contains(query) == true) ||
                                (it.description?.lowercase()?.contains(query) == true) ||
                                (it.venue?.lowercase()?.contains(query) == true)
                    }
                    filtered
                }
                return FilterResults().apply { values = result }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<SchoolEventItem> ?: listOf()
                listener.onSearchResultEmpty(Constant.ONGOING, filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }


    fun updateList(newList: List<SchoolEventItem>?) {
        if (newList != null) {
            filteredList = newList
        }
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<SchoolEventItem> {
        return filteredList
    }


    fun removeItemAt(position: Int) {
        if (position in filteredList.indices) {
            val removedNotice = filteredList[position]
            filteredList = filteredList.toMutableList().apply {
                removeAt(position)
            }
            fullList = fullList.filterNot { it.id == removedNotice.id }
            notifyItemRemoved(position)

            if (filteredList.isEmpty()) {
                listener.onSearchResultEmpty(Constant.ONGOING, true)
            }
        }
    }


    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val event_header: TextView = itemView.findViewById(R.id.event_header)
        private val event_time: TextView = itemView.findViewById(R.id.event_time)
        private val event_location: TextView = itemView.findViewById(R.id.event_location)
        private val status_event: TextView = itemView.findViewById(R.id.status_event)
        private val eventdesc: TextView = itemView.findViewById(R.id.eventdesc)

        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)

        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val rytList: LinearLayout = itemView.findViewById(R.id.rytList)
        private val header: RelativeLayout = itemView.findViewById(R.id.header)
        private val rytList2: LinearLayout = itemView.findViewById(R.id.rytList2)


        fun bind(data: SchoolEventItem, listener: SchoolEventClickListener, context: Context) {
            event_header.text = data.title
            event_time.text =
                "${data.category} ${data.time} - ${Constant.convertDateTimeFormat(data.date)}"
            event_location.text = data.venue
            status_event.text = context.getString(R.string.today_s_event)
            eventdesc.text = data.description
            loadingBar.visibility = View.GONE
            setupPreviewListeners(data)

            if (data.file_path.isEmpty()) {
                rcyImgPDF.visibility = View.GONE
                total_numbers.visibility = View.GONE
            } else {
                rytList.visibility = View.VISIBLE
                rcyImgPDF.visibility = View.VISIBLE

                val fileList = data.file_path
                val totalFiles = fileList.size

                val adapter = EventFilePathAdapter(fileList, context, Constant.isShimmerViewDisable)
                rcyImgPDF.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rcyImgPDF.adapter = adapter

                if (totalFiles > 3) {
                    total_numbers.text = "+${totalFiles - 3}"
                    total_numbers.visibility = View.VISIBLE
                } else {
                    total_numbers.visibility = View.GONE
                }
            }
        }

        private fun openPreview(data: SchoolEventItem) {
            val convertedList = data.file_path.map {
                GetFilePathDetails(type = it.type, url = it.url)
            }

            val isHomeWorkData = FilePreview(
                id = data.id,
                title = data.title,
                description = data.description,
                created_date = data.date,
                subjectName = "",
                sentBy = data.sent_by,
                thumbnail = data.thumbnail,
                isUnread = true,
                isCompleted = true,
                isMenuType = Constant.M_SCHOOL_CLASS_EVENTS,
                fileList = convertedList,
            )

            val intent = Intent(context, ChildHomeWork::class.java)
            intent.putExtra(Constant.isPreViewData, isHomeWorkData)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }

        private fun setupPreviewListeners(data: SchoolEventItem) {
            header.setOnClickListener {
                openPreview(data)
            }
            rcyImgPDF.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child != null && e.action == MotionEvent.ACTION_UP) {
                        openPreview(data)
                    }
                    return false
                }
            })

        }
    }
}

class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun startShimmer() {
        ShimmerUtil.startShimmer(itemView)
    }
}

