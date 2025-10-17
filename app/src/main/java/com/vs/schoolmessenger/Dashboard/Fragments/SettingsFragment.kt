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
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewManagerFactory
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.CreateResetChangePassword.PasswordGeneration
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.TermsConditions.TermsAndConditions
import com.vs.schoolmessenger.Dashboard.School.SchoolDashboard
import com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.WhatsNewActivity
import com.vs.schoolmessenger.Dashboard.Settings.ContactUs.ContactUs
import com.vs.schoolmessenger.Dashboard.Settings.Faq.Faq
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug.ReportTheBug
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ChangeLanguage
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isAwsUploadedFiles
import com.vs.schoolmessenger.Utils.Constant.isCommunicationType
import com.vs.schoolmessenger.Utils.Constant.selectedFiles
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
    private val REQUEST_CONTACT_PERMISSION = 1001

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
        binding.lnrSaveContact.setOnClickListener(this)
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
                isShowLogoutPopup()
            }

            R.id.lnrwhatsnew -> {
                RedirectToWhatsnew()
            }

            R.id.lnrSaveContact -> {
                checkContactPermission()
            }
        }
    }


    private fun RedirectToWhatsnew() {
        val intent = Intent(requireContext(), WhatsNewActivity::class.java)
        startActivity(intent)
    }

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_CONTACT_PERMISSION
            )
        } else {
            if(!Constant.isGlobalVariableData!!.v_card_numbers.equals("")) {

                val contacts = mutableListOf<Pair<String, String>>()

                val numbers =  Constant.isGlobalVariableData!!.v_card_numbers.split(",")
                for (item in numbers) {
                    contacts.add(Pair(Constant.isGlobalVariableData!!.contact_display_name, item.trim()))
                }
                val missingContacts = contacts.filterNot { contactExists(it.second) }
                if (missingContacts.isNotEmpty()) {
                    saveContactsPopup(missingContacts)
                }
                else{
                    Toast.makeText(requireActivity(), "All contacts are already saved", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveContactsPopup(missingContacts: List<Pair<String, String>>)   {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.save_contact_popup, null)

        val alertTitle: TextView = view.findViewById(R.id.alertTitle)
        val alertMessage: TextView = view.findViewById(R.id.alertMessage)
        alertTitle.setText(Constant.isGlobalVariableData!!.contact_alert_title)
        alertMessage.setText(Constant.isGlobalVariableData!!.contact_alert_content)

        val btnSave: TextView = view.findViewById(R.id.lblSave)
        val btnNo: TextView = view.findViewById(R.id.lblNo)

        val rootView = requireActivity().findViewById<ViewGroup>(android.R.id.content)

        val dimView = View(activity).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true // prevent clicks on background
        }

        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, requireActivity().resources.displayMetrics
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

        btnSave.setOnClickListener {
            closePopup()
            saveContacts(missingContacts)
        }
        btnNo.setOnClickListener {
            closePopup()
        }
        dimView.isFocusable = true
        dimView.isFocusableInTouchMode = true
    }

    private fun saveContacts(missingContacts: List<Pair<String, String>>) {
        val newContacts = Array(missingContacts.size) { "" }
        // Loop through and check which contacts are missing
        for (i in missingContacts.indices) {
            val contact = missingContacts[i]
            if (!contactExists(contact.second)) {
                Log.d("Index", "Current index = $i")
                newContacts[i] = contact.second
            }
        }

        // Convert image to byte array (for contact photo)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.school_chimes_logo)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        val data = ArrayList<ContentValues>()

        // Add contact photo
        val rowPhoto = ContentValues().apply {
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArray)
        }
        data.add(rowPhoto)

        // Add all phone numbers
        for (i in newContacts.indices) {
            val number = newContacts[i]
            if (number.isNotEmpty()) {
                val rowNumber = ContentValues().apply {
                    put(
                        ContactsContract.RawContacts.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    put(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
                }
                data.add(rowNumber)
            }
        }

        // Prepare Intent to insert contact (user will confirm)
        val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
        intent.putExtra(ContactsContract.Intents.Insert.NAME, Constant.isGlobalVariableData!!.contact_display_name) // set contact name
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data)

        startActivityForResult(intent, 100)
    }

    private fun contactExists(phoneNumber: String): Boolean {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        val projection = arrayOf(ContactsContract.PhoneLookup._ID)
        var exists = false
        val resolver = requireActivity().contentResolver
        val cursor = resolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                exists = true
            }
        }
        return exists
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