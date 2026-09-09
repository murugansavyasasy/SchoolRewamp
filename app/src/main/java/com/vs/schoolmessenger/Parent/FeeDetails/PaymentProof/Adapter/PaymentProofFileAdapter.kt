package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.Adapter

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FileType
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.ProofUploaded
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class PaymentProofFileAdapter(
    private val context: Context,
    private val items: List<ProofUploaded>,
) : RecyclerView.Adapter<PaymentProofFileAdapter.FileViewHolder>() {

    inner class FileViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {
        val rootCard: android.view.View = itemView.findViewById(R.id.rootCard)
        val iconBox: android.view.View = itemView.findViewById(R.id.iconBox)
        val tvIconType: TextView = itemView.findViewById(R.id.tvIconType)
        val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.payment_proof_file_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val item = items[position]
        val resolvedType = resolveFileType(item.type)

        val displayName = item.file_name ?: item.url?.substringAfterLast('/')?:""
        holder.tvFileName.text = displayName
        holder.tvIconType.text = iconLabel(resolvedType)

        val (bgHex, textHex) = getColorsForType(resolvedType)
        val iconBg = holder.iconBox.background as? GradientDrawable
        iconBg?.setColor(Color.parseColor(bgHex))
        holder.tvIconType.setTextColor(Color.parseColor(textHex))

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val layoutParams = holder.rootCard.layoutParams
        layoutParams.width = if (items.size > 1) {
            (screenWidth * 0.75).toInt()
        } else {
            ViewGroup.LayoutParams.MATCH_PARENT
        }
        holder.rootCard.layoutParams = layoutParams

        holder.rootCard.setOnClickListener {
            openFilePreview(position)
        }
    }

    private fun openFilePreview(position: Int) {
        Constant.commonFileList.clear()
        items.forEach { fileData ->
            Constant.commonFileList.add(
                CommonFileData(
                    type = fileData.type,
                    path = fileData.url?:"",
                    file_name = fileData.file_name ,
                    original_file_name = fileData.original_file_name
                )
            )
        }
        Constant.selectedFileIndex = position

        val intent = Intent(context, FilesViewActivity::class.java)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = items.size

    private fun resolveFileType(typeStr: String): FileType {
        return try {
            FileType.valueOf(typeStr.uppercase())
        } catch (e: IllegalArgumentException) {
            FileType.OTHER
        }
    }

    private fun iconLabel(type: FileType): String {
        return when (type) {
            FileType.IMAGE -> "IMG"
            FileType.PDF -> "PDF"
            FileType.DOC -> "DOC"
            FileType.DOCX -> "DOC"
            FileType.EXCEL -> "XLS"
            FileType.PPT -> "PPT"
            FileType.TXT -> "TXT"
            FileType.VIDEO -> "VID"
            FileType.AUDIO -> "AUD"
            FileType.OTHER -> "FILE"
        }
    }

    private fun getColorsForType(type: FileType): Pair<String, String> {
        return when (type) {
            FileType.PDF -> Pair("#FDE3E3", "#E53935")            // light red bg, red text
            FileType.EXCEL -> Pair("#DFF5E1", "#2E7D32")          // light green bg, green text
            FileType.PPT -> Pair("#FFE8D1", "#EF6C00")            // light orange bg, orange text
            FileType.IMAGE -> Pair("#D6E4FF", "#3B82F6")          // light blue bg, blue text
            FileType.DOC -> Pair("#DCE8FD", "#1E56C0")            // light blue bg, darker blue text
            FileType.DOCX -> Pair("#DCE8FD", "#1E56C0")           // same as DOC
            FileType.TXT -> Pair("#EDEDED", "#444444")            // light gray bg, dark gray text
            FileType.VIDEO -> Pair("#F3DCF9", "#9C27B0")          // light purple bg, purple text
            FileType.AUDIO -> Pair("#FCE4EC", "#D81B60")          // light pink bg, pink text
            FileType.OTHER -> Pair("#EDEDED", "#000000")          // light gray bg, black text
        }
    }
}