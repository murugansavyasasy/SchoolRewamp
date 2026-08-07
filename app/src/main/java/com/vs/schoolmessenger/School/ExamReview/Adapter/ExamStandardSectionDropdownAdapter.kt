package com.vs.schoolmessenger.School.ExamReview.Adapter



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.StandardSection

class ExamStandardSectionDropdownAdapter(
    private val items: List<StandardSection>,
    private val selectedSectionId: String?,
    private val onItemClick: (StandardSection) -> Unit
) : RecyclerView.Adapter<ExamStandardSectionDropdownAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_standard_section_picker, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblTitle: TextView = itemView.findViewById(R.id.lblPickerTitle)
        private val imgCheck: ImageView = itemView.findViewById(R.id.imgPickerCheck)

        fun bind(item: StandardSection) {
            lblTitle.text = "Class ${item.standardName} - Section ${item.sectionName}"
            imgCheck.visibility =
                if (item.sectionId == selectedSectionId) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}