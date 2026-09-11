package com.vs.schoolmessenger.School.SchoolRaiseConcern

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventFilePathAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.FilePath
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.Parent.RaiseConcern.ParentConcernlistModel.ConcernFile
import com.vs.schoolmessenger.Parent.RaiseConcern.ParentConcernlistModel.ParentConcern
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ItemConcernListBinding
import java.text.SimpleDateFormat
import java.util.Locale

class SchoolRaiseConcernAdapter(
    private var list: List<ParentConcern>,
    private val context: Context,
    private val onAcknowledgeClick: (ParentConcern, String) -> Unit = { _, _ -> },
    private val onActionTakenClick: (ParentConcern) -> Unit = {}
) : RecyclerView.Adapter<SchoolRaiseConcernAdapter.ViewHolder>() {

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
            if (item.description.isNullOrEmpty()) {
                lblDescription.isGone = true
            } else {
                lblDescription.isVisible = true
                lblDescription.text = item.description
            }

            if (item.action_taken.isNullOrEmpty()) {
                lblActionDescription.isGone = true
            } else {
                lblActionDescription.isVisible = true
                lblActionDescription.text = item.action_taken
            }
            lblDate.text = "Raised on ${formatDate(item.raised_on)}"

            lblStatus.text = item.status.uppercase()
            val (bgColor, textColor) = statusColors(item.status)
            lblStatus.background.setTint(bgColor)
            lblStatus.setTextColor(textColor)

            btnDelete.visibility = View.GONE
            rytActionButtons.visibility = View.VISIBLE

            if (item.is_acknowledged) {
                btnAcknowledge.text = "Acknowledge"
                btnAcknowledge.setBackgroundResource(R.drawable.bg_button_blue)
                btnAcknowledge.setTextColor(
                    ContextCompat.getColor(btnAcknowledge.context, R.color.white)
                )
                btnAcknowledge.setOnClickListener { showAcknowledgeInputDialog(item) }

                btnActionTaken.visibility = View.GONE
            } else {
                btnAcknowledge.text = "View Acknowledge"
                btnAcknowledge.setBackgroundResource(R.drawable.bg_button_acknowledge_outline)
                btnAcknowledge.setTextColor(
                    ContextCompat.getColor(btnAcknowledge.context, R.color.PrimaryColor)
                )
                btnAcknowledge.setOnClickListener { showAcknowledgeDetailsPopup(item) }

                btnActionTaken.visibility = View.VISIBLE

                if (item.is_action) {
                    btnActionTaken.text = "Action Taken"
                    btnActionTaken.setBackgroundResource(R.drawable.bg_button_green)
                    btnActionTaken.setTextColor(
                        ContextCompat.getColor(btnActionTaken.context, R.color.white)
                    )
                    btnActionTaken.setOnClickListener { onActionTakenClick(item) }
                } else {
                    btnActionTaken.text = "View Action Taken"
                    btnActionTaken.setBackgroundResource(R.drawable.bg_button_green_action_taken)
                    btnActionTaken.setTextColor(
                        ContextCompat.getColor(btnActionTaken.context, R.color.light_shade_yellow)
                    )
                    btnActionTaken.setOnClickListener { showActionTakenDetailsPopup(item) }
                }
            }

            lytParentAttachmentsHeader.setOnClickListener {
                openFilePreview(item, item.file_path)
            }
            lytActionAttachmentsHeader.setOnClickListener {
                openActionFilePreview(item, item.action_file_path)
            }
        }

        bindAttachments(
            files = item.file_path,
            container = holder.binding.rytParentAttachments,
            recyclerView = holder.binding.rcyParentAttachments,
            totalLabel = holder.binding.totalParentAttachments,
            noAttachmentsLabel = holder.binding.lblNoParentAttachments,
            onContainerClick = { files -> openFilePreview(item, files) }
        )

        bindAttachments(
            files = item.action_file_path,
            container = holder.binding.rytActionAttachments,
            recyclerView = holder.binding.rcyActionAttachments,
            totalLabel = holder.binding.totalActionAttachments,
            noAttachmentsLabel = holder.binding.lblNoActionAttachments,
            onContainerClick = { files -> openActionFilePreview(item, files) }
        )

    }


    private fun showAcknowledgeInputDialog(item: ParentConcern) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_acknowledge_concern)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val marginPx = (24 * context.resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(
            context.resources.displayMetrics.widthPixels - (marginPx * 2),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val edtDescription = dialog.findViewById<EditText>(R.id.edtDescription)
        val lblError = dialog.findViewById<TextView>(R.id.lblDescriptionError)
        val btnCancel = dialog.findViewById<TextView>(R.id.btnCancel)
        val btnSubmit = dialog.findViewById<TextView>(R.id.btnSubmit)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val description = edtDescription.text.toString().trim()
            if (description.isEmpty()) {
                lblError.visibility = View.VISIBLE
                edtDescription.requestFocus()
                return@setOnClickListener
            }
            lblError.visibility = View.GONE
            onAcknowledgeClick(item, description)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun showAcknowledgeDetailsPopup(item: ParentConcern) {
        val rows = listOf(
            "ACKNOWLEDGED BY" to item.acknowledged_by.ifBlank { "-" },
            "ACKNOWLEDGED ON" to formatDate(item.acknowledged_on).ifBlank { "-" },
            "REMARKS" to item.acknowledgement.ifBlank { "-" }
        )
        showDetailsDialog(
            title = "Acknowledgement Details",
            iconRes = R.drawable.attachment_icon_2,
            rows = rows
        )
    }

    private fun showActionTakenDetailsPopup(item: ParentConcern) {
        val rows = listOf(
            "ACTION TAKEN BY" to item.action_taken_by.ifBlank { "-" },
            "ACTION TAKEN ON" to formatDate(item.action_taken_on).ifBlank { "-" },
            "DETAILS" to item.action_taken.ifBlank { "-" }
        )
        showDetailsDialog(
            title = "Action Taken Details",
            iconRes = R.drawable.file_noticeboard,
            rows = rows
        )
    }

    private fun showDetailsDialog(title: String, iconRes: Int, rows: List<Pair<String, String>>) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_concern_details)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val marginPx = (24 * context.resources.displayMetrics.density).toInt()
        dialog.window?.setLayout(
            context.resources.displayMetrics.widthPixels - (marginPx * 2),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val imgIcon = dialog.findViewById<android.widget.ImageView>(R.id.imgDialogIcon)
        val imgClose = dialog.findViewById<android.widget.ImageView>(R.id.imgDialogClose)
        val lblTitle = dialog.findViewById<TextView>(R.id.lblDialogTitle)
        val lytRows = dialog.findViewById<LinearLayout>(R.id.lytDetailRows)
        val btnOk = dialog.findViewById<TextView>(R.id.btnDialogOk)

        imgIcon.setImageResource(iconRes)
        lblTitle.text = title

        val inflater = LayoutInflater.from(context)
        rows.forEach { (label, value) ->
            val rowView = inflater.inflate(R.layout.item_detail_row, lytRows, false)
            rowView.findViewById<TextView>(R.id.lblRowLabel).text = label
            rowView.findViewById<TextView>(R.id.lblRowValue).text = value
            lytRows.addView(rowView)
        }

        imgClose.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }


    private fun openFilePreview(item: ParentConcern, files: List<ConcernFile>) {
        val convertedList = files.map {
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
            isMenuType = Constant.M_EXAMREVIEW,
            fileList = convertedList,
        )

        val intent = Intent(context, ChildHomeWork::class.java)
        intent.putExtra(Constant.isPreViewData, isHomeWorkData)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }



    private fun openActionFilePreview(item: ParentConcern, files: List<ConcernFile>) {
        val convertedList = files.map {
            GetFilePathDetails(
                type = it.type,
                url = it.url,
            )
        }
        Constant.isVideoPostedDate = item.raised_on
        val isHomeWorkData = FilePreview(
            id = item.id,
            title = item.action_taken_by,
            description = item.action_taken,
            created_date = item.action_taken_on,
            subjectName = "",
            sentBy = item.student_name,
            thumbnail = "",
            isUnread = true,
            isCompleted = true,
            isMenuType = Constant.M_EXAMREVIEW,
            fileList = convertedList,
        )

        val intent = Intent(context, ChildHomeWork::class.java)
        intent.putExtra(Constant.isPreViewData, isHomeWorkData)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }

    private fun bindAttachments(
        files: List<ConcernFile>,
        container: View,
        recyclerView: RecyclerView,
        totalLabel: TextView,
        noAttachmentsLabel: TextView,
        onContainerClick: (List<ConcernFile>) -> Unit
    ) {
        if (files.isNullOrEmpty()) {
            container.visibility = View.GONE
            noAttachmentsLabel.visibility = View.VISIBLE
            container.setOnClickListener(null)
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

        container.setOnClickListener { onContainerClick(files) }
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
        if (raw.isBlank()) return raw
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