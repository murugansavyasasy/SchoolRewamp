package com.vs.schoolmessenger.Parent.Noticeboard.Adapter

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
import android.widget.LinearLayout
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
import com.vs.schoolmessenger.Parent.Noticeboard.Notice
import com.vs.schoolmessenger.Parent.Noticeboard.NoticeBoardClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator2
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NoticeBoardAdapter(
    private var itemList: List<Notice>?,
    private var listener: NoticeBoardClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<Notice> = itemList ?: listOf()
    private var filteredList: List<Notice> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
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
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            filteredList?.get(position)?.let {
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
                            .contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<Notice> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }

        }
    }

    fun getCurrentList(): List<Notice> {
        return filteredList
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTimeImage: TextView = itemView.findViewById(R.id.lblTimeImage)

        //        private val rlaSelectText: RelativeLayout = itemView.findViewById(R.id.rlaSelectText)
        private val rytList: LinearLayout = itemView.findViewById(R.id.rytList)
        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)

        //        private val tvSeeMoreImage: TextView = itemView.findViewById(R.id.tvSeeMoreImage)
        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)

        //        private val imgNewImage: ImageView = itemView.findViewById(R.id.imgNewImage)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val indicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val remaindertag: TextView = itemView.findViewById(R.id.remaindertag)
        private val header: CardView = itemView.findViewById(R.id.header)

        @SuppressLint("SetTextI18n")
        fun bind(noticeData: Notice, position: Int, adapter: NoticeBoardAdapter) {

            lblTitleImage.text = noticeData.title
            lblContentImage.text = noticeData.description

            val dateTime = noticeData.created_on
            val parts = dateTime.split(" ")
            val date = parts.getOrNull(0) ?: ""
            (parts.getOrNull(1) ?: "") + " " + (parts.getOrNull(2) ?: "")


            loadingBar.visibility = View.GONE

            val hasFiles = !noticeData.file_path.isNullOrEmpty()
            rytList2.visibility = if (hasFiles) View.VISIBLE else View.INVISIBLE
            total_numbers.visibility = View.INVISIBLE


            val dateTime1 = noticeData.created_on ?: ""
            val parts1 = dateTime1.split(" ")
            val date1 = parts1.getOrNull(0) ?: ""
            val time1 = (parts1.getOrNull(1) ?: "") + " " + (parts1.getOrNull(2) ?: "")

            val inputFormat = SimpleDateFormat(Constant.ddMMyyyy, Locale.getDefault())
            val parsedDate = try {
                inputFormat.parse(date1)
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
            lblTimeImage.text = time1
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
            }
            setupPreviewListeners(noticeData)

            remaindertag.setOnClickListener { showReminderPicker(context) }
        }

        private fun openPreview(noticeData: Notice) {
            val convertedList = noticeData.file_path?.map {
                GetFilePathDetails(type = it.type, url = it.url)
            } ?: emptyList()

            val preview = FilePreview(
                id = noticeData.id,
                title = noticeData.title,
                description = noticeData.description,
                subjectName = "",
                sentBy = noticeData.sent_by,
                created_date = noticeData.created_on,
                thumbnail = "",
                isUnread = true,
                intended_for = noticeData.intended_for,
                school_name = "",
                isCompleted = true,
                isMenuType = Constant.M_NOTICEBOARD,
                fileList = convertedList
            )
            Log.d("previewData", preview.toString())
            val intent = Intent(context, ChildHomeWork::class.java)
            intent.putExtra(Constant.isPreViewData, preview)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }

        private fun setupPreviewListeners(noticeData: Notice) {
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
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)

                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
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


        fun scheduleNotification(context: Context, triggerTime: Long) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_allow_exact_alarm_permission_to_schedule_reminders),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}