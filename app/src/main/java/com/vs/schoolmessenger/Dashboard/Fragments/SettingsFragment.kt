package com.vs.schoolmessenger.Dashboard.Fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
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
    private lateinit var binding: SettingsFragmentBinding // Automatically generated binding class

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

        val isAppLanguage = SharedPreference.getLanguage(requireActivity())


        // Inflate the custom dialog layout
        val dialogView =
            LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_language_selector, null)

        // Create the AlertDialog
        val alertDialog = AlertDialog.Builder(requireActivity())
            .setTitle("Select Language")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Get references to the radio group and button
        val radioGroup: RadioGroup = dialogView.findViewById(R.id.radio_language_group)
        val applyButton: Button = dialogView.findViewById(R.id.btn_apply_language)
        val isEnglish: RadioButton = dialogView.findViewById(R.id.radio_english)
        val isTamil: RadioButton = dialogView.findViewById(R.id.radio_tamil)
        val isThai: RadioButton = dialogView.findViewById(R.id.radio_thai)
        val isHindi: RadioButton = dialogView.findViewById(R.id.radio_hindi)

        if (isAppLanguage.equals("ta")) {
            isTamil.isChecked = true
        } else if (isAppLanguage.equals("th")) {
            isThai.isChecked = true
        } else if (isAppLanguage.equals("hi")) {
            isHindi.isChecked = true
        } else if (isAppLanguage.equals("isLanguage")) {
            isEnglish.isChecked = true
        }

        // Apply button click listener
        applyButton.setOnClickListener {
            // Get the selected radio button ID
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            val selectedLanguage = when (selectedRadioButtonId) {
                R.id.radio_english -> "en"
                R.id.radio_tamil -> "ta"
                R.id.radio_thai -> "th"
                R.id.radio_hindi -> "hi"
                else -> "en"
            }
            setAppLocale(selectedLanguage)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun setAppLocale(languageCode: String) {
        // Set the selected language
        val context = ChangeLanguage.setLocale(requireContext(), languageCode)
        val resources = context!!.resources
        val configuration = resources.configuration
        resources.updateConfiguration(configuration, resources.displayMetrics)
        SharedPreference.putLanguage(requireActivity(), languageCode)

        // Recreate the parent activity from the fragment
        activity?.recreate() // Calls recreate on the parent Activity
    }

}