package com.vs.schoolmessenger.CommonScreens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class StudentRoleNumberAdapter(private var childList: List<StudentRoleNumberData>) :
    RecyclerView.Adapter<StudentRoleNumberAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lblAddNumber: TextView = itemView.findViewById(R.id.lblAddNumber)
        val lblRoleNumber: TextView = itemView.findViewById(R.id.lblRoleNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.addnumber_rolenumber_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = childList[position]
        holder.lblAddNumber.text = student.isAddNumber.toString()
        holder.lblRoleNumber.text =student.isRoleNumber.toString()
    }

    override fun getItemCount(): Int = childList.size
}
