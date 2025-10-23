package com.vs.schoolmessenger.School.MessageFromManagement.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
import com.vs.schoolmessenger.School.MessageFromManagement.MsgStaffListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class MessageFromStaffAdapter(
    private var itemList: MutableList<GetMessagesStaffData> = mutableListOf(),
    private val listener: MsgStaffListener,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.message_from_staff)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_from_staff, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(itemList[position], position)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }

    // Replace all data
    fun updateData(newList: List<GetMessagesStaffData>) {
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }

    // Append new data safely
    fun AppendData(newList: List<GetMessagesStaffData>?) {
        if (newList.isNullOrEmpty()) return

        if (itemList == null) {
            itemList = mutableListOf()
        }

        val oldSize = itemList!!.size
        itemList!!.addAll(newList)
        Log.d("ListSize",itemList.size.toString())
        Log.d("ListSize",itemList.toString())

        // Notify correctly based on whether it was empty or not
        if (oldSize == 0) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeInserted(oldSize, newList.size)
        }
    }


    fun getCurrentListSize(): Int {
        return itemList.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblLogo: TextView = itemView.findViewById(R.id.lblLogo)
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblRole: TextView = itemView.findViewById(R.id.lblRole)
        private val lblTimeDate: TextView = itemView.findViewById(R.id.lblTimeDate)
        private val lblSchoolName: TextView = itemView.findViewById(R.id.lblSchoolName)
        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
        private val imgReadStatus: View = itemView.findViewById(R.id.imgReadStatus)
        private val rlaHeader: RelativeLayout = itemView.findViewById(R.id.rlaHeader)

        fun bind(data: GetMessagesStaffData, position: Int) {
            lblTitle.text = data.title
            val name = data.sent_by ?: ""
            lblLogo.text = Constant.getNameInitials(name)
            lblName.text = name
            lblRole.text = data.role
            lblSchoolName.text = data.school_name
            lblSchoolName.visibility=View.GONE

            lblTimeDate.text = "${Constant.isFormatDate(data.date.toString())} ${data.time}"

            lblTitle.apply {
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 2
            }

            lblDescription.apply {
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 3
            }

            imgReadStatus.visibility = if (data.is_unread) View.VISIBLE else View.GONE

            when (data.type) {
                Constant.TEXT -> {
                    lblDescription.visibility = View.VISIBLE
                    lblDescription.text = data.description
                }
                Constant.VOICE -> lblDescription.visibility = View.GONE
                Constant.ATTACHMENT_ -> {
                    lblDescription.visibility = View.VISIBLE
                    lblDescription.text = data.description
                }
            }

            rlaHeader.setOnClickListener {
                imgReadStatus.visibility = View.GONE
                listener.onStaffClick(data)
            }
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}


//package com.vs.schoolmessenger.School.MessageFromManagement.Adapter
//
//import android.content.Context
//import android.text.TextUtils
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.RelativeLayout
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
//import com.vs.schoolmessenger.School.MessageFromManagement.MsgStaffListener
//import com.vs.schoolmessenger.Utils.Constant
//import com.vs.schoolmessenger.Utils.ShimmerUtil
//
//class MessageFromStaffAdapter(
//    private var itemList: List<GetMessagesStaffData>?,
//    private val listener: MsgStaffListener,
//    private var context: Context,
//    private var isLoading: Boolean
//
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.message_from_staff)
//            ShimmerViewHolder(shimmerView)
//        } else {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.message_from_staff, parent, false)
//            DataViewHolder(view)
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is DataViewHolder) {
//            itemList?.get(position)?.let { holder.bind(it, position) }
//        } else if (holder is ShimmerViewHolder) {
//            holder.startShimmer()
//        }
//    }
//
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 20 else itemList?.size ?: 0
//    }
//
//    fun updateData(newList: List<GetMessagesStaffData>) {
//        itemList = newList
//        notifyDataSetChanged()
//    }
//
//    fun getCurrentListSize(): Int {
//        return itemList!!.size
//    }
//
//    fun AppendData(newList: List<GetMessagesStaffData>) {
//        val oldSize = itemList!!.size
//        itemList = itemList!!.toMutableList().apply { addAll(newList) }
//        notifyItemRangeInserted(oldSize, newList.size)
//    }
//
//
//    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
//        private val lblLogo: TextView = itemView.findViewById(R.id.lblLogo)
//        private val lblName: TextView = itemView.findViewById(R.id.lblName)
//        private val lblRole: TextView = itemView.findViewById(R.id.lblRole)
//        private val lblTimeDate: TextView = itemView.findViewById(R.id.lblTimeDate)
//        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
//        private val imgReadStatus: View = itemView.findViewById(R.id.imgReadStatus)
//        private val rlaHeader: RelativeLayout = itemView.findViewById(R.id.rlaHeader)
//
//
//        fun bind(data: GetMessagesStaffData, position: Int) {
//            lblTitle.text = data.title
//            var name=data.sent_by!!
//            lblLogo.text = Constant.getNameInitials(name)
//            lblName.text = name
//            lblRole.text = data.role
//            lblTimeDate.text = Constant.isFormatDate(data.date.toString())+" "+data.time
//
//            lblDescription.apply {
//                isSingleLine = true
//                ellipsize = TextUtils.TruncateAt.END
//                maxLines = 1
//            }
//
//            if (data.is_unread){
//                imgReadStatus.visibility=View.VISIBLE
//            }
//            else{
//                imgReadStatus.visibility=View.GONE
//            }
//
//            when (data.type) {
//                Constant.TEXT -> {
//                    lblDescription.visibility=View.VISIBLE
//                    lblDescription.text=data.content
//                }
//
//                Constant.VOICE -> {
//                    lblDescription.visibility=View.GONE
//                }
//
//                Constant.ATTACHMENT_ -> {
//                    lblDescription.visibility=View.VISIBLE
//                    lblDescription.text=data.description
//                }
//            }
//
//            rlaHeader.setOnClickListener{
//                imgReadStatus.visibility=View.GONE
//                listener.onStaffClick(data)
//            }
//        }
//    }
//
//
//    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun startShimmer() {
//            ShimmerUtil.startShimmer(itemView)
//
//        }
//    }
//
//}