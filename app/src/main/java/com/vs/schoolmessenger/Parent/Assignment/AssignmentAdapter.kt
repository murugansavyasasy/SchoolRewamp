package com.vs.schoolmessenger.Parent.Assignment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.Noticeboard.Adapter.FilePathAdapter
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter
import me.relex.circleindicator.CircleIndicator2

class AssignmentAdapter(
    var itemList: MutableList<AssignmentData>,
    private val listener: AssignmentClickListener,
    private val context: Context,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<AssignmentData> = itemList ?: listOf()
    private var filteredList: List<AssignmentData> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.assignment_report_item, parent, false)
            DataViewHolder(view, context)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let {
                holder.bind(it, position, this,listener)
            }
        } else if (holder is SchoolNoticeBoardAdapter.ShimmerViewHolder) {
            holder.startShimmer()
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
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblassigned: TextView = itemView.findViewById(R.id.lblassigned)
        private val lblCategory: TextView = itemView.findViewById(R.id.lblCategory)
        //        private val lblSubmissionDue: TextView = itemView.findViewById(R.id.lblSubmissionDue)
        private val lblSubject: TextView = itemView.findViewById(R.id.lblSubject)
        private val lblSubmitted: TextView = itemView.findViewById(R.id.lblSubmitted)
        private val lblNotSubmitted: TextView = itemView.findViewById(R.id.lblNotSubmitted)
        private val lbldeadline: TextView = itemView.findViewById(R.id.lbldeadline)
        private val createddate: TextView = itemView.findViewById(R.id.createddate)
        private val lblSendby: TextView = itemView.findViewById(R.id.lblSendby)
        private val rytList: LinearLayout = itemView.findViewById(R.id.rytList)
        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)
        //        private val webView: WebView = itemView.findViewById(R.id.webView)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.loadingBar)
        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)

        //        private val indicator: CircleIndicator2 = itemView.findViewById(R.id.indicator)
//        private val imgDelete: ImageView = itemView.findViewById(R.id.imgDelete)
        private val options: ImageView = itemView.findViewById(R.id.options)
        private val video_player: ImageView = itemView.findViewById(R.id.video_player)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val progressBarAssignment: ProgressBar = itemView.findViewById(R.id.progressBarAssignment)
        @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
        fun bind(
            data: AssignmentData,
            position: Int,
            adapter: AssignmentAdapter,
            listener: AssignmentClickListener
        ) {

            lblDescription.text = data.description
            lblTitle.text = data.title
            lblCategory.text = data.category
            lblassigned.text = "Assigned" + " - " + Constant.convertToReadableDate(data.created_date)
//            lblSubmissionDue.text = "Submission Due" + " - " + data.end_date
            createddate.text = Constant.convertToReadableDate(data.created_date)
            lblSubject.text = data.subject
            lbldeadline.text = "Submission date" +" "+ data.end_date
            lblSendby.text = data.created_date

            lblSubmitted.text ="Submitted"+" - "+ data.submitted_count
            lblNotSubmitted.text ="Not Submitted"+" - "+ data.total_count


            val submittedCount = data.submitted_count ?: 0
            val totalCount = data.total_count ?: 1

            progressBarAssignment.max = totalCount
            progressBarAssignment.progress = submittedCount

            val hasIframe = !data.iframe.isNullOrEmpty()
            val hasFiles = !data.file_path.isNullOrEmpty()

            video_player.visibility = if (hasIframe) View.VISIBLE else View.GONE
            rcyAssignment.visibility = if (hasIframe) View.GONE else View.VISIBLE
            rytList2.visibility = if (hasFiles) View.VISIBLE else View.GONE
            total_numbers.visibility = View.GONE

            if (hasFiles) {
                val fileList = data.file_path!!
                val totalFiles = fileList.size
                val visibleList = if (totalFiles > 3) fileList.subList(0, 3) else fileList

                if (totalFiles > 3) {
                    total_numbers.text = "+${totalFiles - 3}"
                    total_numbers.visibility = View.VISIBLE
                } else {
                    total_numbers.visibility = View.GONE
                }

                rcyAssignment.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                val fileAdapter = ImageSliderAdapter(
                    subjectName = data.subject ?: "",
                    fullList = fileList,
                    visibleList = visibleList,
                    context = context,
                    isLoading = Constant.isShimmerViewDisable
                )

                rcyAssignment.adapter = fileAdapter
            }
//            video_player.isClickable = true
//            video_player.isFocusable = true
//
//            video_player.setOnTouchListener { _, event ->
//                if (event.action == MotionEvent.ACTION_UP) {
//                    Constant.commonFileList.clear()
//                    Constant.selectedFileIndex = -1
//                    Constant.commonFileList = data.file_path.map {
//                        CommonFileData(it.type, it.url)
//                    }.toMutableList()
//                    Constant.selectedFileIndex = position
//                    val intent = Intent(context, FilesViewActivity::class.java)
//                    intent.putExtra(Constant.subjectName, data.subject)
//                    context.startActivity(intent)
//                    return@setOnTouchListener true
//                }
//                false
//            }

            lblSubmitted.setOnClickListener { listener.onSubmittedClick(data) }
            lblNotSubmitted.setOnClickListener { listener.onNotSubmittedClick(data) }
            options.setOnClickListener {
                listener.onEditAndDeleteClick(data,it,adapterPosition)
            }

//            options.setOnClickListener {
//                val popup = PopupMenu(context, options)
//                popup.menuInflater.inflate(R.menu.notice_options_menu, popup.menu)
//
//                try {
//                    val fields = popup.javaClass.declaredFields
//                    for (field in fields) {
//                        if (field.name == "mPopup") {
//                            field.isAccessible = true
//                            val menuPopupHelper = field.get(popup)
//                            val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
//                            val setForceIcons =
//                                classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
//                            setForceIcons.invoke(menuPopupHelper, true)
//                            break
//                        }
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                val pos = adapterPosition
//                popup.setOnMenuItemClickListener {
//
//                        menuItem ->
//                    when (menuItem.itemId) {
//                        R.id.menu_edit -> {
//                            // Uncomment
//                            // listener.onEditNotice(noticeData)
//                            true
//                        }
//
//                        R.id.menu_delete -> {
//                            val pos = adapterPosition
//                            if (pos != RecyclerView.NO_POSITION) {
//                                AlertDialog.Builder(context).setTitle("Delete Confirmation")
//                                    .setMessage("Are you sure you want to delete this assignment?")
//                                    .setPositiveButton("Yes") { dialog, _ ->
//                                        adapter.itemList.removeAt(pos)
//                                        adapter.notifyItemRemoved(pos)
//                                        listener.onDeleteClick(data)
//                                        dialog.dismiss()
//                                    }.setNegativeButton("No") { dialog, _ ->
//                                        dialog.dismiss()
//                                    }.show()
//                            }
//                            true
//                        }
//
//                        else -> false
//                    }
//                }
//
//                popup.show()
//            }


        }

//        private fun CircleIndicator2.attachToRecyclerView(recyclerView: RecyclerView) {
//            val adapter = recyclerView.adapter ?: return
//            createIndicators(adapter.itemCount, 0)
//            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
//                    val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
//                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
//                    this@attachToRecyclerView.animatePageSelected(firstVisible)
//                }
//            })
//
//            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//                override fun onChanged() {
//                    createIndicators(adapter.itemCount, 0)
//                }
//            })
//        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)
        init {
            shimmerLayout.startShimmer()
        }
    }
}
