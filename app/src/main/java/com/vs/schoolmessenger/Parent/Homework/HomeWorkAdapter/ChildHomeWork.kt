package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.CommonScreens.ImagePickingAdapter
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.RecipientActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.LSRW.AudioAdapter
import com.vs.schoolmessenger.Parent.LSRW.Model.LsrwSubmitSkillDataClass
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Assignment.AssignmentStudentList
import com.vs.schoolmessenger.School.Assignment.StudentListFragment
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.LSRW.LsrwStudentListFragment
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_LSRW
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_NEEDS
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ChildHomeworkActivityBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.endsWith
import kotlin.text.ifEmpty

class ChildHomeWork : BaseActivity<ChildHomeworkActivityBinding>(), View.OnClickListener,
    OnImageClickListener {
    override fun getViewBinding(): ChildHomeworkActivityBinding {
        return ChildHomeworkActivityBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    var isHomeworkId = ""
    var isHomeWorkDate: String? = ""
    private var appViewModel: App? = null

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
    private var cameraPermissionDeniedCount = 0

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ImagePickingAdapter? = null

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        internal const val CAMERA_IMAGE_REQUEST = 1004
        private const val MAX_FILES = 10
    }
    private var data: FilePreview? = null

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.childlsrwlayoutxml.imgBack.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
        binding.childlsrwlayoutxml.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.lblClickComplete.setOnClickListener(this)
        data = intent.getParcelableExtra("isPreViewData")
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        val childDetails = SharedPreference.getChildDetails(this)
        isAccessToken = childDetails?.access_token



        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblParentToolBar.text = childDetails!!.name
        binding.toolbarLayout.lblSchoolName.text = childDetails!!.school_name

        binding.lbltitle.text = data!!.title
        binding.lblDescription.text = data!!.description

        if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT && data!!.isParentAssignment == false) {
            binding.lblviewSubmissions.visibility = View.GONE
            binding.linearlayoutContainer.visibility = View.VISIBLE
            binding.createdDate.text = Constant.convertDateFormat(data?.created_date ?: "")
            binding.category.text = data?.category ?: ""
            binding.subject.text = data?.assignmentsubject ?: ""
            binding.fragmentContainer.visibility = View.VISIBLE
            loadFragment(
                StudentListFragment.newInstance(
                    data!!.assignmentid ?: "", "TOTAL", data!!.submittedCount ?: 0, data!!.totalCount ?: 0,data!!.created_date ?: ""
                )
            )
        } else if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT && data!!.isParentAssignment == true) {
            binding.lblviewSubmissions.visibility = View.GONE
            binding.linearlayoutContainer.visibility = View.VISIBLE
            binding.createdDate.text = Constant.convertDateFormat(data?.created_date ?: "")
            binding.category.text = data?.category ?: ""
            binding.subject.text = data?.assignmentsubject ?: ""
            binding.fragmentContainer.visibility = View.GONE

        } else if (SELECTED_SCHOOL_MENU == M_SCHOOL_NEEDS) {
            binding.toolbarLayout.imgBack.visibility = View.GONE
            binding.scrollView.visibility = View.GONE
            binding.childlsrwlayoutxml.root.visibility = View.VISIBLE
            binding.childlsrwlayoutxml.txtTitle.text = data!!.title
            binding.childlsrwlayoutxml.txtSubTitle.text = data!!.assignmentid
            binding.childlsrwlayoutxml.txtDescription.text = data!!.description
            binding.childlsrwlayoutxml.txtDate.text = Constant.convertDateFormat(data?.created_date ?: "")
            Log.d("FragmentCheck", "Loading LsrwStudentListFragment with ID: ${data!!.id}")
            subloadFragment(
                LsrwStudentListFragment.newInstance(
                    data!!.id ?: ""
                )
            )
            Log.d("FragmentCheck", "LsrwStudentListFragment should now be loaded")

        } else if (SELECTED_SCHOOL_MENU == M_LSRW) {


            if (data!!.assignmentid == "Listening") {
                binding.descriptionLabel.visibility = View.GONE
                binding.editDescription.visibility = View.GONE
                binding.rytRecyclewview.visibility = View.GONE
                binding.rcyImages.visibility = View.GONE
                binding.btnSubmit.visibility = View.GONE
            } else if (data!!.assignmentid == "Reading") {
                binding.descriptionLabel.visibility = View.GONE
                binding.editDescription.visibility = View.GONE
                binding.rytRecyclewview.visibility = View.GONE
                binding.rcyImages.visibility = View.GONE
                binding.btnSubmit.visibility = View.GONE
            } else {
                binding.descriptionLabel.visibility = View.VISIBLE
                binding.editDescription.visibility = View.VISIBLE
                binding.rytRecyclewview.visibility = View.VISIBLE
                binding.rcyImages.visibility = View.VISIBLE
                binding.btnSubmit.visibility = View.VISIBLE
            }
            saveDrawableToCache(R.drawable.add_image)?.let {
                Constant.selectedFiles.add(
                    FileItem(
                        it, FileType.IMAGE
                    )
                )
            }
            mAdapter = ImagePickingAdapter(this, Constant.selectedFiles!!, this)
            binding.rcyImages.layoutManager = GridLayoutManager(this, 3)
            binding.rcyImages.adapter = mAdapter
            albumResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        val selectedUris =
                            result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)
                        val remaining =
                            ChildHomeWork.Companion.MAX_FILES - Constant.selectedFiles.size

                        selectedUris?.take(remaining)?.forEach { uri ->
                            val mimeType = contentResolver.getType(uri)
                            val path = when (uri.scheme) {
                                "file" -> uri.path
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
                                    ".docx", true
                                ) -> FileType.DOC

                                fileName.endsWith(".xls", true) || fileName.endsWith(
                                    ".xlsx", true
                                ) -> FileType.EXCEL

                                fileName.endsWith(".ppt", true) || fileName.endsWith(
                                    ".pptx", true
                                ) -> FileType.PPT

                                fileName.endsWith(".txt", true) -> FileType.TXT
                                else -> FileType.OTHER
                            }

                            Constant.selectedFiles.add(FileItem(uri.toString(), type))

                            Log.d("SelectedFile", "URI: $uri, Type: $type")
                        }

                        if ((selectedUris?.size ?: 0) > remaining) {
                            Toast.makeText(
                                this,
                                "Only $remaining files added (max ${ChildHomeWork.Companion.MAX_FILES})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            val audioList = data!!.fileList.filter { it.type.equals(Constant.M4A, ignoreCase = true) }
                .map { it.url }
            if (audioList.isNotEmpty()) {
                binding.rcSeekBarAndTitle.visibility = View.VISIBLE
                val audioAdapter = AudioAdapter(audioList)
                binding.rcSeekBarAndTitle.layoutManager = LinearLayoutManager(binding.root.context)
                binding.rcSeekBarAndTitle.adapter = audioAdapter
            } else {
                binding.rcSeekBarAndTitle.visibility = View.GONE
            }
        } else {
            binding.lblviewSubmissions.visibility = View.GONE
            binding.linearlayoutContainer.visibility = View.GONE
            binding.fragmentContainer.visibility = View.GONE
        }


        binding.lblviewSubmissions.setOnClickListener(this)

        binding.lblviewSubmissions.setOnClickListener {
            val intent = Intent(this, AssignmentStudentList::class.java)
            intent.putExtra("assignment_id", data!!.assignmentid)
            intent.putExtra("submitted_count", data!!.submittedCount)
            Log.d("submitted_count", data!!.submittedCount.toString())
            intent.putExtra("Total_Count", data!!.totalCount)
            Log.d("Total_Count", data!!.totalCount.toString())
            intent.putExtra("type", "TOTAL")
            startActivity(intent)
        }

        if (data!!.isMenuType == Constant.M_HOMEWORK) {
            isHomeworkId = data!!.id
            isHomeWorkDate = intent.getStringExtra("isHomeWorkDate")
            Log.d("isHomeWorkDate2", isHomeWorkDate.toString())

            if (data!!.subjectName != "") {
                binding.lblSubjectName.visibility = View.VISIBLE
                binding.lblSubjectName.text = data!!.subjectName
            }
            if (!data!!.isCompleted) {
                binding.lblClickComplete.visibility = View.VISIBLE
                binding.lblClickComplete.text = "Click \"here\" when you're done "
                binding.thumbContainer.visibility = View.VISIBLE
            } else {
                binding.lblClickComplete.visibility = View.GONE
                binding.thumbContainer.visibility = View.GONE
            }
            if (isHomeWorkDate != "") {
                binding.lblPostedDate.visibility = View.VISIBLE
                binding.lblPostedDate.text =
                    "Posted on : " + Constant.formatDateSmart(isHomeWorkDate.toString())
            }
            if (data!!.sentBy != "") {
                binding.lblPostedBy.visibility = View.VISIBLE
                binding.lblPostedBy.text = "Posted by : " + data!!.sentBy
            }
        } else if (data!!.isMenuType == Constant.M_NOTICEBOARD || data!!.isMenuType == Constant.M_PARENT_CLASS_EVENTS || data!!.isMenuType == Constant.M_SCHOOL_CLASS_EVENTS) {
            binding.lblSubjectName.visibility = View.GONE
            binding.lblClickComplete.visibility = View.GONE
            binding.lblPostedDate.visibility = View.GONE
            binding.lblPostedBy.visibility = View.GONE
        }

        for (i in data!!.fileList.indices) {
            Log.d("isComingFilePath", data!!.fileList[i].url)
        }

        val adapter = HomeWorkChildAdapter(
            this, data!!.fileList, data!!.subjectName!!, SELECTED_SCHOOL_MENU
        )
        if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT) {
            binding.rcChildHW.layoutManager =
                GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
            binding.rcChildHW.adapter = adapter
        } else if (SELECTED_SCHOOL_MENU == M_SCHOOL_NEEDS) {
            binding.childlsrwlayoutxml.rcChildHW.layoutManager =
                GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
            binding.childlsrwlayoutxml.rcChildHW.adapter = adapter
        } else if (SELECTED_SCHOOL_MENU == M_LSRW) {
            binding.rcChildHW.layoutManager =
                GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
            binding.rcChildHW.adapter = adapter
        } else {
            binding.rcChildHW.layoutManager =
                GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
            binding.rcChildHW.adapter = adapter

        }

        appViewModel?.isHomeWorkComplete?.observe(this) { response ->
            if (response!!.status) {
                isSuccessFullCompleteHomework()
            }
        }
        if (adapter.itemCount == 0) {
            if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT) {
                val params = binding.lblPostedBy.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = binding.lblClickComplete.id
                params.topMargin = resources.getDimensionPixelSize(R.dimen.ten)
                binding.lblPostedBy.layoutParams = params
                binding.childlsrwlayoutxml.rcChildHW.visibility = View.GONE
                binding.childlsrwlayoutxml.lblAttachments.visibility = View.GONE
                binding.childlsrwlayoutxml.imgAttachmentIcon.visibility = View.GONE
            } else {
                val params = binding.lblPostedBy.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = binding.lblClickComplete.id
                params.topMargin = resources.getDimensionPixelSize(R.dimen.ten)
                binding.lblPostedBy.layoutParams = params
                binding.rcChildHW.visibility = View.GONE
                binding.lblAttachments.visibility = View.GONE
                binding.imgAttachmentIcon.visibility = View.GONE
            }
        } else {
            if (SELECTED_SCHOOL_MENU == M_ASSIGNMENT) {
                val params = binding.lblPostedBy.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = binding.childlsrwlayoutxml.rcChildHW.id
                params.topMargin = resources.getDimensionPixelSize(R.dimen.ten)
                binding.lblPostedBy.layoutParams = params

                binding.childlsrwlayoutxml.rcChildHW.visibility = View.VISIBLE
                binding.childlsrwlayoutxml.lblAttachments.visibility = View.VISIBLE
                binding.childlsrwlayoutxml.imgAttachmentIcon.visibility = View.VISIBLE
            } else {
                val params = binding.lblPostedBy.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = binding.rcChildHW.id
                params.topMargin = resources.getDimensionPixelSize(R.dimen.ten)
                binding.lblPostedBy.layoutParams = params
                binding.rcChildHW.visibility = View.VISIBLE
                binding.lblAttachments.visibility = View.VISIBLE
                binding.imgAttachmentIcon.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lblClickComplete -> {
                isCompleteHomeWork()
            }

            R.id.btnSubmit -> {
                LsrwSubmitSkill()
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
        builder.setMessage("That's it! Homework done you're amazing")
        builder.setTitle("Well done!")
        builder.setCancelable(false)
        builder.setPositiveButton("Ok") { dialog, which ->
            finish()
        }
        val alertDialog = builder.create()
        alertDialog.show()

    }


    private fun LsrwSubmitSkill() {
        val description = binding.editDescription.text.toString().trim()
        if (description.isEmpty()) {
            binding.editDescription.error = getString(R.string.This_field_required)
            binding.editDescription.requestFocus()
            return
        }
        val isLsrwSubmitSkill = LsrwSubmitSkillDataClass(
            description
        )
        val intent = Intent(this, RecipientActivity::class.java)
        intent.putExtra(Constant.lsrwsubmitskill_data, isLsrwSubmitSkill)
        startActivity(intent)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    private fun subloadFragment(fragment: Fragment) {
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragmentContainer)
        supportFragmentManager.beginTransaction().replace(fragmentContainer.id, fragment).commit()

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
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCameraPermissionSettingsDialog() {
        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("Camera permission is permanently denied. Please enable it from app settings.")
            .setCancelable(false).setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }.setNegativeButton("Cancel") { dialog, _ ->
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

    private fun openSystemDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, Constant.mimeTypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, ChildHomeWork.Companion.PICK_DOCUMENT_REQUEST)
    }


    override fun onBackPressed() {
        Constant.selectedFiles.clear()
        Constant.isAwsUploadedFiles.clear()
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    override fun onImageClick(position: Int) {
        if (position == 0) {
            showBottomDialog()
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

        rlaVideoPick.setOnClickListener {
            val selectedVideoCount = Constant.selectedFiles.count { it.type == FileType.VIDEO }
            if (selectedVideoCount >= 2) {
                Toast.makeText(this, "Only 2 videos are allowed", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Could not create file for photo", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        val remaining = ChildHomeWork.Companion.MAX_FILES - Constant.selectedFiles.size
        if (remaining <= 0) {
            Toast.makeText(
                this, "Max ${ChildHomeWork.Companion.MAX_FILES} files allowed", Toast.LENGTH_SHORT
            ).show()
            return
        }

        fun addPath(uri: Uri) {
            Log.d("isFilePickingUrl", uri.toString())
            if (Constant.selectedFiles.size >= ChildHomeWork.Companion.MAX_FILES) return

            val mimeType = contentResolver.getType(uri)
            if (mimeType?.startsWith("video/") == true || mimeType?.startsWith("audio/") == true) {
                Log.d("SkipFile", "Skipping audio/video file: $uri (MIME: $mimeType)")
                return
            }

            val fileName = getFileName(uri)
            val type = when {
                fileName.endsWith(".pdf", true) -> FileType.PDF
                fileName.endsWith(".doc", true) || fileName.endsWith(".docx", true) -> FileType.DOC
                fileName.endsWith(".xls", true) || fileName.endsWith(
                    ".xlsx", true
                ) -> FileType.EXCEL

                fileName.endsWith(".ppt", true) || fileName.endsWith(".pptx", true) -> FileType.PPT
                fileName.matches(".*\\.(jpg|jpeg|png|webp)$".toRegex(RegexOption.IGNORE_CASE)) -> FileType.IMAGE
                fileName.endsWith(".txt", true) -> FileType.TXT
                else -> FileType.OTHER
            }

            Constant.selectedFiles.add(FileItem(uri.toString(), type))
            for (item in Constant.selectedFiles) {
                Log.d("SelectedFile", "Path: ${item.path}, Type: ${item.type}")
            }
        }

        when (requestCode) {
            CreateEvent.Companion.CAMERA_IMAGE_REQUEST -> {
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
                        addPath(uri)
                    } else {
                        Toast.makeText(this, "Camera image file not found.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } ?: run {
                    Toast.makeText(this, "Camera image failed", Toast.LENGTH_SHORT).show()
                }
            }

            ChildHomeWork.Companion.PICK_DOCUMENT_REQUEST -> {
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

    private fun getPathFromUri(uri: Uri): String? {
        // Content scheme
        if (uri.scheme.equals("content", ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    return cursor.getString(columnIndex)
                }
            }
        }

        // File scheme fallback
        if (uri.scheme.equals("file", ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
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
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }


}