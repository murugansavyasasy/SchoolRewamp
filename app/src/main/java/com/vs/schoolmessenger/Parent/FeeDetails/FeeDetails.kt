package com.vs.schoolmessenger.Parent.FeeDetails

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.FeeDetailsBinding
import java.util.Stack

class FeeDetails : BaseActivity<FeeDetailsBinding>(), View.OnClickListener, InvoiceClickListener {

    override fun getViewBinding(): FeeDetailsBinding {
        return FeeDetailsBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null

    lateinit var mAdapter: FeeReceiptAdapter
    private lateinit var invoiceList: List<InvoiceDetails>

    private var appViewModel: App? = null

    private val popupWebViewStack = Stack<WebView>()
    var alertDialogView: AlertDialog? = null


    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()
        binding.imgBack.setOnClickListener(this)
        binding.btnPayment.setOnClickListener(this)
        binding.btnReceipt.setOnClickListener(this)

        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token

        binding.lblStudentName.text = isChildDetails!!.name
        binding.lblParentToolBar.text = Constant.isParentMenuName
        binding.lblStudentSection.text =
            isChildDetails!!.standard_name + " - " + isChildDetails!!.section_name

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        alertDialogView = AlertDialog.Builder(this@FeeDetails).create()

        loadPaymentPage(binding.payWebview)
        binding.payWebview.loadUrl("https://profile.schoolchimes.com/#/online-fee-payment/13601818/6063/app")

        invoiceList = listOf(
            InvoiceDetails(
                1,
                "Invoice No: INV001",
                "Invoice Date : 01-05-2025",
                "Invoice Amount : 1200",
                "10:45 AM 234 KB"
            ),
            InvoiceDetails(
                2,
                "Invoice No: INV001",
                "Invoice Date : 01-05-2025",
                "Invoice Amount : 1200",
                "10:45 AM 234 KB"

            ),
            InvoiceDetails(
                3,
                "Invoice No: INV001",
                "Invoice Date : 01-05-2025",
                "Invoice Amount : 1200",
                "10:45 AM 234 KB"
            ),
            InvoiceDetails(
                4,
                "Invoice No: INV001",
                "Invoice Date : 01-05-2025",
                "Invoice Amount : 1200",
                "10:45 AM 234 KB"
            ),
            InvoiceDetails(
                5,
                "Invoice No: INV001",
                "Invoice Date : 01-05-2025",
                "Invoice Amount : 1200",
                "10:45 AM 234 KB"
            )
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()

            R.id.btnPayment -> {
                binding.payWebview.visibility = View.VISIBLE
                binding.rvReceipts.visibility = View.GONE
                binding.btnPayment.setBackgroundResource(R.drawable.white_radious)
                binding.btnPayment.setTextColor(Color.BLACK)
                binding.btnReceipt.setBackgroundResource(R.drawable.bg_light_green)
                loadPaymentPage(binding.payWebview)

            }

            R.id.btnReceipt -> {
                binding.payWebview.visibility = View.GONE
                binding.rvReceipts.visibility = View.VISIBLE
                binding.btnPayment.setBackgroundResource(R.drawable.bg_light_green)
                binding.btnReceipt.setTextColor(Color.BLACK)
                binding.btnReceipt.setBackgroundResource(R.drawable.white_radious)
                loadFeeReceipts()

            }
        }
    }

    private fun loadPaymentPage(webView: WebView) {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = false
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.isScrollbarFadingEnabled = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                val newWebView = WebView(this@FeeDetails)
                loadPaymentPage(newWebView)
                val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                newWebView.layoutParams = params
                binding.webviewContainer.addView(newWebView)
                popupWebViewStack.push(newWebView)

                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }

            override fun onCloseWindow(window: WebView) {
                if (popupWebViewStack.isNotEmpty()) {
                    val closingWebView = popupWebViewStack.pop()
                    binding.webviewContainer.removeView(closingWebView)
                    closingWebView.destroy()
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val uri = request.url
                return handleUri(view, uri)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.d("WebView", "Navigating to: $url")
                val uri = Uri.parse(url)
                return handleUri(view, uri)
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Constant.showLoading(this@FeeDetails)
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Constant.hideLoading(this@FeeDetails)

            }

            override fun onPageFinished(view: WebView, url: String) {
                Constant.hideLoading(this@FeeDetails)
                Log.d("callbackURL", url)

                when {
                    url.contains("/#/paymentsucccess/success") -> {
//                        showAlert("Payment Done!!", "Payment Successful. View/Download Receipt on Receipt Tab.")
                    }

                    url.contains("/#/paymentsucccess/failed") -> {
//                        showAlert("Payment failed..", "Please try again later!!")
                    }
                }
            }
        }
    }

    private fun handleUri(view: WebView, url: Uri): Boolean {
        Log.i("", "Uri = $url")
        println("Uri = $url")
        val scheme = url.scheme
        println("scheme detected: $scheme")
        if (scheme != null && scheme.matches(Regex("upi|tez|gpay|phonepe|paytmmp"))) {
            println("UPI Intent uri detected")
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = url
                startActivity(intent)
                return true
            } catch (e: Exception) {
                alertDialogView!!.setTitle("Error")
                alertDialogView!!.setMessage("Check if you have UPI apps installed or not !")
                alertDialogView!!.setButton("OK") { dialog: DialogInterface, _: Int ->
                    // Do nothing
                }
                alertDialogView!!.show()
            }
        } else {
            Log.i("", "Processing webview url click...")
            return false
        }
        return false
    }

    fun handleBackPressed(): Boolean {
        return if (popupWebViewStack.isNotEmpty()) {
            val lastPopup = popupWebViewStack.pop()
            binding.webviewContainer.removeView(lastPopup)
            lastPopup.destroy()
            true
        } else if (binding.payWebview.canGoBack()) {
            binding.payWebview.goBack()
            true
        } else {
            false
        }
    }

    override fun onBackPressed() {
        if (handleBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        if (binding.payWebview != null) {
            binding.payWebview.destroy()
        }
        while (!popupWebViewStack.isEmpty()) {
            popupWebViewStack.pop().destroy()
        }
        super.onDestroy()
    }


    private fun loadFeeReceipts() {

        binding.rvReceipts.layoutManager = LinearLayoutManager(this)
        mAdapter = FeeReceiptAdapter(
            invoiceList,
            this,
            this,
            Constant.isShimmerViewDisable
        )
        binding.rvReceipts.adapter = mAdapter

    }

    override fun onItemClick(data: InvoiceDetails, holder: FeeReceiptAdapter.DataViewHolder) {
        Log.d("InvoiceID", data.id.toString())
        val intent = Intent(this@FeeDetails, FeeReceiptViewActivity::class.java)
        startActivity(intent)

    }
}