package com.vs.schoolmessenger.School.NoticeBoard

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.FilePathAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SchoolNoticeBoardAdapter(
    private var itemList: List<NoticeStaffData>?,
    private val listener: NoticeBoardClickListener,
    private val context: Context,
    var isLoading: Boolean,
    private val noDataImage: ImageView?,
    private val noDataText: TextView?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList: MutableList<NoticeStaffData> =
        (itemList ?: emptyList()).toMutableList()
    private var filteredList: MutableList<NoticeStaffData> =
        (itemList ?: emptyList()).toMutableList()

    init {
        originalList = (itemList ?: emptyList()).toMutableList()
        filteredList = originalList.toMutableList()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.notice_board_rewamp_card)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notice_board_rewamp_card, parent, false)
            DataViewHolder(view, context, listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            filteredList.getOrNull(position)?.let {
                holder.bind(it, position, this)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) {
            3
        } else {
            filteredList.size
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.title.lowercase().contains(query) ||
                                it.description.lowercase().contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList =
                    (results?.values as? List<NoticeStaffData>)?.toMutableList() ?: mutableListOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }

    private fun handleEmptyState(isEmpty: Boolean, query: String) {
        if (isEmpty && query.isNotEmpty()) {
            noDataImage?.visibility = View.VISIBLE
            noDataText?.visibility = View.VISIBLE
            noDataText?.text = "${context.getString(R.string.No_results_found_for)} '$query'"
        } else if (isEmpty && query.isEmpty() && originalList.isEmpty()) {
            noDataImage?.visibility = View.VISIBLE
            noDataText?.visibility = View.VISIBLE
            noDataText?.text = context.getString(R.string.no_notices_available)
        } else {
            noDataImage?.visibility = View.GONE
            noDataText?.visibility = View.GONE
        }
    }

    fun updateList(newList: List<NoticeStaffData>, isFullList: Boolean = true) {
        Log.d("AdapterUpdate", "updateList called with ${newList.size} items")
        if (isFullList) {
            originalList.clear()
            originalList.addAll(newList)
            Log.d("AdapterUpdate", "originalList size after update: ${originalList.size}")
        }
        filteredList.clear()
        filteredList.addAll(newList)
        isLoading = false
        notifyDataSetChanged()
        filter.filter("")
    }

    fun removeItemAt(position: Int) {
        if (position in filteredList.indices) {
            val removedItem = filteredList.removeAt(position)
            originalList.remove(removedItem)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, filteredList.size)

            if (filteredList.isEmpty()) {
                handleEmptyState(true, "")
            }
        }
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: NoticeBoardClickListener
    ) : RecyclerView.ViewHolder(itemView) {

        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTimeImage: TextView = itemView.findViewById(R.id.lblTimeImage)
        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)
        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)

        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val remaindertag: TextView = itemView.findViewById(R.id.remaindertag)
        private val options: ImageView = itemView.findViewById(R.id.options)
        private val header: CardView = itemView.findViewById(R.id.header)

        @SuppressLint("SetTextI18n")
        fun bind(noticeData: NoticeStaffData, position: Int, adapter: SchoolNoticeBoardAdapter) {
            lblTitleImage.text = noticeData.title
            lblContentImage.text = noticeData.description

            val dateTime = noticeData.created_on ?: ""
            val parts = dateTime.split(" ")
            val date = parts.getOrNull(0) ?: ""
            val time = (parts.getOrNull(1) ?: "") + " " + (parts.getOrNull(2) ?: "")

            val inputFormat = SimpleDateFormat(Constant.ddMMyyyy, Locale.getDefault())
            val parsedDate = try {
                inputFormat.parse(date)
            } catch (_: Exception) {
                null
            }

            val calendar = Calendar.getInstance()
            val today = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = calendar.time

            val outputText = when {
                parsedDate != null -> {
                    val sdf = SimpleDateFormat(Constant.yyyyMMdd, Locale.getDefault())
                    when (sdf.format(parsedDate)) {
                        sdf.format(today) -> context.getString(R.string.today)
                        sdf.format(yesterday) -> context.getString(R.string.yesterday)
                        else -> Constant.CustomisedconvertDateTimeFormat(date)
                    }
                }

                else -> Constant.CustomisedconvertDateTimeFormat(date)
            }

            lblDateImage.text = outputText
            lblTimeImage.text = time
            loadingBar.visibility = View.GONE
            options.visibility =
                if (noticeData.can_edit || noticeData.can_delete) View.VISIBLE else View.GONE

            options.setOnClickListener {
                listener.onClickListener(noticeData, it, adapterPosition)
            }

            val hasFiles = !noticeData.file_path.isNullOrEmpty()
            rytList2.visibility = if (hasFiles) View.VISIBLE else View.INVISIBLE
            total_numbers.visibility = View.INVISIBLE

            if (hasFiles) {
                val fileList = noticeData.file_path!!
                val totalFiles = fileList.size
                val visibleList = if (totalFiles > 2) fileList.subList(0, 2) else fileList

                if (totalFiles > 2) {
                    total_numbers.text = "+${totalFiles - 2}"
                    total_numbers.visibility = View.VISIBLE
                }

                rcyImgPDF.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rcyImgPDF.adapter =
                    FilePathAdapter(visibleList, fileList, context, Constant.isShimmerViewDisable)

                setupPreviewListeners(noticeData)
            } else {
                rcyImgPDF.adapter = null
            }

            remaindertag.setOnClickListener { showReminderPicker(context) }
        }

        private fun openPreview(noticeData: NoticeStaffData) {
            val convertedList = noticeData.file_path?.map {
                GetFilePathDetails(type = it.type, url = it.url)
            } ?: emptyList()

            val preview = FilePreview(
                id = "",
                title = noticeData.title,
                description = noticeData.description,
                subjectName = "",
                sentBy = "",
                thumbnail = "",
                isUnread = true,
                isCompleted = true,
                isMenuType = Constant.M_NOTICEBOARD,
                fileList = convertedList
            )
            val intent = Intent(context, ChildHomeWork::class.java)
            intent.putExtra(Constant.isPreViewData, preview)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }

        private fun setupPreviewListeners(noticeData: NoticeStaffData) {
            header.setOnClickListener {
                openPreview(noticeData)
            }
            rcyImgPDF.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child != null && e.action == MotionEvent.ACTION_UP) {
                        openPreview(noticeData)
                    }
                    return false
                }
            })
        }


        private fun showReminderPicker(context: Context) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    TimePickerDialog(
                        context,
                        { _, h, min ->
                            calendar.set(Calendar.HOUR_OF_DAY, h)
                            calendar.set(Calendar.MINUTE, min)
                            calendar.set(Calendar.SECOND, 0)
                            scheduleNotification(context, calendar.timeInMillis)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        private fun scheduleNotification(context: Context, triggerTime: Long) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                    Toast.makeText(
                        context,
                        context.getString(R.string.Please_allow_exact_alarm_permission),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() = ShimmerUtil.startShimmer(itemView)
    }
}


