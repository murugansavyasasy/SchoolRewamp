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
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.LSRW.CreateNewTask
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.AddQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.PickQuestionAdapter
import com.vs.schoolmessenger.School.QuizExam.AddQuestionListner
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.QuizQuestionRequest
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.QuizRequestBody
import com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion.UpdateQBankItem
import com.vs.schoolmessenger.School.QuizExam.Model.CreateQuiz.SaveCreateExamQuizDetails
import com.vs.schoolmessenger.School.QuizExam.Model.PickFromQuestionBank.GetPickFromQBankData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizAttachmentData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.QuestionSource
import com.vs.schoolmessenger.School.QuizExam.OnAttachmentListener
import com.vs.schoolmessenger.School.QuizExam.QuizTempHolder
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

    private val MAX_FILES_PER_QUESTION = 10
    private val MAX_VIDEO_PER_QUESTION = 2

    private val uploadedFiles = mutableListOf<AwsUploadedFiles>()
    private var pendingFiles: List<FilePath> = emptyList()
    private var currentIndex = 0
    private lateinit var pendingBody: QuizRequestBody
    private var currentVideoPath: String? = null
    private var itemList: MutableList<GetQuizQuestionReportData> = mutableListOf()
    private var quizAdapter: AddQuestionAdapter? = null
    private var isDialogShowing = false
    var isAttachmentAdapterPosition = 0
    private var cameraPermissionDeniedCount = 0
    var isQuestionPick: Boolean? = null
    var isOptionsFieldId: TextView? = null

    private var totalFilesToUpload = 0
    private var pickImagesLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickVideoLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val MAX_FILES = 10
        internal const val CAMERA_IMAGE_REQUEST = 1004
    }

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
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
    private var isQuizCreateData: SaveCreateExamQuizDetails? = null

    private var appViewModel: App? = null
    override fun setupViews() {
        super.setupViews()

        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        isQuizCreateData = QuizDataTempHolder.quizDataBody

        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.lblImportQuestion.setOnClickListener(this)
        binding.lblSendQuiz.setOnClickListener(this)

        pickImagesLauncher =
            registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(Constant.isFilesAllow)
            ) { uris ->

                if (uris.isEmpty()) return@registerForActivityResult

                if (uris.size > Constant.isFilesAllow) {
                    Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show()
                }

                val limitedUris = uris.take(Constant.isFilesAllow)

                handleSelectedImages(limitedUris, Constant.IMAGE)
            }

        pickVideoLauncher =
            registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(Constant.isVideoAllow)
            ) { uris ->

                if (uris.isEmpty()) return@registerForActivityResult

                if (uris.size > Constant.isVideoAllow) {
                    Toast.makeText(this, "Only 2 videos allowed", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                handleSelectedImages(uris, Constant.VIDEO)
            }


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

//We are two scenrio are handle here
//        Before that In Create Quiz the title, decription,no of questions,flag all details are fetched
//        1.Here comes the main thing if user check "ADD_NOW" means it all the details will be saved and no quiz will be created directly we are redirected to "Add Question Page" here we are adding the question and then going to "Recipient page" and then taking all the target details etc and finally calling the "create quiz api" call
//        2.Here if user already created means we used have all the details about the quiz and only need to add the questions and do "Add Question api"

        if (isQuizCreateData != null) {
            if (isQuizCreateData!!.type == "ADD_NOW") {
                binding.lblSendQuiz.text = getString(R.string.NEXT)
                Log.d("ScreenName", "AddNowScreen")
                Constant.isQuestionLimit = isQuizCreateData!!.no_of_question.toInt()
                isSavedQuestionLimit = isQuizCreateData!!.no_of_question.toInt()
                isSubmittedCount = 0
                isQuizID = ""
                isSubjectID = ""
                isQuizTitle = isQuizCreateData!!.title
                isOkFlag = isSubmittedCount > 0
                binding.toolbarLayout.lblParentToolBar.text = isQuizTitle
                binding.lblImportQuestion.visibility = View.GONE

                //Note: we are making both savedQuizQuestionReportList and editableQuizQuestionReportList as empty because to have a one default question
                savedQuizQuestionReportList = emptyList()
                editableQuizQuestionReportList = mutableListOf()

                //here avoid the isGetQuizQuestionReport api because we have not yet created the quiz and not yet add the question so directly load empty list
                //Load adapter with empty list
                isLoadQuizQuestionReport()

            } else {
                Log.d("isComing", "isComing")
                Log.d("isComing", isQuizCreateData!!.toString())
                Log.d("isComing", isQuizCreateData!!.type.toString())
            }
        } else {
            Log.d("ScreenName", "AddQuestionScreen")
            binding.lblSendQuiz.text = getString(R.string.send_quiz)
            Constant.isQuestionLimit = intent.getIntExtra(Constant.limitQuestion, -1)
            isSavedQuestionLimit = intent.getIntExtra(Constant.limitQuestion, -1)
            isSubmittedCount = intent.getIntExtra(Constant.submittedCount, -1)
            isQuizID = intent.getStringExtra(Constant.quiz_Id).toString()
            isSubjectID = intent.getStringExtra(Constant.subjectID).toString()
            isQuizTitle = intent.getStringExtra(Constant.quiz_Title).toString()
            val openToStudentMsg = intent.getBooleanExtra(Constant.openToStudent, false)
            if (!openToStudentMsg) {
                binding.marqueeText.visibility = View.VISIBLE
                binding.marqueeText.isSelected = true
                setMarqueeText(
                    binding.marqueeText,
                    "⏳ ${getString(R.string.the_quiz_will_be_visible_to_students_only_after_all_questions_are_filled_and_submitted)}"
                )
            } else {
                binding.marqueeText.visibility = View.GONE
            }
            isOkFlag = isSubmittedCount > 0
            binding.toolbarLayout.lblParentToolBar.text = isQuizTitle
            binding.lblImportQuestion.visibility = View.VISIBLE
            isFetchQuizQuestionReport()
        }
        Log.d("isQuestionLimit", Constant.isQuestionLimit.toString())
    }

    private fun handleSelectedImages(uris: List<Uri>, fileType: String) {

        if (uris.isEmpty()) return

        var allowedUris = uris

        // Normal file limit only
        if (uris.size > Constant.isFileLimit) {
            Toast.makeText(
                this,
                "You can select only ${Constant.isFileLimit} files",
                Toast.LENGTH_SHORT
            ).show()
        }

        allowedUris = uris.take(Constant.isFileLimit)

        if (Constant.Remaining <= 0 || allowedUris.isEmpty()) {
            Log.d("LimitReached", "No remaining file slots")
            return
        }

        if (isAttachmentAdapterPosition == RecyclerView.NO_POSITION) return
        if (isAttachmentAdapterPosition >= itemList.size) return

        val quizItem = itemList[isAttachmentAdapterPosition]

        val previousCount = Constant.selectedFiles.size
        Constant.Remaining -= allowedUris.size
        if (Constant.Remaining < 0) Constant.Remaining = 0

        allowedUris.forEach { uri ->

            val mimeType = contentResolver.getType(uri)

            val fileName = getFileName(uri).takeIf { it.isNotEmpty() }
                ?: uri.lastPathSegment?.substringAfterLast("/")
                ?: "temp_file_${System.currentTimeMillis()}"

            val type = when {
                mimeType?.startsWith("image/") == true -> FileType.IMAGE
                mimeType?.startsWith("video/") == true -> FileType.VIDEO
                mimeType?.startsWith("audio/") == true -> FileType.AUDIO
                fileName.endsWith(".pdf", true) -> FileType.PDF
                fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> FileType.DOC
                fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> FileType.EXCEL
                fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT
                fileName.endsWith(".txt", true) -> FileType.TXT
                else -> FileType.OTHER
            }

            if (type == FileType.VIDEO) {

                val videoCount = Constant.selectedFiles.count {
                    it.type == FileType.VIDEO
                }

                if (videoCount >= 2) {
                    Toast.makeText(
                        this,
                        "Only 2 videos are allowed",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@forEach
                }
            }

            if (isQuestionPick == true) {

                if (!canAddAttachment(quizItem, type)) {
                    return@forEach
                }

                quizItem.file_path?.add(
                    FilePath(
                        url = uri.toString(),
                        type = type.toString()
                    )
                )

            } else {

                when (resources.getResourceEntryName(isOptionsFieldId!!.id)) {
                    "lblAddImageA" -> quizItem.a_image = uri.toString()
                    "lblAddImageB" -> quizItem.b_image = uri.toString()
                    "lblAddImageC" -> quizItem.c_image = uri.toString()
                    "lblAddImageD" -> quizItem.d_image = uri.toString()
                }
            }

            if (Constant.selectedFiles.size < CreateNewTask.MAX_FILES) {
                Constant.selectedFiles.add(
                    FileItem(uri.toString(), type)
                )
            } else {
                Constant.Remaining = 0
            }

            Log.d("SelectedFile", "URI: $uri, Type: $type")
        }

        quizAdapter?.notifyItemChanged(isAttachmentAdapterPosition)

        val addedCount = Constant.selectedFiles.size - previousCount
        val totalCount = Constant.selectedFiles.size

        Log.d("FinalSelectedFiles", "Total: $totalCount, Added: $addedCount")
        isAttachmentAdapterPosition = RecyclerView.NO_POSITION
        isQuestionPick = false
        isOptionsFieldId = null
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
        appViewModel?.isGetQuizQuestionReport(isAccessToken ?: "", isQuizID, this)
    }

    private fun isFetchFromQuestionBank() {
        appViewModel?.isGetPickFromQBank(isAccessToken ?: "", isSubjectID, this)
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
                    binding.lblSendQuiz.isEnabled = true
                    binding.lblSendQuiz.alpha = 1f
                } else {
                    binding.rcAddQuestion.visibility = View.GONE
                    binding.lytList.visibility = View.VISIBLE
                    binding.lblSendQuiz.isEnabled = false
                    binding.lblSendQuiz.alpha = 0.5f
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

    private fun canAddAttachment(
        quizItem: GetQuizQuestionReportData,
        newType: FileType
    ): Boolean {

        val attachments = quizItem.file_path ?: return true

        // ---- TOTAL FILE LIMIT ----
        if (attachments.size >= MAX_FILES_PER_QUESTION) {
            Toast.makeText(
                this,
                "Only $MAX_FILES_PER_QUESTION files allowed per question",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        // ---- VIDEO LIMIT ----
        if (newType == FileType.VIDEO) {
            val videoCount = attachments.count {
                it.type == FileType.VIDEO.toString()
            }

            if (videoCount >= MAX_VIDEO_PER_QUESTION) {
                Toast.makeText(
                    this,
                    "Only $MAX_VIDEO_PER_QUESTION videos allowed per question",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }

        return true
    }


    fun UpdateQuestionCount() {
        val text = "${isSavedQuestionLimit - Constant.isQuestionLimit}/$isSavedQuestionLimit"
        val spannable = SpannableString(text)

        // Apply blue color only to part before "/"
        val slashIndex = text.indexOf("/")
        spannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this, R.color.PrimaryColor
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
            file_path = this.file_path?.toMutableList() ?: mutableListOf()
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
            val remaining = isSavedQuestionLimit - quizAdapter!!.getUpdatedList().size
            val total_no_of_questions = isSavedQuestionLimit

            val textQuestion = if (total_no_of_questions == 1) {
                getString(R.string.question_)  // e.g. "question"
            } else {
                getString(R.string.questions) // e.g. "questions"
            }

            val remainingText = if (remaining == 1) {
                getString(R.string.question_)
            } else {
                getString(R.string.questions)
            }

            val isMessage =
                "${getString(R.string.almost_there_you_ve_created)} $currentCount ${getString(R.string.out_of)} $total_no_of_questions $textQuestion.\n ${
                    getString(
                        R.string.you_still_need_to_add
                    )
                } $remaining ${getString(R.string.more)} $remainingText ${getString(R.string.to_complete_the_quiz_but_don_t_worry_you_can_add_them_later)} \n ${
                    getString(
                        R.string.note_the_quiz_will_be_visible_to_students_only_after_all_questions_are_filled
                    )
                }"

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
                            this, getString(R.string.alert), isQuestionBankErrorMsg.toString()
                        )
                    } else {
                        Log.d("isEmpty", "isNotEmpty")
                        showResumeListDialog(this, pickQBankList)
                    }
                }
            }

            R.id.lblSendQuiz -> {
                if (isQuizCreateData != null) {
                    if (isQuizCreateData!!.type == "ADD_NOW") {
                        if (quizAdapter!!.showValidationErrors(binding.rcAddQuestion)) {
                            if (quizAdapter!!.getUpdatedList().size <= isSavedQuestionLimit) {
                                if (isSavedQuestionLimit == quizAdapter!!.getUpdatedList().size) {
                                    isAddQuestionSubmit()
                                } else {
                                    val currentCount = quizAdapter!!.getUpdatedList().size
                                    val remaining =
                                        isSavedQuestionLimit - quizAdapter!!.getUpdatedList().size
                                    val total_no_of_questions = isSavedQuestionLimit

                                    val textQuestion = if (total_no_of_questions == 1) {
                                        getString(R.string.question_)  // e.g. "question"
                                    } else {
                                        getString(R.string.questions) // e.g. "questions"
                                    }

                                    val remainingText = if (remaining == 1) {
                                        getString(R.string.question_)
                                    } else {
                                        getString(R.string.questions)
                                    }

                                    val isMessage =
                                        "${getString(R.string.almost_there_you_ve_created)} $currentCount ${
                                            getString(
                                                R.string.out_of
                                            )
                                        } $total_no_of_questions $textQuestion.\n ${
                                            getString(
                                                R.string.you_still_need_to_add
                                            )
                                        } $remaining ${"more"} $remainingText ${"to complete the quiz — but don’t worry, you can add them later"} \n ${"Note: The quiz will be visible to students only after all questions are filled"}"

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
                            } else {
                                Constant.showErrorAlert(
                                    this,
                                    getString(R.string.alert),
                                    getString(R.string.question_limit_reached)
                                )
                            }
                        }
                    }
                } else {
                    Log.d("Iscoming", "IsComingtoAddQuestionScreen")
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
    }


    override fun onCountUpdated() {
        UpdateQuestionCount()
    }

    override fun onUICheck(list: List<GetQuizQuestionReportData>) {
        if (list.isEmpty()) {
            Log.d("isLog", "isEmpty")
            binding.rcAddQuestion.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.lblSendQuiz.isEnabled = false
            binding.lblSendQuiz.alpha = 0.5f
        } else {
            Log.d("isLog", "isNotEmpty")
            binding.rcAddQuestion.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
            binding.lblSendQuiz.isEnabled = true
            binding.lblSendQuiz.alpha = 1f

        }
    }

    override fun onDeleteQuizQuestion(
        id: String,
        onResult: (Boolean) -> Unit
    ) {
        Constant.showSendConfirmationDialog(
            this,
            getString(R.string.confirmation),
            getString(R.string.delete),
            getString(R.string.Cancel),
            "",
            getString(R.string.are_you_sure_you_want_to_delete_this_question)
        ) { confirmed ->
            if (confirmed) {
                Constant.showLoading(this)

                appViewModel?.isDeleteQuizQuestion?.observe(this) { response ->
                    Constant.hideLoading(this)
                    if (response != null) {
                        if (response.status) {
                            onResult(true)
                        } else {
                            onResult(false)
                            Constant.showDataValidationNoDashboardRedirect(
                                resources.getString(R.string.Oops), response.message, this
                            )
                        }
                    } else {
                        onResult(false)
                        Constant.showDataValidationNoDashboardRedirect(
                            resources.getString(R.string.Oops),
                            getString(R.string.something_went_wrong_please_try_again_later),
                            this
                        )
                    }
                }

                val jsonObject = JsonObject().apply {
                    addProperty("id", id)
                }

                appViewModel?.isDeleteQuizQuestion(isAccessToken!!, jsonObject, this)

            }
        }
    }

    override fun onAttachmentPick(
        position: Int,
        item: MutableList<GetQuizQuestionReportData>?,
        isQuestion: Boolean,
        isOptionsImageId: TextView
    ) {
        Constant.isQuizQuestionPickCount = item!![position].file_path!!.size
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
        if (isFileType == Constant.DOCUMENT) {
            openSystemDocumentPicker()
        }
        else if(isFileType == Constant.VIDEO){
            pickVideoLauncher!!.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }
        else if(isFileType == Constant.IMAGE) {
            pickImagesLauncher!!.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }
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
            Constant.isFileLimit = 10
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

        val photoFile = try {
            createImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        if (photoFile == null) {
            Toast.makeText(
                this,
                getString(R.string.could_not_create_file_for_photo),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val photoURI = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )

        cameraImageFilePath = photoFile.absolutePath

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Camera not available on this device",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("CameraError", "Camera launch failed", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        fun addPath(uri: Uri) {

            if (isAttachmentAdapterPosition == RecyclerView.NO_POSITION) return
            if (isAttachmentAdapterPosition >= itemList.size) return

            val quizItem = itemList[isAttachmentAdapterPosition]
            val fileName = getFileName(uri)
            val mimeType = contentResolver.getType(uri) ?: ""

            val type = when {

                mimeType == "application/pdf" ||
                        fileName.endsWith(".pdf", true) ->
                    FileType.PDF

                mimeType == "application/msword" ||
                        mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                        fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) ->
                    FileType.DOC

                mimeType == "application/vnd.ms-excel" ||
                        mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                        fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) ->
                    FileType.EXCEL

                mimeType == "application/vnd.ms-powerpoint" ||
                        mimeType == "application/vnd.openxmlformats-officedocument.presentationml.presentation" ||
                        fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) ->
                    FileType.PPT

                mimeType.startsWith("image/") ||
                        fileName.matches(".*\\.(jpg|jpeg|png|webp)$".toRegex(RegexOption.IGNORE_CASE)) ->
                    FileType.IMAGE

                mimeType == "text/plain" ||
                        fileName.endsWith(".txt", true) ->
                    FileType.TXT

                else -> FileType.OTHER
            }


            // RESTRICTION CHECK
            if (isQuestionPick == true && !canAddAttachment(quizItem, type)) {
                return
            }

            if (isQuestionPick == true) {
                quizItem.file_path!!.add(
                    FilePath(
                        url = uri.toString(),
                        type = type.toString()
                    )
                )
            } else {
                when (resources.getResourceEntryName(isOptionsFieldId!!.id)) {
                    "lblAddImageA" -> quizItem.a_image = uri.toString()
                    "lblAddImageB" -> quizItem.b_image = uri.toString()
                    "lblAddImageC" -> quizItem.c_image = uri.toString()
                    "lblAddImageD" -> quizItem.d_image = uri.toString()
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

                        val uri = Uri.fromFile(file)
                        Constant.Remaining = Constant.Remaining - 1
                        addPath(uri)

                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.camera_image_file_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } ?: run {
                    Toast.makeText(this, R.string.camera_image_failed, Toast.LENGTH_SHORT).show()
                }
            }

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
        quizAdapter?.notifyItemChanged(isAttachmentAdapterPosition)
        isAttachmentAdapterPosition = RecyclerView.NO_POSITION
        isQuestionPick = false
        isOptionsFieldId = null
    }

    private fun getFileName(uri: Uri): String {
        var fileName: String? = null

        if (uri.scheme.equals("content", ignoreCase = true)) {
            val projection = arrayOf(OpenableColumns.DISPLAY_NAME)

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        fileName = cursor.getString(columnIndex)
                    }
                }
            }
        }

        if (fileName.isNullOrEmpty()) {
            fileName = uri.lastPathSegment
            fileName = fileName?.substringAfterLast("/")
        }

        return fileName ?: "temp_file_${System.currentTimeMillis()}"
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat(Constant.yyyyMMdd_HHmmss, Locale.ENGLISH).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
        return File.createTempFile(
            "${Constant.IMG_}${timeStamp}${Constant.underscore}", ".jpg", storageDir
        )
    }

    fun isAddQuestionSubmit() {
        val allQuestions = quizAdapter!!.getUpdatedList()

        val apiUserQuestions =
            allQuestions.filter { it.sourceType == QuestionSource.API || it.sourceType == QuestionSource.USER || it.sourceType == QuestionSource.QBANK }
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
                        file_path = it.file_path ?: mutableListOf()
                    )
                }

        val updateQBankList: List<UpdateQBankItem> =
            allQuestions.filter { it.sourceType == QuestionSource.QBANK }
                .mapNotNull { updatedItem ->
                    val originalItem = pickQBankList.find { it.id == updatedItem.id }

                    if (originalItem != null) {
                        if (updatedItem.question != originalItem.question || updatedItem.chapter != originalItem.chapter || updatedItem.a_option != originalItem.a_option || updatedItem.b_option != originalItem.b_option || updatedItem.c_option != originalItem.c_option || updatedItem.d_option != originalItem.d_option || updatedItem.a_image != originalItem.a_image || updatedItem.b_image != originalItem.b_image || updatedItem.c_image != originalItem.c_image || updatedItem.d_image != originalItem.d_image || updatedItem.answer != originalItem.answer || updatedItem.mark != originalItem.mark) {
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

        //here actually if all the question are filled means we pass as open_to_student as true so the reciver side this particular quiz will be visible if false means this particular quiz will not be visible
        var open_to_student = false
        open_to_student = isSavedQuestionLimit == quizAdapter!!.getUpdatedList().size

        val body = QuizRequestBody(
            quiz_id = isQuizID,
            questions = apiUserQuestions,
            max_mark = totalMaxMark,
            open_to_student = open_to_student,
            ok_flag = isOkFlag,
            update_question_bank = updateQBankList
        )

        Log.d("FinalJSON", body.toString())
        QuizTempHolder.quizBody = body
        QuizDataTempHolder.quizDataBody = isQuizCreateData
        Log.d("isUpdatedQBankQuestion", isUpdatedQBankQuestions.toString())
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
                    if (Constant.isQuizReportPage) {
                        submitQuiz(body)
                    } else {
                        val intent = Intent(this, RecipientActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        } else {
            if (Constant.isQuizReportPage) {
                submitQuiz(body)
            } else {
                val intent = Intent(this, RecipientActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun submitQuiz(body: QuizRequestBody) {

        ProgressDialogHelper.show(this)

        pendingBody = body
        uploadedFiles.clear()

        val filesToUpload = collectLocalFiles(body)

        totalFilesToUpload = filesToUpload.size
        currentIndex = 0
        ProgressDialogHelper.updateProgress(0)

        if (filesToUpload.isEmpty()) {
            ProgressDialogHelper.updateProgress(100)
            callApi(body)
        } else {
            pendingFiles = filesToUpload
            uploadNextFile()
        }
    }

    private fun updateProgress() {
        if (totalFilesToUpload == 0) return

        val percent = ((currentIndex.toFloat() / totalFilesToUpload) * 100).toInt()
        ProgressDialogHelper.updateProgress(percent.coerceAtMost(100))
    }

    private fun isAlreadyUploaded(path: String?): Boolean {
        return path.isNullOrEmpty() || path.startsWith("http")
    }

    private fun uploadNextFile() {
        if (currentIndex >= pendingFiles.size) {
            replaceUrlsInBody(pendingBody)
            ProgressDialogHelper.updateProgress(100)
            callApi(pendingBody)
            return
        }

        val file = pendingFiles[currentIndex]

        if (file.type == "VIDEO") {
            uploadVideo(file)
        } else {
            uploadToAws(file)
        }
    }

    private fun uploadToAws(file: FilePath) {

        val fileName = File(file.url).name

        isAwsUploadingPreSigned?.getPreSignedUrl(
            file.url,
            isStaffDetails!!.school_id,
            Constant.quiz,
            this,
            SharedPreference.getCountryId(this)!!,
            false,
            object : UploadCallback {

                override fun onUploadSuccess(response: String?, isFileUploaded: String?) {

                    uploadedFiles.add(
                        AwsUploadedFiles(
                            isFileUrl = isFileUploaded!!,
                            isFileType = file.type,
                            originalFileName = fileName
                        )
                    )

                    currentIndex++
                    updateProgress()
                    uploadNextFile()
                }

                override fun onUploadError(error: String?) {
                    runOnUiThread {
                        ProgressDialogHelper.dismiss()
                        Toast.makeText(
                            this@AddQuestion, "AWS upload failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    private fun collectLocalFiles(body: QuizRequestBody): List<FilePath> {
        val list = mutableListOf<FilePath>()

        body.questions.forEach { q ->

            // Question attachments
            q.file_path.forEach { file ->
                if (!isAlreadyUploaded(file.url)) {
                    list.add(file)
                }
            }

            // Option images
            addIfLocalFilePath(q.a_image, list)
            addIfLocalFilePath(q.b_image, list)
            addIfLocalFilePath(q.c_image, list)
            addIfLocalFilePath(q.d_image, list)
        }

        return list
    }

    private fun addIfLocalFilePath(
        path: String?, list: MutableList<FilePath>
    ) {
        if (!isAlreadyUploaded(path)) {
            list.add(FilePath(path!!, "IMAGE"))
        }
    }

    private fun uploadVideo(file: FilePath) {
        currentVideoPath = file.url

        VimeoVideoUpload.uploadVideo(
            this, Constant.quiz, Constant.quiz, file.url, this
        )
    }

    private fun replaceUrlsInBody(body: QuizRequestBody) {

        body.questions.forEach { q ->

            // Question attachments
            q.file_path = q.file_path.map { file ->
                if (file.url.startsWith("http")) {
                    file
                } else {
                    val name = File(file.url).name
                    val uploaded = uploadedFiles.find { it.originalFileName == name }

                    if (uploaded != null) {
                        FilePath(uploaded.isFileUrl, uploaded.isFileType)
                    } else {
                        file
                    }
                }
            }.toMutableList()

            // Option images
            q.a_image = mapOptionImage(q.a_image)
            q.b_image = mapOptionImage(q.b_image)
            q.c_image = mapOptionImage(q.c_image)
            q.d_image = mapOptionImage(q.d_image)
        }
    }

    private fun mapOptionImage(path: String?): String? {
        if (path.isNullOrEmpty() || path.startsWith("http")) return path

        val name = File(path).name
        return uploadedFiles.find { it.originalFileName == name }?.isFileUrl ?: path
    }

    private fun callApi(body: QuizRequestBody) {
        ProgressDialogHelper.dismiss()
        val quizRequest: QuizRequestBody = body
        val mainJson = JsonObject()
        mainJson.addProperty("ok_flag", false)
        mainJson.addProperty("max_mark", quizRequest.max_mark)
        mainJson.addProperty("open_to_student", quizRequest.open_to_student)
        val updateQBankArray = JsonArray()
        quizRequest.update_question_bank.forEach { item ->
            val obj = JsonObject()
            obj.addProperty("ques_no", item.ques_no)
            obj.addProperty("subject_id", item.subject_id)
            obj.addProperty("chapter", item.chapter)
            obj.addProperty("question", item.question)
            obj.addProperty("a_option", item.a_option)
            obj.addProperty("b_option", item.b_option)
            obj.addProperty("c_option", item.c_option)
            obj.addProperty("d_option", item.d_option)
            obj.addProperty("a_image", item.a_image)
            obj.addProperty("b_image", item.b_image)
            obj.addProperty("c_image", item.c_image)
            obj.addProperty("d_image", item.d_image)
            obj.addProperty("answer", item.answer)
            obj.addProperty("mark", item.mark)

            updateQBankArray.add(obj)
        }
        mainJson.add("update_question_bank", updateQBankArray)
        val questionsArray = JsonArray()

        quizRequest.questions.forEach { q ->
            val qObj = JsonObject()
            qObj.addProperty("ques_no", q.quesNo)
            qObj.addProperty("chapter", q.chapter)
            qObj.addProperty("question", q.question)
            qObj.addProperty("a_option", q.a_option)
            qObj.addProperty("b_option", q.b_option)
            qObj.addProperty("c_option", q.c_option)
            qObj.addProperty("d_option", q.d_option)
            qObj.addProperty("answer", q.answer)
            qObj.addProperty("mark", q.mark)
            qObj.addProperty("iframe", q.iframe)
            qObj.addProperty("file_size", q.file_size)
            qObj.addProperty("thumbnail", q.thumbnail)
            qObj.addProperty("a_image", q.a_image ?: "")
            qObj.addProperty("b_image", q.b_image ?: "")
            qObj.addProperty("c_image", q.c_image ?: "")
            qObj.addProperty("d_image", q.d_image ?: "")

            // file path array
            val fileArray = JsonArray()
            q.file_path.forEach { file ->
                val fileObj = JsonObject()
                fileObj.addProperty("url", file.url)
                fileObj.addProperty("type", file.type)
                fileArray.add(fileObj)
            }
            qObj.add("q_file_path", fileArray)

            questionsArray.add(qObj)
        }

        mainJson.add("questions", questionsArray)
        mainJson.addProperty("quiz_id", quizRequest.quiz_id)
        Log.d("FINAL_JSON", mainJson.toString())
        appViewModel!!.isQuizAddQuestion(isAccessToken!!, mainJson, this)

    }

    override fun onUploadComplete(success: Boolean, iframe: String?, link: String?) {

        if (!success || link == null) {
            runOnUiThread {
                ProgressDialogHelper.dismiss()
                Toast.makeText(this, "Vimeo upload failed", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val fileName = File(currentVideoPath!!).name

        uploadedFiles.add(
            AwsUploadedFiles(
                isFileUrl = link, isFileType = "VIDEO", originalFileName = fileName
            )
        )

        currentIndex++
        updateProgress()
        uploadNextFile()
    }

    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
        }
    }

    private fun setMarqueeText(textView: TextView, message: String) {
        textView.apply {
            text = message
            visibility = View.VISIBLE
            isSelected = true // start marquee

            //  Force marquee even if text is short
            post {
                val textWidth = paint.measureText(message)
                val viewWidth = width.toFloat()

                if (textWidth <= viewWidth) {
                    // Repeat text with spaces to make it scroll continuously
                    val repeatCount = ((viewWidth / textWidth) + 8).toInt().coerceAtLeast(3)
                    val repeatedText = (message + "     ").repeat(repeatCount)
                    text = repeatedText
                }

                // Re-enable marquee indefinitely
                isSelected = true
                marqueeRepeatLimit = -1 // -1 = infinite loop
            }
        }
    }
}

