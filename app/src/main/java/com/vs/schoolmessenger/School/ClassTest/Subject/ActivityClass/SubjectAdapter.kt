package com.vs.schoolmessenger.School.ClassTest.Subject.ActivityClass
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ClassTest.Subject.ModelClass.SectionDataDetail
import com.vs.schoolmessenger.School.ClassTest.Subject.ModelClass.SubjectDataDetail

class SubjectAdapter(
    private val sections: List<SectionDataDetail>,
    private val onSelectionChanged: (totalSelected: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SECTION_HEADER = 0
        private const val VIEW_TYPE_SUBJECT_ROW    = 1
    }

    private sealed class ListItem {
        data class SectionHeader(
            val sectionId: String,
            val sectionName: String,
            val totalSubjects: Int
        ) : ListItem()

        data class SubjectRow(
            val sectionId: String,
            val subject: SubjectDataDetail,
            var isSelected: Boolean = false
        ) : ListItem()
    }

    private val flatList = mutableListOf<ListItem>()

    private val selectedMap = mutableMapOf<String, MutableSet<String>>()

    init {
        buildFlatList()
    }

    private fun buildFlatList() {
        flatList.clear()
        for (section in sections) {
            flatList.add(
                ListItem.SectionHeader(
                    sectionId    = section.section_id,
                    sectionName  = section.section_name,
                    totalSubjects = section.subjects.size
                )
            )
            for (subject in section.subjects) {
                flatList.add(
                    ListItem.SubjectRow(
                        sectionId  = section.section_id,
                        subject    = subject,
                        isSelected = false
                    )
                )
            }
            selectedMap[section.section_id] = mutableSetOf()
        }
    }


    inner class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSectionInitial: TextView  = itemView.findViewById(R.id.tvSectionInitial)
        val tvSectionName: TextView     = itemView.findViewById(R.id.tvSectionName)
        val tvSelectionInfo: TextView   = itemView.findViewById(R.id.tvSelectionInfo)
    }

    inner class SubjectRowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox      = itemView.findViewById(R.id.checkboxSubject)
        val ivBook: ImageView       = itemView.findViewById(R.id.ivBookIcon)
        val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
        val tvSelected: TextView    = itemView.findViewById(R.id.tvSelectedBadge)
    }


    override fun getItemViewType(position: Int): Int =
        when (flatList[position]) {
            is ListItem.SectionHeader -> VIEW_TYPE_SECTION_HEADER
            is ListItem.SubjectRow    -> VIEW_TYPE_SUBJECT_ROW
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SECTION_HEADER -> SectionHeaderViewHolder(
                inflater.inflate(R.layout.item_subject_section_header, parent, false)
            )
            else -> SubjectRowViewHolder(
                inflater.inflate(R.layout.item_subject_row, parent, false)
            )
        }
    }

    override fun getItemCount() = flatList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = flatList[position]) {
            is ListItem.SectionHeader -> bindSectionHeader(holder as SectionHeaderViewHolder, item)
            is ListItem.SubjectRow    -> bindSubjectRow(holder as SubjectRowViewHolder, item)
        }
    }

    private fun bindSectionHeader(holder: SectionHeaderViewHolder, item: ListItem.SectionHeader) {
        holder.tvSectionInitial.text = item.sectionName.first().toString()
        holder.tvSectionName.text    = "Section ${item.sectionName}"
        val selectedCount = selectedMap[item.sectionId]?.size ?: 0
        holder.tvSelectionInfo.text  = "$selectedCount/${item.totalSubjects} selected"
    }

    private fun bindSubjectRow(holder: SubjectRowViewHolder, item: ListItem.SubjectRow) {
        holder.tvSubjectName.text = item.subject.name

        val currentPos = flatList.indexOf(item)
        val isLastInSection = currentPos == flatList.size - 1 ||
                flatList[currentPos + 1] is ListItem.SectionHeader

        holder.itemView.setBackgroundResource(
            if (isLastInSection) R.drawable.bg_table_row_last
            else R.drawable.bg_table_row
        )

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = item.isSelected

        if (item.isSelected) {
            holder.tvSelected.visibility = View.VISIBLE
            holder.tvSelected.text = "Selected"
        } else {
            holder.tvSelected.visibility = View.GONE
        }

        val toggle = {
            item.isSelected = !item.isSelected
            val sectionSet = selectedMap.getOrPut(item.sectionId) { mutableSetOf() }
            if (item.isSelected) sectionSet.add(item.subject.id)
            else sectionSet.remove(item.subject.id)

            notifyItemChanged(holder.adapterPosition)
            refreshSectionHeader(item.sectionId)
            onSelectionChanged(getTotalSelectedCount())
        }

        holder.checkbox.setOnCheckedChangeListener { _, _ -> toggle() }
        holder.itemView.setOnClickListener { toggle() }
    }


    private fun refreshSectionHeader(sectionId: String) {
        val headerPos = flatList.indexOfFirst {
            it is ListItem.SectionHeader && it.sectionId == sectionId
        }
        if (headerPos >= 0) notifyItemChanged(headerPos)
    }

    private fun getTotalSelectedCount(): Int =
        selectedMap.values.sumOf { it.size }

    fun getTotalSubjectCount(): Int =
        flatList.count { it is ListItem.SubjectRow }

    fun getAllSelectedSubjects(): List<SubjectDataDetail> =
        flatList
            .filterIsInstance<ListItem.SubjectRow>()
            .filter { it.isSelected }
            .map { it.subject }
}