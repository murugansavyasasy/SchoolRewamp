package com.vs.schoolmessenger.Dashboard.Combination

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.SchoolDetailsListItemBinding

class StaffDetailAdapter(private val itemList: List<StaffDetailData>, val context: Context) :
    RecyclerView.Adapter<StaffDetailAdapter.GridViewHolder>() {

    class GridViewHolder(val binding: SchoolDetailsListItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding =
            SchoolDetailsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = itemList[position]

        when (position) {

            0 -> {
//                holder.binding.rlaStaffDetails.setBackgroundDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.rect_shadow_light_sky_blue
//                    )
//                )

                holder.binding.rlaStaffDetails.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_blue_gradient
                    )
                )

                holder.binding.imgSchool.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.school_building_1
                    )
                )

                holder.binding.rlaStaffDetails.setPadding(50, 50, 50, 50)
            }

            1 -> {
//                holder.binding.rlaStaffDetails.setBackgroundDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.rect_shadow_violet
//                    )
//                )
                holder.binding.rlaStaffDetails.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_purple_gradient
                    )
                )


                holder.binding.imgSchool.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.school_building_2
                    )
                )

                holder.binding.rlaStaffDetails.setPadding(50, 50, 50, 50)
            }

            2 -> {
//                holder.binding.rlaStaffDetails.setBackgroundDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.rect_shadow_green
//                    )
//                )
                holder.binding.rlaStaffDetails.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_green_gradient
                    )
                )


                holder.binding.imgSchool.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.school_building_3
                    )
                )

                holder.binding.rlaStaffDetails.setPadding(50, 50, 50, 50)
            }

            3 -> {
//                holder.binding.rlaStaffDetails.setBackgroundDrawable(
//                    ContextCompat.getDrawable(
//                        context,
//                        R.drawable.rect_shadow_blue
//                    )
//                )
                holder.binding.rlaStaffDetails.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_orange_gradient
                    )
                )


                holder.binding.imgSchool.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.school_building_4
                    )
                )
                holder.binding.rlaStaffDetails.setPadding(50, 50, 50, 50)
            }
        }

        holder.binding.lblSchoolName.text = item.schoolName
        holder.binding.lblRole.text = item.role
        holder.binding.lblStaffName.text = item.name
        holder.binding.lblSchoolAddress.text = item.place
    }

    override fun getItemCount(): Int = itemList.size
}