package com.vs.schoolmessenger.School.LSRW.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.School.LSRW.AvgPerformanceModel.AvgStudentSubmission
import com.vs.schoolmessenger.databinding.ItemStudentlistReccleBinding

class StudentListAdapter(
    private val items: List<AvgStudentSubmission>
) : RecyclerView.Adapter<StudentListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemStudentlistReccleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentlistReccleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            txtTitle.text = item.student_name
            txtclassname.text = "Class ${item.std_sec}"
            txtValue.text  = item.remark
            val name = item.student_name
            avatarText.text = if (!name.isNullOrEmpty()) {
                name.first().toString().uppercase()
            } else {
                "-"
            }



        }
    }

    override fun getItemCount(): Int = items.size
}
