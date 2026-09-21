package com.vs.schoolmessenger.Dashboard.Fragments.Profile

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileField
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileItem
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.Listener.DocumentClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import java.util.Calendar

class ProfileRewampFragmentAdapter(
    private var itemList: List<ProfileItem>,
    private val context: Context,
    private val listener: DocumentClickListener,
    private val rcyImages: RecyclerView? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_FIELD = 1
    }

    private var isEditMode: Boolean = false

    fun setEditMode(enabled: Boolean) {
        if (isEditMode != enabled) {
            isEditMode = enabled
            notifyDataSetChanged()
        }
    }

    fun isInEditMode(): Boolean = isEditMode

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is ProfileItem.Header -> VIEW_TYPE_HEADER
            is ProfileItem.Field -> VIEW_TYPE_FIELD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_rewamp_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_rewamp_list, parent, false)
            FieldViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = itemList[position]) {
            is ProfileItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ProfileItem.Field -> (holder as FieldViewHolder).bind(item.field, position)
        }
    }

    override fun getItemCount(): Int = itemList.size

    private fun isFirstInSection(position: Int): Boolean {
        var i = position - 1
        while (i >= 0) {
            when (val item = itemList[i]) {
                is ProfileItem.Header -> return true
                is ProfileItem.Field -> {
                    if (item.field.node.equals("photoPath", ignoreCase = true)) {
                        i--
                        continue
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun isLastInSection(position: Int): Boolean {
        var i = position + 1
        while (i < itemList.size) {
            when (val item = itemList[i]) {
                is ProfileItem.Header -> return true
                is ProfileItem.Field -> {
                    if (item.field.node.equals("photoPath", ignoreCase = true)) {
                        i++
                        continue
                    }
                    return false
                }
            }
        }
        return true
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val header: TextView = itemView.findViewById(R.id.header)

        fun bind(item: ProfileItem.Header) {
            if (item.title.equals("PhotoPath", ignoreCase = true)) {
                header.visibility = View.GONE
            } else {
                header.visibility = View.VISIBLE
                header.text = item.title
            }
        }
    }

    inner class FieldViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titlelabel: TextView = itemView.findViewById(R.id.titlelabel)
        private val titlevalue: EditText = itemView.findViewById(R.id.titlevalue)
        private val titleValueBox: LinearLayout = itemView.findViewById(R.id.titleValueBox)

        private val remarksvalue: EditText = itemView.findViewById(R.id.remarksvalue)

        private val datelabel: TextView = itemView.findViewById(R.id.datelabel)
        private val datevalue: TextView = itemView.findViewById(R.id.datevalue)
        private val dateValueBox: LinearLayout = itemView.findViewById(R.id.dateValueBox)

        private val dropdownvalue: AutoCompleteTextView = itemView.findViewById(R.id.dropdownvalue)
        private val dropdownValueBox: TextInputLayout = itemView.findViewById(R.id.dropdownValueBox)

        private val datelayout: LinearLayout = itemView.findViewById(R.id.datelayout)
        private val dropdownlayout: LinearLayout = itemView.findViewById(R.id.dropdownlayout)
        private val remarkslayout: LinearLayout = itemView.findViewById(R.id.remarks_layout)
        private val dropdownlabel: TextView = itemView.findViewById(R.id.dropdownlabel)
        private val remarkslabel: TextView = itemView.findViewById(R.id.remarkslabel)
        private val genderLayout: ConstraintLayout = itemView.findViewById(R.id.genderLayout)
        private val titlelayout: LinearLayout = itemView.findViewById(R.id.titlelayout)
        private val imagelayout: LinearLayout = itemView.findViewById(R.id.imagelayout)
        private val imagelabel: TextView = itemView.findViewById(R.id.imagelabel)
        private val addlabel: TextView = itemView.findViewById(R.id.addlabel)
        private val selectedFilesContainer: FrameLayout =
            itemView.findViewById(R.id.selectedFilesContainer)

        var isRcyImagesAttached = false


        private fun applyEditableStyle(view: View) {
            view.setBackgroundResource(
                if (isEditMode) R.drawable.field_background else R.drawable.field_underline_bg
            )
        }


        private fun isActuallyEditable(field: ProfileField): Boolean {
            return isEditMode && field.is_editable
        }

        private fun editStatusSuffix(field: ProfileField): String {
            if (!isEditMode) return ""
            return if (field.is_editable) {
                " <font color='#4CAF50'>(Editable)</font>"
            } else {
                " <font color='#9E9E9E'>(Non-editable)</font>"
            }
        }

        private fun dp(value: Int): Int =
            (value * itemView.resources.displayMetrics.density).toInt()

        /**
         * Gives the row a rounded "card" look (top/middle/bottom/single piece of a
         * white card grouping all fields belonging to one section header) and shows
         * or hides the thin divider between consecutive rows in the same card.
         */
        private fun applyCardGrouping(position: Int) {
            val first = isFirstInSection(position)
            val last = isLastInSection(position)

            itemView.setBackgroundResource(
                when {
                    first && last -> R.drawable.bg_card_single
                    first -> R.drawable.bg_card_top
                    last -> R.drawable.bg_card_bottom
                    else -> R.drawable.bg_card_middle
                }
            )

//            rowDivider.visibility = if (last) View.GONE else View.VISIBLE

            (itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                lp.marginStart = dp(16)
                lp.marginEnd = dp(16)
                lp.topMargin = if (first) dp(8) else 0
                lp.bottomMargin = if (last) dp(16) else 0
                itemView.layoutParams = lp
            }
        }

        fun bind(field: ProfileField, position: Int) {

            datelayout.visibility = View.GONE
            dropdownlayout.visibility = View.GONE
            genderLayout.visibility = View.GONE
            remarkslayout.visibility = View.GONE
            titlelayout.visibility = View.GONE
            imagelayout.visibility = View.GONE
            selectedFilesContainer.visibility = View.GONE

            if (field.node.equals("photoPath", ignoreCase = true)) {
                itemView.visibility = View.GONE
                (itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                    lp.height = 0
                    lp.width = 0
                    itemView.layoutParams = lp
                }
                return
            }

            itemView.visibility = View.VISIBLE
            (itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                itemView.layoutParams = lp
            }

            applyCardGrouping(position)

            val canEdit = isActuallyEditable(field)

            when (field.type) {
                Constant.text_, Constant.mobile, Constant.number -> {
                    titlelayout.visibility = View.VISIBLE
                    applyEditableStyle(titleValueBox)

                    val editStatusText = editStatusSuffix(field)

                    titlelabel.text = if (!field.optional) {
                        Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>$editStatusText",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        Html.fromHtml(
                            "${field.title}$editStatusText",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    }

                    titlevalue.setSafeTextWatcher(field) { field.value = it }
                    titlevalue.isEnabled = canEdit
                    titlevalue.isFocusable = canEdit
                    titlevalue.isFocusableInTouchMode = canEdit
                    titlevalue.isClickable = canEdit
                }


                Constant.image_ -> {
                    imagelayout.visibility = View.VISIBLE

                    val editStatusText = editStatusSuffix(field)

                    if (field.optional == false) {
                        imagelabel.text = Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>$editStatusText",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        imagelabel.text = Html.fromHtml(
                            "${field.title}$editStatusText",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    }
                    val recyclerView: RecyclerView = itemView.findViewById(R.id.rcChildHW)
                    recyclerView.layoutManager = GridLayoutManager(itemView.context, 2)

                    val files = field.value?.let { valueStr ->
                        if (valueStr.isNotEmpty()) {
                            val fileName = valueStr.substringAfterLast("/")
                            val extension = fileName.substringAfterLast(".", "").uppercase()
                            val type = when (extension) {
                                "JPG", "JPEG", "PNG", "GIF" -> "IMG"
                                else -> extension.ifEmpty { "IMG" }
                            }
                            listOf(CommonFileData(type = type, path = valueStr))
                        } else {
                            emptyList()
                        }
                    } ?: emptyList()

                    recyclerView.adapter = DocumentImageAdapter(
                        context = itemView.context, files = files, isSubjectName = field.title ?: ""
                    )

                    if (field.isRcyImagesAttached) {
                        attachRcyImagesBelowField()
                    }

                    // Add button only visible AND clickable when actually editable
                    addlabel.isVisible = canEdit
                    addlabel.isEnabled = canEdit
                    addlabel.setOnClickListener {
                        if (!canEdit) return@setOnClickListener
                        listener.onDocumentClicked(field, position)
                        field.isRcyImagesAttached = true
                        attachRcyImagesBelowField()
                    }
                }

                Constant.document_ -> {
                    imagelayout.visibility = View.VISIBLE

                    val editStatusText = editStatusSuffix(field)

                    val labelText = if (field.optional == false) {
                        "${field.title} <font color='#FF0000'>*</font>$editStatusText"
                    } else {
                        "${field.title}$editStatusText"
                    }

                    imagelabel.text = Html.fromHtml(
                        labelText,
                        Html.FROM_HTML_MODE_LEGACY
                    )

                    val recyclerView: RecyclerView = itemView.findViewById(R.id.rcChildHW)
                    recyclerView.layoutManager = GridLayoutManager(itemView.context, 2)

                    val files = field.file_path?.map { doc ->
                        val fileName = doc.documentName
                            ?: doc.documentPath.substringAfterLast("/")

                        val extension = fileName.substringAfterLast(".", "")
                            .uppercase()

                        val type = when (extension) {
                            "JPG", "JPEG", "PNG", "GIF" -> "IMG"
                            "PDF" -> "PDF"
                            "DOC", "DOCX" -> "DOC"
                            "XLS", "XLSX" -> "XLS"
                            "MP4", "AVI", "MKV" -> "VID"
                            "MP3", "WAV" -> "AUD"
                            else -> extension.ifEmpty { "FILE" }
                        }

                        CommonFileData(type = type, path = doc.documentPath)
                    } ?: emptyList()
                    recyclerView.adapter = DocumentImageAdapter(
                        context = itemView.context,
                        files = files,
                        isSubjectName = field.title.orEmpty()
                    )

                    if (field.isRcyImagesAttached) {
                        attachRcyImagesBelowField()
                    }

                    addlabel.isVisible = canEdit
                    addlabel.isEnabled = canEdit
                    addlabel.setOnClickListener {
                        if (!canEdit) return@setOnClickListener
                        listener.onDocumentClicked(field, position)
                        field.isRcyImagesAttached = true
                        attachRcyImagesBelowField()
                    }
                }


                Constant.address -> {
                    remarkslayout.visibility = View.VISIBLE
                    applyEditableStyle(remarksvalue)

                    val editStatusText = editStatusSuffix(field)

                    val labelText = if (field.optional == false) {
                        "${field.title} <font color='#FF0000'>*</font>$editStatusText"
                    } else {
                        "${field.title}$editStatusText"
                    }

                    remarkslabel.text = Html.fromHtml(
                        labelText,
                        Html.FROM_HTML_MODE_LEGACY
                    )

                    remarksvalue.setSafeTextWatcher(field) { field.value = it }
                    remarksvalue.isEnabled = canEdit
                    remarksvalue.isFocusable = canEdit
                    remarksvalue.isFocusableInTouchMode = canEdit
                    remarksvalue.isClickable = canEdit
                }


                Constant.calendar -> {
                    datelayout.visibility = View.VISIBLE
                    applyEditableStyle(dateValueBox)

                    val editStatusText = editStatusSuffix(field)

                    val labelText = if (field.optional == false) {
                        "${field.title} <font color='#FF0000'>*</font>$editStatusText"
                    } else {
                        "${field.title}$editStatusText"
                    }

                    datelabel.text = Html.fromHtml(
                        labelText,
                        Html.FROM_HTML_MODE_LEGACY
                    )

                    datevalue.text = field.value.orEmpty()

                    datelayout.isEnabled = canEdit
                    datelayout.setOnClickListener {
                        if (!canEdit) return@setOnClickListener

                        val calendar = Calendar.getInstance()

                        DatePickerDialog(
                            itemView.context,
                            { _, year, month, day ->
                                val selectedDate = String.format(
                                    "%02d-%02d-%04d",
                                    day,
                                    month + 1,
                                    year
                                )
                                datevalue.text = selectedDate
                                field.value = selectedDate
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            datePicker.maxDate = System.currentTimeMillis()
                        }.show()
                    }
                }


                Constant.gender -> {
                    genderLayout.visibility = View.VISIBLE
                    val editStatusText = editStatusSuffix(field)
                    val genderLabel: TextView = itemView.findViewById(R.id.genderLabel)
                    val radioMale: RadioButton = itemView.findViewById(R.id.radioMale)
                    val radioFemale: RadioButton = itemView.findViewById(R.id.radioFemale)
                    val radioOthers: RadioButton = itemView.findViewById(R.id.radioOthers)
                    val allGenderButtons = listOf(radioMale, radioFemale, radioOthers)

                    genderLabel.text = if (field.optional == false) {
                        Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>$editStatusText",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        Html.fromHtml(
                            "${field.title}$editStatusText",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    }

                    radioMale.isChecked = field.value?.lowercase() == Constant.male
                    radioFemale.isChecked = field.value?.lowercase() == Constant.female
                    radioOthers.isChecked = field.value?.lowercase() == Constant.others

                    allGenderButtons.forEach { it.isEnabled = canEdit }

                    val genderClickListener = View.OnClickListener { clicked ->
                        if (!canEdit) return@OnClickListener
                        allGenderButtons.forEach { it.isChecked = (it === clicked) }
                        field.value = when (clicked.id) {
                            R.id.radioMale -> Constant.male
                            R.id.radioFemale -> Constant.female
                            R.id.radioOthers -> Constant.others
                            else -> null
                        }
                    }
                    allGenderButtons.forEach { it.setOnClickListener(genderClickListener) }
                }

                Constant.dropdown -> {
                    dropdownlayout.visibility = View.VISIBLE
                    applyEditableStyle(dropdownValueBox)

                    val editStatusText = editStatusSuffix(field)

                    val labelText = if (field.optional == false) {
                        "${field.title} <font color='#FF0000'>*</font>$editStatusText"
                    } else {
                        "${field.title}$editStatusText"
                    }

                    dropdownlabel.text = Html.fromHtml(
                        labelText,
                        Html.FROM_HTML_MODE_LEGACY
                    )

                    val options = field.options.orEmpty()

                    val adapterDropdown = ArrayAdapter(
                        itemView.context,
                        android.R.layout.simple_dropdown_item_1line,
                        options
                    )

                    dropdownvalue.setAdapter(adapterDropdown)
                    dropdownvalue.setText(field.value.orEmpty(), false)

                    // IMPORTANT: the OutlinedBox.ExposedDropdownMenu style gives the
                    // TextInputLayout its own end (dropdown-arrow) icon with its own click
                    // handling — disabling only the AutoCompleteTextView is not enough,
                    // tapping that icon would still open the popup. Disable the box itself too.
                    dropdownValueBox.isEnabled = canEdit
                    dropdownValueBox.endIconMode = if (canEdit) {
                        TextInputLayout.END_ICON_DROPDOWN_MENU
                    } else {
                        TextInputLayout.END_ICON_NONE
                    }

                    dropdownvalue.isEnabled = canEdit
                    dropdownvalue.isClickable = canEdit
                    dropdownvalue.isFocusable = canEdit

                    dropdownvalue.setOnClickListener {
                        if (canEdit) dropdownvalue.showDropDown()
                    }

                    dropdownvalue.setSafeTextWatcher(field) { field.value = it }
                }

            }
        }

        fun mapUrlsToCommonFileData(urls: List<String>): List<CommonFileData> {
            return urls.map { url ->
                val fileName = url.substringAfterLast("/")
                val type = when (val extension = fileName.substringAfterLast(".", "").uppercase()) {
                    "JPG", "JPEG", "PNG", "GIF" -> "IMAGE"
                    "PDF" -> "PDF"
                    "DOC", "DOCX" -> "DOC"
                    "XLS", "XLSX" -> "XLS"
                    "MP4", "AVI", "MKV" -> "VIDEO"
                    "MP3", "WAV" -> "AUDIO"
                    else -> extension.ifEmpty { "FILE" }
                }
                CommonFileData(type = type, path = url)
            }
        }


        private fun attachRcyImagesBelowField() {
            rcyImages?.let { rv ->
                if (!isRcyImagesAttached) {
                    (rv.parent as? ViewGroup)?.removeView(rv)
                    selectedFilesContainer.addView(
                        rv, FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )
                    )
                    selectedFilesContainer.visibility = View.VISIBLE
                    rv.visibility = View.VISIBLE
                    isRcyImagesAttached = true
                }
            }
        }

        fun detachRcyImages() {
            if (isRcyImagesAttached) {
                rcyImages?.let { rv ->
                    selectedFilesContainer.removeView(rv)
                    rv.visibility = View.GONE
                }
                isRcyImagesAttached = false
                selectedFilesContainer.visibility = View.GONE
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is FieldViewHolder && holder.isRcyImagesAttached) {
            holder.detachRcyImages()
        }
    }

    fun getUpdatedField(node: String): ProfileField? {
        return itemList.filterIsInstance<ProfileItem.Field>().map { it.field }
            .find { it.node.equals(node, ignoreCase = true) }
    }

    /** All fields with their current (possibly edited) in-memory values. */
    fun getAllFields(): List<ProfileField> {
        return itemList.filterIsInstance<ProfileItem.Field>().map { it.field }
    }

    fun EditText.setSafeTextWatcher(field: ProfileField, onChanged: (String) -> Unit) {
        (this.tag as? TextWatcher)?.let { removeTextChangedListener(it) }

        if (text.toString() != field.value.orEmpty()) {
            setText(field.value ?: "")
            setSelection(text.length)
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onChanged(s?.toString() ?: "")
            }
        }
        addTextChangedListener(watcher)
        this.tag = watcher
    }

    fun AutoCompleteTextView.setSafeDropdownListener(
        options: List<String>, onChanged: (String) -> Unit
    ) {
        (this.tag as? AdapterView.OnItemClickListener)?.let { onItemClickListener = null }
        val listener = AdapterView.OnItemClickListener { _, _, position, _ ->
            onChanged(options[position])
        }
        onItemClickListener = listener
        this.tag = listener
    }
}