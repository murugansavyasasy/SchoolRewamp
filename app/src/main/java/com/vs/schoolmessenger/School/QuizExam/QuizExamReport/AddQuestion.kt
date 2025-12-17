package com.vs.schoolmessenger.School.QuizExam.QuizExamReport

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Attachment.Attachment
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.AddQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.PickQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.AddQuestionListner
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.QuizQuestionRequest
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.QuizRequestBody
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.UpdateQBankItem
import com.vs.schoolmessenger.School.QuizExam.Model.PickFromQuestionBank.GetPickFromQBankData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizAttachmentData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.QuestionSource
import com.vs.schoolmessenger.School.QuizExam.OnAttachmentListener
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AddQuestionBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AddQuestion : BaseActivity<AddQuestionBinding>(), View.OnClickListener, AddQuestionListner,
    OnAttachmentListener, VimeoVideoUpload.UploadCompletionListener {

    override fun getViewBinding(): AddQuestionBinding {
        return AddQuestionBinding.inflate(layoutInflater)
    }

    val quizAttachments: MutableList<QuizAttachmentData> = mutableListOf()
    val isQuizUploadedFiles: MutableList<QuizAttachmentData> = mutableListOf()

    private var itemList: MutableList<GetQuizQuestionReportData> = mutableListOf()
    private var quizAdapter: AddQuestionAdapter? = null
    private var isDialogShowing = false

    private var clickedPosition: Int = RecyclerView.NO_POSITION

    var isAttachmentAdapterPosition = 0
    private var cameraPermissionDeniedCount = 0

    var isQuestionPick: Boolean? = null
    var isOptionsFieldId: EditText? = null

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val MAX_FILES = 10
        internal const val CAMERA_IMAGE_REQUEST = 1004
    }

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null

    var file_path: List<FilePath> = emptyList()

    var isQuizID = ""
    var isQuizTitle = ""
    var isSubjectID = ""
    var isUpdatedQBankQuestions = -1
    var isSavedQuestionLimit = -1
    var isSubmittedCount = -1
    var isOkFlag = false
    var isFirstClick = true
    private lateinit var savedQuizQuestionReportList: List<GetQuizQuestionReportData>
    private lateinit var pickQBankList: List<GetPickFromQBankData>
    private lateinit var editableQuizQuestionReportList: MutableList<GetQuizQuestionReportData>

    private var isAccessToken: String? = null
    private var isQuestionBankErrorMsg: String? = null
    private var isStaffDetails: StaffDetails? = null
    private lateinit var adapter2: PickQuestionAdapter

    val isVideoSelectedArrayList = mutableListOf<QuizAttachmentData>()
    var isTotalSelectedItem = 0
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null


    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblParentToolBar.setOnClickListener {
            val FinalList = quizAdapter!!.getUpdatedList()
            Log.d("FinalList", FinalList.toString())
        }
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        Constant.isQuestionLimit = intent.getIntExtra(Constant.limitQuestion, -1)
        isSavedQuestionLimit = intent.getIntExtra(Constant.limitQuestion, -1)
        Log.d("isQuestionLimit", Constant.isQuestionLimit.toString())
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        isSubmittedCount = intent.getIntExtra(Constant.submittedCount, -1)
        isQuizID = intent.getStringExtra(Constant.quiz_Id).toString()
        isSubjectID = intent.getStringExtra(Constant.subjectID).toString()
        isQuizTitle = intent.getStringExtra(Constant.quiz_Title).toString()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblImportQuestion.setOnClickListener(this)
        binding.lblSendQuiz.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = isQuizTitle
        isOkFlag = isSubmittedCount > 0

        appViewModel?.isGetQuizQuestionReport?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    savedQuizQuestionReportList = response.data

                    // Mark all as API type
                    editableQuizQuestionReportList = savedQuizQuestionReportList.map {
                        it.copy(sourceType = QuestionSource.API)
                    }.toMutableList()
                    Constant.isQuestionLimit -= savedQuizQuestionReportList.size
                    isLoadQuizQuestionReport()
                } else {
                    Constant.showErrorAlert(
                        this, getString(R.string.alert), response.message
                    )

                    savedQuizQuestionReportList = response.data
                    editableQuizQuestionReportList = savedQuizQuestionReportList.map {
                        it.copy(sourceType = QuestionSource.API)
                    }.toMutableList()
                    Constant.isQuestionLimit -= savedQuizQuestionReportList.size
                    isLoadQuizQuestionReport()
                }
            } else {
                Constant.showErrorAlert(
                    this,
                    getString(R.string.fail),
                    getString(R.string.Something_went_wrong_Please_try_again)
                )
            }
        }

        appViewModel!!.isAddQuestion?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this@AddQuestion)
                    Constant.showDataValidation(
                        resources.getString(R.string.success), response.message, this
                    )
                } else {
                    Constant.hideLoading(this@AddQuestion)
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, this
                    )
                }
            } else {
                Constant.hideLoading(this@AddQuestion)
                Constant.showDataValidation(
                    resources.getString(R.string.fail),
                    getString(R.string.Something_went_wrong_Please_try_again),
                    this
                )
            }
        }

        appViewModel?.isGetPickFromQBank?.observe(this) { response ->
            binding.lblImportQuestion.isEnabled = true//now enable after api call
            if (response != null) {
                if (response.status) {
                    Constant.hideLoading(this)
                    pickQBankList = response.data.map { it.copy(checked = false) }
                    showResumeListDialog(this, pickQBankList)
                } else {
                    Constant.hideLoading(this)
                    pickQBankList = emptyList()
                    isQuestionBankErrorMsg = response.message
                    Constant.showErrorAlert(
                        this, getString(R.string.alert), response.message
                    )
                }
            } else {
                Constant.hideLoading(this)
                pickQBankList = emptyList()
                Constant.showErrorAlert(
                    this,
                    getString(R.string.fail),
                    getString(R.string.Something_went_wrong_Please_try_again)
                )
                isQuestionBankErrorMsg = getString(R.string.Something_went_wrong_Please_try_again)
            }
        }
        isFetchQuizQuestionReport()

        binding.lblAddQuestion.setOnClickListener {

            if (quizAdapter!!.showValidationErrors(binding.rcAddQuestion)) {
                Log.d("QuestionLimit", Constant.isQuestionLimit.toString())
                Log.d("FinalListSize", quizAdapter!!.getUpdatedList().size.toString())
                if (Constant.isQuestionLimit > 0) {
                    binding.rcAddQuestion.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                    quizAdapter!!.addItem(binding.rcAddQuestion)
                    UpdateQuestionCount()
                } else {
                    Constant.showErrorAlert(
                        this, getString(R.string.alert), getString(R.string.question_limit_reached)
                    )
                }
            }
        }


        // Attachment Code

        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@registerForActivityResult
                if (isAttachmentAdapterPosition == RecyclerView.NO_POSITION) return@registerForActivityResult
                if (isAttachmentAdapterPosition >= itemList.size) return@registerForActivityResult
                val selectedUris =
                    result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)


                selectedUris!!.forEach { uri ->
                    val mimeType = contentResolver.getType(uri)
                    val path = when (uri.scheme) {
                        Constant.file_ -> uri.path
                        else -> getPathFromUri(uri)
                    }

                    if (path == null) {
                        Log.w("addPath", "Could not resolve path from URI: $uri")
                        return@forEach
                    }

                    val fileName = getFileName(uri).ifEmpty { File(path).name }

                    val type = when {
                        mimeType?.startsWith("image/") == true -> FileType.IMAGE
                        mimeType?.startsWith("video/") == true -> FileType.VIDEO
                        mimeType?.startsWith("audio/") == true -> FileType.AUDIO
                        fileName.endsWith(".pdf", true) -> FileType.PDF
                        fileName.endsWith(".doc", true) || fileName.endsWith(
                            ".docx",
                            true
                        ) -> FileType.DOC

                        fileName.endsWith(".xls", true) || fileName.endsWith(
                            ".xlsx",
                            true
                        ) -> FileType.EXCEL

                        fileName.endsWith(".ppt", true) || fileName.endsWith(
                            ".pptx",
                            true
                        ) -> FileType.PPT

                        fileName.endsWith(".txt", true) -> FileType.TXT
                        else -> FileType.OTHER
                    }


//                selectedUris!!.forEach { uri ->
//
//                }
//
//                val uri = result.data
//                    ?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)
//                    ?.firstOrNull()
//                    ?: result.data?.data
//                    ?: return@registerForActivityResult
//
//                val fileName = getFileName(uri)
//                val mimeType = contentResolver.getType(uri)
//
//                val type = when {
//                    mimeType?.startsWith("image/") == true -> FileType.IMAGE
//                    mimeType?.startsWith("video/") == true -> FileType.VIDEO
//                    mimeType?.startsWith("audio/") == true -> FileType.AUDIO
//                    fileName.endsWith(".pdf", true) -> FileType.PDF
//                    fileName.endsWith(".doc", true) || fileName.endsWith(
//                        ".docx",
//                        true
//                    ) -> FileType.DOC
//
//                    fileName.endsWith(".xls", true) || fileName.endsWith(
//                        ".xlsx",
//                        true
//                    ) -> FileType.EXCEL
//
//                    fileName.endsWith(".ppt", true) || fileName.endsWith(
//                        ".pptx",
//                        true
//                    ) -> FileType.PPT
//
//                    fileName.endsWith(".txt", true) -> FileType.TXT
//                    else -> FileType.OTHER

                    if (isQuestionPick!!) {
                        val filePath = FilePath(
                            url = uri.toString(),
                            type = type.toString()
                        )
                        val quizItem = itemList[isAttachmentAdapterPosition]
                        quizItem.file_path.add(filePath)
                        Log.d(
                            "",
                            "Saved file at position $isAttachmentAdapterPosition => $filePath"
                        )
                    } else {
                        val quizItem = itemList[isAttachmentAdapterPosition]
                        if (resources.getResourceEntryName(isOptionsFieldId!!.id)
                                .toString() == "edtOptionA"
                        ) {
                            quizItem.a_image = uri.toString()
                        } else if (resources.getResourceEntryName(isOptionsFieldId!!.id)
                                .toString() == "edtOptionB"
                        ) {
                            quizItem.b_image = uri.toString()
                        } else if (resources.getResourceEntryName(isOptionsFieldId!!.id)
                                .toString() == "edtOptionC"
                        ) {
                            quizItem.c_image = uri.toString()
                        } else if (resources.getResourceEntryName(isOptionsFieldId!!.id)
                                .toString() == "edtOptionD"
                        ) {
                            quizItem.d_image = uri.toString()
                        }
                    }
                }

                mAdapter?.notifyItemChanged(isAttachmentAdapterPosition)
            }
    }

    private fun isLoadQuizQuestionReport() {
        Log.d("QuestionLimitInAdapter", Constant.isQuestionLimit.toString())

        quizAdapter = AddQuestionAdapter(
            editableQuizQuestionReportList.toMutableList(), this, this, this, false
        )
        binding.rcAddQuestion.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcAddQuestion.isNestedScrollingEnabled = false
        binding.rcAddQuestion.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.rcAddQuestion.adapter = quizAdapter

        quizAdapter!!.onQBankItemRemoved = { removedId ->
            adapter2.uncheckItemById(removedId)
        }

        // Add an empty item only if list is empty
        if (editableQuizQuestionReportList.isEmpty()) {
            quizAdapter!!.addItem(binding.rcAddQuestion)
        }

        UpdateQuestionCount()
    }

    private fun isFetchQuizQuestionReport() {
        quizAdapter = AddQuestionAdapter(null, this, this, this, true)
        binding.rcAddQuestion.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcAddQuestion.isNestedScrollingEnabled = false
        binding.rcAddQuestion.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        binding.rcAddQuestion.adapter = quizAdapter
        appViewModel?.isGetQuizQuestionReport(isAccessToken ?: "", isQuizID)
    }

    private fun getPathFromUri(uri: Uri): String? {
        // Content scheme
        if (uri.scheme.equals(Constant.content_, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return cursor.getString(columnIndex)
                }
            }
        }

        // File scheme fallback
        if (uri.scheme.equals(Constant.file_, ignoreCase = true)) {
            return uri.path
        }
        return null
    }
    private fun isFetchFromQuestionBank() {
        appViewModel?.isGetPickFromQBank(isAccessToken ?: "", isSubjectID)
    }

    fun showResumeListDialog(
        activity: Activity, pickFomQbank: List<GetPickFromQBankData>
    ) {
        if (isDialogShowing || activity.isFinishing || activity.isDestroyed) return
        isDialogShowing =
            true//This is to ensure next time if it is clicked multiple times it will not open the dialog more than one time

        val dialogView =
            LayoutInflater.from(activity).inflate(R.layout.pick_question_from_qbank, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!activity.isFinishing && !activity.isDestroyed) {
            alertDialog.show()
        }

        val lblClose = dialogView.findViewById<TextView>(R.id.lblClose)
        val lblImportQuestion = dialogView.findViewById<TextView>(R.id.lblImportQuestion)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcPickFromQBank)
        val cbSelect = dialogView.findViewById<CheckBox>(R.id.cbSelect)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter2 = PickQuestionAdapter(pickFomQbank.toMutableList(), activity, false)

        cbSelect.isChecked = false // default

        adapter2.onSelectionChanged = { allSelected ->
            // Update cbSelect programmatically, but avoid infinite loop
            Log.d("allSelectedBefore", allSelected.toString())
            if (cbSelect.isChecked != allSelected) {
                cbSelect.setOnCheckedChangeListener(null)
                cbSelect.isChecked = allSelected
                cbSelect.setOnCheckedChangeListener { _, isChecked ->
                    adapter2.selectAll(isChecked)
                }
            }
            Log.d("allSelectedFinal", allSelected.toString())
        }
        adapter2.notifySelectionChanged()


        cbSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val allQuestions = adapter2.getAllNotImported()
                val totalToSelect = allQuestions.size

                if (totalToSelect <= Constant.isQuestionLimit) {
                    //  only temporary selection, not permanent
                    adapter2.selectAll(true)
                } else {
                    Constant.showErrorAlert(
                        this, getString(R.string.alert), getString(R.string.question_limit_reached)
                    )
                    cbSelect.isChecked = false
                }
            } else {
                //  clear only temporary selections
                adapter2.selectAll(false)
            }
            adapter2.notifySelectionChanged()
        }

        recyclerView.adapter = adapter2

        lblImportQuestion.setOnClickListener {

            //Prevent import if no question is selected
            if (!adapter2.hasAnySelected()) {
                Toast.makeText(
                    this,
                    getString(R.string.please_select_at_least_one_question_from_question_bank),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val selectedQuestions = adapter2.getSelected()
            val selectedIds = selectedQuestions.map { it.id }.toSet()

            val currentAdded = quizAdapter!!.getUpdatedList()
            val alreadyAddedIds = currentAdded.map { it.id }.toSet()

            // QBANK items currently present in AddQuestionAdapter
            val qbankAddedIds =
                currentAdded.filter { it.sourceType == QuestionSource.QBANK && it.id.isNotEmpty() }
                    .map { it.id }

            // Items user just selected in dialog that are genuinely new to AddQuestionAdapter
            val newSelectedQuestions = selectedQuestions.filter { it.id !in alreadyAddedIds }

            // QBANK ids that were previously in AddQuestionAdapter but user deselected in the dialog now
            val deselectedIds = qbankAddedIds.filter { it !in selectedIds }

            // Effective available slots: current limit + slots freed by deselection
            val effectiveAvailableSlots = Constant.isQuestionLimit + deselectedIds.size

            Log.d(
                "ImportDebug",
                "selected=${selectedIds.size}, newSelected=${newSelectedQuestions.size}, alreadyAdded=${alreadyAddedIds.size}, deselected=${deselectedIds.size}, effectiveSlots=$effectiveAvailableSlots"
            )

            if (effectiveAvailableSlots <= 0) {
                Constant.showErrorAlert(
                    this, getString(R.string.alert), getString(R.string.question_limit_reached)
                )
                return@setOnClickListener
            }

            if (newSelectedQuestions.size <= effectiveAvailableSlots) {
                // convert to AddQuestion model
                val quizQuestions = newSelectedQuestions.map {
                    it.toQuizQuestionReportData().copy(sourceType = QuestionSource.QBANK)
                }

                // Remove only the deselected QBANK items (permanently)
                if (deselectedIds.isNotEmpty()) {
                    quizAdapter!!.removeItemsByIds(deselectedIds)
                }

                // Add newly selected QBANK items
                if (quizQuestions.isNotEmpty()) {
                    quizAdapter!!.addItems(quizQuestions)
                }

                //Update remaining limit: available - used
                val newRemaining =
                    (effectiveAvailableSlots - newSelectedQuestions.size).coerceAtLeast(0)
                Constant.isQuestionLimit = newRemaining

                // Sync PickQuestionAdapter: mark newly imported as imported (sets checked = true in pick adapter)
                adapter2.markAsImported(newSelectedQuestions)

                //ensure the deselected items in pick adapter are unchecked (defensive)
                deselectedIds.forEach { id -> adapter2.uncheckItemById(id) }

                UpdateQuestionCount()

                alertDialog.dismiss()
                isDialogShowing = false

                if (quizAdapter!!.getUpdatedList().size > 0) {
                    binding.rcAddQuestion.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                } else {
                    binding.rcAddQuestion.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                }

            } else {
                // not enough slots; do nothing (no removals), just show error
                Constant.showErrorAlert(
                    this, getString(R.string.alert), getString(R.string.question_limit_reached)
                )
            }


        }


        lblClose.setOnClickListener {
            // revert to permanent state
            adapter2.resetTemporarySelections()

            // update the "Select All" checkbox based on real imported items
            cbSelect.isChecked = adapter2.isAllImported()

            alertDialog.dismiss()
            isDialogShowing = false

        }


    }

    fun UpdateQuestionCount() {
        val text = "${isSavedQuestionLimit - Constant.isQuestionLimit}/$isSavedQuestionLimit"
        val spannable = SpannableString(text)

        // Apply blue color only to part before "/"
        val slashIndex = text.indexOf("/")
        spannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this,
                    R.color.PrimaryColor
                )
            ), // your blue color
            0, slashIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set black color for rest (after slash)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.black)),
            slashIndex,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvQuestionCount.text = spannable


    }

    fun GetPickFromQBankData.toQuizQuestionReportData(): GetQuizQuestionReportData {
        return GetQuizQuestionReportData(
            id = this.id,
            quiz_id = "",
            question = this.question,
            chapter = this.chapter,
            answer = this.answer,
            a_option = this.a_option,
            b_option = this.b_option,
            c_option = this.c_option,
            d_option = this.d_option,
            a_image = this.a_image,
            b_image = this.b_image,
            c_image = this.c_image,
            d_image = this.d_image,
            mark = this.mark,
            iframe = "",
            file_size = "",
            thumbnail = "",
            sourceType = QuestionSource.QBANK,
            file_path = this.file_path as MutableList<FilePath>
        )
    }

    fun isCallAddQuestion() {

        if (isSavedQuestionLimit == quizAdapter!!.getUpdatedList().size) {
            Constant.showSendConfirmationDialog(
                this,
                getString(R.string.confirmation),
                getString(R.string.send),
                getString(R.string.Cancel),
                "",
                getString(R.string.are_you_sure_want_to_send_the_quiz)
            ) { confirmed ->
                if (confirmed) {
                    isAddQuestionSubmit()
                }
            }
        } else {
            val currentCount = quizAdapter!!.getUpdatedList().size
            val isBalance = isSavedQuestionLimit - quizAdapter!!.getUpdatedList().size
            val textQuestion = if (isBalance == 1) {
                getString(R.string.question_)  // e.g. "question"
            } else {
                getString(R.string.questions) // e.g. "questions"
            }

            val isMessage =
                "${getString(R.string.almost_there_You_ve_entered)} $currentCount $textQuestion. " + "${
                    getString(R.string.just)
                } $isBalance ${getString(R.string.more_to_complete_the_quiz_but_don_t_worry_you_can_add_them_later)}"

            Constant.showSendConfirmationDialog(
                this,
                getString(R.string.confirmation),
                getString(R.string.send),
                getString(R.string.Cancel),
                "",
                isMessage
            ) { confirmed ->
                if (confirmed) {
                    isAddQuestionSubmit()
                }
            }
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblImportQuestion -> {
                if (isFirstClick) {
                    Constant.showLoading(this)
                    isFetchFromQuestionBank()
                    isFirstClick = false
                    binding.lblImportQuestion.isEnabled =
                        false //to avoid clicking multiple times i have disabled the button to api call

                } else {
                    if (pickQBankList.isEmpty()) {
                        Log.d("isEmpty", "isEmpty")
                        Constant.showErrorAlert(
                            this,
                            getString(R.string.alert),
                            isQuestionBankErrorMsg.toString()
                        )
                    } else {
                        Log.d("isEmpty", "isNotEmpty")
                        showResumeListDialog(this, pickQBankList)
                    }
                }

            }

            R.id.lblSendQuiz -> {
                if (quizAdapter!!.showValidationErrors(binding.rcAddQuestion)) {
                    if (quizAdapter!!.getUpdatedList().size <= isSavedQuestionLimit) {
                        if (isSubmittedCount <= 0) {
                            isCallAddQuestion()
                        } else {
                            val studentText = if (isSubmittedCount == 1) {
                                getString(R.string.student_)
                            } else {
                                getString(R.string.students)
                            }

                            val isMessage =
                                getString(R.string.this_question_has_already_been_submitted_by) + " ${isSubmittedCount} $studentText " + getString(
                                    R.string.do_you_want_to_update_it
                                )

                            Constant.showSendConfirmationDialog(
                                this,
                                getString(R.string.confirmation),
                                getString(R.string.permission_ok),
                                getString(R.string.Cancel),
                                "",
                                isMessage
                            ) { confirmed ->
                                if (confirmed) {
                                    isCallAddQuestion()
                                }
                            }
                        }
                    } else {
                        Constant.showErrorAlert(
                            this,
                            getString(R.string.alert),
                            getString(R.string.question_limit_reached)
                        )
                    }
                }
            }
        }
    }


    override fun onCountUpdated() {
        UpdateQuestionCount()
    }

    override fun onAttachmentPick(
        position: Int,
        item: MutableList<GetQuizQuestionReportData>?,
        isQuestion: Boolean,
        isOptionsImageId: EditText
    ) {
        isAttachmentAdapterPosition = position
        itemList = item!!
        isQuestionPick = isQuestion
        isOptionsFieldId = isOptionsImageId
        Log.d("isOptionsFieldId", resources.getResourceEntryName(isOptionsFieldId!!.id).toString())
        if (isQuestionPick!!) {
            showBottomDialog()
        } else {
            Constant.isFileLimit = 1
            openAlbumSelectActivity(Constant.IMAGE)
        }
    }


    // Attachment Pick Code

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraIntent()
        } else {
            // Show rationale if user has denied permission before
            if (cameraPermissionDeniedCount >= 2 && !ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.CAMERA
                )
            ) {
                showCameraPermissionSettingsDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraIntent()
            } else {
                cameraPermissionDeniedCount++
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.CAMERA
                    )
                ) {
                    showCameraPermissionSettingsDialog()
                } else {
                    Toast.makeText(
                        this, getString(R.string.camera_permission_is_required), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showCameraPermissionSettingsDialog() {
        AlertDialog.Builder(this).setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.camera_permission_is_permanently_denied_please_enable_it_from_app_settings))
            .setCancelable(false).setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }.setNegativeButton(getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun openAlbumSelectActivity(isFileType: String) {

        Log.d("FileComing", isFileType)
        val sdkInt = Build.VERSION.SDK_INT
        if (isFileType == Constant.DOCUMENT && sdkInt < Build.VERSION_CODES.R) {
            openSystemDocumentPicker()
        } else {
            val intent = Intent(this, AlbumSelectActivity::class.java)
            intent.putExtra(Constant.isFileType, isFileType)
            albumResultLauncher.launch(intent)
        }
    }

    // Opens the system file picker for DOCUMENT on Android 10 and below
    private fun openSystemDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, Constant.mimeTypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICK_DOCUMENT_REQUEST)
    }

    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.filepick_bottom_sheet)

        val rlaGallery = dialog.findViewById<RelativeLayout>(R.id.rlaGallery)
        val rlaCamera = dialog.findViewById<RelativeLayout>(R.id.rlaCamera)
        val rlaDocument = dialog.findViewById<RelativeLayout>(R.id.rlaVideo)
        val rlaVoice = dialog.findViewById<RelativeLayout>(R.id.rlaVoice)
        val rlaVideoPick = dialog.findViewById<RelativeLayout>(R.id.rlaVideoPick)

        rlaGallery.setOnClickListener {
            Constant.isFileLimit = 10
            openAlbumSelectActivity(Constant.IMAGE)
            dialog.dismiss()
        }

        rlaVoice.setOnClickListener {
            Constant.isFileLimit = 10
            openAlbumSelectActivity(Constant.AUDIO)
            dialog.dismiss()
        }

        rlaVideoPick.setOnClickListener {
            Constant.isFileLimit = 2
            openAlbumSelectActivity(Constant.VIDEO)
            dialog.dismiss()
        }


        rlaDocument.setOnClickListener {
            Constant.isFileLimit = 10
            openAlbumSelectActivity(Constant.DOCUMENT)
            dialog.dismiss()
        }

        rlaCamera.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
            dialog.dismiss()
        }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setWindowAnimations(R.style.PopupAnimation)
        }
        dialog.show()
    }

    private fun openCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }

            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this, "${applicationContext.packageName}.fileprovider", photoFile
                )
                cameraImageFilePath = photoFile.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
            } else {
                Toast.makeText(
                    this, getString(R.string.could_not_create_file_for_photo), Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.no_camera_app_found), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

//        val remaining = MAX_FILES - Constant.selectedFiles.size
//        if (remaining <= 0) {
//            Toast.makeText(
//                this,
//                "${getString(R.string.Max)} ${MAX_FILES} ${getString(R.string.files_allowed)}",
//                Toast.LENGTH_SHORT
//            ).show()
//            return
//        }

        fun addPath(uri: Uri) {
            if (isAttachmentAdapterPosition == RecyclerView.NO_POSITION) return
            if (isAttachmentAdapterPosition >= itemList.size) return

            val fileName = getFileName(uri)
            val mimeType = contentResolver.getType(uri)
            val type = when {
                mimeType?.startsWith("image/") == true -> FileType.IMAGE
                mimeType?.startsWith("video/") == true -> FileType.VIDEO
                mimeType?.startsWith("audio/") == true -> FileType.AUDIO
                fileName.endsWith(".pdf", true) -> FileType.PDF
                fileName.endsWith(".doc", true) || fileName.endsWith(
                    ".docx",
                    true
                ) -> FileType.DOC

                fileName.endsWith(".xls", true) || fileName.endsWith(
                    ".xlsx",
                    true
                ) -> FileType.EXCEL

                fileName.endsWith(".ppt", true) || fileName.endsWith(
                    ".pptx",
                    true
                ) -> FileType.PPT

                fileName.endsWith(".txt", true) -> FileType.TXT
                else -> FileType.OTHER
            }

            if (isQuestionPick!!) {
                val filePath = FilePath(
                    url = uri.toString(),
                    type = type.toString()
                )
                val quizItem = itemList[isAttachmentAdapterPosition]
                quizItem.file_path.add(filePath)
                Log.d(
                    "",
                    "Saved file at position $isAttachmentAdapterPosition => $filePath"
                )
            } else {
                val quizItem = itemList[isAttachmentAdapterPosition]
                if (resources.getResourceEntryName(isOptionsFieldId!!.id)
                        .toString() == "edtOptionA"
                ) {
                    quizItem.a_image = uri.toString()
                } else if (resources.getResourceEntryName(isOptionsFieldId!!.id)
                        .toString() == "edtOptionB"
                ) {
                    quizItem.b_image = uri.toString()
                } else if (resources.getResourceEntryName(isOptionsFieldId!!.id)
                        .toString() == "edtOptionC"
                ) {
                    quizItem.c_image = uri.toString()
                } else if (resources.getResourceEntryName(isOptionsFieldId!!.id)
                        .toString() == "edtOptionD"
                ) {
                    quizItem.d_image = uri.toString()
                }
            }
        }

        when (requestCode) {
            CAMERA_IMAGE_REQUEST -> {
                cameraImageFilePath?.let { filePath ->
                    var file = File(filePath)
                    if (file.exists()) {
                        if (!file.name.endsWith(".jpg", true)) {
                            val newFile = File(file.parent, file.nameWithoutExtension + ".jpg")
                            if (file.renameTo(newFile)) {
                                cameraImageFilePath = newFile.absolutePath
                                file = newFile
                            }
                        }

                        val fixedBitmap = fixImageOrientation(file.absolutePath)

                        if (fixedBitmap != null) {
                            val outputStream = FileOutputStream(file)
                            fixedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            outputStream.flush()
                            outputStream.close()
                        }

                        val uri = Uri.fromFile(file)
                        Constant.Remaining = Constant.Remaining - 1
                        addPath(uri)

                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.camera_image_file_not_found),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } ?: run {
                    Toast.makeText(this, R.string.camera_image_failed, Toast.LENGTH_SHORT).show()
                }
            }


//            CAMERA_IMAGE_REQUEST -> {
//                cameraImageFilePath?.let { filePath ->
//                    var file = File(filePath)
//                    if (file.exists()) {
//                        if (!file.name.endsWith(".jpg", true)) {
//                            val newFile = File(file.parent, file.nameWithoutExtension + ".jpg")
//                            if (file.renameTo(newFile)) {
//                                cameraImageFilePath = newFile.absolutePath
//                                file = newFile
//                            }
//                        }
//                        val uri = Uri.fromFile(file)
//                        addPath(uri)
//                    } else {
//                        Toast.makeText(
//                            this,
//                            getString(R.string.camera_image_file_not_found),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                } ?: run {
//                    Toast.makeText(
//                        this, getString(R.string.camera_image_failed), Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }

            PICK_DOCUMENT_REQUEST -> {
                val clipData = data?.clipData
                val singleUri = data?.data

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        addPath(uri)
                    }
                } else if (singleUri != null) {
                    addPath(singleUri)
                }
            }
        }
        mAdapter?.notifyDataSetChanged()
    }

    private fun fixImageOrientation(imagePath: String): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: return null
        val exif = ExifInterface(imagePath)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            else -> return bitmap
        }

        return try {
            val fixedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()  // Free up memory from the original bitmap
            fixedBitmap
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == Constant.content_) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: ""
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat(Constant.yyyyMMdd_HHmmss, Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
        return File.createTempFile(
            "${Constant.IMG_}${timeStamp}${Constant.underscore}", ".jpg", storageDir
        )
    }

    fun isAddQuestionSubmit() {
        val allQuestions = quizAdapter!!.getUpdatedList()

        val apiUserQuestions = allQuestions
            .filter { it.sourceType == QuestionSource.API || it.sourceType == QuestionSource.USER || it.sourceType == QuestionSource.QBANK }
            .map {
                QuizQuestionRequest(
                    quesNo = it.id,
                    chapter = it.chapter,
                    question = it.question,
                    a_option = it.a_option,
                    b_option = it.b_option,
                    c_option = it.c_option,
                    d_option = it.d_option,
                    a_image = it.a_image,
                    b_image = it.b_image,
                    c_image = it.c_image,
                    d_image = it.d_image,
                    answer = it.answer,
                    mark = it.mark,
                    iframe = it.iframe ?: "",
                    file_size = "",
                    thumbnail = it.thumbnail ?: "",
                    file_path = it.file_path
                )
            }

        val updateQBankList: List<UpdateQBankItem> = allQuestions
            .filter { it.sourceType == QuestionSource.QBANK }
            .mapNotNull { updatedItem ->
                val originalItem = pickQBankList.find { it.id == updatedItem.id }

                if (originalItem != null) {
                    if (
                        updatedItem.question != originalItem.question ||
                        updatedItem.chapter != originalItem.chapter ||
                        updatedItem.a_option != originalItem.a_option ||
                        updatedItem.b_option != originalItem.b_option ||
                        updatedItem.c_option != originalItem.c_option ||
                        updatedItem.d_option != originalItem.d_option ||
                        updatedItem.a_image != originalItem.a_image ||
                        updatedItem.b_image != originalItem.b_image ||
                        updatedItem.c_image != originalItem.c_image ||
                        updatedItem.d_image != originalItem.d_image ||
                        updatedItem.answer != originalItem.answer ||
                        updatedItem.mark != originalItem.mark
                    ) {
                        UpdateQBankItem(
                            ques_no = updatedItem.id,
                            subject_id = isSubjectID,
                            chapter = updatedItem.chapter,
                            question = updatedItem.question,
                            a_option = updatedItem.a_option,
                            b_option = updatedItem.b_option,
                            c_option = updatedItem.c_option,
                            d_option = updatedItem.d_option,
                            a_image = updatedItem.a_image!!,
                            b_image = updatedItem.b_image!!,
                            c_image = updatedItem.c_image!!,
                            d_image = updatedItem.d_image!!,
                            answer = updatedItem.answer,
                            mark = updatedItem.mark
                        )
                    } else null
                } else null
            }

        isUpdatedQBankQuestions = updateQBankList.size
        val totalMaxMark = apiUserQuestions.sumOf { it.mark }

        val body = QuizRequestBody(
            quiz_id = isQuizID,
            questions = apiUserQuestions,
            max_mark = totalMaxMark,
            ok_flag = isOkFlag,
            update_question_bank = updateQBankList
        )

        val jsonObject = Gson().toJsonTree(body).asJsonObject
        Log.d("FinalJSON", jsonObject.toString())

        Log.d("isUpdatedQBankQuestion", isUpdatedQBankQuestions.toString())
//        Constant.hideLoading(this)

        if (isUpdatedQBankQuestions > 0) {

            val textQuestion = if (isUpdatedQBankQuestions == 1) {
                getString(R.string.question_)
            } else {
                getString(R.string.questions)
            }

            val isMessage =
                "${getString(R.string.You_have_modified)} $isUpdatedQBankQuestions $textQuestion ${
                    getString(R.string.from_the_Question_Bank_Do_you_want_to_update_the_Question_Bank)
                }"
            Constant.showSendConfirmationDialog(
                this,
                getString(R.string.confirmation),
                getString(R.string.Update),
                getString(R.string.Cancel),
                "",
                isMessage
            ) { confirmed ->
                if (confirmed) {
                    Constant.showLoading(this)
                    submitQuiz(body)
//                    isUploadFilesInServer(jsonObject)
                    // appViewModel?.isQuizAddQuestion(isAccessToken!!, jsonObject)
                }
            }

        } else {
            Constant.showLoading(this)
            submitQuiz(body)
            //  appViewModel?.isQuizAddQuestion(isAccessToken!!, jsonObject)
        }
    }

    fun submitQuiz(body: QuizRequestBody) {
        Constant.showLoading(this)

        if (hasLocalFiles(body)) {
            uploadFilesAndSubmit(body)
        } else {
            callApi(body)
        }
    }

    private fun hasLocalFiles(body: QuizRequestBody): Boolean {
        return body.questions.any { q ->
            q.file_path.any { !it.url.contains("amazonaws") } ||
                    listOf(q.a_image, q.b_image, q.c_image, q.d_image)
                        .any { !it.isNullOrEmpty() && !it.contains("amazonaws") }
        }
    }

    private fun uploadFilesAndSubmit(body: QuizRequestBody) {

        val uploadQueue = mutableListOf<Pair<String, String>>() // path to type

        body.questions.forEach { q ->
            q.file_path.forEach {
                if (!it.url.contains("amazonaws")) {
                    uploadQueue.add(it.url to it.type)
                }
            }

            addIfLocal(q.a_image, uploadQueue)
            addIfLocal(q.b_image, uploadQueue)
            addIfLocal(q.c_image, uploadQueue)
            addIfLocal(q.d_image, uploadQueue)
        }

        if (uploadQueue.isEmpty()) {
            callApi(body)
            return
        }

        Constant.isAwsUploadedFiles.clear()
        uploadNext(0, uploadQueue, body)
    }

    private fun addIfLocal(
        path: String?,
        list: MutableList<Pair<String, String>>
    ) {
        if (!path.isNullOrEmpty() && !path.contains("amazonaws")) {
            list.add(path to "IMAGE")
        }
    }

    private fun uploadNext(
        index: Int,
        queue: List<Pair<String, String>>,
        body: QuizRequestBody
    ) {
        if (index >= queue.size) {
            replaceUrlsInBody(body)
            callApi(body)
            return
        }

        val (path, type) = queue[index]

        isAwsUploadingPreSigned?.getPreSignedUrl(
            path,
            isStaffDetails!!.school_id,
            Constant.quiz,
            this,
            SharedPreference.getCountryId(this)!!,
            false,
            object : UploadCallback {

                override fun onUploadSuccess(response: String?, isFileUploaded: String?) {
                    Constant.isAwsUploadedFiles.add(
                        AwsUploadedFiles(isFileUploaded!!, type)
                    )
                    uploadNext(index + 1, queue, body)
                }

                override fun onUploadError(error: String?) {
                    runOnUiThread {
                        Toast.makeText(this@AddQuestion, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun replaceUrlsInBody(body: QuizRequestBody) {

        val iterator = Constant.isAwsUploadedFiles.iterator()

        body.questions.forEach { q ->

            val updatedFiles = q.file_path.map {
                if (it.url.contains("amazonaws")) it
                else iterator.next().let { aws ->
                    FilePath(aws.isFileUrl, aws.isFileType)
                }
            }

            q.a_image = replaceIfLocal(q.a_image, iterator)
            q.b_image = replaceIfLocal(q.b_image, iterator)
            q.c_image = replaceIfLocal(q.c_image, iterator)
            q.d_image = replaceIfLocal(q.d_image, iterator)

            // Replace question files
            q.file_path.clear()
            q.file_path.addAll(updatedFiles)
        }
    }

    private fun replaceIfLocal(
        path: String?,
        iterator: Iterator<AwsUploadedFiles>
    ): String? {
        return if (!path.isNullOrEmpty() && !path.contains("amazonaws")) {
            iterator.next().isFileUrl
        } else path
    }

    private fun callApi(body: QuizRequestBody) {
        val json = Gson().toJsonTree(body).asJsonObject
        Log.d("FINAL_JSON", json.toString())

        appViewModel?.isQuizAddQuestion(isAccessToken!!, json)
    }


    fun isUploadFilesInServer(isFileType: JsonObject?) {

//        if (SELECTED_MENU_ID == M_ATTACHMENTS) {
//            Constant.selectedFiles.removeAt(0) // Remove '+' placeholder
//        }
//        ProgressDialogHelper.updateProgress(50)
        isTotalSelectedItem = Constant.selectedFiles.size
        isVideoSelectedArrayList.clear()
        Constant.isAwsUploadedFiles.clear()
        val iterator = Constant.selectedFiles.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.type == FileType.VIDEO) {
//                isVideoSelectedArrayList.add(file)
                iterator.remove()
            }
        }

        val numNonVideoFiles = Constant.selectedFiles.size
        val numVideos = isVideoSelectedArrayList.size

        val videoSteps = 10
        var totalTasks = (numNonVideoFiles * 2) + (numVideos * videoSteps)

        if (totalTasks == 0 && numVideos > 0) {
            totalTasks = videoSteps
        }
        var completedTasks = 0

        fun updateProgress() {
            if (totalTasks > 0) {
                val progress = (completedTasks * 100) / totalTasks
                ProgressDialogHelper.updateProgress(progress)
            } else {
                ProgressDialogHelper.dismiss()
            }
        }

        when {
//            Constant.selectedFiles.isNotEmpty() -> isFileUploadInAws(
//                isFileType,
//                totalTasks,
//                { completedTasks++; updateProgress() })

            isVideoSelectedArrayList.isNotEmpty() -> videoUploading(
                totalTasks,
                { completedTasks++; updateProgress() })
        }
//        ProgressDialogHelper.updateProgress(80)
    }

    private fun isFileUploadInAws(
        isFileType: String?,
        totalTasks: Int,
        onTaskComplete: () -> Unit
    ) {
        Constant.isAwsUploadedFiles.clear()
        val iterator = Constant.selectedFiles.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("amazonaws.")) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
                    )
                )
                iterator.remove()
            }
        }

        val isCountryId = SharedPreference.getCountryId(this)
        if (Constant.selectedFiles.isEmpty()) {
            if (isVideoSelectedArrayList.isEmpty()) {
                ProgressDialogHelper.dismiss()
//                isUpdateAttachment()
            } else {
                videoUploading(totalTasks, onTaskComplete)
            }
        } else {
            val outputDir =
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput")
            outputDir.mkdirs()
            val newSelectedFiles = mutableListOf<FileItem>()
            Constant.compressImageFilesOnly(
                context = this,
                files = Constant.selectedFiles,
                outputDir = outputDir.absolutePath,
                format = Bitmap.CompressFormat.JPEG,
                quality = 80,
                maxWidth = 1280,
                maxHeight = 1280,
                onEachProcessed = { original, outputPath, success ->
                    if (success && outputPath != null) {
                        val compressedFile = File(outputPath)
                        val originalSizeKB = try {
                            if (original.path.startsWith("content://")) {
                                contentResolver.openFileDescriptor(
                                    Uri.parse(original.path), "r"
                                )?.statSize ?: 0
                            } else {
                                File(original.path).length()
                            }
                        } catch (e: Exception) {
                            0L
                        }

                        Log.d(
                            "Compressor",
                            "Compressed: $outputPath (${compressedFile.length() / 1024}KB), Original: ${originalSizeKB / 1024}KB"
                        )

                        newSelectedFiles.add(FileItem(path = outputPath, type = original.type))
                    } else {
                        Log.e("Compressor", "Failed: ${original.path}")
                    }
                    onTaskComplete()
                },
                onComplete = {
                    Constant.selectedFiles.clear()
                    Constant.selectedFiles.addAll(newSelectedFiles)
                    val isAwsUploadingFile = ArrayList<String>()

                    val isSelectedFileCount = Constant.selectedFiles.size
                    for (i in Constant.selectedFiles.indices) {
                        isAwsUploadingPreSigned?.getPreSignedUrl(
                            Constant.selectedFiles[i].path,
                            isStaffDetails!!.school_id,
                            isFileType!!,
                            this,
                            isCountryId!!,
                            false,
                            object : UploadCallback {

                                override fun onUploadSuccess(
                                    response: String?, isFileUploaded: String?
                                ) {
                                    isAwsUploadingFile.add(isFileUploaded!!)
                                    Constant.isAwsUploadedFiles.add(
                                        AwsUploadedFiles(
                                            isFileUrl = isFileUploaded,
                                            isFileType = Constant.selectedFiles[i].type.name
                                        )
                                    )
                                    onTaskComplete()

                                    if (isTotalSelectedItem == Constant.isAwsUploadedFiles.size) {
                                        ProgressDialogHelper.dismiss()
//                                        isUpdateAttachment()
                                    } else {
                                        if (isAwsUploadingFile.size == isSelectedFileCount) {
                                            videoUploading(totalTasks, onTaskComplete)
                                        }
                                    }
                                }

                                override fun onUploadError(error: String?) {
                                    Log.d("isUploadIssue", error.toString())
                                }
                            })
                    }

                    Log.d("Compressor", "All files compressed and uploaded.")
                })
        }
    }

    private fun videoUploading(
        totalTasks: Int,
        onTaskComplete: () -> Unit
    ) {
        val iterator = isVideoSelectedArrayList.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
//            if (fileItem.path.contains("player.vimeo.com")) {
//                Constant.isAwsUploadedFiles.add(
//                    AwsUploadedFiles(
//                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
//                    )
//                )
//                iterator.remove()
//            }
        }
        if (isVideoSelectedArrayList.isNotEmpty()) {
            for (i in isVideoSelectedArrayList.indices) {
                Thread {
                    for (x in 1..10) {
                        Thread.sleep(400)
                        runOnUiThread { onTaskComplete() }
                    }
                }.start()
//                VimeoVideoUpload.uploadVideo(
//                    this, Constant.quiz, Constant.quiz, isVideoSelectedArrayList[i].path, this
//                )
            }
        } else {
            ProgressDialogHelper.dismiss()
//            isUpdateAttachment()
        }
    }

    override fun onUploadComplete(
        success: Boolean, iframe: String?, link: String?
    ) {
        runOnUiThread {
            Log.d("link", link.toString())
            Constant.isAwsUploadedFiles.add(
                AwsUploadedFiles(
                    isFileUrl = link.toString(), isFileType = Constant.VIDEO
                )
            )

            if (Constant.isAwsUploadedFiles.size == isTotalSelectedItem) {
                ProgressDialogHelper.dismiss()
//                isUpdateAttachment()
            }
        }
    }


    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
        }
    }
}

