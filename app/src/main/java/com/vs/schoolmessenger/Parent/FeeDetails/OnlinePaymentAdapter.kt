package com.vs.schoolmessenger.Parent.FeeDetails

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.FeeDetails.Model.OnlinePaymentData
import com.vs.schoolmessenger.R

class OnlinePaymentAdapter(
    private val originalList: ArrayList<OnlinePaymentData>,
    private val onRefreshClick: (OnlinePaymentData, Int) -> Unit,
    private val filterResultListener: FeeDetails.OnFilterResultListener? = null
) : RecyclerView.Adapter<OnlinePaymentAdapter.ViewHolder>(), android.widget.Filterable {

    private var filteredList: List<OnlinePaymentData> = originalList

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val txtOrderValue: TextView = itemView.findViewById(R.id.txtOrderValue)
        val txtStudentValue: TextView = itemView.findViewById(R.id.txtStudentValue)
        val txtCreatedValue: TextView = itemView.findViewById(R.id.txtCreatedValue)

        val imgRefresh: ImageView = itemView.findViewById(R.id.imgRefresh)
        val imgSuccess: ImageView = itemView.findViewById(R.id.imgSuccess)
        val divider2: View = itemView.findViewById(R.id.divider2)

        val txtSuccess: TextView = itemView.findViewById(R.id.txtSuccess)
        val layoutSuccess: LinearLayout = itemView.findViewById(R.id.layoutSuccess)

        val layoutCompleted: LinearLayout = itemView.findViewById(R.id.layoutCompleted)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.all_payment_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = filteredList[position]

        holder.txtAmount.text = item.total_amount
        holder.txtOrderValue.text = item.order_id
        holder.txtStudentValue.text = item.created_on
        holder.txtCreatedValue.text = item.order_status_update_on

        val status = item.order_status
        holder.txtSuccess.text = status
        if (status == "PAID") {

            holder.imgRefresh.visibility = View.GONE
            holder.divider2.visibility = View.VISIBLE
            holder.layoutCompleted.visibility = View.VISIBLE
            holder.txtSuccess.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.green)
            )
            holder.layoutSuccess.setBackgroundResource(R.drawable.bg_light_green_radious)
            holder.imgSuccess.setImageResource(R.drawable.checked_green)

        } else {

            holder.imgRefresh.visibility = View.VISIBLE
            holder.divider2.visibility = View.GONE
            holder.layoutCompleted.visibility = View.GONE
            holder.txtSuccess.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.red)
            )
            holder.layoutSuccess.setBackgroundResource(R.drawable.rect_bg_light_red_absent)
            holder.imgSuccess.setImageResource(R.drawable.close_red_color)
        }

        holder.imgRefresh.setOnClickListener {
            // Use the item's real index in the underlying (unfiltered) list,
            // so refresh still targets the right record while a filter is active.
            val realPos = originalList.indexOf(item)
            onRefreshClick(item, if (realPos != -1) realPos else position)
        }
    }

    override fun getFilter(): android.widget.Filter {
        return object : android.widget.Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val results: List<OnlinePaymentData> = if (query.isEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.order_id?.lowercase()?.contains(query) == true ||
                                it.total_amount?.lowercase()?.contains(query) == true ||
                                it.student_id?.lowercase()?.contains(query) == true ||
                                it.order_status?.lowercase()?.contains(query) == true
                    }
                }
                return FilterResults().apply { values = results }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<OnlinePaymentData> ?: listOf()
                notifyDataSetChanged()
                filterResultListener?.onFilterResult(filteredList.isEmpty())
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<OnlinePaymentData>) {
        originalList.clear()
        originalList.addAll(list)
        filteredList = originalList
        notifyDataSetChanged()
    }
}

//package com.vs.schoolmessenger.Parent.FeeDetails
//
//import android.annotation.SuppressLint
//import android.graphics.Color
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.Parent.FeeDetails.Model.OnlinePaymentData
//import com.vs.schoolmessenger.R
//
//class OnlinePaymentAdapter(
//    private val paymentList: ArrayList<OnlinePaymentData>,
//    private val onRefreshClick: (OnlinePaymentData, Int) -> Unit
//) : RecyclerView.Adapter<OnlinePaymentAdapter.ViewHolder>() {
//
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        val txtAmount: TextView =
//            itemView.findViewById(R.id.txtAmount)
//
//        val txtOrderValue: TextView =
//            itemView.findViewById(R.id.txtOrderValue)
//
//        val txtStudentValue: TextView =
//            itemView.findViewById(R.id.txtStudentValue)
//
//        val txtCreatedValue: TextView =
//            itemView.findViewById(R.id.txtCreatedValue)
//
//        val imgRefresh: ImageView = itemView.findViewById(R.id.imgRefresh)
//        val imgSuccess: ImageView = itemView.findViewById(R.id.imgSuccess)
//        val divider2: View = itemView.findViewById(R.id.divider2)
//
//        val txtSuccess: TextView = itemView.findViewById(R.id.txtSuccess)
//        val layoutSuccess: LinearLayout = itemView.findViewById(R.id.layoutSuccess)
//
//        val layoutCompleted: LinearLayout =
//            itemView.findViewById(R.id.layoutCompleted)
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): ViewHolder {
//
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.all_payment_item, parent, false)
//
//        return ViewHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        return paymentList.size
//    }
//
//    @SuppressLint("SetTextI18n")
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//        val item = paymentList[position]
//
//        holder.txtAmount.text = item.total_amount
//        holder.txtOrderValue.text = item.order_id
//        holder.txtStudentValue.text = item.student_id
//        holder.txtStudentValue.text = item.created_on
//        holder.txtCreatedValue.text = item.order_status_update_on
//
//        val status = item.order_status
//        holder.txtSuccess.text = status
//        if (status == "PAID") {
//
//            holder.imgRefresh.visibility = View.GONE
//            holder.divider2.visibility = View.VISIBLE
//            holder.layoutCompleted.visibility = View.VISIBLE
//            holder.txtSuccess.setTextColor(
//                ContextCompat.getColor(holder.itemView.context, R.color.green)
//            )
//            holder.layoutSuccess.setBackgroundResource(R.drawable.bg_light_green_radious)
//            holder.imgSuccess.setImageResource(R.drawable.checked_green)
//
//        } else {
//
//            holder.imgRefresh.visibility = View.VISIBLE
//            holder.divider2.visibility = View.GONE
//            holder.layoutCompleted.visibility = View.GONE
//            holder.txtSuccess.setTextColor(
//                ContextCompat.getColor(holder.itemView.context, R.color.red)
//            )
//            holder.layoutSuccess.setBackgroundResource(R.drawable.rect_bg_light_red_absent)
//            holder.imgSuccess.setImageResource(R.drawable.close_red_color)
//        }
//
//        holder.imgRefresh.setOnClickListener {
//            onRefreshClick(item, position)
//        }
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun updateList(list: List<OnlinePaymentData>) {
//        paymentList.clear()
//        paymentList.addAll(list)
//        notifyDataSetChanged()
//    }
//}