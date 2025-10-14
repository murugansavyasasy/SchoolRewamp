package com.vs.schoolmessenger.Dashboard.Fragments.Profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileField
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileItem
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.Listener.DocumentClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.M_ASSIGNMENT
import com.vs.schoolmessenger.Utils.Constant.M_ATTACHMENTS
import com.vs.schoolmessenger.Utils.Constant.M_HOMEWORK
import com.vs.schoolmessenger.Utils.Constant.M_LSRW
import com.vs.schoolmessenger.Utils.Constant.M_NOTICEBOARD
import com.vs.schoolmessenger.Utils.Constant.M_SCHOOL_CLASS_EVENTS
import com.vs.schoolmessenger.Utils.Constant.SELECTED_SCHOOL_MENU
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.ProgressDialogHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ProfileFragmentBinding
import com.vs.schoolmessenger.util.VimeoVideoUpload
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.iterator
import kotlin.text.endsWith
import kotlin.text.ifEmpty
import androidx.core.content.ContextCompat
import com.google.gson.JsonArray


class ParentProfileRewampFragment : Fragment(), View.OnClickListener, DocumentClickListener,
    OnImageClickListener, VimeoVideoUpload.UploadCompletionListener {

    private lateinit var binding: ProfileFragmentBinding
    private lateinit var appViewModel: App
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    private var originalData: List<Map<String, List<ProfileField>>> = emptyList()
    private var adapter: ProfileRewampFragmentAdapter? = null

    val isVideoSelectedArrayList = mutableListOf<FileItem>()
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
    private var cameraPermissionDeniedCount = 0

    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        internal const val CAMERA_IMAGE_REQUEST = 1004
        private const val MAX_FILES = 10
    }

    private var cameraImageFilePath: String? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var mAdapter: ProfileImagePickingAdapter? = null

    var isTotalSelectedItem = 0
    private var pendingChangedData: JsonObject? = null

    private var profilePhotoFileItem: FileItem? = null
    private var currentEditMode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = ProfileFragmentBinding.inflate(layoutInflater)
        isChildDetails = SharedPreference.getChildDetails(requireContext())
        isAccessToken = isChildDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()
        isAwsUploadingPreSigned = AwsUploadingPreSigned()
        fetchProfileData()


        binding.btnupdateprofile.setOnClickListener(this)
        binding.imgEdit.setOnClickListener(this)
//        binding.imgProfile.setOnClickListener(this)
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        appViewModel.isParentprofilelist?.observe(viewLifecycleOwner) { response ->
            Constant.hideLoading(requireActivity())
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val items = mutableListOf<ProfileItem>()
                originalData = response.data.map { section ->
                    section.mapValues { entry ->
                        entry.value.map { field ->
                            field.copy(originalValue = field.value)
                        }
                    }
                }

                var photoUrl: String? = null
                response.data.forEach { sectionMap ->
                    sectionMap.forEach { (sectionName, fields) ->
                        items.add(ProfileItem.Header(sectionName))
                        fields.forEach { field ->
                            items.add(ProfileItem.Field(field))

                            if (sectionName.equals(
                                    "PhotoPath", ignoreCase = true
                                ) && field.node.equals("photoPath", ignoreCase = true)
                            ) {
                                photoUrl = field.value
                            }
                        }
                    }
                }

                adapter =
                    ProfileRewampFragmentAdapter(items, requireContext(), this, binding.rcyImages)
                binding.recyclerview.adapter = adapter
                binding.recyclerview.visibility = View.VISIBLE
                binding.lytNoDataFound.visibility = View.GONE

                val defaultProfileRes = R.drawable.default_profile
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(photoUrl).placeholder(defaultProfileRes)
                        .error(defaultProfileRes).into(binding.imgProfile)
                } else {
                    Glide.with(this).load(defaultProfileRes).into(binding.imgProfile)
                }
            } else {
                binding.recyclerview.visibility = View.GONE
                binding.lytNoDataFound.visibility = View.VISIBLE
            }
        }

        appViewModel.ispresubmission?.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.status) {
                    showDataValidation(
                        resources.getString(R.string.success), response.message, requireActivity()
                    )
                    Constant.selectedFiles.clear()
                    isVideoSelectedArrayList.clear()
                    Constant.isAwsUploadedFiles.clear()
                    profilePhotoFileItem = null
                    binding.rcyImages.visibility = View.GONE
                    mAdapter?.notifyDataSetChanged()
                } else {
                    showDataValidation(
                        resources.getString(R.string.fail), response.message, requireActivity()
                    )
                }
                pendingChangedData = null
            }
        }

        binding.rcyImages.layoutManager = GridLayoutManager(requireContext(), 2)
        mAdapter = ProfileImagePickingAdapter(requireContext(), Constant.selectedFiles!!, this)
        binding.rcyImages.adapter = mAdapter
        binding.rcyImages.visibility = View.GONE

        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedUris =
                        result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)
                    if (currentEditMode == "profile_photo") {
                        selectedUris?.firstOrNull()?.let { uri ->
                            profilePhotoFileItem = FileItem(uri.toString(), FileType.IMAGE)
                            val defaultProfileRes = R.drawable.default_profile
                            Glide.with(this).load(uri).placeholder(defaultProfileRes)
                                .error(defaultProfileRes).into(binding.imgProfile)
                        }
                        currentEditMode = null
                    } else {
                        val remaining = MAX_FILES - Constant.selectedFiles.size

                        selectedUris?.take(remaining)?.forEach { uri ->
                            val mimeType = requireContext().contentResolver.getType(uri)
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
                                requireContext(),
                                "${getString(R.string.Only)} $remaining ${getString(R.string.files_added_max)} ${MAX_FILES})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        mAdapter?.notifyDataSetChanged()
                    }
                }
            }

        return binding.root
    }


    private fun fetchProfileData() {
        appViewModel.isParentprofilelist(isAccessToken!!)
        Constant.showLoading(requireActivity())
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnupdateprofile -> isUpdateProfile()
            R.id.imgEdit -> {
                currentEditMode = "profile_photo"
                showBottomDialog()
            }
//            R.id.imgProfile -> {
//                currentEditMode = "profile_photo"
//                showBottomDialog()
//            }
        }
    }


    private fun isUpdateProfile() {
        // Prepare changed profile fields
        val changedData = JsonObject()
        for (section in originalData) {
            for ((_, originalFields) in section) {
                for (original in originalFields) {
                    val current = adapter?.getUpdatedField(original.node)
                    if (current != null && current.value != original.originalValue) {
                        changedData.addProperty(current.node, current.value ?: "")
                    }
                }
            }
        }

        pendingChangedData = if (changedData.entrySet().isEmpty()) null else changedData
        if (pendingChangedData == null && profilePhotoFileItem == null && Constant.selectedFiles.isEmpty()) {
            showDataValidation(
                getString(R.string.fail), "No changes detected", requireActivity()
            )
            return
        }

        if (Constant.selectedFiles.isEmpty()) {
            val textPayload = JsonObject()
            pendingChangedData?.entrySet()?.forEach { entry ->
                textPayload.addProperty(entry.key, entry.value.asString)
            }
            if (profilePhotoFileItem == null) {
                Log.d("UpdatePayload", textPayload.toString())
                appViewModel.ispresubmission(isAccessToken!!, textPayload, requireActivity())
            } else {
                uploadProfilePhoto { url ->
                    ProgressDialogHelper.dismiss()
                    if (url != null) {
                        textPayload.addProperty("photoPath", url)
                    } else {
                        showDataValidation(
                            getString(R.string.fail),
                            "Profile photo upload failed",
                            requireActivity()
                        )
                        if (textPayload.entrySet().isEmpty()) return@uploadProfilePhoto
                    }
                    Log.d("UpdatePayload", textPayload.toString())
                    appViewModel.ispresubmission(isAccessToken!!, textPayload, requireActivity())
                    profilePhotoFileItem = null
                }
                ProgressDialogHelper.show(requireContext())
            }
        } else {
            ProgressDialogHelper.show(requireContext())
            isUploadFilesInServer("Documents")
        }
    }


    private fun onAllUploadsComplete() {
        val documentsArray = JsonArray()
        // Map Constant.isAwsUploadedFiles to the required document format
        Constant.isAwsUploadedFiles.forEach { file ->
            val fileName = file.originalFileName?.takeIf { it.isNotBlank() }
                ?: file.isFileUrl.substringAfterLast("/")
            val documentObject = JsonObject().apply {
                addProperty("documentName", fileName)
                addProperty("documentPath", file.isFileUrl)
                addProperty("documentDisplayName", fileName)
            }
            documentsArray.add(documentObject)
        }

        val payload = JsonObject()
        pendingChangedData?.entrySet()?.forEach { entry ->
            payload.addProperty(entry.key, entry.value.asString)
        }
        if (documentsArray.size() > 0) {
            payload.add("documents", documentsArray)
        }
        if (profilePhotoFileItem != null) {
            uploadProfilePhoto { url ->
                ProgressDialogHelper.dismiss()
                if (url != null) {
                    payload.addProperty("photoPath", url)
                } else {
                    showDataValidation(
                        getString(R.string.fail), "Profile photo upload failed", requireActivity()
                    )
                }
                if (payload.entrySet().isEmpty()) return@uploadProfilePhoto
                Log.d("UpdatePayload", payload.toString())
                appViewModel.ispresubmission(isAccessToken!!, payload, requireActivity())
                profilePhotoFileItem = null
            }
            ProgressDialogHelper.show(requireContext())
            return
        }
        ProgressDialogHelper.dismiss()
        if (payload.entrySet().isEmpty()) {
            return
        }
        Log.d("UpdatePayload", payload.toString())
        appViewModel.ispresubmission(isAccessToken!!, payload, requireActivity())
    }

    private fun uploadProfilePhoto(onComplete: (String?) -> Unit) {
        val fileItem = profilePhotoFileItem ?: run {
            onComplete(null)
            return
        }
        val outputDir = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CompressedOutput"
        )
        outputDir.mkdirs()
        Constant.compressImageFilesOnly(
            context = requireContext(),
            files = listOf(fileItem),
            outputDir = outputDir.absolutePath,
            format = Bitmap.CompressFormat.JPEG,
            quality = 80,
            maxWidth = 1280,
            maxHeight = 1280,
            onEachProcessed = { original, outputPath, success ->
                if (success && outputPath != null) {
                    val isCountryId = SharedPreference.getCountryId(requireContext())
                    isAwsUploadingPreSigned?.getPreSignedUrl(
                        outputPath,
                        isChildDetails!!.school_id,
                        "Documents",
                        requireActivity(),
                        isCountryId!!,
                        true,
                        false,
                        object : UploadCallback {
                            override fun onUploadSuccess(
                                response: String?, isFileUploaded: String?
                            ) {
                                File(outputPath).delete()
                                onComplete(isFileUploaded)
                            }

                            override fun onUploadError(error: String?) {
                                File(outputPath).delete()
                                onComplete(null)
                            }
                        })
                } else {
                    onComplete(null)
                }
            },
            onComplete = {
                // Optional for single file
            })
    }

    override fun onDocumentClicked(field: ProfileField, position: Int) {
        currentEditMode = null
        showBottomDialog()
        mAdapter?.notifyItemChanged(position)
    }

    override fun onImageClick(position: Int) {
//        if (position == 0) {
//            showBottomDialog()
//        }
    }

    private fun showBottomDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.filepick_bottom_sheet)

        val rlaGallery = dialog.findViewById<RelativeLayout>(R.id.rlaGallery)
        val rlaCamera = dialog.findViewById<RelativeLayout>(R.id.rlaCamera)
        val rlaDocument = dialog.findViewById<RelativeLayout>(R.id.rlaVideo)
        val rlaVoice = dialog.findViewById<RelativeLayout>(R.id.rlaVoice)
        val rlaVideoPick = dialog.findViewById<RelativeLayout>(R.id.rlaVideoPick)

        rlaVoice.visibility = View.GONE
        rlaVideoPick.visibility = View.GONE
        rlaGallery.visibility = View.VISIBLE
        rlaCamera.visibility = View.VISIBLE

        if (currentEditMode == "profile_photo") {
            rlaDocument.visibility = View.GONE
        }

        rlaGallery.setOnClickListener {
            val limit = if (currentEditMode == "profile_photo") 1 else 10
            Constant.isFileLimit = limit
            Log.d("Constant.isFileLimit", Constant.isFileLimit.toString())

            openAlbumSelectActivity(Constant.IMAGE)
            dialog.dismiss()
        }

        rlaVoice.setOnClickListener {
            if (currentEditMode == "profile_photo") {
                currentEditMode = null
            }
            Constant.isFileLimit = 10
            openAlbumSelectActivity(Constant.AUDIO)
            dialog.dismiss()
        }

        rlaVideoPick.setOnClickListener {
            if (currentEditMode == "profile_photo") {
                currentEditMode = null
            }
            val selectedVideoCount = Constant.selectedFiles.count { it.type == FileType.VIDEO }
            if (selectedVideoCount >= 2) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.only_2_videos_are_allowed),
                    Toast.LENGTH_SHORT
                ).show()
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
            if (currentEditMode == "profile_photo") {
                currentEditMode = null
            }
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

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraIntent()
        } else {
            if (cameraPermissionDeniedCount >= 2 && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showCameraPermissionSettingsDialog()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showCameraPermissionSettingsDialog() {
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.camera_permission_is_permanently_denied_please_enable_it_from_app_settings))
            .setCancelable(false).setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
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
            val intent = Intent(requireContext(), AlbumSelectActivity::class.java)
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
        startActivityForResult(intent, ParentProfileRewampFragment.Companion.PICK_DOCUMENT_REQUEST)
    }

    private fun openCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }

            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    requireContext(), "${requireContext().packageName}.fileprovider", photoFile
                )
                cameraImageFilePath = photoFile.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, CreateEvent.CAMERA_IMAGE_REQUEST)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.could_not_create_file_for_photo),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.no_camera_app_found), Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        val remaining =
            ParentProfileRewampFragment.Companion.MAX_FILES - Constant.selectedFiles.size
        if (remaining <= 0) {
            Toast.makeText(
                requireContext(),
                "${getString(R.string.Max)} ${ParentProfileRewampFragment.Companion.MAX_FILES} ${
                    getString(R.string.files_allowed)
                }",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        fun addPath(uri: Uri) {
            Log.d("isFilePickingUrl", uri.toString())
            if (Constant.selectedFiles.size >= ParentProfileRewampFragment.Companion.MAX_FILES) return

            val mimeType = requireContext().contentResolver.getType(uri)
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
            ParentProfileRewampFragment.Companion.CAMERA_IMAGE_REQUEST -> {
                if (currentEditMode == "profile_photo") {
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
                            profilePhotoFileItem = FileItem(uri.toString(), FileType.IMAGE)
                            val defaultProfileRes = R.drawable.default_profile
                            Glide.with(this).load(uri).placeholder(defaultProfileRes)
                                .error(defaultProfileRes).into(binding.imgProfile)
                        } else {
                            Toast.makeText(
                                requireContext(), "Camera image file not found.", Toast.LENGTH_SHORT
                            ).show()
                        }
                    } ?: run {
                        Toast.makeText(requireContext(), "Camera image failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                    currentEditMode = null
                } else {
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
                            Toast.makeText(
                                requireContext(), "Camera image file not found.", Toast.LENGTH_SHORT
                            ).show()
                        }
                    } ?: run {
                        Toast.makeText(requireContext(), "Camera image failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            ParentProfileRewampFragment.Companion.PICK_DOCUMENT_REQUEST -> {
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
        if (currentEditMode != "profile_photo") {
            mAdapter?.notifyDataSetChanged()
        }
    }

    private fun getPathFromUri(uri: Uri): String? {

        if (uri.scheme.equals(Constant.content_, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            requireContext().contentResolver.query(uri, projection, null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        return cursor.getString(columnIndex)
                    }
                }
        }

        if (uri.scheme.equals(Constant.file_, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == Constant.content_) {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
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
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: requireContext().cacheDir
        return File.createTempFile(
            "${Constant.IMG_}${timeStamp}${Constant.underscore}", ".jpg", storageDir
        )
    }

    private fun isUploadFilesInServer(isFileType: String?) {
        if (SELECTED_SCHOOL_MENU == M_ATTACHMENTS || SELECTED_SCHOOL_MENU == M_HOMEWORK || SELECTED_SCHOOL_MENU == M_SCHOOL_CLASS_EVENTS || SELECTED_SCHOOL_MENU == M_ASSIGNMENT || SELECTED_SCHOOL_MENU == M_NOTICEBOARD || SELECTED_SCHOOL_MENU == M_LSRW) {
            Constant.selectedFiles.removeAt(0)
        }
        ProgressDialogHelper.updateProgress(50)
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
            else -> onAllUploadsComplete()
        }
        ProgressDialogHelper.updateProgress(80)
    }


    private fun isFileUploadInAws(isFileType: String?) {
        Constant.isAwsUploadedFiles.clear()
        val iterator = Constant.selectedFiles.iterator()
        while (iterator.hasNext()) {
            val fileItem = iterator.next()
            if (fileItem.path.contains("amazonaws.")) {
                Constant.isAwsUploadedFiles.add(
                    AwsUploadedFiles(
                        isFileUrl = fileItem.path,
                        isFileType = fileItem.type.name,
                        originalFileName = getFileName(Uri.parse(fileItem.path)) // Store original file name
                    )
                )
                iterator.remove()
            }
        }

        val isCountryId = SharedPreference.getCountryId(requireContext())
        if (Constant.selectedFiles.isEmpty()) {
            if (isVideoSelectedArrayList.isEmpty()) {
                onAllUploadsComplete()
            } else {
                videoUploading()
            }
        } else {
            val outputDir = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "CompressedOutput"
            )
            outputDir.mkdirs()
            val newSelectedFiles = mutableListOf<FileItem>()
            Constant.compressImageFilesOnly(
                context = requireContext(),
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
                                requireContext().contentResolver.openFileDescriptor(
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
                },
                onComplete = {
                    Constant.selectedFiles.clear()
                    Constant.selectedFiles.addAll(newSelectedFiles)
                    val isAwsUploadingFile = ArrayList<String>()

                    val isSelectedFileCount = Constant.selectedFiles.size
                    for (i in Constant.selectedFiles.indices) {
                        val originalFileName =
                            getFileName(Uri.parse(Constant.selectedFiles[i].path))
                        isAwsUploadingPreSigned?.getPreSignedUrl(
                            Constant.selectedFiles[i].path,
                            isChildDetails!!.school_id,
                            isFileType!!,
                            requireActivity(),
                            isCountryId!!,
                            true,
                            false,
                            object : UploadCallback {
                                override fun onUploadSuccess(
                                    response: String?, isFileUploaded: String?
                                ) {
                                    isAwsUploadingFile.add(isFileUploaded!!)
                                    Constant.isAwsUploadedFiles.add(
                                        AwsUploadedFiles(
                                            isFileUrl = isFileUploaded,
                                            isFileType = Constant.selectedFiles[i].type.name,
                                            originalFileName = originalFileName // Store original file name
                                        )
                                    )

                                    if (isTotalSelectedItem == Constant.isAwsUploadedFiles.size) {
                                        onAllUploadsComplete()
                                    } else {
                                        if (isAwsUploadingFile.size == isSelectedFileCount) {
                                            videoUploading()
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
            for (i in isVideoSelectedArrayList.indices) {
                VimeoVideoUpload.uploadVideo(
                    requireActivity(), "quiz", "quiz", isVideoSelectedArrayList[i].path, this
                )
            }
        } else {
            onAllUploadsComplete()
        }
    }


    private fun showDataValidation(title: String, message: String, activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.success_popup, null)

        val messageText = view.findViewById<TextView>(R.id.alertMessage)
        val titleText = view.findViewById<TextView>(R.id.alertTitle)
        val okButton = view.findViewById<TextView>(R.id.btnOk)
        titleText.text = title
        messageText.text = message

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
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
            closePopup()
        }

    }

    override fun onUploadComplete(
        success: Boolean, iframe: String?, link: String?
    ) {
        requireActivity().runOnUiThread {
            Log.d("link", link.toString())
            Constant.isAwsUploadedFiles.add(
                AwsUploadedFiles(
                    isFileUrl = link.toString(), isFileType = Constant.VIDEO
                )
            )

            if (Constant.isAwsUploadedFiles.size == isTotalSelectedItem) {
                onAllUploadsComplete()
            }
        }
    }

    override fun onFailure(errorMessage: String?) {
        requireActivity().runOnUiThread {
            Log.e("VimeoUploadError", errorMessage ?: "Unknown error")
        }
    }
}