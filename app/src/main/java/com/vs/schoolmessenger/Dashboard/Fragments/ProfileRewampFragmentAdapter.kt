package com.vs.schoolmessenger.Dashboard.Fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileField
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileItem
import com.vs.schoolmessenger.R


class ProfileRewampFragmentAdapter(
    private var itemList: List<ProfileItem>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_FIELD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is ProfileItem.Header -> VIEW_TYPE_HEADER
            is ProfileItem.Field -> VIEW_TYPE_FIELD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_rewamp_list, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_rewamp_list, parent, false)
            FieldViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = itemList[position]) {
            is ProfileItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ProfileItem.Field -> (holder as FieldViewHolder).bind(item.field)
        }
    }

    override fun getItemCount(): Int = itemList.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.header)
        fun bind(item: ProfileItem.Header) {
            tvHeader.text = item.title
        }
    }

    inner class FieldViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titlelabel: TextView = itemView.findViewById(R.id.titlelabel)
        private val titlevalue: EditText = itemView.findViewById(R.id.titlevalue)

        fun bind(field: ProfileField) {
            titlelabel.text = field.title
            titlevalue.setText(field.value ?: "")

            titlevalue.isEnabled = field.is_editable

            val imgCalendar: ImageView? = itemView.findViewById(R.id.imgCalendar)
            imgCalendar?.visibility = if (field.type == "calendar") View.VISIBLE else View.GONE
        }
    }
}
