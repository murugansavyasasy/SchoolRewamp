package com.vs.schoolmessenger.Dashboard.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordGeneration
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.TermsConditions.TermsAndConditions
import com.vs.schoolmessenger.Dashboard.Settings.ContactUs.ContactUs
import com.vs.schoolmessenger.Dashboard.Settings.Faq.Faq
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model.RateUsListener
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.RateUsDialog
import com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug.ReportTheBug
import com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.WhatsNewActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SettingsFragmentBinding


class SettingsFragment : Fragment(), View.OnClickListener {

    override fun onAttach(context: Context) {
        val savedLanguage = ChangeLanguage.getPersistedLanguage(context)
        val newContext = ChangeLanguage.setLocale(context, savedLanguage)
        super.onAttach(newContext ?: context)
    }

    private lateinit var binding: SettingsFragmentBinding
    private var isSelectedLanguage = ""
    private lateinit var imgClose: ImageView
    private lateinit var imgTamil: ImageView
    private lateinit var imgEnglish: ImageView
    private lateinit var imgThai: ImageView
    private lateinit var imgHindi: ImageView
    private lateinit var imgArabic: ImageView
    private lateinit var chEnglish: CheckBox
    private lateinit var chTamil: CheckBox
    private lateinit var chThai: CheckBox
    private lateinit var chHindi: CheckBox
    private lateinit var chArabic: CheckBox
    private lateinit var btnConfirm: TextView
    private lateinit var rlaEnglish: RelativeLayout
    private lateinit var rlaTamil: RelativeLayout
    private lateinit var rlaThai: RelativeLayout
    private lateinit var rlaHindi: RelativeLayout
    private lateinit var rlaArabic: RelativeLayout

    var authViewModel: Auth? = null

    private var isChecking = false

    private var popupWindow: PopupWindow? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentBinding.inflate(layoutInflater)
        binding.lnrTermsConditions.setOnClickListener(this)
        binding.lnrNotification.setOnClickListener(this)
        binding.lnrContactUs.setOnClickListener(this)
        binding.lnrReportBug.setOnClickListener(this)
        binding.lnrFeedBack.setOnClickListener(this)
        binding.lnrFaq.setOnClickListener(this)
        binding.lnrLogout.setOnClickListener(this)
        binding.lnrLanguage.setOnClickListener(this)
        binding.lnrChangePassword.setOnClickListener(this)
        binding.lnrPrivacyPolicy.setOnClickListener(this)
        binding.lnrAboutTheApp.setOnClickListener(this)
        binding.lnrHowToUseApp.setOnClickListener(this)
        binding.lnrwhatsnew.setOnClickListener(this)
        val pInfo = requireContext().packageManager
            .getPackageInfo(requireActivity().packageName, 0)

        val versionName = pInfo.versionName

        val versionCode =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }

        binding.lblAppVersion.text =
            "${getString(R.string.App_Version)} - $versionName"


        authViewModel = ViewModelProvider(this)[Auth::class.java]
        authViewModel!!.init()


        authViewModel!!.isLogout?.observe(requireActivity()) { response ->
            Constant.hideLoading(requireActivity())
            if (response != null) {
                if (response.status) {
                    // Dismiss popup to prevent WindowLeaked
                    popupWindow?.dismiss()
                    popupWindow = null
                    clearDim()

                    SharedPreference.putLogout(requireActivity(), true)
                    SharedPreference.setLoggedIn(requireActivity(), false)

                    val intent = Intent(requireActivity(), Login::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                    Toast.makeText(requireActivity(), response.message, Toast.LENGTH_SHORT).show()
                } else {
                    Constant.showErrorAlert(
                        requireActivity(),
                        getString(R.string.Oops),
                        response?.message
                            ?: getString(R.string.something_went_wrong_please_try_again_later)
                    )
                }
            }

        }

        if (Constant.checkBiometricSupport(requireActivity())) {
            binding.lnrEnableFingerPrint.visibility = View.VISIBLE
        } else {
            binding.lnrEnableFingerPrint.visibility = View.GONE
        }

        binding.switchFingerprint.isChecked =
            SharedPreference.isFingerprintEnabled(requireActivity())
        binding.switchFingerprint.setOnCheckedChangeListener { _, isChecked ->
            SharedPreference.setFingerprintEnabled(requireActivity(), isChecked)
            Toast.makeText(
                requireActivity(),
                "${getString(R.string.Fingerprint_login)} ${
                    if (isChecked) getString(R.string.enabled) else getString(
                        R.string.disabled
                    )
                }",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (Constant.isParentChoose) {
            binding.rlaLblSettings.setBackgroundResource(R.drawable.gradient_theme_school)
        } else {
            binding.rlaLblSettings.setBackgroundResource(R.drawable.gradient_theme_school)
        }

        return binding.root
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.lnrTermsConditions -> {
                val intent = Intent(requireActivity(), TermsAndConditions::class.java)
                intent.putExtra("screen_name", "isTerms")
                startActivity(intent)
            }

            R.id.lnrPrivacyPolicy -> {
                val intent = Intent(requireActivity(), TermsAndConditions::class.java)
                intent.putExtra("screen_name", "isPrivacy")
                startActivity(intent)
            }

            R.id.lnrAboutTheApp -> {
                val intent = Intent(requireActivity(), TermsAndConditions::class.java)
                intent.putExtra("screen_name", "isAboutTheApp")
                startActivity(intent)
            }

            R.id.lnrHowToUseApp -> {
                val intent = Intent(requireActivity(), TermsAndConditions::class.java)
                intent.putExtra("screen_name", "HowToUse")
                startActivity(intent)
            }


            R.id.lnrNotification -> {
                startActivity(Intent(requireActivity(), Notification::class.java))
            }

            R.id.lnrContactUs -> {
                startActivity(Intent(requireActivity(), ContactUs::class.java))
            }

            R.id.lnrReportBug -> {
                startActivity(Intent(requireActivity(), ReportTheBug::class.java))
            }

            R.id.lnrFeedBack -> {
                val dialog = RateUsDialog(
                    fromScreen = "",
                    listener = object : RateUsListener {
                        override fun onRateUsCompleted(isSuccess: Boolean) {

                        }
                    }
                )
                dialog.show(parentFragmentManager, "RateUsDialog")

            }

            R.id.lnrFaq -> {
                startActivity(Intent(requireActivity(), Faq::class.java))
            }

            R.id.lnrLanguage -> {
                showLanguageSelectorDialog()
            }

            R.id.lnrChangePassword -> {
                val intent = Intent(requireActivity(), PasswordGeneration::class.java)
                intent.putExtra("type", "change")                 // Int
                startActivity(intent)
            }

            R.id.lnrLogout -> {
                isShowLogoutPopup()
            }

            R.id.lnrwhatsnew -> {
                RedirectToWhatsnew()
            }
        }
    }

    fun testInAppReviewUI() {
        val manager: ReviewManager = if (BuildConfig.DEBUG) {
            // Use fake manager in debug builds
            FakeReviewManager(requireActivity())
        } else {
            // Use real manager in release builds
            ReviewManagerFactory.create(requireActivity())
        }

        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo: ReviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.review_flow_completed_debug_simulation),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.failed_to_start_review_flow), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun RedirectToWhatsnew() {
        val intent = Intent(requireContext(), WhatsNewActivity::class.java)
        startActivity(intent)
    }


    private fun showInAppReview(requireActivity: FragmentActivity) {
        val manager = ReviewManagerFactory.create(requireActivity())
        val request: Task<ReviewInfo> =
            manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener {
                    // The flow has finished, you cannot know if user submitted or not
                    // Do any post-review logic here (optional)
                }
            } else {
                // If something fails, fallback to Play Store app page
                redirectToAppRating(requireActivity)
            }
        }
    }

    private fun redirectToAppRating(context: FragmentActivity) {
        //        val packageName = context.packageName
        val packageName = "com.vs.schoolmessenger"
        try {
            // Open Play Store app directly
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            )
            intent.setPackage("com.android.vending") // Ensure it opens in Play Store app
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // If Play Store app is not available, open in browser
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            context.startActivity(intent)
        }
    }

    private fun isShowLogoutPopup() {

        if (!isAdded || requireActivity().isFinishing || requireActivity().isDestroyed) {
            return
        }

        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.logout_popup, null)

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )

        dimBehind(popupWindow!!)
        val btnCancel: TextView = popupView.findViewById(R.id.btnCancel)
        val rlaLogout: RelativeLayout = popupView.findViewById(R.id.rlaLogout)
        btnCancel.setOnClickListener {
            clearDim()
            popupWindow!!.dismiss()
        }

        rlaLogout.setOnClickListener {
            clearDim()
            popupWindow!!.dismiss()

            val jsonObject = JsonObject().apply {
                addProperty(
                    APIKeyNames.Req_mobile_number,
                    SharedPreference.getMobileNumber(requireActivity()).toString()
                )
                addProperty(APIKeyNames.Req_device_type, Constant.isDeviceType)
                addProperty(
                    APIKeyNames.Req_secure_id,
                    Constant.getAndroidSecureId(requireActivity())
                )
            }

            authViewModel!!.isLogout(jsonObject, requireActivity())
            Constant.showLoading(requireActivity())

        }
        val activity = activity ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        val rootView = activity.window?.decorView?.rootView ?: return
        popupWindow!!.showAtLocation(rootView, Gravity.CENTER, 0, 0)

        popupWindow!!.setOnDismissListener {
            clearDim()
        }
    }

    private fun dimBehind(popupWindow: PopupWindow) {
        val window = requireActivity().window
        val layoutParams = window.attributes
        layoutParams.alpha = 0.4f // Lower alpha to dim the background
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams
    }

    private fun clearDim() {
        val window = requireActivity().window
        val layoutParams = window.attributes
        layoutParams.alpha = 1.0f
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = layoutParams
    }


    private fun showLanguageSelectorDialog() {

        val dialogView =
            LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_language_selector, null)
        val alertDialog = AlertDialog.Builder(requireActivity())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        imgClose = dialogView.findViewById(R.id.imgClose)

        chEnglish = dialogView.findViewById(R.id.chEnglish)
        chTamil = dialogView.findViewById(R.id.chTamil)
        chThai = dialogView.findViewById(R.id.chThai)
        chHindi = dialogView.findViewById(R.id.chHindi)
        chArabic = dialogView.findViewById(R.id.chArabic)

        btnConfirm = dialogView.findViewById(R.id.btnConfirm)

        imgTamil = dialogView.findViewById(R.id.imgTamil)
        imgEnglish = dialogView.findViewById(R.id.imgEnglish)
        imgThai = dialogView.findViewById(R.id.imgThai)
        imgHindi = dialogView.findViewById(R.id.imgHindi)
        imgArabic = dialogView.findViewById(R.id.imgArabic)

        rlaEnglish = dialogView.findViewById(R.id.rlaEnglish)
        rlaTamil = dialogView.findViewById(R.id.rlaTamil)
        rlaThai = dialogView.findViewById(R.id.rlaThai)
        rlaHindi = dialogView.findViewById(R.id.rlaHindi)
        rlaArabic = dialogView.findViewById(R.id.rlaArabic)

        chTamil.buttonTintList = null
        chEnglish.buttonTintList = null
        chHindi.buttonTintList = null
        chArabic.buttonTintList = null
        chThai.buttonTintList = null


        fun isCheckEnabledButton() {
            if (SharedPreference.getLanguage(requireActivity()) == isSelectedLanguage) {
                btnConfirm.apply {
                    alpha = 0.4f
                    isEnabled = false
                }
            } else {
                btnConfirm.apply {
                    alpha = 1f
                    isEnabled = true
                }
            }
        }

        isRemoveCheckBox()
        isCheckEnabledButton()



        rlaEnglish.setOnClickListener {
            chEnglish.performClick()
        }

        rlaTamil.setOnClickListener {
            chTamil.performClick()
        }

        rlaThai.setOnClickListener {
            chThai.performClick()
        }

        rlaHindi.setOnClickListener {
            chHindi.performClick()
        }

        rlaArabic.setOnClickListener {
            chArabic.performClick()
        }


        chEnglish.setOnCheckedChangeListener { _, isChecked ->
            isRemoveCheckBox()
            if (isChecked) {
                isChecking = true
                isSelectedLanguage = Constant.en
                chEnglish.isChecked = true
                isSelectedImageSetting(imgEnglish)
            } else {
                isChecking = false
                isResetBackgroud()
            }
            isCheckEnabledButton()
        }

        chTamil.setOnCheckedChangeListener { _, isChecked ->
            isRemoveCheckBox()
            if (isChecked) {
                isChecking = true
                isSelectedLanguage = Constant.ta
                chTamil.isChecked = true
                isSelectedImageSetting(imgTamil)
            } else {
                isChecking = false
                isResetBackgroud()
            }
            isCheckEnabledButton()
        }

        chThai.setOnCheckedChangeListener { _, isChecked ->
            isRemoveCheckBox()
            if (isChecked) {
                isChecking = true
                isSelectedLanguage = Constant.th
                chThai.isChecked = true
                isSelectedImageSetting(imgThai)
            } else {
                isChecking = false
                isResetBackgroud()
            }
            isCheckEnabledButton()
        }

        chHindi.setOnCheckedChangeListener { _, isChecked ->
            isRemoveCheckBox()
            if (isChecked) {
                isChecking = true
                isSelectedLanguage = Constant.hi
                chHindi.isChecked = true
                isSelectedImageSetting(imgHindi)
            } else {
                isChecking = false
                isResetBackgroud()
            }
            isCheckEnabledButton()
        }

        chArabic.setOnCheckedChangeListener { _, isChecked ->
            isRemoveCheckBox()
            if (isChecked) {
                isChecking = true
                isSelectedLanguage = Constant.ar
                chArabic.isChecked = true
                isSelectedImageSetting(imgArabic)
            } else {
                isChecking = false
                isResetBackgroud()
            }
            isCheckEnabledButton()
        }

        var isAppLanguage = SharedPreference.getLanguage(requireActivity()) ?: "en"
        Log.d("isAppLanguage", isAppLanguage.toString())
        if (isAppLanguage == "") {
            isAppLanguage = Constant.en
        }
        when (isAppLanguage) {
            Constant.ta -> {
                chTamil.isChecked = true
            }

            Constant.th -> {
                chThai.isChecked = true
            }

            Constant.hi -> {
                chHindi.isChecked = true
            }

            Constant.en -> {
                chEnglish.isChecked = true
            }

            Constant.ar -> {
                chArabic.isChecked = true
            }
        }


        btnConfirm.setOnClickListener {

            if (isChecking) {
                isChecking = false
                (requireActivity() as? BaseActivity<*>)?.changeLanguage(isSelectedLanguage)
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireActivity(), R.string.lblSelectlanguage, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        imgClose.setOnClickListener {
            isChecking = false
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun isSelectedImageSetting(isSelectedImage: ImageView) {
        imgTamil.setImageResource(R.drawable.ta_language_gray)
        imgEnglish.setImageResource(R.drawable.en_language_gray)
        imgThai.setImageResource(R.drawable.th_language_gray)
        imgHindi.setImageResource(R.drawable.hi_language_gray)
        imgArabic.setImageResource(R.drawable.ara_language_gray)

        when (isSelectedImage) {
            imgTamil -> imgTamil.setImageResource(R.drawable.ta_language_orange)
            imgEnglish -> imgEnglish.setImageResource(R.drawable.en_language_orange)
            imgThai -> imgThai.setImageResource(R.drawable.th_language_orange)
            imgHindi -> imgHindi.setImageResource(R.drawable.hi_language_orange)
            imgArabic -> imgArabic.setImageResource(R.drawable.ara_language_orange)
        }
    }

    private fun isResetBackgroud() {
        imgTamil.setImageResource(R.drawable.ta_language_gray)
        imgEnglish.setImageResource(R.drawable.en_language_gray)
        imgThai.setImageResource(R.drawable.th_language_gray)
        imgHindi.setImageResource(R.drawable.hi_language_gray)
        imgArabic.setImageResource(R.drawable.ara_language_gray)
    }

    private fun isRemoveCheckBox() {
        chEnglish.isChecked = false
        chTamil.isChecked = false
        chThai.isChecked = false
        chHindi.isChecked = false
        chArabic.isChecked = false
    }
}