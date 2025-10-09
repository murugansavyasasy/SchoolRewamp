package com.vs.schoolmessenger.Parent.LSRW

import android.content.Context
import android.content.Intent
import android.media.Image
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
import com.vs.schoolmessenger.Parent.Assignment.AssignmentClickListener
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.Parent.LSRW.Model.SkillData
import com.vs.schoolmessenger.Parent.LSRW.Model.lsrwitemclicklistener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant


class LSRWAdapter(
    private var itemList: List<SkillData>,
    private val context: Context,
    private val listener: lsrwitemclicklistener
) : RecyclerView.Adapter<LSRWAdapter.HeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_parent_lsrw, parent, false)
        return HeaderViewHolder(view, context, listener)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size

    fun updateList(newList: List<SkillData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(
        itemView: View, private val context: Context, private val listener: lsrwitemclicklistener
    ) : RecyclerView.ViewHolder(itemView) {

        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtSubtitle: TextView = itemView.findViewById(R.id.txtSubtitle)
        private val txtMainDesc: TextView = itemView.findViewById(R.id.txtMainDesc)
        private val txtSubDesc: TextView = itemView.findViewById(R.id.txtSubDesc)
        private val txtProfile: TextView = itemView.findViewById(R.id.txtProfile)
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)

        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)

        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val imgArrow: ImageView = itemView.findViewById(R.id.imgArrow)
        private val headerrelative_layout: RelativeLayout =
            itemView.findViewById(R.id.headerrelative_layout)
        private val imgNewImage: ImageView = itemView.findViewById(R.id.imgNewImage)


        fun bind(item: SkillData) {
            txtTitle.text = item.subject ?: "-"
            txtSubtitle.text = item.activity_type ?: "-"
            txtMainDesc.text = item.title ?: "-"
            txtSubDesc.text = item.description ?: "-"
            txtProfile.text = item.sent_by ?: "-"

            imgArrow.visibility = View.GONE

            imgNewImage.visibility = if (item.is_unread) View.VISIBLE else View.GONE


            val markAsRead = {
                if (item.is_unread) {
                    item.is_unread = false
                    imgNewImage.visibility = View.GONE
                    listener.onReadStatusClick(item, adapterPosition)
                }
            }

            if (item.activity_type == Constant.Listening) {
                imgIcon.setImageResource(R.drawable.headphonesvgformat)
            } else if (item.activity_type == Constant.Speaking) {
                imgIcon.setImageResource(R.drawable.micsvgformatstyle)
            } else if (item.activity_type == Constant.Reading) {
                imgIcon.setImageResource(R.drawable.booksvg_formatstyle)
            } else if (item.activity_type == Constant.Writing) {
                imgIcon.setImageResource(R.drawable.pensvgformatstyle)
            } else {
                imgIcon.setImageResource(R.drawable.questionmark)
            }


            val hasFiles = !item.file_path.isNullOrEmpty()


            rytList2.visibility = if (hasFiles) View.GONE else View.GONE
            total_numbers.visibility = View.GONE




            rytList2.setOnClickListener {
                markAsRead()
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
                    sentBy = item.created_on,
                    thumbnail = item.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_LSRW,
                    fileList = convertedList,
                    submittedCount = 0,
                    totalCount = 0,
                    assignmentid = item.activity_type,
                    created_date = item.submitted_date,
                    category = "",
                    is_submitted = item.is_submitted,
                    assignmentsubject = "",
                    isParentAssignment = true
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            headerrelative_layout.setOnClickListener {
                markAsRead()
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
                    sentBy = item.created_on,
                    thumbnail = item.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_LSRW,
                    fileList = convertedList,
                    submittedCount = 0,
                    totalCount = 0,
                    assignmentid = item.activity_type,
                    created_date = item.submitted_date,
                    category = "",
                    is_submitted = item.is_submitted,
                    assignmentsubject = "",
                    isParentAssignment = true
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            rcyAssignment.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    markAsRead()
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
                            sentBy = item.created_on,
                            thumbnail = item.thumbnail,
                            isUnread = true,
                            isCompleted = true,
                            isMenuType = Constant.M_LSRW,
                            fileList = convertedList,
                            submittedCount = 0,
                            totalCount = 0,
                            assignmentid = item.activity_type,
                            created_date = item.submitted_date,
                            category = "",
                            is_submitted = item.is_submitted,
                            assignmentsubject = "",
                            isParentAssignment = true
                        )

                        val intent = Intent(context, ChildHomeWork::class.java)
                        intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(intent)
                    }
                    return false
                }
            })


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

