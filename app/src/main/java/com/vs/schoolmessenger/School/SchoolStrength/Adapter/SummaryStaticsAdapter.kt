//package com.vs.schoolmessenger.School.SchoolStrength.Adapter
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ProgressBar
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.SchoolStrength.Model.SummaryItem
//import com.vs.schoolmessenger.Utils.ShimmerUtil
//
//
//class SummaryStaticsAdapter(
//    private var itemList: List<SummaryItem>,
//    private var context: Context,
//    private var isLoading: Boolean,
//    private val totalStudents: Int? = null  // New: For % progress calc
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.summarystatics_report)
//            ShimmerViewHolder(shimmerView)
//        } else {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.summarystatics_report, parent, false)
//            DataViewHolder(view)
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is DataViewHolder && !isLoading) {
//            holder.bind(itemList[position], totalStudents)
//        } else if (holder is ShimmerViewHolder) {
//            holder.startShimmer()
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 3 else itemList.size
//    }
//
//    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val student_count: TextView = itemView.findViewById(R.id.student_count)
//        private val boyscount: TextView = itemView.findViewById(R.id.boyscount)
//        private val girlscount: TextView = itemView.findViewById(R.id.girlscount)
//        private val growth_value: TextView = itemView.findViewById(R.id.growth_value)
//        private val progressbar: ProgressBar = itemView.findViewById(R.id.progressbar)
//
//        @SuppressLint("SetTextI18n")
//        fun bind(data: SummaryItem, totalStudents: Int?) {
//            student_count.text = data.value
//            growth_value.text = data.title
//
//            boyscount.visibility = View.GONE
//            girlscount.visibility = View.GONE
//
//            val valueInt = data.value.toIntOrNull() ?: 0
//            val isStudentRelated = data.title != "Staff"
//            if (isStudentRelated && totalStudents != null && totalStudents > 0) {
//                val progressValue = if (data.title == "Students") {
//                    100
//                } else {
//                    ((valueInt * 100f / totalStudents).toInt().coerceIn(0, 100))
//                }
//                progressbar.progress = progressValue
//                progressbar.visibility = View.VISIBLE
//            } else {
//                progressbar.visibility = View.GONE
//            }
//        }
//    }
//
//    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun startShimmer() {
//            ShimmerUtil.startShimmer(itemView)
//        }
//    }
//}
