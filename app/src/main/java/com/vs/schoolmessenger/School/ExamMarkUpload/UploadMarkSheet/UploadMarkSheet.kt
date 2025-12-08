package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.AWS.AwsUploadingPreSigned
import com.vs.schoolmessenger.AWS.UploadCallback
import com.vs.schoolmessenger.AlbumImage.AlbumSelectActivity
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.MapActivity
import com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.ReviewAndEditMarks
import com.vs.schoolmessenger.Utils.AwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.UploadMarkSheetBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UploadMarkSheet : BaseActivity<UploadMarkSheetBinding>(), View.OnClickListener {

    override fun getViewBinding(): UploadMarkSheetBinding {
        return UploadMarkSheetBinding.inflate(layoutInflater)
    }

    private lateinit var albumResultLauncher: ActivityResultLauncher<Intent>
    private var appViewModel: App? = null
    private var cameraPermissionDeniedCount = 0
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var cameraImageFilePath: String? = null
    var isTotalSelectedItem = 0
    var isAwsUploadingPreSigned: AwsUploadingPreSigned? = null


    companion object {
        private const val PICK_DOCUMENT_REQUEST = 1003
        private const val PICK_IMAGE_REQUEST = 1001
        private const val CAMERA_IMAGE_REQUEST = 1004
    }

    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private var MAX_FILES = 10


    override fun setupViews() {
        super.setupViews()
        Constant.Remaining = MAX_FILES


        isToolBarPrimarySchool(
            mainViewId = R.id.main, statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.cardUploadImage.setOnClickListener(this)
        binding.cardManual.setOnClickListener(this)
        binding.lnrUploadFile.setOnClickListener(this)
        binding.lnrUpload.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName


        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        isAwsUploadingPreSigned = AwsUploadingPreSigned()

        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        setBulletText(binding.lblIns1, getString(R.string.student_names_and_roll_numbers))
        setBulletText(binding.lblIns2, getString(R.string.subject_columns_and_marks))
        setBulletText(binding.lblIns3, getString(R.string.table_structure_and_layout))


        albumResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedUris =
                        result.data?.getParcelableArrayListExtra<Uri>(Constant.isSelectedFiles)

                    Log.d("Constant.Remaining", Constant.Remaining.toString())

                    if (Constant.Remaining > 0 && !selectedUris.isNullOrEmpty()) {

                        val previousCount = Constant.selectedFiles.size
                        Constant.Remaining -= selectedUris.size
                        selectedUris.forEach { uri ->
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

                            Log.d("fileName", fileName)

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

                            Log.d("MAX_FILES", MAX_FILES.toString())

                            if (Constant.selectedFiles.size < MAX_FILES + 1) {
                                Constant.selectedFiles.add(FileItem(uri.toString(), type))
                            } else {
                                Constant.Remaining = 0
                            }

                            if (Constant.selectedFiles.size > 0) {
                                binding.lblFileName.text = fileName.toString()
                                binding.lnrUpload.visibility = View.VISIBLE
                            } else {
                                binding.lblFileName.text =
                                    getString(R.string.click_to_upload_or_drag_and_drop)
                                binding.lnrUpload.visibility = View.GONE

                            }
                            Log.d("SelectedFile", "URI: $uri, Type: $type")
                        }
//                        mAdapter?.notifyDataSetChanged()
                        val addedCount = Constant.selectedFiles.size - previousCount
                        val totalCount = Constant.selectedFiles.size

//                        Toast.makeText(
//                            this,
//                            "${getString(R.string.Added)} $addedCount ${getString(R.string.file)}${
//                                if (addedCount > 1) "${
//                                    getString(
//                                        R.string.s_
//                                    )
//                                }" else ""
//                            }",
//                            Toast.LENGTH_SHORT
//                        ).show()

                        Log.d("FinalSelectedFiles", "Total: $totalCount, Added: $addedCount")
                    } else if (Constant.Remaining <= 0) {
//                        Toast.makeText(
//                            this,
//                            getString(R.string.you_have_reached_the_maximum_file_limit),
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }


    }

    fun setBulletText(textView: TextView, text: String) {
        val fullText = "• $text"
        val spannable = SpannableString(fullText)

        // Get colors from resources
        val bulletColor = ContextCompat.getColor(textView.context, R.color.dark_bg_orange_2)
        val textColor = ContextCompat.getColor(textView.context, R.color.black)

        // Make bullet (•) red
        spannable.setSpan(
            ForegroundColorSpan(bulletColor), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Remaining text custom color
        spannable.setSpan(
            ForegroundColorSpan(textColor), 2, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }

    fun AppCompatActivity.dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), this.resources.displayMetrics
        ).toInt()
    }


    private fun isFileUploadInAws(
        isFileType: String?
    ) {
        Constant.isAwsUploadedFiles.clear()
        isTotalSelectedItem = Constant.selectedFiles.size
        val isCountryId = SharedPreference.getCountryId(this)
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
            },
            onComplete = {
                Constant.selectedFiles.clear()
                Constant.selectedFiles.addAll(newSelectedFiles)
                val isAwsUploadingFile = ArrayList<String>()
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
                                Log.d("UploadSuccess", isFileUploaded.toString())
                                isAwsUploadingFile.add(isFileUploaded!!)
                                Constant.isAwsUploadedFiles.add(
                                    AwsUploadedFiles(
                                        isFileUrl = isFileUploaded,
                                        isFileType = Constant.selectedFiles[i].type.name
                                    )
                                )

                                if (isTotalSelectedItem == Constant.isAwsUploadedFiles.size) {
                                    Log.d(
                                        "UploadSuccess",
                                        Constant.isAwsUploadedFiles.get(0).isFileUrl
                                    )
                                    // need to do a api call
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

    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.filepick_bottom_sheet)

        val rlaGallery = dialog.findViewById<RelativeLayout>(R.id.rlaGallery)
        val rlaCamera = dialog.findViewById<RelativeLayout>(R.id.rlaCamera)
        val rlaDocument = dialog.findViewById<RelativeLayout>(R.id.rlaVideo)
        val rlaVoice = dialog.findViewById<RelativeLayout>(R.id.rlaVoice)
        val rlaVideoPick = dialog.findViewById<RelativeLayout>(R.id.rlaVideoPick)

        rlaDocument.visibility = View.GONE
        rlaVoice.visibility = View.GONE
        rlaVideoPick.visibility = View.GONE

        rlaGallery.setOnClickListener {
            Constant.selectedFiles.clear()
            Constant.isFileLimit = 1
            openAlbumSelectActivity(Constant.IMAGE)
            dialog.dismiss()
        }

        rlaCamera.setOnClickListener {
            Constant.selectedFiles.clear()
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

    private fun openAlbumSelectActivity(isFileType: String) {
        Log.d("FileComing", isFileType)
        val sdkInt = Build.VERSION.SDK_INT
        if (isFileType == Constant.DOCUMENT && sdkInt < Build.VERSION_CODES.R) {
            openSystemDocumentPicker()
        } else {
            val intent = Intent(this, AlbumSelectActivity::class.java)
            intent.putExtra(Constant.isFileType, isFileType)
            intent.putExtra("isWithOutHotCodeImage", true)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("cameraImageFilePath", cameraImageFilePath)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        cameraImageFilePath = savedInstanceState.getString("cameraImageFilePath")
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
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    photoFile
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat(Constant.yyyyMMdd_HHmmss, Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: cacheDir
        return File.createTempFile(
            "${Constant.IMG_}${timeStamp}${Constant.underscore}", ".jpg", storageDir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        if (Constant.Remaining!! == 0) {
            Toast.makeText(
                this,
                "${getString(R.string.Max)} ${MAX_FILES} ${getString(R.string.files_allowed)}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        fun addPath(uri: Uri) {
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

            if (Constant.selectedFiles.size < MAX_FILES + 1) {
                Constant.selectedFiles.add(FileItem(uri.toString(), type))
            } else {
                Constant.Remaining = 0
            }

            if (Constant.selectedFiles.isNotEmpty()) {
                binding.lblFileName.text = fileName
                binding.lnrUpload.visibility = View.VISIBLE

            } else {
                binding.lblFileName.text = getString(R.string.click_to_upload_or_drag_and_drop)
                binding.lnrUpload.visibility = View.GONE
            }

            for (item in Constant.selectedFiles) {
                Log.d("SelectedFile", "Path: ${item.path}, Type: ${item.type}")
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
                    Constant.Remaining = Constant.Remaining - clipData.itemCount

                } else if (singleUri != null) {
                    addPath(singleUri)
                    Constant.Remaining = Constant.Remaining - 1

                }
            }
        }
//        mAdapter?.notifyDataSetChanged()
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
                        this,
                        Manifest.permission.CAMERA
                    )
                ) {
                    showCameraPermissionSettingsDialog()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.camera_permission_is_required),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        Constant.selectedFiles.clear()
        Constant.isAwsUploadedFiles.clear()
        Constant.Remaining = MAX_FILES
        super.onBackPressed()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.lnrUpload -> {
//                isFileUploadInAws("Image")
                val intent = Intent(this, MapActivity::class.java)
                this.startActivity(intent)
            }

            R.id.cardUploadImage -> {
                val bg = binding.lnrUploadImage.background as GradientDrawable
                val imgBg = binding.imgUpload.background as GradientDrawable
                bg.mutate()
                imgBg.mutate()

                if (!binding.lnrContainer.isVisible) {
                    binding.lnrContainer.visibility = View.VISIBLE

                    bg.setStroke(dp(2), ContextCompat.getColor(this, R.color.dark_bg_orange_2))
                    imgBg.setColor(ContextCompat.getColor(this, R.color.dark_bg_orange_2))

                    binding.imgUpload.setColorFilter(
                        ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_IN
                    )

                    binding.cardManual.alpha = 0.4f

                    binding.cardUploadImage.cardElevation = 0f

                } else {
                    binding.lnrContainer.visibility = View.GONE

                    bg.setStroke(dp(2), ContextCompat.getColor(this, android.R.color.white))
                    imgBg.setColor(ContextCompat.getColor(this, R.color.very_light_gray_14))

                    binding.imgUpload.setColorFilter(
                        ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_IN
                    )

                    binding.cardManual.alpha = 1f

                    binding.cardUploadImage.cardElevation = dp(5).toFloat()
                }
            }

            R.id.lnrUploadFile -> {
                showBottomDialog()
            }

            R.id.cardManual -> {
                Log.d("LastSaved", Constant.selectedFiles.toString())
                Log.d("LastSaved", Constant.isAwsUploadedFiles.toString())
                Log.d("LastSaved", Constant.isAwsUploadedFiles.toString())

                Constant.selectedFiles.clear()
                Constant.isAwsUploadedFiles.clear()
                Constant.Remaining = MAX_FILES

                binding.lblFileName.text = getString(R.string.click_to_upload_or_drag_and_drop)
                binding.lnrUpload.visibility = View.GONE



                Log.d("After", Constant.selectedFiles.toString())
                Log.d("After", Constant.isAwsUploadedFiles.toString())
                Log.d("After", Constant.isAwsUploadedFiles.toString())

                binding.lnrContainer.visibility = View.GONE

                val bg = binding.lnrUploadImage.background as GradientDrawable
                val imgBg = binding.imgUpload.background as GradientDrawable
                bg.mutate()
                imgBg.mutate()


                bg.setStroke(dp(2), ContextCompat.getColor(this, android.R.color.white))
                imgBg.setColor(ContextCompat.getColor(this, R.color.very_light_gray_14))

                binding.imgUpload.setColorFilter(
                    ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_IN
                )

                binding.cardManual.alpha = 1f

                binding.cardUploadImage.cardElevation = dp(5).toFloat()

                Constant.showSendConfirmation(
                    this,
                    getString(R.string.continue_to_manual_entry),
                    getString(R.string.yes_continue_manually),
                    getString(R.string.Cancel),
                    getString(R.string.you_ll_enter_student_marks_manually_in_the_next_step_you_can_add_and_edit_all_mark_data_directly_without_ai_processing)
                ) { confirmed ->
                    if (confirmed) {
                        val intent = Intent(this, ReviewAndEditMarks::class.java)
                        this.startActivity(intent)
                    }
                }

            }


        }
    }


}