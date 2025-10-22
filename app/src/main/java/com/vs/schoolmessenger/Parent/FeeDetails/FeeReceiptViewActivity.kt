package com.vs.schoolmessenger.Parent.FeeDetails

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeeReceiptViewActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class FeeReceiptViewActivity : BaseActivity<FeeReceiptViewActivityBinding>(), View.OnClickListener {

    override fun getViewBinding(): FeeReceiptViewActivityBinding {
        return FeeReceiptViewActivityBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null
    private var pdfUrl: String? = null

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()

        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.imgDownload.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.downloadicon)
        )
        binding.imgDownload.visibility = View.VISIBLE
        binding.imgDownload.setOnClickListener(this)
        binding.toolbarLayout.rytSearch.visibility = View.GONE

        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.fee_receipt)
        binding.toolbarLayout.lblStudentSection.text =
            "${isChildDetails?.standard_name} - ${isChildDetails?.section_name}"

        pdfUrl = intent.getStringExtra("pdf_url")
            ?: "https://schoolchimes-fee-receipts.s3.ap-south-1.amazonaws.com/undefined/fee_receipt/PDF_1748065242703.pdf"

        val googleDocsUrl = "${Constant.google_g_view_embedded}$pdfUrl"
        Constant.loadWebView(this, binding.feeReceiptWebview, googleDocsUrl)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.imgDownload -> {
                if (checkStoragePermission()) downloadFeeReceipt()
                else requestStoragePermission()
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permission, 101)
    }

    private fun downloadFeeReceipt() {
        pdfUrl?.let { url ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    var fileName = url.substringAfterLast("/").substringBefore("?")
                    if (!fileName.endsWith(".pdf")) fileName += ".pdf"

                    val baseFolder = "SchoolChimes"
                    val subFolder = "Documents"
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val targetDir = File(downloadsDir, "$baseFolder/$subFolder")
                    if (!targetDir.exists()) targetDir.mkdirs()

                    val file = File(targetDir, fileName)
                    if (!file.exists()) {
                        URL(url).openStream().use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        Constant.showValidationAlertPopup(
                            "Successfully Download...✅",
                            "File saved to Downloads/$baseFolder/$subFolder/$fileName",
                            this@FeeReceiptViewActivity
                        )
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@FeeReceiptViewActivity,
                            "Download failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

}
