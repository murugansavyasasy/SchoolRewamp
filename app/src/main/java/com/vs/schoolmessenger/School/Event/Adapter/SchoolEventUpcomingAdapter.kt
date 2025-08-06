package com.vs.schoolmessenger.School.Event.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filter.FilterResults
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventFilePathAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventUpcomingAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.ShimmerViewHolder
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.EventItem
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Event.Listener.SchoolEventClickListener
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator2

class SchoolEventUpcomingAdapter (
    private var itemList: List<SchoolEventItem>?,
    private var listener: SchoolEventClickListener,
    private var context: Context,
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
                ShimmerUtil.wrapWithShimmer(parent, R.layout.event_upcoming_recyclerview)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.event_upcoming_recyclerview, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            filteredList?.get(position)?.let {
                holder.bind(it, position, listener, this)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) {
            3
        } else {
            filteredList?.size ?: 0
        }
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.title.lowercase().contains(query) || it.description.lowercase()
                            .contains(query) || it.venue.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<SchoolEventItem> ?: listOf()
                listener.onSearchResultEmpty("UPCOMING", filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
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
                listener.onSearchResultEmpty("UPCOMING", true)
            }
        }
    }



    fun updateList(newList: List<SchoolEventItem>?) {
        this.itemList = newList
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val event_header: TextView = itemView.findViewById(R.id.event_header)
        private val event_time: TextView = itemView.findViewById(R.id.event_time)
        private val event_location: TextView = itemView.findViewById(R.id.event_location)
        private val eventdesc: TextView = itemView.findViewById(R.id.eventdesc)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)

        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val video_player: ImageView = itemView.findViewById(R.id.video_player)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val rytList: LinearLayout = itemView.findViewById(R.id.rytList)
        private val arrow_icon: ImageView = itemView.findViewById(R.id.arrow_icon)
        private val header: RelativeLayout = itemView.findViewById(R.id.header)
        private val options: ImageView = itemView.findViewById(R.id.options)

        private val imgEditAndDelete: ImageView = itemView.findViewById(R.id.imgEditAndDelete)



        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: SchoolEventItem,
            position: Int,
            listener: SchoolEventClickListener,
            adapter: SchoolEventUpcomingAdapter
        ) {
            event_header.text = data.title
            event_time.text = data.time + " - " + Constant.convertDateTimeFormat(data.date)
            event_location.text = data.venue
            eventdesc.text = data.description


            video_player.visibility = View.GONE
            loadingBar.visibility = View.GONE

            if (data.can_edit && data.can_delete) {
                imgEditAndDelete.visibility = View.VISIBLE
            }else{
                imgEditAndDelete.visibility = View.GONE
            }

            imgEditAndDelete.setOnClickListener {
                listener.onEditAndDelete(data, it, adapterPosition)
            }


            options.setOnClickListener {
                if (data.can_edit != true && data.can_delete != true) {
                    return@setOnClickListener
                }
                val popup = PopupMenu(context, options)
                popup.menuInflater.inflate(R.menu.notice_options_menu, popup.menu)
                popup.menu.findItem(R.id.menu_edit).isVisible = data.can_edit == true
                popup.menu.findItem(R.id.menu_delete).isVisible = data.can_delete == true

                try {
                    val fields = popup.javaClass.declaredFields
                    for (field in fields) {
                        if (field.name == "mPopup") {
                            field.isAccessible = true
                            val menuPopupHelper = field.get(popup)
                            val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                            val setForceIcons =
                                classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                            setForceIcons.invoke(menuPopupHelper, true)
                            break
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_edit -> {
                            // Uncomment
                            // listener.onEditNotice(noticeData)
                            true
                        }

                        R.id.menu_delete -> {
                            listener.onDeleteEvent(data.id, data.id, adapterPosition)
                            true
                        }

                        else -> false
                    }
                }

                popup.show()
            }

            header.setOnClickListener {
                val convertedList = data.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }
                val isHomeWorkData = FilePreview(
                    id = "",
                    title = data.title,
                    description = data.description,
                    subjectName = "",
                    sentBy = "",
                    thumbnail = data.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_SCHOOL_CLASS_EVENTS,
                    fileList = convertedList,
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra("isPreViewData", isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }


            if (!data.iframe.isNullOrEmpty()) {
                video_player.visibility = View.VISIBLE
                rytList.visibility = View.VISIBLE
                rcyImgPDF.visibility = View.GONE

            } else {
                if (data.file_path.isNullOrEmpty()) {
//                    rytList.visibility = View.GONE
                    arrow_icon.visibility = View.VISIBLE
                    rcyImgPDF.visibility = View.GONE
                    total_numbers.visibility = View.GONE
                    video_player.visibility = View.GONE
                } else {
                    rytList.visibility = View.VISIBLE
                    arrow_icon.visibility = View.VISIBLE
                    rcyImgPDF.visibility = View.VISIBLE
                    video_player.visibility = View.GONE

                    val fileList = data.file_path
                    val totalFiles = fileList.size

                    val adapter =
                        EventFilePathAdapter(fileList, context, Constant.isShimmerViewDisable)
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

            fun CircleIndicator2.attachToRecyclerView(recyclerView: RecyclerView) {
                val adapter = recyclerView.adapter ?: return
                this.createIndicators(adapter.itemCount, 0)

                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(rv, dx, dy)
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
        }
    }
}