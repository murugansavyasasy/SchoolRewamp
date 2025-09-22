package com.vs.schoolmessenger.Dashboard.Fragments.Profile

import android.app.DatePickerDialog
import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        private val remarksvalue: EditText = itemView.findViewById(R.id.remarksvalue)
        private val datelabel: TextView = itemView.findViewById(R.id.datelabel)
        private val datevalue: TextView = itemView.findViewById(R.id.datevalue)
        private val dropdownvalue: AutoCompleteTextView = itemView.findViewById(R.id.dropdownvalue)
        private val datelayout: LinearLayout = itemView.findViewById(R.id.datelayout)
        private val dropdownlayout: LinearLayout = itemView.findViewById(R.id.dropdownlayout)
        private val remarkslayout: LinearLayout = itemView.findViewById(R.id.remarks_layout)
        private val dropdownlabel: TextView = itemView.findViewById(R.id.dropdownlabel)
        private val remarkslabel: TextView = itemView.findViewById(R.id.remarkslabel)
        private val genderLayout: LinearLayout = itemView.findViewById(R.id.genderLayout)
        private val titlelayout: LinearLayout = itemView.findViewById(R.id.titlelayout)
        private val imagelayout: LinearLayout = itemView.findViewById(R.id.imagelayout)
        private val imagelabel: TextView = itemView.findViewById(R.id.imagelabel)
        private val addlabel: TextView = itemView.findViewById(R.id.addlabel)
        private val selectedFilesContainer: FrameLayout =
            itemView.findViewById(R.id.selectedFilesContainer)  // New container

        var isRcyImagesAttached = false

        fun bind(field: ProfileField, position: Int) {


            datelayout.visibility = View.GONE
            dropdownlayout.visibility = View.GONE
            genderLayout.visibility = View.GONE
            remarkslayout.visibility = View.GONE
            titlelayout.visibility = View.GONE
            imagelayout.visibility = View.GONE
            selectedFilesContainer.visibility = View.GONE

            if (field.node.equals("photoPath", ignoreCase = true)) return

            when (field.type) {
                Constant.text_, Constant.mobile, Constant.number -> {
                    titlelayout.visibility = View.VISIBLE
                    if (field.optional == false) {
                        titlelabel.text = Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        titlelabel.text = field.title
                    }

                    titlevalue.setSafeTextWatcher(field) { field.value = it }
                    titlevalue.isEnabled = field.is_editable
                }

                Constant.image_ -> {
                    imagelayout.visibility = View.VISIBLE
                    if (field.optional == false) {
                        imagelabel.text = Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        imagelabel.text = field.title
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

                    addlabel.isVisible = field.is_editable
                    addlabel.setOnClickListener {
                        listener.onDocumentClicked(field, position)
                        field.isRcyImagesAttached = true
                        attachRcyImagesBelowField()
                    }
                }

                Constant.document_ -> {
                    imagelayout.visibility = View.VISIBLE
                    if (field.optional == false) {
                        imagelabel.text = Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        imagelabel.text = field.title
                    }

                    val recyclerView: RecyclerView = itemView.findViewById(R.id.rcChildHW)
                    recyclerView.layoutManager = GridLayoutManager(itemView.context, 2)

                    val files = field.file_path?.map { doc ->
                        val fileName = doc.documentName ?: doc.documentPath.substringAfterLast("/")
                        val extension = fileName.substringAfterLast(".", "").uppercase()
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
                        context = itemView.context, files = files, isSubjectName = field.title ?: ""
                    )

                    if (field.isRcyImagesAttached) {
                        attachRcyImagesBelowField()
                    }

                    addlabel.isVisible = field.is_editable
                    addlabel.setOnClickListener {
                        listener.onDocumentClicked(field, position)
                        field.isRcyImagesAttached = true
                        attachRcyImagesBelowField()
                    }
                }

                Constant.address -> {
                    remarkslayout.visibility = View.VISIBLE
                    if (field.optional == false) {
                        remarkslabel.text = Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        remarkslabel.text = field.title
                    }
                    remarksvalue.setSafeTextWatcher(field) { field.value = it }
                    remarksvalue.isEnabled = field.is_editable
                }

                Constant.calendar -> {
                    datelayout.visibility = View.VISIBLE
                    if (field.optional == false) {
                        datelabel.text = Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        datelabel.text = field.title
                    }
                    datevalue.text = field.value ?: ""

                    datelayout.setOnClickListener {
                        if (field.is_editable) {
                            val calendar = Calendar.getInstance()
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            DatePickerDialog(
                                itemView.context, { _, selectedYear, selectedMonth, selectedDay ->
                                    val selectedDate = String.format(
                                        "%02d-%02d-%04d",
                                        selectedDay,
                                        selectedMonth + 1,
                                        selectedYear
                                    )
                                    datevalue.text = selectedDate
                                    field.value = selectedDate
                                }, year, month, day
                            ).show()
                        }
                    }
                }

                Constant.gender -> {
                    genderLayout.visibility = View.VISIBLE
                    val genderLabel: TextView = itemView.findViewById(R.id.genderLabel)
                    val radioGroup: RadioGroup = itemView.findViewById(R.id.radioGenderGroup)
                    val radioMale: RadioButton = itemView.findViewById(R.id.radioMale)
                    val radioFemale: RadioButton = itemView.findViewById(R.id.radioFemale)
                    val radioOthers: RadioButton = itemView.findViewById(R.id.radioOthers)


                    if (field.optional == false) {
                        genderLabel.text = Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        genderLabel.text = field.title
                    }

                    when (field.value?.lowercase()) {
                        Constant.male -> radioMale.isChecked = true
                        Constant.female -> radioFemale.isChecked = true
                        Constant.others -> radioOthers.isChecked = true
                        else -> radioGroup.clearCheck()
                    }

                    for (i in 0 until radioGroup.childCount) {
                        radioGroup.getChildAt(i).isEnabled = field.is_editable
                    }

                    radioGroup.setOnCheckedChangeListener { _, checkedId ->
                        field.value = when (checkedId) {
                            R.id.radioMale -> Constant.male
                            R.id.radioFemale -> Constant.female
                            R.id.radioOthers -> Constant.others
                            else -> null
                        }
                    }
                }

                Constant.dropdown -> {
                    dropdownlayout.visibility = View.VISIBLE

                    if (field.optional == false) {
                        dropdownlabel.text = Html.fromHtml(
                            "${field.title} <font color='#FF0000'>*</font>",
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } else {
                        dropdownlabel.text = field.title
                    }


                    val options = field.options ?: emptyList()

                    val adapterDropdown = ArrayAdapter(
                        itemView.context, android.R.layout.simple_dropdown_item_1line, options
                    )
                    dropdownvalue.setAdapter(adapterDropdown)

                    dropdownvalue.setText(field.value ?: "", false)
                    dropdownvalue.isEnabled = field.is_editable

                    dropdownvalue.setSafeTextWatcher(field) { field.value = it }
                }
            }
        }

        fun mapUrlsToCommonFileData(urls: List<String>): List<CommonFileData> {
            return urls.map { url ->
                val fileName = url.substringAfterLast("/")
                val extension = fileName.substringAfterLast(".", "").uppercase()
                val type = when (extension) {
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