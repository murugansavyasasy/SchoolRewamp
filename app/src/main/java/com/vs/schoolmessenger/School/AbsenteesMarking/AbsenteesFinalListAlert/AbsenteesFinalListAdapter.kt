package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesFinalListAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AbsenteesFinalListAdapter(
    private var itemList: MutableList<NameAndIds>,
    private var context: Context,
    private var isLoading: Boolean,
    private val onRemove: (NameAndIds) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.absentees_final_list_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.absentees_final_list_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList[position].let { holder.bind(it) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblAdmissionNo: TextView = itemView.findViewById(R.id.lblAdmissionNo)
        private val lblRegNo: TextView = itemView.findViewById(R.id.lblRegNo)
        private val btnRemove: TextView = itemView.findViewById(R.id.btnRemove)

        fun bind(data: NameAndIds) {
            lblName.text = data.name
            if (data.admission_no.isNullOrEmpty()){
                lblAdmissionNo.visibility=View.GONE
            }
            else{
                lblAdmissionNo.visibility=View.VISIBLE
                lblAdmissionNo.text = "Admission no: ${data.admission_no}"
            }

            if (data.roll_no.isNullOrEmpty()){
                lblRegNo.visibility=View.GONE
            }
            else{
                lblRegNo.visibility=View.VISIBLE
                lblRegNo.text = "Roll no: ${data.admission_no}"
            }

            btnRemove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRemove(data)

                    itemList.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
    }
}