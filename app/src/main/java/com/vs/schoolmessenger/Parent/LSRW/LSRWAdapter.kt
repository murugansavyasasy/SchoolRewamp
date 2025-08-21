package com.vs.schoolmessenger.Parent.LSRW

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
import com.vs.schoolmessenger.Parent.LSRW.Model.SkillData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant


class LSRWAdapter(
    private var itemList: List<SkillData>,
    private val context: Context
) : RecyclerView.Adapter<LSRWAdapter.HeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_parent_lsrw, parent, false)
        return HeaderViewHolder(view)
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

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtSubtitle: TextView = itemView.findViewById(R.id.txtSubtitle)
        private val txtMainDesc: TextView = itemView.findViewById(R.id.txtMainDesc)
        private val txtSubDesc: TextView = itemView.findViewById(R.id.txtSubDesc)
        private val txtProfile: TextView = itemView.findViewById(R.id.txtProfile)
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)

        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)

        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)
        private val video_player: ImageView = itemView.findViewById(R.id.video_player)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val headerrelative_layout: RelativeLayout = itemView.findViewById(R.id.headerrelative_layout)


        fun bind(item: SkillData) {
            txtTitle.text = item.subject ?: "-"
            txtSubtitle.text = item.activity_type ?: "-"
            txtMainDesc.text = item.title ?: "-"
            txtSubDesc.text = item.description ?: "-"
            txtProfile.text = item.sent_by ?: "-"


            if (item.activity_type == "Listening") {
                imgIcon.setImageResource(R.drawable.headphonesvgformat)
            } else if (item.activity_type == "Speaking") {
                imgIcon.setImageResource(R.drawable.micsvgformatstyle)
            } else if (item.activity_type == "Reading"){
                imgIcon.setImageResource(R.drawable.booksvg_formatstyle)
            } else if (item.activity_type == "Writing"){
                imgIcon.setImageResource(R.drawable.pensvgformatstyle)
            } else {
                imgIcon.setImageResource(R.drawable.questionmark)
            }

            val hasIframe = !item.iframe.isNullOrEmpty()
            val hasFiles = !item.file_path.isNullOrEmpty()

            video_player.visibility = if (hasIframe) View.VISIBLE else View.GONE
            rcyAssignment.visibility = if (hasIframe) View.GONE else View.VISIBLE
            rytList2.visibility = if (hasFiles) View.VISIBLE else View.GONE
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
                    subjectName = "",
                    sentBy = "",
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
                    assignmentsubject = ""
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra("isPreViewData", isHomeWorkData)
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
                    subjectName = "",
                    sentBy = "",
                    thumbnail = item.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_SCHOOL_NEEDS,
                    fileList = convertedList,
                    submittedCount = 0,
                    totalCount = 0,
                    assignmentid = item.activity_type,
                    created_date = item.submitted_date,
                    category = "",
                    assignmentsubject = "",
                    isParentAssignment = false
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra("isPreViewData", isHomeWorkData)
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
                                subjectName = "",
                                sentBy = "",
                                thumbnail = item.thumbnail,
                                isUnread = true,
                                isCompleted = true,
                                isMenuType = Constant.M_SCHOOL_NEEDS,
                                fileList = convertedList,
                                submittedCount = 0,
                                totalCount = 0,
                                assignmentid = item.activity_type,
                                created_date = item.submitted_date,
                                category = "",
                                assignmentsubject = "",
                                isParentAssignment = false
                            )

                            val intent = Intent(context, ChildHomeWork::class.java)
                            intent.putExtra("isPreViewData", isHomeWorkData)
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

