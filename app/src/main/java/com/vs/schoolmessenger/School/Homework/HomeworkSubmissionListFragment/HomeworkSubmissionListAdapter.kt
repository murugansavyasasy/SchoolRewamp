package com.vs.schoolmessenger.School.Homework.HomeworkSubmissionListFragment



import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Homework.HomeworkSubmissionListModel.GetHomeworkSubmissionListData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.util.Locale

class HomeworkSubmissionListAdapter(
    private var itemList: List<GetHomeworkSubmissionListData>?,
    private var context: Context,
    private var isLoading: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList: MutableList<GetHomeworkSubmissionListData> =
        (itemList ?: emptyList()).toMutableList()
    private var filteredList: MutableList<GetHomeworkSubmissionListData> = originalList.toMutableList()

    var onDataChange: ((Boolean) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.assignment_student_list)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.assignment_student_list, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isLoading && holder is DataViewHolder) {
            filteredList.getOrNull(position)?.let { student ->
                holder.bind(student, position)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else filteredList.size
    }

    fun updateData(newData: List<GetHomeworkSubmissionListData>) {
        originalList.clear()
        originalList.addAll(newData)
        filteredList.clear()
        filteredList.addAll(newData)
        isLoading = false
        notifyDataSetChanged()
        onDataChange?.invoke(filteredList.isNotEmpty())
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString =
                    constraint?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""

                val resultList = if (charString.isEmpty()) {
                    originalList
                } else {
                    originalList.filter { student ->
                        student.status?.lowercase(Locale.getDefault())
                            ?.contains(charString) == true ||
                                student.roll_no?.lowercase(Locale.getDefault())
                                    ?.contains(charString) == true ||
                                student.name?.lowercase(Locale.getDefault())
                                    ?.contains(charString) == true ||
                                student.admission_no?.lowercase(Locale.getDefault())
                                    ?.contains(charString) == true
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList.clear()
                filteredList.addAll(results?.values as? List<GetHomeworkSubmissionListData> ?: emptyList())
                notifyDataSetChanged()
                onDataChange?.invoke(filteredList.isNotEmpty())
            }
        }
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
    ) : RecyclerView.ViewHolder(itemView) {

        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)
        private val statusLabel: TextView = itemView.findViewById(R.id.statuslabel)
        private val sectionlabel: TextView = itemView.findViewById(R.id.sectionlabel)
        private val cancelImage: ImageView = itemView.findViewById(R.id.cancelimage)
        private val statusButton: LinearLayout = itemView.findViewById(R.id.statusButton)
        private val avatarText: TextView = itemView.findViewById(R.id.avatarText)
        private val statusText: TextView = itemView.findViewById(R.id.statuslabel)

        fun bind(data: GetHomeworkSubmissionListData, position: Int) {

            lblStudentName.text = data.name
            avatarText.text = data.name?.firstOrNull()?.uppercase()?.toString() ?: "-"
            if (data.roll_no.isEmpty() || data.roll_no.equals("")){
                sectionlabel.visibility= View.GONE
            }else{
                sectionlabel.visibility= View.VISIBLE
                sectionlabel.text ="${context.getString(R.string.roll_no)} : ${data.roll_no}"
            }
            statusLabel.text = data.status



            if (data.status == Constant.Completed) {
                statusText.text = context.getString(R.string.completed)
                cancelImage.setImageDrawable(null)
                cancelImage.setBackgroundResource(R.drawable.correcticonsvg)
                statusText.setTextColor(ContextCompat.getColor(context, R.color.clr_green))
                statusButton.setBackgroundResource(R.drawable.completed_button_bg)
            } else {
                statusText.text = context.getString(R.string.pending)
                cancelImage.setImageDrawable(null)
                cancelImage.setBackgroundResource(R.drawable.downloadsvgformat)
                statusText.setTextColor("#9e6e40".toColorInt())
                statusButton.setBackgroundResource(R.drawable.pending_button_bg)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
