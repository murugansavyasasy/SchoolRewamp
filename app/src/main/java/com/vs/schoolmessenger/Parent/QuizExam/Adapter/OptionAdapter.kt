package com.vs.schoolmessenger.Parent.QuizExam.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.QuizExam.Model.OptionModel
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ItemOptionBinding

class OptionAdapter(
    private val list: MutableList<OptionModel>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<OptionAdapter.OptionVH>() {

    inner class OptionVH(val binding: ItemOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionVH {
        val binding = ItemOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OptionVH(binding)
    }

    override fun onBindViewHolder(holder: OptionVH, position: Int) {
        val item = list[position]

        holder.binding.tvOptionBullet.text = item.option
        holder.binding.tvOptionText.text = item.text

        holder.binding.ivCheckmark.visibility =
            if (item.isSelected) View.VISIBLE else View.GONE

        holder.binding.optionContainer.setBackgroundResource(
            if (item.isSelected)
                R.drawable.bg_option_selected
            else
                R.drawable.bg_option_normal
        )

        if (!item.graphImageUrl.isNullOrEmpty()) {
            holder.binding.ivGraph.visibility = View.VISIBLE

            Glide.with(holder.binding.ivGraph.context)
                .load(item.graphImageUrl)
                .placeholder(R.drawable.default_image_icon)
                .error(R.drawable.default_image_icon)
                .into(holder.binding.ivGraph)
        } else {
            holder.binding.ivGraph.visibility = View.GONE
        }

        holder.binding.root.setOnClickListener {
            onItemClick(position)
        }

        holder.binding.layoutGraph.setOnClickListener {
            val context = holder.binding.root.context
            val imageUrl = item.graphImageUrl ?: return@setOnClickListener

            com.vs.schoolmessenger.Utils.Constant.commonFileList.clear()
            com.vs.schoolmessenger.Utils.Constant.commonFileList.add(
                com.vs.schoolmessenger.CommonScreens.CommonFileData(
                    type = com.vs.schoolmessenger.Utils.Constant.IMAGE,
                    path = imageUrl
                )
            )

            com.vs.schoolmessenger.Utils.Constant.selectedFileIndex = 0

            val intent = android.content.Intent(context, com.vs.schoolmessenger.CommonScreens.FilesViewActivity::class.java)
            context.startActivity(intent)
        }

        holder.binding.ivGraph.setOnClickListener {
            holder.binding.layoutGraph.performClick()
        }


        holder.binding.ivGraph.setOnClickListener {
            holder.binding.layoutGraph.performClick()
        }
    }



    override fun getItemCount(): Int = list.size
}
