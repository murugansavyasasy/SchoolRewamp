package com.vs.schoolmessenger.Dashboard.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordGeneration
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.TermsConditions.TermsAndConditions
import com.vs.schoolmessenger.Dashboard.Settings.ContactUs.ContactUs
import com.vs.schoolmessenger.Dashboard.Settings.Faq.Faq
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.Dashboard.Settings.RateUs.RateUs
import com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug.ReportTheBug
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ChangeLanguage
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
    private lateinit var chEnglish: CheckBox
    private lateinit var chTamil: CheckBox
    private lateinit var chThai: CheckBox
    private lateinit var chHindi: CheckBox
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
                startActivity(Intent(requireActivity(), RateUs::class.java))
            }

            R.id.lnrFaq -> {
                startActivity(Intent(requireActivity(), Faq::class.java))
            }

            R.id.lnrLanguage -> {
                showLanguageSelectorDialog()
            }

            R.id.lnrChangePassword -> {
                startActivity(Intent(requireActivity(), PasswordGeneration::class.java))
            }

            R.id.lnrLogout -> {
                isShowLogoutPopup()
            }
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
            startActivity(Intent(requireActivity(), Login::class.java))
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
                isSelectedLanguage = "en"
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
                isSelectedLanguage = "ta"
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
                isSelectedLanguage = "th"
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
                isSelectedLanguage = "hi"
                chHindi.isChecked = true
                isSelectedImageSetting(imgHindi)
            } else {
                isChecking = false
            }
        }

        var isAppLanguage = SharedPreference.getLanguage(requireActivity())
        Log.d("isAppLanguage", isAppLanguage.toString())
        if (isAppLanguage.equals("")) {
            isAppLanguage = "en"
        }
        if (isAppLanguage.equals("ta")) {
            chTamil.isChecked = true
        } else if (isAppLanguage.equals("th")) {
            chThai.isChecked = true
        } else if (isAppLanguage.equals("hi")) {
            chHindi.isChecked = true
        } else if (isAppLanguage.equals("en")) {
            chEnglish.isChecked = true
        }

        btnConfirm.setOnClickListener {
            if (isChecking) {
                isChecking = false
                SharedPreference.putLanguage(requireActivity(), isSelectedLanguage)
                refreshFragment()
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
    }

    private fun refreshFragment() {
        ChangeLanguage.setLocale(requireContext(), isSelectedLanguage)
        requireActivity().recreate()
    }
}