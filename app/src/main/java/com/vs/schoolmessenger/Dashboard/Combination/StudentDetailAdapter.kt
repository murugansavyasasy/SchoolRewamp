package com.vs.schoolmessenger.Dashboard.Combination

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Dashboard.School.Dashboard
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.StudentDetailsListItemBinding

class StudentDetailAdapter(private val itemList: List<StudentDetailsData>, val context: Context) :
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
        val item = itemList[position]

        when (position) {

            0 -> {
//                holder.binding.imgStudentProfile.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.school_building_1
//                    )
//                )

//                holder.binding.rlaStudent.setBackgroundDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.rect_shadow_light_sky_blue
//                    )
//                )
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

//                holder.binding.imgStudentProfile.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.school_building_2
//                    )
//                )

//                holder.binding.rlaStudent.setBackgroundDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.rect_shadow_violet
//                    )
//                )
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

//                holder.binding.imgStudentProfile.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.school_building_3
//                    )
//                )

//                holder.binding.rlaStudent.setBackgroundDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.rect_shadow_green
//                    )
//                )

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

//                holder.binding.imgStudentProfile.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.school_building_4
//                    )
//                )

//                holder.binding.rlaStudent.setBackgroundDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.rect_shadow_blue
//                    )
//                )

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
            val intent = Intent(context, Dashboard::class.java)
            context.startActivity(intent)
        }

        holder.binding.lblRegisterNumber.text = item.registerNumber
        holder.binding.lblName.text = item.name
        holder.binding.lblClass.text = item.standard + " - " + item.section
        holder.binding.lblSchoolName.text = item.schoolName
        holder.binding.lblSchoolPlace.text = item.place
    }

    override fun getItemCount(): Int = itemList.size
}