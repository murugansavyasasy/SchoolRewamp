package com.vs.schoolmessenger.School.SchoolStrength.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.SchoolStrength.Model.Section
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SchoolStrengthDetailAdapter(
    private var itemList: List<Section>,
    private var context: Context,
    private var isLoading: Boolean,

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.schoolstrength_report_detail)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.schoolstrength_report_detail, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position, this)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val sectionname: TextView = itemView.findViewById(R.id.sectionname)
        private val totalcount: TextView = itemView.findViewById(R.id.totalcount)
        private val boyscount: TextView = itemView.findViewById(R.id.boyscount)
        private val girlscount: TextView = itemView.findViewById(R.id.girlscount)
        private val otherscount: TextView = itemView.findViewById(R.id.otherscount)


        @SuppressLint("SetTextI18n")
        fun bind(
            data: Section, position: Int, adapter: SchoolStrengthDetailAdapter
        ) {
            sectionname.text = context.getString(R.string.Section) + "-" + data.name
            val studentCount = data.total_students.toIntOrNull() ?: 0

            val count = studentCount ?: 0
            totalcount.text =
                if (count <= 1) "${context.getString(R.string.Total_student)} - $count" else "${
                    context.getString(
                        R.string.total_students
                    )
                } - $count"





            boyscount.text = HtmlCompat.fromHtml(
                "<font color='#808080'> ${context.getString(R.string.boys)} :</font> ${data.boys_count}",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            girlscount.text = HtmlCompat.fromHtml(
                "<font color='#808080'>  ${context.getString(R.string.girls)} :</font> ${data.girls_count}",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            val othersCountValue = data.others_count?.takeIf { it.isNotBlank() } ?: "0"
            otherscount.text = HtmlCompat.fromHtml(
                "<font color='#808080'>${"Not specified"} :</font> $othersCountValue",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )


        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}