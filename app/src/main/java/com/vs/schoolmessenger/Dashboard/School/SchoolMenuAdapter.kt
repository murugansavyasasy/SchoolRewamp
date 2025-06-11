package com.vs.schoolmessenger.Dashboard.School

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.Dashboard.Parent.AdImageAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SchoolMenuAdapter(
    private var context: Context,
    private var listener: MenuClickListener,
    private var itemList: List<MenuDetail>?,
    private var isAdItem: List<AdItem>?,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private val TYPE_AD = 2

    override fun getItemViewType(position: Int): Int {
        val showAd = !isLoading && isAdItem?.isNotEmpty() == true && (itemList?.size ?: 0) > 9
        return when {
            isLoading -> TYPE_SHIMMER
            showAd && position == 9 -> TYPE_AD
            else -> TYPE_DATA
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SHIMMER -> {
                val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.menu_item_card)
                ShimmerViewHolder(shimmerView)
            }

            TYPE_AD -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.ad_item, parent, false)
                AdViewHolder(view, this)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.menu_item_card, parent, false)
                DataViewHolder(view, context)
            }
        }
    }

    fun updateList(newList: List<MenuDetail>) {
        itemList = emptyList()
        itemList = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val showAd = isAdItem?.isNotEmpty() == true && (itemList?.size ?: 0) > 9

        when (holder) {
            is DataViewHolder -> {
                val actualPosition = if (showAd && position > 9) position - 1 else position
                itemList?.getOrNull(actualPosition)?.let { menuDetail ->
                    holder.bind(menuDetail, actualPosition, listener)
                }
            }

            is ShimmerViewHolder -> holder.startShimmer()

            is AdViewHolder -> holder.bind(isAdItem ?: emptyList(), context)
        }
    }


    override fun getItemCount(): Int {
        if (isLoading) return 20
        val baseSize = itemList?.size ?: 0
        val showAd = isAdItem?.isNotEmpty() == true && baseSize > 9
        return if (showAd) baseSize + 1 else baseSize
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
        private val imgReadCount: View = itemView.findViewById(R.id.imgReadCount)
        private val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)
        private val rlaMenu: RelativeLayout = itemView.findViewById(R.id.rlaMenu)

        fun bind(data: MenuDetail, position: Int, listener: MenuClickListener) {
            lblMenuName.text = data.name

            if(data.unreadCount>=1){
                imgReadCount.visibility=View.VISIBLE
            }

            when (data.id) {
                Constant.M_COMMUNICATION -> {
                    imgMenu.setImageResource(R.drawable.communication_icon_dashboard)
                }

                Constant.M_ASSIGNMENT -> {
                    imgMenu.setImageResource(R.drawable.assignment_icon_school)
                }

                Constant.M_HOMEWORK -> {
                    imgMenu.setImageResource(R.drawable.home_work_icon_school)
                }

                Constant.M_ATTENDANCE_MARKING -> {
                    imgMenu.setImageResource(R.drawable.attendance_marking)
                }

                Constant.M_ABSENTEES_REPORT -> {
                    imgMenu.setImageResource(R.drawable.absentees_report)
                }

                Constant.M_SCHOOL_STRENGTH -> {
                    imgMenu.setImageResource(R.drawable.school_strength)
                }

                Constant.M_NOTICEBOARD -> {
                    imgMenu.setImageResource(R.drawable.noticeboard_icon)
                }

                Constant.M_SCHOOL_CLASS_EVENTS -> {
                    imgMenu.setImageResource(R.drawable.event_icon_school)
                }

                Constant.M_SCHEDULE_EXAM_TEST -> {
                    imgMenu.setImageResource(R.drawable.schedule_exam_icon)
                }

                Constant.M_MESSAGES_FROM_MANAGEMENT -> {
                    imgMenu.setImageResource(R.drawable.message_f_management)
                }

                Constant.M_FINANCE -> {
                    imgMenu.setImageResource(R.drawable.finance_icon)
                }

                Constant.M_SCHOOL_CLASS_EVENTS -> {
                    imgMenu.setImageResource(R.drawable.finance_icon)
                }


                Constant.M_ATTACHMENTS -> {
                    imgMenu.setImageResource(R.drawable.attachement_icon)
                }

                Constant.M_ONLINE_MEETING -> {
                    imgMenu.setImageResource(R.drawable.online_meeting_icon)
                }

                Constant.M_DAILY_COLLECTION -> {
                    imgMenu.setImageResource(R.drawable.daily_collection)
                }

                Constant.M_STUDENT_REPORT -> {
                    imgMenu.setImageResource(R.drawable.student_report)
                }

                Constant.M_LESSON_PLAN -> {
                    imgMenu.setImageResource(R.drawable.lesson_plan)
                }

                Constant.M_FEEDBACK -> {
                    imgMenu.setImageResource(R.drawable.fee_pending_reports)
                }

                Constant.M_VERY_IMPORTANT_INFO -> {
                    imgMenu.setImageResource(R.drawable.very_important_icon)
                }

                Constant.M_MARK_YOUR_ATTENDANCE -> {
                    imgMenu.setImageResource(R.drawable.fee_pending_reports)
                }

                Constant.M_STAFF_WISE_ATTENDANCE_REPORT -> {
                    imgMenu.setImageResource(R.drawable.importent_info)
                }

                Constant .M_LEAVE_REQUEST -> {
                    imgMenu.setImageResource(R.drawable.leave_request_icon_school)
                }
            }

            rlaMenu.setOnClickListener {
                Constant.SELECTED_SCHOOL_MENU = data.id
                listener.onClick(data)

            }
        }
    }

    class AdViewHolder(itemView: View, private val adapter: SchoolMenuAdapter) :
        RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewAdImages)
        private val rlaMenuExample: LinearLayout = itemView.findViewById(R.id.rlaMenuExample)
        private lateinit var layoutManager: LinearLayoutManager

        fun bind(isAds: List<AdItem>, context: Context) {
            layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = AdImageAdapter(isAds)
            rlaMenuExample.visibility = View.GONE
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}