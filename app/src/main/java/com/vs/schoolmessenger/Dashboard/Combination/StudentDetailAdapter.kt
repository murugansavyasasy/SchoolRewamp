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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant.isParentDashBoardData
import com.vs.schoolmessenger.databinding.StudentDetailsListItemNewBinding

class StudentDetailAdapter(
    private val itemList: List<ChildDetails>?, private var listener: PriorityClickListener,
    val context: Context
) :
    RecyclerView.Adapter<StudentDetailAdapter.GridViewHolder>() {
    private val bgMap = HashMap<String, Int>()

    class GridViewHolder(val binding: StudentDetailsListItemNewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = StudentDetailsListItemNewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = itemList!![position]
        var bgType = bgMap[item.child_id]
        if (bgType == null) {
            bgType = bgMap.size % 4
            bgMap[item.child_id] = bgType
        }

        when (bgType) {
            0 -> holder.binding.rlaStudent.setBackgroundResource(
                R.drawable.bg_gradient_student_one
            )

            1 -> holder.binding.rlaStudent.setBackgroundResource(
                R.drawable.bg_gradient_student_two
            )

            2 -> holder.binding.rlaStudent.setBackgroundResource(
                R.drawable.bg_gradient_student_three
            )

            3 -> holder.binding.rlaStudent.setBackgroundResource(
                R.drawable.bg_gradient_student_four
            )
        }

        holder.binding.rlaStudent.setPadding(10, 10, 10, 10)

        holder.binding.rlaStudent.setOnClickListener {
            isParentDashBoardData = null
            listener.onItemClick(item)
        }

        holder.binding.lblRegisterNumber.text =
            context.getString(R.string.Roll_No) + item.roll_number
        holder.binding.lblClassTeacher.text =
            context.getString(R.string.Class_Teacher) + item.class_teacher
        if (item.school_name_regional == "") {
            holder.binding.lblSchoolRegionalName.visibility = View.GONE
        } else {
            holder.binding.lblSchoolRegionalName.visibility = View.VISIBLE
        }
        holder.binding.lblSchoolRegionalName.text = item.school_name_regional
        holder.binding.lblName.text = item.name
        holder.binding.lblClass.text = item.standard_name + " - " + item.section_name
        holder.binding.lblSchoolName.text = item.school_name
        holder.binding.lblSchoolPlace.text = item.school_city
        holder.binding.lblacademicyear.text =
            "${context.getString(R.string.academic_year)} : ${item.academic_year_name}"

        Glide.with(context)
            .load(item.profile)
            .thumbnail(0.1f)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(600, 600)
            .placeholder(R.drawable.default_profile_fill_icon) // Temporary image while loading
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
            .into(holder.binding.imgStudentProfile)


    }

    override fun getItemCount(): Int = itemList!!.size
}