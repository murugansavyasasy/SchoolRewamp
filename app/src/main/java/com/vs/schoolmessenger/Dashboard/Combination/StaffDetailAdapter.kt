package com.vs.schoolmessenger.Dashboard.Combination

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SchoolClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.SchoolDetailsListItemBinding

class StaffDetailAdapter(
    private val itemList: List<StaffDetails>?,
    val context: Context,
    private var listener: SchoolClickListener,
    private val isStaffRole: String
) :
    RecyclerView.Adapter<StaffDetailAdapter.GridViewHolder>() {

    class GridViewHolder(val binding: SchoolDetailsListItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding =
            SchoolDetailsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = itemList!![position]

        if (isStaffRole == Constant.isStaffRole) {
            holder.binding.selectionarrow.visibility = View.VISIBLE
        } else {
            holder.binding.selectionarrow.visibility = View.GONE
        }
        when (position) {

            0 -> {

                holder.binding.rlaStaffDetails.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_blue_gradient
                    )
                )

                holder.binding.rlaStaffDetails.setPadding(50, 50, 50, 50)
            }

            1 -> {

                holder.binding.rlaStaffDetails.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_purple_gradient
                    )
                )

                holder.binding.rlaStaffDetails.setPadding(50, 50, 50, 50)
            }

            2 -> {

                holder.binding.rlaStaffDetails.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_green_gradient
                    )
                )

                holder.binding.rlaStaffDetails.setPadding(50, 50, 50, 50)
            }

            3 -> {

                holder.binding.rlaStaffDetails.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_orange_gradient
                    )
                )
                holder.binding.rlaStaffDetails.setPadding(50, 50, 50, 50)
            }
        }

        holder.binding.lblSchoolName.text = item.school_name
        holder.binding.lblRole.text = item.role
        holder.binding.lblStaffName.text = item.name
        holder.binding.lblSchoolAddress.text = item.school_address
        holder.binding.lblCity.text = item.city

        holder.binding.rlaStaffDetails.setOnClickListener {
            listener.onItemClick(item)
        }

        Glide.with(context)
            .load(item.school_logo)
            .placeholder(R.drawable.splash_icon1) // Temporary image while loading
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    // Log the error if needed
                    Log.e("GlideError", "Image load failed", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(holder.binding.imgSchool)

    }

    override fun getItemCount(): Int = itemList!!.size
}