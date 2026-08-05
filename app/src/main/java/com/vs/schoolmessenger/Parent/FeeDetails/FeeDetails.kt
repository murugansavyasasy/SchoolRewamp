package com.vs.schoolmessenger.Parent.FeeDetails

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Dashboard.Parent.ParentDashboard
import com.vs.schoolmessenger.Parent.FeeDetails.Model.FeeInvoiceResponse
import com.vs.schoolmessenger.Parent.FeeDetails.Model.OnlinePaymentData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
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

    private lateinit var onlinePaymentAdapter: OnlinePaymentAdapter
    private var paymentList = ArrayList<OnlinePaymentData>()

    lateinit var mAdapter: FeeReceiptAdapter

    var isProgressLoading=false

    private var appViewModel: App? = null
    var isChildId = ""
    var isSchoolID = ""

    var isClickedTap = 2

    private val popupWebViewStack = Stack<WebView>()
    var alertDialogView: AlertDialog? = null

    private var msg_id: Int = -1
    private var fromNotification: Boolean = false

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.btnPayment.setOnClickListener(this)
        binding.btnReceipt.setOnClickListener(this)
        binding.btnAllTrance.setOnClickListener(this)
        binding.rytRefresh.setOnClickListener(this)

        isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token
        isChildId = isChildDetails!!.child_id
        isSchoolID = isChildDetails!!.school_id

        msg_id = intent.getIntExtra(Constant.msg_id, -1)

        fromNotification = intent.getBooleanExtra("fromNotification", false)
        if (fromNotification) {
            Constant.isParentChoose = true
        }
        val feeUrl = Constant.isGlobalVariableData!!.fees_url
        val isFinalFeeUrl = feeUrl
            .replace(Constant.isStudentID, isChildId)
            .replace(Constant.isSchoolID, isSchoolID)

        Log.d(
            "isFinalFeeUrl",
            "isChildId: ${isChildId} |isSchoolID: ${isSchoolID} | StudentFinalFeeUrl: ${isFinalFeeUrl}"
        )


        binding.toolbarLayout.lblStudentName.text = isChildDetails!!.name
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSelectedMenuName
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails!!.standard_name + " - " + isChildDetails!!.section_name

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        alertDialogView = AlertDialog.Builder(this@FeeDetails).create()
        binding.lblHeaderTitle.text = Constant.isSelectedMenuName

        appViewModel?.apiParentRepositories?.isInvoiceDetails?.observe(this) { response ->
            Constant.hideLoading(this)
            Log.d("Data","isResponseComing")

            if (response != null && response.status && response.data.isNotEmpty()) {
                Log.d("Data","isResponseStatusComing")
                val intent = Intent(this, FeeReceiptViewActivity::class.java)
                intent.putExtra("pdf_url", response.data[0])
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    response?.message ?: getString(R.string.unable_to_fetch_invoice),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.toolbarLayout.imgSearchToolBar.setOnClickListener {
            if (binding.rytSearch1.visibility == View.VISIBLE) {
                binding.rytSearch1.visibility = View.GONE
                binding.txtSearchMenuBox.setText("")
            } else {
                binding.rytSearch1.visibility = View.VISIBLE
                binding.txtSearchMenuBox.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.txtSearchMenuBox, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        loadPaymentPage(binding.payWebview)
        binding.payWebview.loadUrl(isFinalFeeUrl)

        binding.txtSearchMenuBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        mAdapter = FeeReceiptAdapter(listOf(), this, this, true)
        binding.rvReceipts.layoutManager = LinearLayoutManager(this)
        binding.rvReceipts.adapter = mAdapter

        mAdapter.showShimmer()

        appViewModel!!.isFeeInvoices?.observe(this) { response ->
            isProgressLoading=false
            Constant.hideLoading(this)
            if (response != null && response.status && response.data.isNotEmpty()) {
                mAdapter.setData(response.data)
                Log.d("FeeDetails_Response", "Invoices received: $response")
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                binding.lytList.visibility = View.GONE
                binding.rvReceipts.visibility = View.VISIBLE
                binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE
                scrollToMessageId(msg_id)
            } else {
                mAdapter.setData(listOf())
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.lytList.visibility = View.VISIBLE
                binding.rvReceipts.visibility = View.GONE
                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
                Log.d("FeeDetails_Response", "No invoices found or response null")
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE

            }
        }

        appViewModel!!.isOnlinePaymentResponse?.observe(this) { response ->

            Constant.hideLoading(this)

            if (response != null &&
                response.status &&
                response.data.isNotEmpty()
            ) {
                paymentList.clear()
                binding.lytList.visibility= View.GONE
                onlinePaymentAdapter = OnlinePaymentAdapter(
                    paymentList
                ) { item, position ->

                    Log.d("RefreshClick", "ID : ${item.id}")
                    Log.d("RefreshClick", "Order ID : ${item.order_id}")
                    Constant.showLoading(this)
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("id", item.id)

                    appViewModel?.isPaymentStatus(
                        isAccessToken!!,
                        jsonObject,
                        this
                    )
                }
                binding.rvReceipts.layoutManager =
                    LinearLayoutManager(this)
                binding.rvReceipts.adapter =
                    onlinePaymentAdapter
                binding.rvReceipts.visibility = View.VISIBLE
                onlinePaymentAdapter.updateList(response.data)
            }
            else{
                binding.lytList.visibility= View.VISIBLE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.rvReceipts.visibility = View.GONE
                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
                Log.d("FeeDetails_Response", "No invoices found or response null")
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
            }
        }

        appViewModel!!.isPaymentStatusResponse?.observe(this) { response ->
                Log.d("isResponse",response.toString())
                appViewModel?.isOnlinePayment(isAccessToken!!, this)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
            R.id.btnPayment -> {
                Log.d("isClickedTap", isClickedTap.toString())
                if (isClickedTap != 2) {
                    isClickedTap = 2
                    isProgressLoading=false
                    Constant.hideLoading(this)
                    binding.payWebview.visibility = View.VISIBLE
                    binding.rvReceipts.visibility = View.GONE
                    binding.rytRefresh.visibility = View.VISIBLE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                    binding.rytSearch1.visibility = View.GONE
                    binding.linePayment.setBackgroundResource(R.color.PrimaryColor)
                    binding.lineReceipt.setBackgroundResource(R.color.athens_gray)
                    binding.lineTrance.setBackgroundResource(R.color.athens_gray)
                    binding.btnPayment.setTextColor(Color.parseColor("#0D47A1"))
                    binding.btnReceipt.setTextColor(Color.BLACK)
                    binding.btnAllTrance.setTextColor(Color.BLACK)
                    loadPaymentPage(binding.payWebview)
                    //reloadPaymentPage()
                }
            }

            R.id.btnReceipt -> {
                Log.d("isClickedTap", isClickedTap.toString())
                if (isClickedTap != 1) {
                    isClickedTap = 1
                    isProgressLoading=false
                    Constant.hideLoading(this)
                    binding.payWebview.visibility = View.GONE
                    binding.rvReceipts.visibility = View.VISIBLE
                    binding.rytRefresh.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE

                    binding.linePayment.setBackgroundResource(R.color.athens_gray)
                    binding.lineTrance.setBackgroundResource(R.color.athens_gray)
                    binding.lineReceipt.setBackgroundResource(R.color.PrimaryColor)
                    binding.btnPayment.setTextColor(Color.BLACK)
                    binding.btnAllTrance.setTextColor(Color.BLACK)
                    binding.btnReceipt.setTextColor(Color.parseColor("#0D47A1"))

                    loadFeeReceipts()

                    Constant.showLoading(this)
                    appViewModel?.getStudentInvoices(isAccessToken!!, this)
                    Log.d("FeeDetails_Token", "Fetching invoices with token: $isAccessToken")
                }
            }

            R.id.btnAllTrance -> {
                Log.d("isClickedTap", isClickedTap.toString())
                if (isClickedTap != 3) {
                    isClickedTap = 3
                    isProgressLoading=false
                    Constant.hideLoading(this)
                    binding.payWebview.visibility = View.GONE
                    binding.rvReceipts.visibility = View.GONE
                    binding.rytRefresh.visibility = View.GONE
                    binding.toolbarLayout.imgSearchToolBar.visibility = View.VISIBLE

                    binding.linePayment.setBackgroundResource(R.color.athens_gray)
                    binding.lineTrance.setBackgroundResource(R.color.PrimaryColor)
                    binding.lineReceipt.setBackgroundResource(R.color.athens_gray)
                    binding.btnPayment.setTextColor(Color.BLACK)
                    binding.btnReceipt.setTextColor(Color.BLACK)
                    binding.btnAllTrance.setTextColor(Color.parseColor("#0D47A1"))
                    appViewModel?.isOnlinePayment(isAccessToken!!, this)
                    Constant.showLoading(this)
                }
            }


            R.id.rytRefresh -> {
                isClickedTap = 2
                isProgressLoading=false
                Constant.hideLoading(this)
                binding.payWebview.visibility = View.VISIBLE
                binding.rvReceipts.visibility = View.GONE
                binding.rytRefresh.visibility = View.VISIBLE
                binding.toolbarLayout.imgSearchToolBar.visibility = View.GONE
                binding.rytSearch1.visibility = View.GONE
                binding.linePayment.setBackgroundResource(R.color.PrimaryColor)
                binding.lineReceipt.setBackgroundResource(R.color.athens_gray)
                binding.btnPayment.setTextColor(Color.parseColor("#0D47A1"))
                binding.btnReceipt.setTextColor(Color.BLACK)
                clearPopupWebViews()
//                reloadPaymentPage()
            }
        }
    }

//    private fun reloadPaymentPage() {
//        binding.payWebview.apply {
//            clearHistory()
//            clearCache(true)
//            reload()
//        }
//    }


    private fun clearPopupWebViews() {
        while (popupWebViewStack.isNotEmpty()) {
            val webView = popupWebViewStack.pop()
            binding.webviewContainer.removeView(webView)
            webView.destroy()
        }
        binding.payWebview.reload()
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
        settings.databaseEnabled = true

        settings.cacheMode = WebSettings.LOAD_DEFAULT
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
                return handleUri(view, request.url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val uri = Uri.parse(url)
                return handleUri(view, uri)
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                if (!isProgressLoading){
                    isProgressLoading=true
                    Constant.showLoading(this@FeeDetails)
                }
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Constant.hideLoading(this@FeeDetails)
                isProgressLoading=false
            }

            override fun onPageFinished(view: WebView, url: String) {
                isProgressLoading=false
                Constant.hideLoading(this@FeeDetails)
                Log.d("callbackURL", url)

                when {
                    url.contains("/#/paymentsucccess/success") -> {
                        paymentSuccess(
                            getString(R.string.payment_done),
                            getString(R.string.payment_successful_view_download_receipt_on_receipt_tab)
                        )
                    }

                    url.contains("/#/paymentsucccess/failed") -> {
                        paymentFailed(
                            getString(R.string.payment_failed),
                            getString(R.string.please_try_again_later)
                        )
                    }
                }
            }
        }
    }

    fun viewInvoice(invoiceId: String) {
        Log.d("Data","isDataComing")
        Constant.showLoading(this)
        appViewModel?.getInvoiceDetails(isAccessToken!!, invoiceId, this)

    }

    private fun scrollToMessageId(msg_id: Int) {
        if (msg_id == -1) return

        val dataList = mAdapter?.getCurrentList()
        if (!dataList.isNullOrEmpty()) {
            val index = dataList.indexOfFirst { it.id.toIntOrNull() == msg_id }
            if (index != -1) {
                Log.d("ScrollDebug", "Scrolling to index $index")
                binding.rvReceipts.post {
                    binding.rvReceipts.smoothScrollToPosition(index)
                    highlightItemTemporarily(binding.rvReceipts, index)
                }
            } else {
                Log.d("ScrollDebug", "No index found for msg_id $msg_id")
            }
        }
    }


    private fun highlightItemTemporarily(recyclerView: RecyclerView, position: Int) {
        recyclerView.post {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.itemView?.setBackgroundColor(
                resources.getColor(
                    R.color.light_yellow_5,
                    null
                )
            )
            recyclerView.postDelayed({
                viewHolder?.itemView?.setBackgroundColor(Color.TRANSPARENT)
            }, Constant.TIME_OUT)
        }
    }

    private fun paymentSuccess(title: String, msg: String) {
        val mobileNumber = SharedPreference.getMobileNumber(this)
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.mobile_number, mobileNumber)
            addProperty(APIKeyNames.activity, Constant.add_points_pay_fees)
            addProperty(APIKeyNames.user_type, Constant.user_type_as_parent)
            addProperty(APIKeyNames.menu_id, Constant.SELECTED_MENU_ID)
        }
        appViewModel?.isAddRewardPoints("" ?: "", jsonObject, this)

        val dialogView =
            LayoutInflater.from(this@FeeDetails).inflate(R.layout.payment_success, null)
        val builder = AlertDialog.Builder(this@FeeDetails)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        // Access views
        val titleText = dialogView.findViewById<TextView>(R.id.alertTitle)
        val messageText = dialogView.findViewById<TextView>(R.id.alertMessage)
        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        messageText.text = msg
        titleText.text = title
        okButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun paymentFailed(title: String, msg: String) {
        val dialogView = LayoutInflater.from(this@FeeDetails).inflate(R.layout.payment_failed, null)
        val builder = AlertDialog.Builder(this@FeeDetails)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
        alertDialog.show()
        // Access views
        val titleText = dialogView.findViewById<TextView>(R.id.alertTitle)
        val messageText = dialogView.findViewById<TextView>(R.id.alertMessage)
        val okButton = dialogView.findViewById<TextView>(R.id.btnOk)
        messageText.text = msg
        titleText.text = title
        okButton.setOnClickListener {
            alertDialog.dismiss()
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
                alertDialogView!!.setTitle(getString(R.string.error))
                alertDialogView!!.setMessage(getString(R.string.check_if_you_have_upi_apps_installed_or_not))
                alertDialogView!!.setButton(getString(R.string.OK_2)) { dialog: DialogInterface, _: Int ->
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
//        super.onBackPressed()
//        val intent = Intent(this, ParentDashboard::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//        finish()
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
        mAdapter = FeeReceiptAdapter(
            listOf(),
            this,
            this,
            Constant.isShimmerViewDisable,
            object : OnFilterResultListener {
                override fun onFilterResult(isEmpty: Boolean) {
                    if (isEmpty) {
                        binding.nomessage.visibility = View.VISIBLE
                        binding.txtNoData.visibility = View.VISIBLE
                        binding.rvReceipts.visibility = View.GONE
                    } else {
                        binding.nomessage.visibility = View.GONE
                        binding.txtNoData.visibility = View.GONE
                        binding.rvReceipts.visibility = View.VISIBLE
                    }
                }
            }
        )

        binding.rvReceipts.layoutManager = LinearLayoutManager(this)
        binding.rvReceipts.adapter = mAdapter
    }

    interface OnFilterResultListener {
        fun onFilterResult(isEmpty: Boolean)
    }


    override fun onItemClick(
        data: FeeInvoiceResponse.InvoiceData,
        holder: FeeReceiptAdapter.DataViewHolder
    ) {
        viewInvoice(data.id)
//        Log.d("InvoiceID", data.id)
//        val intent = Intent(this@FeeDetails, FeeReceiptViewActivity::class.java)
//        intent.putExtra("invoice_id", data.id)
//        startActivity(intent)
    }

}