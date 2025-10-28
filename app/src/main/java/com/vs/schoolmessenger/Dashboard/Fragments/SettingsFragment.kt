package com.vs.schoolmessenger.Dashboard.Fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.airbnb.lottie.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordGeneration
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.TermsConditions.TermsAndConditions
import com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.WhatsNewActivity
import com.vs.schoolmessenger.Dashboard.Settings.ContactUs.ContactUs
import com.vs.schoolmessenger.Dashboard.Settings.Faq.Faq
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug.ReportTheBug
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.SettingsFragmentBinding
import java.io.ByteArrayOutputStream

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
    private lateinit var chEnglish: CheckBox
    private lateinit var chTamil: CheckBox
    private lateinit var chThai: CheckBox
    private lateinit var chHindi: CheckBox
    private lateinit var chArabic: CheckBox
    private lateinit var btnConfirm: TextView
    private var isChecking = false

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
//        binding.lnrSaveContact.setOnClickListener(this)
        binding.lnrwhatsnew.setOnClickListener(this)

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
                "Fingerprint login ${if (isChecked) "enabled" else "disabled"}",
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
                startActivity(Intent(requireActivity(), TermsAndConditions::class.java))
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
                redirectToAppRating(requireActivity())
                //showInAppReview(requireActivity())
                //startActivity(Intent(requireActivity(), RateUs::class.java))
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
//                testInAppReviewUI()
                isShowLogoutPopup()
            }

            R.id.lnrwhatsnew -> {
                RedirectToWhatsnew()
            }

//            R.id.lnrSaveContact -> {
//                //checkContactPermission()
//            }
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
                    Toast.makeText(requireActivity(), "Review flow completed (debug simulation)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireActivity(), "Failed to start review flow", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun RedirectToWhatsnew() {
        val intent = Intent(requireContext(), WhatsNewActivity::class.java)
        startActivity(intent)
    }


   private fun showInAppReview(requireActivity: FragmentActivity) {
        val manager = ReviewManagerFactory.create(requireActivity())
        val request: Task<com.google.android.play.core.review.ReviewInfo> = manager.requestReviewFlow()

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
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.logout_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )

        dimBehind(popupWindow)
        val btnCancel: TextView = popupView.findViewById(R.id.btnCancel)
        val rlaLogout: RelativeLayout = popupView.findViewById(R.id.rlaLogout)
        btnCancel.setOnClickListener {
            clearDim()
            popupWindow.dismiss()
        }

        rlaLogout.setOnClickListener {
            SharedPreference.putLogout(requireActivity(), true)
            SharedPreference.setLoggedIn(requireActivity(), false)

            val intent = Intent(requireActivity(), Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish()

        }

        val rootView = requireActivity().window.decorView.rootView
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)

        popupWindow.setOnDismissListener {
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

        isRemoveCheckBox()
        chEnglish.setOnCheckedChangeListener { _, isChecked ->
            isRemoveCheckBox()
            if (isChecked) {
                isChecking = true
                isSelectedLanguage = Constant.en
                chEnglish.isChecked = true
                isSelectedImageSetting(imgEnglish)
            } else {
                isChecking = false
            }
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
            }
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
            }
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
            }
        }

        chArabic.setOnCheckedChangeListener { _, isChecked ->
            isRemoveCheckBox()
            if (isChecked) {
                isChecking = true
                isSelectedLanguage = Constant.ar
                chArabic.isChecked = true
                isSelectedImageSetting(imgHindi)
            } else {
                isChecking = false
            }
        }

        var isAppLanguage = SharedPreference.getLanguage(requireActivity())?: "en"
        Log.d("isAppLanguage", isAppLanguage.toString())
        if (isAppLanguage.equals("")) {
            isAppLanguage = Constant.en
        }
        if (isAppLanguage.equals(Constant.ta)) {
            chTamil.isChecked = true
        } else if (isAppLanguage.equals(Constant.th)) {
            chThai.isChecked = true
        } else if (isAppLanguage.equals(Constant.hi)) {
            chHindi.isChecked = true
        } else if (isAppLanguage.equals(Constant.en)) {
            chEnglish.isChecked = true
        } else if (isAppLanguage.equals(Constant.ar)) {
            chArabic.isChecked = true
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

        when (isSelectedImage) {
            imgTamil -> imgTamil.setImageResource(R.drawable.ta_language_orange)
            imgEnglish -> imgEnglish.setImageResource(R.drawable.en_language_orange)
            imgThai -> imgThai.setImageResource(R.drawable.th_language_orange)
            imgHindi -> imgHindi.setImageResource(R.drawable.hi_language_orange)
        }
    }

    private fun isRemoveCheckBox() {
        chEnglish.isChecked = false
        chTamil.isChecked = false
        chThai.isChecked = false
        chHindi.isChecked = false
        chArabic.isChecked = false
    }

    private fun refreshFragment() {
        ChangeLanguage.setLocale(requireContext(), isSelectedLanguage)
        requireActivity().recreate()
    }
}