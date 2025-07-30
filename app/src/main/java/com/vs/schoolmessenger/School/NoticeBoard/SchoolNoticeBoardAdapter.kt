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
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
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
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.FilePathAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.NoticeBoard.Model.NoticeStaffData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import me.relex.circleindicator.CircleIndicator2
import java.util.Calendar

class SchoolNoticeBoardAdapter(
    private var itemList: List<NoticeStaffData>?,
    private var listener: NoticeBoardClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<NoticeStaffData> = itemList ?: listOf()
    private var filteredList: List<NoticeStaffData> = itemList ?: listOf()

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
            DataViewHolder(view, context, listener)
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
                filteredList = results?.values as? List<NoticeStaffData> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
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

            // Optional: show "No Data Found" if empty
            if (filteredList.isEmpty()) {
                listener.onSearchResultEmpty(true)
            }
        }
    }


    class DataViewHolder(
        itemView: View, private val context: Context, private val listener: NoticeBoardClickListener
    ) : RecyclerView.ViewHolder(itemView) {

        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTimeImage: TextView = itemView.findViewById(R.id.lblTimeImage)

        private val rytList: LinearLayout = itemView.findViewById(R.id.rytList)
        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)

        private val rcyImgPDF: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)

        //        private val webView: android.webkit.WebView = itemView.findViewById(R.id.webView)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val indicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)

        private val video_player: ImageView = itemView.findViewById(R.id.video_player)

        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val remaindertag: TextView = itemView.findViewById(R.id.remaindertag)
        private val options: ImageView = itemView.findViewById(R.id.options)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(noticeData: NoticeStaffData, position: Int, adapter: SchoolNoticeBoardAdapter) {
            lblTitleImage.text = noticeData.title
            lblContentImage.text = noticeData.description
            val dateTime = noticeData.created_on
            val parts = dateTime.split(" ")
            val date = parts.getOrNull(0) ?: ""
            val time = parts.getOrNull(1) + " " + (parts.getOrNull(2) ?: "")
            lblDateImage.text = Constant.convertDateTimeFormat(date)
            lblTimeImage.text = time
            video_player.visibility = View.GONE
            loadingBar.visibility = View.GONE
            options.visibility = View.VISIBLE

            if (noticeData.can_edit != true && noticeData.can_delete != true) {

                options.visibility = View.GONE
            }

            options.setOnClickListener {
                if (noticeData.can_edit != true && noticeData.can_delete != true) {
                    return@setOnClickListener
                }
                val popup = PopupMenu(context, options)
                popup.menuInflater.inflate(R.menu.notice_options_menu, popup.menu)
                popup.menu.findItem(R.id.menu_edit).isVisible = noticeData.can_edit == true
                popup.menu.findItem(R.id.menu_delete).isVisible = noticeData.can_delete == true

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
                            listener.onDeleteNotice(noticeData.id, noticeData.id, adapterPosition)
                            true
                        }

                        else -> false
                    }
                }

                popup.show()
            }


            val hasIframe = !noticeData.iframe.isNullOrEmpty()
            val hasFiles = !noticeData.file_path.isNullOrEmpty()


            video_player.visibility = if (hasIframe) View.VISIBLE else View.GONE
            rcyImgPDF.visibility = if (hasIframe) View.GONE else View.VISIBLE
            rytList2.visibility = if (hasFiles) View.VISIBLE else View.INVISIBLE
            total_numbers.visibility = View.GONE

            if (hasIframe) {
                video_player.setOnClickListener {
                    val commonList = noticeData.file_path?.map {
                        CommonFileData(type = it.type, path = it.url)
                    }?.toMutableList() ?: mutableListOf()

                    Constant.commonFileList = commonList
                    Constant.selectedFileIndex = position

                    val intent = Intent(context, FilesViewActivity::class.java)
                    intent.putExtra(Constant.subjectName, noticeData.title)
                    context.startActivity(intent)
                }
            }

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

            remaindertag.setOnClickListener {
                val context = it.context
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
                        "Please allow exact alarm permission to schedule reminders",
                        Toast.LENGTH_LONG
                    ).show()
                    return // prevent crash
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


