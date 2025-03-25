package com.vs.schoolmessenger.Dashboard.Combination

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.StudentDetailsListItemBinding

class StudentDetailAdapter(private val itemList: List<ChildDetails>?, val context: Context) :
    RecyclerView.Adapter<StudentDetailAdapter.GridViewHolder>() {


    class GridViewHolder(val binding: StudentDetailsListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = StudentDetailsListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = itemList!![position]

        when (position) {

            0 -> {

                holder.binding.rlaStudent.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_blue_gradient
                    )
                )

                holder.binding.rlaSchoolName.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_bg_blue
                    )
                )
            }

            1 -> {

                holder.binding.rlaStudent.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_purple_gradient
                    )
                )

                holder.binding.rlaSchoolName.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_bg_purple
                    )
                )
            }

            2 -> {
                holder.binding.rlaStudent.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_orange_gradient
                    )
                )
                holder.binding.rlaSchoolName.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_bg_orange
                    )
                )
            }

            3 -> {

                holder.binding.rlaStudent.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_green_gradient
                    )
                )
                holder.binding.rlaSchoolName.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_bg_green
                    )
                )
            }
        }

        holder.binding.rlaStudent.setOnClickListener {
            val intent = Intent(context, SchoolDashboard::class.java)
            context.startActivity(intent)
        }

        holder.binding.lblRegisterNumber.text = "Register No:"+ item.roll_number
        holder.binding.lblName.text = item.child_name
        holder.binding.lblClass.text = item.standard_name + " - " + item.section_name
        holder.binding.lblSchoolName.text = item.school_name
        holder.binding.lblSchoolPlace.text = item.school_city

        Glide.with(context)
            .load(item.school_logo_url)
            .into(holder.binding.imgStudentProfile)
    }

    override fun getItemCount(): Int = itemList!!.size
}