package com.vs.schoolmessenger.School.LSRW.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.Listener.lsrwskillreportlistener
import com.vs.schoolmessenger.School.LSRW.Model.LsrwTask
import com.vs.schoolmessenger.School.LSRW.Model.Overview
import com.vs.schoolmessenger.Utils.Constant

class LsrwCompletedAdapter (
    private var itemList: List<LsrwTask>,
    private val context: Context,
    private var listener: lsrwskillreportlistener,
    private val noDataImage: ImageView? = null,
    private val noDataText: TextView? = null
) : RecyclerView.Adapter<LsrwCompletedAdapter.HeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lsrw_report_item, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size


    fun updateList(newList: List<LsrwTask>) {
        itemList = newList
        notifyDataSetChanged()
    }

    fun removeItemAt(position: Int) {
        if (position in itemList.indices) {
            val removedNotice = itemList[position]
            itemList = itemList.toMutableList().apply {
                removeAt(position)
            }
            itemList = itemList.filterNot { it.id == removedNotice.id }
            notifyItemRemoved(position)

        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtSubTitle: TextView = itemView.findViewById(R.id.txtSubTitle)
        private val txtDescription: TextView = itemView.findViewById(R.id.txtDescription)
        private val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        private val txtSubmitted: TextView = itemView.findViewById(R.id.txtSubmitted)
        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val txtsubdesc: TextView = itemView.findViewById(R.id.txtsubdesc)
        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val headerrelative_layout: RelativeLayout = itemView.findViewById(R.id.headerrelative_layout)
        private val imgEditAndDelete: ImageView = itemView.findViewById(R.id.imgEditAndDelete)


        fun bind(item: LsrwTask) {
            txtTitle.text = item.subject
            txtSubTitle.text = item.activity_type
            txtDescription.text = item.title
            txtsubdesc.text = item.description
            txtDate.text =Constant.convertDateTimeFormat(item.created_on)
            txtSubmitted.text = item.submitted_average + " "+context.getString(R.string.submitted)


            if (item.can_edit || item.can_delete) {
                imgEditAndDelete.visibility = View.VISIBLE
            } else {
                imgEditAndDelete.visibility = View.GONE
            }

            imgEditAndDelete.setOnClickListener {
                listener.onEditAndDeleteCompleted(item, it, adapterPosition,"COMPLETED")
            }

            if (item.activity_type == Constant.Listening) {
                imgIcon.setImageResource(R.drawable.headphonesvgformat)
            } else if (item.activity_type == Constant.Speaking) {
                imgIcon.setImageResource(R.drawable.micsvgformatstyle)
            } else if (item.activity_type == Constant.Reading){
                imgIcon.setImageResource(R.drawable.booksvg_formatstyle)
            } else if (item.activity_type ==Constant.Writing){
                imgIcon.setImageResource(R.drawable.pensvgformatstyle)
            } else {
                imgIcon.setImageResource(R.drawable.questionmark)
            }



            val hasFiles = !item.file_path.isNullOrEmpty()


            rytList2.visibility = if (hasFiles) View.GONE else View.GONE
            total_numbers.visibility = View.GONE

            rytList2.setOnClickListener {
                val convertedList = item.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }
                val isHomeWorkData = FilePreview(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    subjectName = item.subject,
                    sentBy = "",
                    thumbnail = item.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_LSRW,
                    fileList = convertedList,
                    submittedCount = 0,
                    totalCount = 0,
                    assignmentid = item.activity_type,
                    created_date = item.created_on,
                    category = "",
                    assignmentsubject = "",
                    isParentAssignment = false
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            headerrelative_layout.setOnClickListener {
                val convertedList = item.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }
                val isHomeWorkData = FilePreview(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    subjectName = item.subject,
                    sentBy = "",
                    thumbnail = item.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_LSRW,
                    fileList = convertedList,
                    submittedCount = 0,
                    totalCount = 0,
                    assignmentid = item.activity_type,
                    created_date = item.created_on,
                    category = "",
                    assignmentsubject = "",
                    isParentAssignment = false
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            rcyAssignment.addOnItemTouchListener(
                object : RecyclerView.SimpleOnItemTouchListener() {
                    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                        val child = rv.findChildViewUnder(e.x, e.y)
                        if (child != null && e.action == MotionEvent.ACTION_UP) {
                            rv.getChildAdapterPosition(child)
                            val convertedList = item.file_path.map {
                                GetFilePathDetails(
                                    type = it.type,
                                    url = it.url,
                                )
                            }
                            val isHomeWorkData = FilePreview(
                                id = item.id,
                                title = item.title,
                                description = item.description,
                                subjectName = item.subject,
                                sentBy = "",
                                thumbnail = item.thumbnail,
                                isUnread = true,
                                isCompleted = true,
                                isMenuType = Constant.M_LSRW,
                                fileList = convertedList,
                                submittedCount = 0,
                                totalCount = 0,
                                assignmentid = item.activity_type,
                                created_date = item.created_on,
                                category = "",
                                assignmentsubject = "",
                                isParentAssignment = false
                            )

                            val intent = Intent(context, ChildHomeWork::class.java)
                            intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            context.startActivity(intent)
                        }
                        return false
                    }
                }
            )


            if (hasFiles) {
                val fileList = item.file_path!!
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
                    subjectName = "",
                    fullList = fileList,
                    visibleList = visibleList,
                    context = context,
                    isLoading = Constant.isShimmerViewDisable
                )

                rcyAssignment.adapter = fileAdapter
            }

        }
    }
}