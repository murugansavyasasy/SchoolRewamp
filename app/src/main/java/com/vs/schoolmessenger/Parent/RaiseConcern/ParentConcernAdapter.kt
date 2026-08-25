package com.vs.schoolmessenger.Parent.RaiseConcern

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.RaiseConcern.ParentConcernlistModel.ParentConcern
import com.vs.schoolmessenger.databinding.ItemConcernListBinding

class ParentConcernAdapter(
    private var list: List<ParentConcern>
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
            lblConcernType.text = item.type_name
            // repurposed - the response has no "raised_to", so show class/section context instead
            lblRaisedTo.text = "Class: ${item.class_name} - ${item.section_name}"
            lblDescription.text = item.description
            lblStatus.text = item.status
            lblDate.text = item.raised_on
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<ParentConcern>) {
        list = newList
        notifyDataSetChanged()
    }
}