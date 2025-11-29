package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.LSRW.AudioAdapter
import com.vs.schoolmessenger.Parent.LSRW.LSRW
import com.vs.schoolmessenger.Parent.LSRW.MySubmissionView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.ApiCallRequest.islsrwSkillSubmit
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.AssignmentTargetDetails.AssignmentChildStandardAdapter
import com.vs.schoolmessenger.School.Assignment.StudentListFragment
import com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard.ChildStandardAdapter
import com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard.SchoolNameTarget
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.LSRW.Adapter.LSRWImagePickingAdapter
import com.vs.schoolmessenger.School.LSRW.CreateNewTask
import com.vs.schoolmessenger.School.LSRW.LsrwStudentListFragment
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_LSRW
import com.vs.schoolmessenger.Utils.Constant.SELECTED_MENU_ID
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.isCommunicationType
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ChildHomeworkActivityBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

class ChildHomeWork : BaseActivity<ChildHomeworkActivityBinding>(), View.OnClickListener,
    OnImageClickListener, VimeoVideoUpload.UploadCompletionListener {
    override fun getViewBinding(): ChildHomeworkActivityBinding {
        return ChildHomeworkActivityBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    var isHomeworkId = ""
    var isHomeWorkDate: String? = ""
    private var appViewModel: App? = null
    var isIframe = ""
    var isFileSize = ""
    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
    private var cameraPermissionDeniedCount = 0

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: LSRWImagePickingAdapter? = null

    private var audioAdapter: AudioAdapter? = null

    lateinit var childstandardadapter: ChildStandardAdapter
    lateinit var assignmentchildstandardAdapter: AssignmentChildStandardAdapter


    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        internal const val CAMERA_IMAGE_REQUEST = 1004
        private const val MAX_FILES = 10
    }


    private var selectedSkill: String = Constant.Listening
    private lateinit var tabList: List<LinearLayout>
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFilePath: String? = null
    private var isRecording = false
    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 201
    private var audioPermissionDeniedCount = 0

    private var data: FilePreview? = null

    private var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null
    private var isChildDetails: ChildDetails? = null
    private var isStaffDetails: StaffDetails? = null
    var userDetails: UserDetails? = null

    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isTotalSelectedItem = 0
    private var dummyPath: String? = null


    private fun updateRemainingCount() {
        val usedSlots = Constant.selectedFiles.size - 1
        Constant.Remaining = (CreateNewTask.Companion.MAX_FILES - usedSlots).coerceAtLeast(0)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )
        Constant.Remaining = MAX_FILES

        binding.childlsrwlayoutxml.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.toolbarLayout.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
        binding.btnSubmit.setOnClickListener(this)

        Log.d("isSchoolMenuName", Constant.isSelectedMenuName)
        data = intent.getParcelableExtra("isPreViewData")

        binding.toolbarLayout.lblStudentName.visibility = View.VISIBLE
        binding.toolbarLayout.lblStudentSection.visibility = View.GONE
        binding.toolbarLayout.lblStudentName.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblSubjectName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSubjectName.text = data!!.subjectName

        binding.childlsrwlayoutxml.btnSubmit.setOnClickListener {
            LsrwSubmitSkill()
        }

        binding.lblClickComplete.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        isChildDetails = SharedPreference.getChildDetails(this)
        isStaffDetails = SharedPreference.getStaffDetails(this)
        userDetails = SharedPreference.getUserDetails(this)


        if (Constant.isParentChoose) {
            isAccessToken = isChildDetails?.access_token
        } else {
            if (userDetails!!.staff_role.equals(Constant.isStaffRole)) {
                isAccessToken = isStaffDetails!!.access_token
            } else {
                isAccessToken = userDetails!!.staff_details[0].access_token
            }
        }

        Log.d("isAccessToken", isAccessToken!!)
        isAwsUploadingPreSigned = AwsUploadingPreSigned()

        binding.lbltitle.text = data!!.title
        binding.lblDescription.text = data!!.description


        if (SELECTED_MENU_ID == Constant.M_SCHOOL_CLASS_EVENTS) {
            binding.sendtostandardLabel.visibility = View.VISIBLE
            loadEventChildHomewordStandard()
        } else {
            binding.sendtostandardLabel.visibility = View.GONE
            Log.d("", "")
        }


        if (SELECTED_MENU_ID == Constant.M_ATTACHMENTS && data!!.isParentAssignment == false) {
            binding.sendtostandardLabel.visibility = View.VISIBLE
            loadattachmentsChildHomeWorkstandard()
        } else {
            binding.sendtostandardLabel.visibility = View.GONE
        }


        if(SELECTED_MENU_ID == Constant.M_ASSIGNMENT && data!!.my_submissionadapter == true) {
            binding.toolbarLayout.lblSubjectName.visibility = View.GONE

            binding.toolbarLayout.lblPostedOn.visibility = View.VISIBLE
            val params =
                binding.toolbarLayout.rlaStudentName.layoutParams as RelativeLayout.LayoutParams
            params.removeRule(RelativeLayout.START_OF)
            params.addRule(
                RelativeLayout.START_OF, R.id.lblPostedOn
            )
            binding.toolbarLayout.rlaStudentName.layoutParams = params
            binding.toolbarLayout.lblPostedOn.text =
                "${getString(R.string.posted_on)} : ${Constant.formatDatepostedby(data!!.created_date.toString())}"
            Log.d("Posted On isStudentlistdetail", data!!.created_date.toString())

            if (data!!.sentBy != "") {
                binding.lblPostedBy.visibility = View.VISIBLE
                binding.lblPostedBy.text = "${getString(R.string.posted_by)} : " + data!!.sentBy
            }
        }

        if (SELECTED_MENU_ID == M_ASSIGNMENT && data!!.isParentAssignment == false) {
            binding.sendtostandardLabel.visibility = View.VISIBLE
            loadAssignemntChildHomewordStandard()
        } else {
            binding.sendtostandardLabel.visibility = View.GONE
            Log.d("", "")
        }


        if (SELECTED_MENU_ID == Constant.M_ASSIGNMENT && data!!.isStudentlistdetail == true) {
            binding.toolbarLayout.lblPostedOn.visibility = View.VISIBLE
            val params =
                binding.toolbarLayout.rlaStudentName.layoutParams as RelativeLayout.LayoutParams
            params.removeRule(RelativeLayout.START_OF)
            params.addRule(
                RelativeLayout.START_OF, R.id.lblPostedOn
            )
            binding.toolbarLayout.rlaStudentName.layoutParams = params
            binding.toolbarLayout.lblPostedOn.text =
                "${getString(R.string.posted_on)} : ${Constant.formatDatepostedby(data!!.created_date.toString())}"
            Log.d("Posted On isStudentlistdetail", data!!.created_date.toString())

        } else {
            Log.d("Posted On isStudentlistdetail", data!!.created_date.toString())
        }





        if (SELECTED_MENU_ID == M_ASSIGNMENT && data!!.isParentAssignment == false) {
            binding.toolbarLayout.lblStudentName.visibility = View.VISIBLE
            binding.toolbarLayout.lblStudentSection.visibility = View.GONE
            binding.toolbarLayout.lblStudentName.text = Constant.isSelectedMenuName
            binding.lblviewSubmissions.visibility = View.GONE
            binding.linearlayoutContainer.visibility = View.GONE
            binding.createdDate.text = Constant.convertToReadableDate(data?.created_date ?: "")
            Log.d("createddatevalue", data?.created_date ?: "")
            binding.category.text = data?.category ?: ""
            binding.subject.text = data?.assignmentsubject ?: ""
            binding.fragmentContainer.visibility = View.VISIBLE
            loadFragment(
                StudentListFragment.newInstance(
                    data!!.assignmentid ?: "",
                    data!!.title ?: "",
                    data!!.assignmentsubject ?: "",
                    "TOTAL",
                    data!!.submittedCount ?: 0,
                    data!!.totalCount ?: 0,
                    data!!.created_date ?: ""
                )
            )
        } else if (SELECTED_MENU_ID == M_ASSIGNMENT && data!!.isParentAssignment == true) {
            binding.lblviewSubmissions.visibility = View.GONE
            binding.linearlayoutContainer.visibility = View.GONE
            binding.createdDate.text = Constant.convertToReadableDate(data?.created_date ?: "")
            binding.category.text = data?.category ?: ""
            binding.subject.text = data?.assignmentsubject ?: ""
            binding.fragmentContainer.visibility = View.GONE

        } else if (SELECTED_MENU_ID == M_LSRW && data!!.isParentAssignment == false) {
            binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.GONE
            if (data!!.assignmentid == "Listening") {
                binding.childlsrwlayoutxml.imgIcon.setImageResource(R.drawable.headphonesvgformat)
            } else if (data!!.assignmentid == "Speaking") {
                binding.childlsrwlayoutxml.imgIcon.setImageResource(R.drawable.micsvgformatstyle)
            } else if (data!!.assignmentid == "Reading") {
                binding.childlsrwlayoutxml.imgIcon.setImageResource(R.drawable.booksvg_formatstyle)
            } else if (data!!.assignmentid == "Writing") {
                binding.childlsrwlayoutxml.imgIcon.setImageResource(R.drawable.pensvgformatstyle)
            } else {
                binding.childlsrwlayoutxml.imgIcon.setImageResource(R.drawable.questionmark)
            }
            binding.childlsrwlayoutxml.toolbarLayout.lblParentToolBar.text = getString(R.string.lsrw)
            binding.childlsrwlayoutxml.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
            binding.childlsrwlayoutxml.toolbarLayout.lblSchoolName.text =
                getString(R.string.listening_speaking_reading_writing)

            binding.toolbarLayout.imgBack.visibility = View.VISIBLE
            binding.scrollView.visibility = View.GONE
            binding.childlsrwlayoutxml.root.visibility = View.VISIBLE
            binding.childlsrwlayoutxml.txtTitle.text = data!!.subjectName
            binding.childlsrwlayoutxml.txtSubTitle.text = data!!.assignmentid
            binding.childlsrwlayoutxml.txtDescription.text = data!!.title
            binding.childlsrwlayoutxml.txtDescription1.text = data!!.description
            binding.childlsrwlayoutxml.lsrwgragmentcontainer.visibility = View.VISIBLE
            binding.childlsrwlayoutxml.txtDate.text = getFormattedDateText(data?.created_date ?: "")
            Log.d("FragmentCheck", "Loading LsrwStudentListFragment with ID: ${data!!.id}")
            subloadFragment(
                LsrwStudentListFragment.newInstance(
                    data!!.id ?: ""
                )
            )
            Log.d("FragmentCheck", "LsrwStudentListFragment should now be loaded")

            val audioList =
                data!!.fileList.filter { it.type.equals(Constant.AUDIO, ignoreCase = true) }
                    .map { it.url }
            if (audioList.isNotEmpty()) {
                binding.childlsrwlayoutxml.rcSeekBarAndTitle.visibility = View.VISIBLE
                audioAdapter = AudioAdapter(audioList)
                binding.childlsrwlayoutxml.rcSeekBarAndTitle.layoutManager =
                    LinearLayoutManager(binding.root.context)
                binding.childlsrwlayoutxml.rcSeekBarAndTitle.adapter = audioAdapter
            } else {
                binding.childlsrwlayoutxml.rcSeekBarAndTitle.visibility = View.GONE
            }

        } else if (SELECTED_MENU_ID == M_LSRW && data!!.isParentAssignment == true) {
            binding.childlsrwlayoutxml.toolbarLayout.lblParentToolBar.text = getString(R.string.lsrw)
            binding.childlsrwlayoutxml.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
            binding.childlsrwlayoutxml.toolbarLayout.lblSchoolName.text =getString(R.string.listening_speaking_reading_writing)
            binding.toolbarLayout.imgBack.visibility = View.GONE
            binding.scrollView.visibility = View.GONE
            binding.childlsrwlayoutxml.footerLabel.visibility = View.GONE
            binding.childlsrwlayoutxml.headerLabel.visibility = View.GONE
            binding.childlsrwlayoutxml.root.visibility = View.VISIBLE
            binding.childlsrwlayoutxml.txtTitle.text = data!!.subjectName
            binding.childlsrwlayoutxml.txtSubTitle.text = data!!.assignmentid
            binding.childlsrwlayoutxml.txtDescription.text = data!!.title
            binding.childlsrwlayoutxml.txtDescription1.text = data!!.description
            binding.childlsrwlayoutxml.txtDate.text = getFormattedDateText(data?.sentBy ?: "")
            if (data!!.assignmentid == "Listening") {
                if (data!!.is_submitted == true) {
                    binding.childlsrwlayoutxml.descriptionLabel.visibility = View.GONE
                    binding.childlsrwlayoutxml.editDescription.visibility = View.GONE
                    binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.GONE
                    binding.childlsrwlayoutxml.rcyImages.visibility = View.GONE
                    binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.btnSubmit.visibility = View.GONE
                } else {
                    binding.childlsrwlayoutxml.descriptionLabel.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.attachmentLabel.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.editDescription.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.rcyImages.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.btnSubmit.visibility = View.VISIBLE
                }
            } else if (data!!.assignmentid == "Reading") {
                if (data!!.is_submitted == true) {
                    binding.childlsrwlayoutxml.descriptionLabel.visibility = View.GONE
                    binding.childlsrwlayoutxml.editDescription.visibility = View.GONE
                    binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.GONE
                    binding.childlsrwlayoutxml.rcyImages.visibility = View.GONE
                    binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.btnSubmit.visibility = View.GONE
                } else {
                    binding.childlsrwlayoutxml.descriptionLabel.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.attachmentLabel.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.editDescription.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.rcyImages.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.btnSubmit.visibility = View.VISIBLE
                }
            } else {
                if (data!!.is_submitted == true) {
                    binding.childlsrwlayoutxml.descriptionLabel.visibility = View.GONE
                    binding.childlsrwlayoutxml.editDescription.visibility = View.GONE
                    binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.GONE
                    binding.childlsrwlayoutxml.rcyImages.visibility = View.GONE
                    binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.btnSubmit.visibility = View.GONE
                } else {
                    binding.childlsrwlayoutxml.descriptionLabel.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.attachmentLabel.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.editDescription.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.rcyImages.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.btnSubmit.visibility = View.VISIBLE
                }
            }
            Constant.selectedFiles.clear()
            dummyPath = saveDrawableToCache(R.drawable.attachment_with_bg)
            dummyPath?.let {
                Constant.selectedFiles.add(
                    FileItem(
                        it, FileType.IMAGE
                    )
                )
            }

            mAdapter = LSRWImagePickingAdapter(this, Constant.selectedFiles!!, this)
            binding.childlsrwlayoutxml.rcyImages.layoutManager = GridLayoutManager(this, 1)
            binding.childlsrwlayoutxml.rcyImages.adapter = mAdapter

            updateRemainingCount()


            albumResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        val selectedUris = result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)
                        if (selectedUris.isNullOrEmpty()) return@registerForActivityResult

                        var addedCount = 0
                        selectedUris.forEach { uri ->
                            if (Constant.selectedFiles.size >= CreateNewTask.Companion.MAX_FILES + 1) {
                                Toast.makeText(this, getString(R.string.max_10_files_allowed), Toast.LENGTH_SHORT).show()
                                return@forEach
                            }

                            val mimeType = contentResolver.getType(uri)
                            val path = when (uri.scheme) {
                                Constant.file_ -> uri.path
                                else -> getPathFromUri(uri)
                            } ?: run {
                                Log.w("addPath", "Could not resolve path from URI: $uri")
                                return@forEach
                            }

                            val fileName = getFileName(uri).ifEmpty { File(path).name }
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

                            if (type == FileType.AUDIO) {
                                lifecycleScope.launch {
                                    val wavFile = Constant.convertToWav(this@ChildHomeWork, uri)
                                    if (wavFile != null) {
                                        Constant.selectedFiles.add(FileItem(wavFile.absolutePath, FileType.AUDIO))
                                        mAdapter?.notifyDataSetChanged()
                                        updateRemainingCount()
                                    } else {
                                        Toast.makeText(this@ChildHomeWork, "Audio convert failed!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                return@forEach
                            }


                            Constant.selectedFiles.add(FileItem(uri.toString(), type))
                            addedCount++
                        }

                        if (addedCount > 0) {
                            mAdapter!!.notifyDataSetChanged()
                            updateRemainingCount()

                            if (addedCount < selectedUris.size) {
                                Toast.makeText(this, getString(R.string.only_x_files_added, Constant.Remaining), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            val audioList = data!!.fileList
                .filter { it.type.equals(Constant.AUDIO, ignoreCase = true) }
                .map { it.url }

            data!!.fileList.forEach {
                Log.d("IncomingType", "Value: ${it.type}")
            }

            if (audioList.isNotEmpty()) {
                binding.childlsrwlayoutxml.rcSeekBarAndTitle.visibility = View.VISIBLE
                 audioAdapter = AudioAdapter(audioList)
                binding.childlsrwlayoutxml.rcSeekBarAndTitle.layoutManager =
                    LinearLayoutManager(binding.root.context)
                binding.childlsrwlayoutxml.rcSeekBarAndTitle.adapter = audioAdapter
            } else {
                binding.childlsrwlayoutxml.rcSeekBarAndTitle.visibility = View.GONE
            }
        } else {
            binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.GONE
        }



        if (SELECTED_MENU_ID == M_LSRW && data!!.isParentAssignment == true) {
            if (data!!.is_submitted == true) {
                binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.VISIBLE
                Log.d("lblviewSubmissions Visible success", "lblviewSubmissions Visible success")
            } else {
                binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.GONE
                Log.d("lblviewSubmissions Visible failed", "lblviewSubmissions Visible failed")
            }

        } else if (SELECTED_MENU_ID == M_LSRW && data!!.isParentAssignment == false) {
            binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.GONE
            Log.d("lblviewSubmissions Visible failed", "lblviewSubmissions Visible failed")
        } else {
            binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.GONE
            Log.d("lblviewSubmissions Visible failed", "lblviewSubmissions Visible failed")
        }

        binding.childlsrwlayoutxml.lblviewSubmissions.setOnClickListener(this)

        binding.childlsrwlayoutxml.lblviewSubmissions.setOnClickListener {
            val intent = Intent(this, MySubmissionView::class.java)
            intent.putExtra(Constant.id_, data!!.id)
            startActivity(intent)
        }


        if (data!!.isMenuType == Constant.M_HOMEWORK) {
            isHomeworkId = data!!.id
            isHomeWorkDate = intent.getStringExtra("isHomeWorkDate")

            binding.toolbarLayout.lblPostedOn.visibility = View.VISIBLE
            val params =
                binding.toolbarLayout.rlaStudentName.layoutParams as RelativeLayout.LayoutParams// Assuming you already have view binding set up
            params.removeRule(RelativeLayout.START_OF) // Remove the old rule pointing to imgSearchToolBar
            params.addRule(
                RelativeLayout.START_OF, R.id.lblPostedOn
            ) // Add a new rule pointing to lblPostedOn
            binding.toolbarLayout.rlaStudentName.layoutParams =
                params // Apply the updated layout params
            binding.toolbarLayout.lblPostedOn.text =
                "${getString(R.string.posted_on)} : ${Constant.formatDatepostedby(data!!.created_date.toString())}"


            if (!data!!.isCompleted) {
                binding.lblClickComplete.visibility = View.VISIBLE
                binding.lblClickComplete.text = getString(R.string.click_here_when_you_re_done)
                binding.thumbContainer.visibility = View.VISIBLE
            } else {
                binding.lblClickComplete.visibility = View.GONE
                binding.thumbContainer.visibility = View.GONE
            }

            if (data!!.sentBy != "") {
                binding.lblPostedBy.visibility = View.VISIBLE
                binding.lblPostedBy.text = "${getString(R.string.posted_by)} : " + data!!.sentBy
            }
        } else if (data!!.isMenuType == Constant.M_NOTICEBOARD || data!!.isMenuType == Constant.M_PARENT_CLASS_EVENTS || data!!.isMenuType == Constant.M_SCHOOL_CLASS_EVENTS || data!!.isMenuType == Constant.M_ATTACHMENTS) {

            if (data!!.created_date.isNullOrBlank()) {
                binding.toolbarLayout.lblPostedOn.visibility = View.GONE
            } else {
                binding.toolbarLayout.lblPostedOn.visibility = View.VISIBLE
            }

            val params =
                binding.toolbarLayout.rlaStudentName.layoutParams as RelativeLayout.LayoutParams// Assuming you already have view binding set up
            params.removeRule(RelativeLayout.START_OF) // Remove the old rule pointing to imgSearchToolBar
            params.addRule(
                RelativeLayout.START_OF, R.id.lblPostedOn
            ) // Add a new rule pointing to lblPostedOn
            binding.toolbarLayout.rlaStudentName.layoutParams =
                params // Apply the updated layout params
            Log.d("data!!.created_date", data!!.created_date.toString())
            binding.toolbarLayout.lblPostedOn.text =
                "${getString(R.string.posted_on)} : ${Constant.formatDatepostedby(data!!.created_date.toString())}"

            binding.lblClickComplete.visibility = View.GONE
            binding.toolbarLayout.lblStudentName.visibility = View.VISIBLE
            binding.toolbarLayout.lblStudentSection.visibility = View.GONE
            binding.toolbarLayout.lblStudentName.text = Constant.isSelectedMenuName
            if (data!!.sentBy != "") {
                binding.lblPostedBy.visibility = View.VISIBLE
                binding.lblPostedBy.text = "${getString(R.string.posted_by)} : " + data!!.sentBy
            }
        }

        for (i in data!!.fileList.indices) {
            Log.d("isComingFilePath", data!!.fileList[i].url)
        }

        val isParentAssignment =
            (SELECTED_MENU_ID == M_LSRW && data?.isParentAssignment == true)


        Log.d("Child Homework Redirection", SELECTED_MENU_ID.toString())
        Log.d("Child Homework Redirection", isParentAssignment.toString())

        // Filter out AUDIO files before passing to HomeWorkChildAdapter (AUDIO handled separately via AudioAdapter)
        val filteredFileList = data!!.fileList.filter { it.type.uppercase() != Constant.AUDIO }

        val adapter = HomeWorkChildAdapter(
            this, filteredFileList, data!!.subjectName!!, SELECTED_MENU_ID, isParentAssignment
        )

        val recyclerView = if (SELECTED_MENU_ID == M_LSRW) {
            binding.childlsrwlayoutxml.rcChildHW
        } else {
            binding.rcChildHW
        }

        Log.d("ParentAssignmentValue", isParentAssignment.toString())

        val spanCount = when {
            SELECTED_MENU_ID == M_ASSIGNMENT -> 2
            SELECTED_MENU_ID == M_LSRW && !isParentAssignment -> 2
            SELECTED_MENU_ID == M_LSRW && isParentAssignment -> 2
            else -> 3
        }

        recyclerView.layoutManager =
            GridLayoutManager(this, spanCount, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        appViewModel?.isHomeWorkComplete?.observe(this) { response ->
            if (response?.status == true) isSuccessFullCompleteHomework()
        }


        appViewModel?.islsrwSkillSubmit?.observe(this) { response ->
            Constant.hideLoading(this@ChildHomeWork)
            binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.VISIBLE
            response?.let {
                Log.d("Response", it.status.toString())
                if (it.status) {
                    binding.childlsrwlayoutxml.descriptionLabel.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.editDescription.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.attachmentLabel.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.btnSubmit.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.lblviewSubmissions.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.rcyImages.visibility = View.VISIBLE
                    binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.VISIBLE

                    val submittedFiles = Constant.isAwsUploadedFiles.map { aws ->
                        FileItem(aws.isFileUrl, FileType.valueOf(aws.isFileType))
                    }

                    if (SELECTED_MENU_ID == M_LSRW && data!!.isParentAssignment == true) {
                        Constant.selectedFiles.removeAll { it.path == dummyPath }
                    }

                    Constant.selectedFiles.clear()
                    Constant.selectedFiles.addAll(submittedFiles)
                    mAdapter?.notifyDataSetChanged()

                    Constant.isAwsUploadedFiles.clear()
                    isVideoSelectedArrayList.clear()
                }
                showTopAlertParentPopup(it.message, this)
            }
        }

        appViewModel!!.getchildhomeworkstandard?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    response.data?.let { dataList ->
                        Constant.isShimmerViewDisable = false

                        val flattenedTargetList = dataList.flatMap { dataItem ->
                            dataItem.name?.flatMap { nameObj ->
                                when {
                                    !nameObj.standardList.isNullOrEmpty() ->
                                        nameObj.standardList.map { std ->
                                            SchoolNameTarget(name = std)
                                        }

                                    !nameObj.institute.isNullOrEmpty() ->
                                        nameObj.institute.map { inst ->
                                            SchoolNameTarget(name = inst)
                                        }

                                    !nameObj.group.isNullOrEmpty() ->
                                        nameObj.group.map { grp ->
                                            SchoolNameTarget(name = grp)
                                        }

                                    !nameObj.sectionList.isNullOrEmpty() ->
                                        nameObj.sectionList.map { sec ->
                                            SchoolNameTarget(name = sec)
                                        }

                                    else -> emptyList()
                                }
                            } ?: emptyList()
                        }

                        val typelist = dataList.mapNotNull { it.type }
                        childstandardadapter = ChildStandardAdapter(
                            flattenedTargetList,
                            this,
                            Constant.isShimmerViewDisable
                        )
                        binding.rcystandard.adapter = childstandardadapter
                        binding.sendtostandardLabel.visibility = View.VISIBLE
                        binding.standardValue.text = "\uD83C\uDF93 ${typelist.joinToString(", ")}"
                    }

                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    binding.sendtostandardLabel.visibility = View.GONE
                }
            }
        }

        appViewModel!!.getattachmentchildhomework?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    response.data?.let { dataList ->
                        Constant.isShimmerViewDisable = false

                        val flattenedTargetList = dataList.flatMap { dataItem ->
                            dataItem.name?.flatMap { nameObj ->
                                when {
                                    !nameObj.standardList.isNullOrEmpty() ->
                                        nameObj.standardList.map { std ->
                                            SchoolNameTarget(name = std)
                                        }

                                    !nameObj.institute.isNullOrEmpty() ->
                                        nameObj.institute.map { inst ->
                                            SchoolNameTarget(name = inst)
                                        }

                                    !nameObj.group.isNullOrEmpty() ->
                                        nameObj.group.map { grp ->
                                            SchoolNameTarget(name = grp)
                                        }

                                    !nameObj.sectionList.isNullOrEmpty() ->
                                        nameObj.sectionList.map { sec ->
                                            SchoolNameTarget(name = sec)
                                        }

                                    else -> emptyList()
                                }
                            } ?: emptyList()
                        }

                        val typelist = dataList.mapNotNull { it.type }
                        childstandardadapter = ChildStandardAdapter(
                            flattenedTargetList,
                            this,
                            Constant.isShimmerViewDisable
                        )
                        binding.rcystandard.adapter = childstandardadapter
                        binding.sendtostandardLabel.visibility = View.VISIBLE
                        binding.standardValue.text = "\uD83C\uDF93 ${typelist.joinToString(", ")}"
                    }

                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    binding.sendtostandardLabel.visibility = View.GONE
                }
            }
        }




        appViewModel!!.getassignmentchildhomework?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    response.data?.let { dataList ->
                        Constant.isShimmerViewDisable = false

                        val flattenedTargetList = dataList.flatMap { dataItem ->
                            dataItem.name?.flatMap { nameObj ->
                                when {
                                    !nameObj.standardList.isNullOrEmpty() ->
                                        nameObj.standardList.map { std ->
                                            SchoolNameTarget(name = std)
                                        }

                                    !nameObj.institute.isNullOrEmpty() ->
                                        nameObj.institute.map { inst ->
                                            SchoolNameTarget(name = inst)
                                        }

                                    !nameObj.group.isNullOrEmpty() ->
                                        nameObj.group.map { grp ->
                                            SchoolNameTarget(name = grp)
                                        }

                                    !nameObj.sectionList.isNullOrEmpty() ->
                                        nameObj.sectionList.map { sec ->
                                            SchoolNameTarget(name = sec)
                                        }

                                    else -> emptyList()
                                }
                            } ?: emptyList()
                        }

                        val typelist = dataList.mapNotNull { it.type }
                        childstandardadapter = ChildStandardAdapter(
                            flattenedTargetList,
                            this,
                            Constant.isShimmerViewDisable
                        )
                        binding.rcystandard.adapter = childstandardadapter
                        binding.sendtostandardLabel.visibility = View.VISIBLE
                        binding.standardValue.text = "\uD83C\uDF93 ${typelist.joinToString(", ")}"
                    }

                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    binding.sendtostandardLabel.visibility = View.GONE
                }
            }
        }


        val isEmpty = adapter.itemCount == 0
        val isAssignment = SELECTED_MENU_ID == M_ASSIGNMENT
        val isLsrw = SELECTED_MENU_ID == M_LSRW

        val params = binding.lblPostedBy.layoutParams as ConstraintLayout.LayoutParams
        if (isEmpty) {
            params.topToBottom = binding.lblClickComplete.id
            params.topMargin = resources.getDimensionPixelSize(R.dimen.ten)
        } else {
            params.topToBottom = recyclerView.id
            params.topMargin = resources.getDimensionPixelSize(R.dimen.ten)
        }
        binding.lblPostedBy.layoutParams = params

        when {
            isAssignment && data!!.isParentAssignment == true -> {
                binding.rcChildHW.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.lblAttachments.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.imgAttachmentIcon.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }

            isAssignment && data!!.isParentAssignment == false -> {
                binding.rcChildHW.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.lblAttachments.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.imgAttachmentIcon.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }

            isLsrw -> {
                binding.childlsrwlayoutxml.rcChildHW.visibility =
                    if (isEmpty) View.GONE else View.VISIBLE
                binding.childlsrwlayoutxml.lblAttachments.visibility =
                    if (isEmpty) View.GONE else View.VISIBLE
                binding.childlsrwlayoutxml.imgAttachmentIcon.visibility =
                    if (isEmpty) View.GONE else View.VISIBLE
            }

            else -> {
                recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.lblAttachments.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.attachmentsContainer.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.imgAttachmentIcon.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
        }

    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.lblClickComplete -> {
                isCompleteHomeWork()
            }

        }
    }

    fun isCompleteHomeWork() {
        binding.lottieView.visibility = View.VISIBLE
        binding.imgThumbsUp.visibility = View.GONE
        binding.lottieView.playAnimation()
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", isHomeworkId)
        appViewModel?.isHomeWorkComplete(isAccessToken!!, jsonObject)
    }

    fun isSuccessFullCompleteHomework() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.that_s_it_homework_done_you_re_amazing))
        builder.setTitle(getString(R.string.Well_done))
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.OK_2)) { dialog, which ->
            Constant.isCompletedHomeworkId = isHomeworkId
            finish()
        }
        val alertDialog = builder.create()
        alertDialog.show()

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFormattedDateText(dateString: String): String {
        if (dateString.isBlank()) return ""

        try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val date = LocalDate.parse(dateString, formatter)
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            return when {
                date == today -> getString(R.string.today)
                date == yesterday -> getString(R.string.yesterday)
                else -> Constant.convertToReadableDate(dateString)
            }
        } catch (e: DateTimeParseException) {
            Log.e("DateParsing", "Invalid date format: $dateString", e)
            return Constant.convertToReadableDate(dateString)
        }
    }


    private fun LsrwSubmitSkill() {
        binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.VISIBLE
        val description = binding.childlsrwlayoutxml.editDescription.text.toString().trim()
        val file_size = calculateFileSize()
        if (description.isEmpty()) {
            binding.childlsrwlayoutxml.editDescription.error = getString(R.string.Description_required)
            binding.childlsrwlayoutxml.editDescription.requestFocus()
            return
        }
        val totalSizeKB = file_size.split(" ")[0].toIntOrNull() ?: 0
        if (totalSizeKB <= 0) {
            Toast.makeText(this,
                getString(R.string.at_least_one_attachment_is_required), Toast.LENGTH_SHORT).show()
            return
        }
        Constant.showLoading(this@ChildHomeWork)
        isUploadFilesInServer("Documents")
    }


    fun isUploadFilesInServer(isFileType: String?) {
        Log.d("ChildHomeWork", "Starting file upload, total: ${Constant.selectedFiles.size}")
        if (SELECTED_MENU_ID == M_LSRW && data!!.isParentAssignment == true) {
            Constant.selectedFiles.removeAt(0)
        }
        isTotalSelectedItem = Constant.selectedFiles.size
        isVideoSelectedArrayList.clear()
        Constant.isAwsUploadedFiles.clear()
        val iterator = Constant.selectedFiles.iterator()
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.type == FileType.VIDEO) {
                isVideoSelectedArrayList.add(file)
                iterator.remove()
            }
        }

        when {
            Constant.selectedFiles.isNotEmpty() -> isFileUploadInAws(isFileType)
            isVideoSelectedArrayList.isNotEmpty() -> videoUploading()
        }
    }


    private fun isFileUploadInAws(
        isFileType: String?
    ) {
        val allNonVideos = Constant.selectedFiles.toList()
        val imagesToCompress = allNonVideos.filter { it.type == FileType.IMAGE }
        val nonImages = allNonVideos.filter { it.type != FileType.IMAGE }
        val newSelectedFiles = mutableListOf<FileItem>()
        val outputDir = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput"
        )
        outputDir.mkdirs()
        Constant.compressImageFilesOnly(
            context = this,
            files = imagesToCompress,
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
                    newSelectedFiles.add(original)
                    Log.e("Compressor", "Failed: ${original.path}")
                }
            },
            onComplete = {
                val updatedFiles = newSelectedFiles + nonImages
                Constant.selectedFiles.clear()
                Constant.selectedFiles.addAll(updatedFiles)
                val nonVideoCount = Constant.selectedFiles.size
                var awsCompleted = 0
                val isCountryId = SharedPreference.getCountryId(this)
                for (i in Constant.selectedFiles.indices) {
                    isAwsUploadingPreSigned?.getPreSignedUrl(
                        Constant.selectedFiles[i].path,
                        isChildDetails!!.school_id,
                        isFileType!!,
                        this,
                        isCountryId!!,
                        false,
                        object : UploadCallback {

                            override fun onUploadSuccess(
                                response: String?, isFileUploaded: String?
                            ) {
                                Constant.isAwsUploadedFiles.add(
                                    AwsUploadedFiles(
                                        isFileUrl = isFileUploaded!!,
                                        isFileType = Constant.selectedFiles[i].type.name
                                    )
                                )

                                awsCompleted++
                                if (awsCompleted == nonVideoCount) {
                                    if (isVideoSelectedArrayList.isEmpty()) {
                                        onAllUploadsComplete()
                                    } else {
                                        videoUploading()
                                    }
                                }
                            }

                            override fun onUploadError(error: String?) {
                                Log.d("isUploadIssue", error.toString())
                                awsCompleted++
                                if (awsCompleted == nonVideoCount) {
                                    if (isVideoSelectedArrayList.isEmpty()) {
                                        onAllUploadsComplete()
                                    } else {
                                        videoUploading()
                                    }
                                }
                            }
                        })
                }

                Log.d("Compressor", "All files compressed and uploaded.")
            })
    }


    private fun videoUploading() {
        val iterator = isVideoSelectedArrayList.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("player.vimeo.com")) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path, isFileType = fileItem.type.name
                    )
                )
                iterator.remove()
            }
        }
        Log.d("isVideoSelectedArrayList", isVideoSelectedArrayList.size.toString())
        if (isVideoSelectedArrayList.isNotEmpty()) {
            var videoCompleted = 0
            val videoCount = isVideoSelectedArrayList.size
            for (i in isVideoSelectedArrayList.indices) {
                VimeoVideoUpload.uploadVideo(
                    this, "lsrw", "lsrw", isVideoSelectedArrayList[i].path, this
                )
            }
        } else {
            onAllUploadsComplete()
        }
    }


    private fun onAllUploadsComplete() {
        val description = binding.childlsrwlayoutxml.editDescription.text.toString().trim()
        val file_size = calculateFileSize()
        val jsonObject = islsrwSkillSubmit(
            file_size = file_size,
            iframe = isIframe,
            id = data!!.id,
            thumbnail = "",
            description = description
        )
        appViewModel!!.islsrwSkillSubmit(isAccessToken!!, jsonObject, this)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    private fun subloadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.lsrwgragmentcontainer, fragment)
            .commit()
    }


    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraIntent()
        } else {

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


    private fun checkRecordPermissionAndStartRecording() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceRecording()
        } else {
            if (audioPermissionDeniedCount >= 2 && !ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECORD_AUDIO
                )
            ) {
                showAudioPermissionSettingsDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_REQUEST_CODE
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
                    Toast.makeText(this, getString(R.string.camera_permission_is_required), Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecording()
            } else {
                audioPermissionDeniedCount++
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.RECORD_AUDIO
                    )
                ) {
                    showAudioPermissionSettingsDialog()
                } else {
                    Toast.makeText(this, getString(R.string.microphone_permission_is_required), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun loadEventChildHomewordStandard() {
        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.rcystandard.layoutManager = flexboxLayoutManager
        childstandardadapter = ChildStandardAdapter(emptyList(), this, true)
        binding.rcystandard.adapter = childstandardadapter
        appViewModel!!.getchildhomeworkstandard(isAccessToken!!, data!!.id.toInt())
    }


    private fun loadattachmentsChildHomeWorkstandard() {
        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.rcystandard.layoutManager = flexboxLayoutManager
        childstandardadapter = ChildStandardAdapter(emptyList(), this, true)
        binding.rcystandard.adapter = childstandardadapter
        appViewModel!!.getattachmentchildhomework(
            isAccessToken!!, data!!.id.toInt(), data!!.target_type!!.toInt()
        )
    }


    private fun loadAssignemntChildHomewordStandard() {
        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.rcystandard.layoutManager = flexboxLayoutManager
        childstandardadapter = ChildStandardAdapter(emptyList(), this, true)
        binding.rcystandard.adapter = childstandardadapter
        appViewModel!!.getassignmentchildhomework(
            isAccessToken!!, data!!.id.toInt(), data!!.target_type!!.toInt()
        )
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

    private fun showAudioPermissionSettingsDialog() {
        AlertDialog.Builder(this).setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.microphone_permission_is_permanently_denied_please_enable_it_from_app_settings))
            .setCancelable(false).setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }.setNegativeButton(getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun startVoiceRecording() {
        if (isRecording) return

        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir("recordings") ?: cacheDir
        val audioFile: File = try {
            File.createTempFile("AUDIO_${timeStamp}_", Constant.wav, storageDir)
        } catch (ex: IOException) {
            ex.printStackTrace()
            Toast.makeText(
                this,
                getString(R.string.could_not_create_file_for_audio),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        recordingFilePath = audioFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordingFilePath)
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: Exception) {
                e.printStackTrace()
                releaseRecorder()
                Toast.makeText(
                    this@ChildHomeWork,
                    getString(R.string.failed_to_start_recording),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // Replace your old dialog code with this:
        val dialogView = layoutInflater.inflate(R.layout.dialog_voice_recording, null)
        val stopBtn = dialogView.findViewById<TextView>(R.id.btnStop)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        stopBtn.setOnClickListener {
            stopVoiceRecording()
            dialog.dismiss()
        }
    }


    private fun stopVoiceRecording() {
        if (!isRecording) return
        isRecording = false
        try {
            mediaRecorder?.stop()
        } catch (e: RuntimeException) { }
        mediaRecorder?.release()
        mediaRecorder = null

        recordingFilePath?.let { path ->
            val file = File(path)
            if (file.exists() && file.length() > 0) {
                if (Constant.selectedFiles.size < CreateNewTask.Companion.MAX_FILES + 1) {
                    Constant.selectedFiles.add(FileItem(path, FileType.AUDIO))
                    mAdapter?.notifyDataSetChanged()
                    updateRemainingCount()
                    Toast.makeText(this, getString(R.string.audio_recorded_and_added), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.max_10_files_allowed), Toast.LENGTH_SHORT).show()
                    file.delete()
                }
            } else {
                Toast.makeText(this, getString(R.string.recording_failed_file_empty), Toast.LENGTH_SHORT).show()
                file.delete()
            }
        }
        recordingFilePath = null
    }

    private fun releaseRecorder() {
        if (isRecording) {
            stopVoiceRecording()
        } else {
            mediaRecorder?.release()
            mediaRecorder = null
        }
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

    private fun openSystemDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, Constant.mimeTypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, ChildHomeWork.Companion.PICK_DOCUMENT_REQUEST)
    }


    private fun calculateFileSize(): String {
        var totalSize = 0L
        Constant.selectedFiles.filter { it.path != dummyPath }.forEach { fileItem ->
            val size = getFileSize(fileItem.path)  // Pass path directly as String
            Log.d("FileSizeDebug", "File: ${fileItem.path}, Size: $size bytes")
            totalSize += size
        }
        val sizeInKB = totalSize / 1024
        Log.d("FileSizeDebug", "Total Size: $sizeInKB KB")
        return "$sizeInKB KB"
    }

    private fun getFileSize(path: String): Long {
        return try {
            val uri = Uri.parse(path)
            when (uri.scheme) {
                "content" -> {
                    contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                        afd.length
                    } ?: 0L
                }
                "file" -> File(uri.path ?: path).length()
                null -> File(path).length()
                else -> 0L
            }
        } catch (e: Exception) {
            Log.e("getFileSize", "Error getting size for $path", e)
            0L
        }
    }


    override fun onBackPressed() {
        audioAdapter?.release()
        if (isRecording) {
            stopVoiceRecording()
        }
//        Constant.selectedFiles.clear()
        Constant.isAwsUploadedFiles.clear()
        isVideoSelectedArrayList.clear()
        Constant.Remaining = MAX_FILES
        updateRemainingCount()
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopVoiceRecording()
        }

        Constant.stopDelay()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseRecorder()

    }

    override fun onImageClick(position: Int) {
        if (position == 0) {
            showBottomDialog()
        }
    }

    override fun onUploadComplete(
        success: Boolean, iframe: String?, link: String?
    ) {
        runOnUiThread {
            var videoCompleted = 0
            if (success && link != null) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = link, isFileType = "VIDEO"
                    )
                )
                if (!iframe.isNullOrEmpty()) {
                    isIframe = iframe
                }
            }
            videoCompleted++
            val videoCount = isVideoSelectedArrayList.size
            if (videoCompleted == videoCount) {
                onAllUploadsComplete()
            }
        }
    }

    override fun onFailure(errorMessage: String?) {
        runOnUiThread {
            var videoCompleted = 0
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
            videoCompleted++
            val videoCount = isVideoSelectedArrayList.size
            if (videoCompleted == videoCount) {
                onAllUploadsComplete()
            }
        }
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
        val rlavoicerecorder = dialog.findViewById<RelativeLayout>(R.id.rlavoicerecorder)

        rlaVoice.visibility = View.VISIBLE
        rlavoicerecorder.visibility = View.VISIBLE

        rlaGallery.setOnClickListener {
            Constant.isFileLimit = 10
            Log.d("Constant.isFileLimit", Constant.isFileLimit.toString())

            openAlbumSelectActivity(Constant.IMAGE)
            dialog.dismiss()
        }

        rlaVoice.setOnClickListener {
            Constant.isFileLimit = 10
            openAlbumSelectActivity(Constant.AUDIO)
            dialog.dismiss()
        }

        rlavoicerecorder.setOnClickListener {
            Constant.isFileLimit = 10
            openVoiceRecorder()
            dialog.dismiss()
        }

        rlaVideoPick.setOnClickListener {
            val selectedVideoCount = Constant.selectedFiles.count { it.type == FileType.VIDEO }
            if (selectedVideoCount >= 2) {
                Toast.makeText(this, getString(R.string.only_2_videos_are_allowed), Toast.LENGTH_SHORT).show()
            } else {
                if (Constant.selectedFiles.size == 1 || selectedVideoCount == 0) {
                    Constant.isFileLimit = 2
                } else if (selectedVideoCount == 1) {
                    Constant.isFileLimit = 1
                }
                openAlbumSelectActivity(Constant.VIDEO)
                dialog.dismiss()
            }
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


    private fun openVoiceRecorder() {
        checkRecordPermissionAndStartRecording()
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
                startActivityForResult(intent, CreateEvent.Companion.CAMERA_IMAGE_REQUEST)
            } else {
                Toast.makeText(this, getString(R.string.could_not_create_file_for_photo), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.no_camera_app_found), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || Constant.selectedFiles.size >= CreateNewTask.Companion.MAX_FILES + 1) {
            if (Constant.selectedFiles.size >= CreateNewTask.Companion.MAX_FILES + 1) {
                Toast.makeText(this, getString(R.string.max_10_files_allowed), Toast.LENGTH_SHORT).show()
            }
            return
        }

        fun addFile(uri: Uri) {
            if (Constant.selectedFiles.size >= CreateNewTask.Companion.MAX_FILES + 1) return

            val mimeType = contentResolver.getType(uri)
            if (mimeType?.startsWith("video/") == true || mimeType?.startsWith("audio/") == true) return

            val fileName = getFileName(uri)
            val type = when {
                fileName.endsWith(".pdf", true) -> FileType.PDF
                fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> FileType.DOC
                fileName.endsWith(".xls", true) || fileName.endsWith(".xlsx", true) -> FileType.EXCEL
                fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT
                fileName.matches(".*\\.(jpg|jpeg|png|webp)$".toRegex(RegexOption.IGNORE_CASE)) -> FileType.IMAGE
                fileName.endsWith(".txt", true) -> FileType.TXT
                else -> FileType.OTHER
            }

            Constant.selectedFiles.add(FileItem(uri.toString(), type))
        }

        when (requestCode) {
            CreateNewTask.Companion.CAMERA_IMAGE_REQUEST -> {
                cameraImageFilePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val finalFile = if (!file.name.endsWith(".jpg", true)) {
                            val newFile = File(file.parent, file.nameWithoutExtension + ".jpg")
                            file.renameTo(newFile)
                            newFile
                        } else file
                        addFile(Uri.fromFile(finalFile))
                    }
                }
            }
            ChildHomeWork.Companion.PICK_DOCUMENT_REQUEST -> {
                data?.clipData?.let { clip ->
                    for (i in 0 until clip.itemCount) {
                        addFile(clip.getItemAt(i).uri)
                    }
                } ?: data?.data?.let { addFile(it) }
            }
        }

        mAdapter?.notifyDataSetChanged()
        updateRemainingCount()
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
        return File.createTempFile("${Constant.IMG_}${timeStamp}${Constant.underscore}", ".jpg", storageDir)
    }


    private fun showTopAlertParentPopup(message: String, activity: Activity) {
        binding.childlsrwlayoutxml.rytRecyclewview.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true // prevent clicks on background
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, activity.resources.displayMetrics
        ).toInt()

        val popupLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(view, popupLayoutParams)

        val closePopup = {
            rootView.removeView(view)
            rootView.removeView(dimView)
        }

        okButton.setOnClickListener {
            isAwsUploadedFiles.clear()
            selectedFiles.clear()
            isCommunicationType = 1
            val intent = Intent(activity, LSRW::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true
    }


}