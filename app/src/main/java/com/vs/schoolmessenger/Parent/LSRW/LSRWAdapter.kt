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
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.Parent.LSRW.Model.SkillData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.LsrwAdapter
import com.vs.schoolmessenger.School.LSRW.Model.lsrwskilldata
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

        fun bind(item: SkillData) {
            txtTitle.text = item.title ?: "-"
            txtSubtitle.text = item.activity_type ?: "-"
            txtMainDesc.text = item.subject ?: "-"
            txtSubDesc.text = item.description ?: "-"
            txtProfile.text = item.sent_by ?: "-"
        }
    }
}

