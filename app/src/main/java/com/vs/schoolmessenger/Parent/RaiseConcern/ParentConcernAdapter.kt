package com.vs.schoolmessenger.Parent.RaiseConcern

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventFilePathAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.FilePath
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.Parent.RaiseConcern.ParentConcernlistModel.ConcernFile
import com.vs.schoolmessenger.Parent.RaiseConcern.ParentConcernlistModel.ParentConcern
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ItemConcernListBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ParentConcernAdapter(
    private var list: List<ParentConcern>,
    private val context: Context,
    private val onDeleteClick: (ParentConcern) -> Unit = {}

) : RecyclerView.Adapter<ParentConcernAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemConcernListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConcernListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.apply {
            lblStudentName.text = item.student_name
            lblClassInfo.text =
                "Class ${item.class_name}-${item.section_name} \u00b7 ID: ${item.student_id.take(3)}..."
            lblInitials.text = getInitials(item.student_name)
            lblConcernType.text = item.type_name
            lblDescription.text = item.description
            lblDate.text = "Raised on ${formatDate(item.raised_on)}"

            lblStatus.text = item.status.uppercase()
            val (bgColor, textColor) = statusColors(item.status)
            lblStatus.background.setTint(bgColor)
            lblStatus.setTextColor(textColor)

            btnDelete.visibility = if (item.can_delete) View.VISIBLE else View.GONE
            btnDelete.setOnClickListener { onDeleteClick(item) }

            rytActionButtons.visibility = View.GONE
        }

        bindAttachments(
            files = item.file_path,
            container = holder.binding.rytParentAttachments,
            recyclerView = holder.binding.rcyParentAttachments,
            totalLabel = holder.binding.totalParentAttachments,
            noAttachmentsLabel = holder.binding.lblNoParentAttachments
        )

        bindAttachments(
            files = item.action_file_path,
            container = holder.binding.rytActionAttachments,
            recyclerView = holder.binding.rcyActionAttachments,
            totalLabel = holder.binding.totalActionAttachments,
            noAttachmentsLabel = holder.binding.lblNoActionAttachments
        )

        holder.binding.header.setOnClickListener {
            val convertedList = item.file_path.map {
                GetFilePathDetails(
                    type = it.type,
                    url = it.url,
                )
            }
            Constant.isVideoPostedDate = item.raised_on
            val isHomeWorkData = FilePreview(
                id = "",
                title = item.student_name,
                description = item.description,
                created_date = item.raised_on,
                subjectName = "",
                sentBy = item.action_taken_by,
                thumbnail = "",
                isUnread = true,
                isCompleted = true,
                isMenuType = Constant.M_PARENT_CLASS_EVENTS,
                fileList = convertedList,
            )

            val intent = Intent(context, ChildHomeWork::class.java)
            intent.putExtra(Constant.isPreViewData, isHomeWorkData)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }

    private fun bindAttachments(
        files: List<ConcernFile>,
        container: View,
        recyclerView: RecyclerView,
        totalLabel: TextView,
        noAttachmentsLabel: TextView
    ) {
        if (files.isNullOrEmpty()) {
            container.visibility = View.GONE
            noAttachmentsLabel.visibility = View.VISIBLE
            return
        }

        noAttachmentsLabel.visibility = View.GONE
        container.visibility = View.VISIBLE

        val mappedList: List<FilePath> = files.map {
            FilePath(url = it.url, type = it.type)
        }

        val adapter = EventFilePathAdapter(mappedList, context, Constant.isShimmerViewDisable)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        val totalFiles = mappedList.size
        if (totalFiles > 3) {
            totalLabel.text = "+${totalFiles - 3}"
            totalLabel.visibility = View.VISIBLE
        } else {
            totalLabel.visibility = View.GONE
        }
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> ""
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    private fun statusColors(status: String): Pair<Int, Int> {
        return when (status.trim().lowercase(Locale.getDefault())) {
            "pending" -> Color.parseColor("#FFF3CD") to Color.parseColor("#B8860B")
            "acknowledged" -> Color.parseColor("#DCEBFF") to Color.parseColor("#1D5FC2")
            "resolved", "action taken" -> Color.parseColor("#DFF5E6") to Color.parseColor("#1E8E5A")
            "rejected" -> Color.parseColor("#FDE8E8") to Color.parseColor("#C0392B")
            else -> Color.parseColor("#EFEFEF") to Color.parseColor("#666666")
        }
    }

    private fun formatDate(raw: String): String {
        return try {
            val input = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            output.format(input.parse(raw)!!)
        } catch (e: Exception) {
            raw
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<ParentConcern>) {
        list = newList
        notifyDataSetChanged()
    }
}