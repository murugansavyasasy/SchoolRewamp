package com.vs.schoolmessenger.School.MarkYourAttendance

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.GPSStatusReceiver
import com.vs.schoolmessenger.Utils.LocationHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MarkYourAttendanceBinding
import java.util.Calendar

class MarkYourAttendance : BaseActivity<MarkYourAttendanceBinding>(),
    View.OnClickListener, GPSStatusListener, LocationLatLongListener,
    AttendanceReportClickListener {

    override fun getViewBinding(): MarkYourAttendanceBinding {
        return MarkYourAttendanceBinding.inflate(layoutInflater)
    }
    var isStaffAttendanceReportAdapter: StaffAttendanceReportAdapter? = null

    private var appViewModel: App? = null

    var ifBiometricAvailable: Boolean = false
    private lateinit var gpsStatusReceiver: GPSStatusReceiver
    private  val locationRequestCode = 1000
    private var biometricPrompt: BiometricPrompt? = null

    private var authenticatealertpopupWindow: PopupWindow? = null
    private var enableBiometricPopup: PopupWindow? = null
    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null
    private var isLatitude: Double? = null
    private var isLongitude: Double? = null


    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        binding.btnEnableLocation.setOnClickListener(this)
        binding.btnPresent.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)

        binding.toolbarLayout.rytAddLocation.visibility = View.VISIBLE


        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        val isEnabled = SharedPreference.getBiometricEnabled(this@MarkYourAttendance)
        binding.enableSwitch.setChecked(isEnabled!!)
        binding.enableSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                val enabled = SharedPreference.getBiometricEnabled(this@MarkYourAttendance)
                if (!enabled!!) {
                    enableLocalFingerPrint()
                }
            } else {
                val enabled = SharedPreference.getBiometricEnabled(this@MarkYourAttendance)
                if (enabled!!) {
                    showFingerPrintDisablepopup()
                }
            }
        })

        binding.toolbarLayout.rytAddLocation.setOnClickListener {
            val intent = Intent(this@MarkYourAttendance, AddLocationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
//                binding.rytEnableFingerPrint.visibility = View.VISIBLE
                ifBiometricAvailable = true

                Log.d(
                    "BIOMETRIC_STATUS",
                    "Biometric features are available and the user has enrolled biometric credentials"
                )
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                ifBiometricAvailable = false
//                binding.rytEnableFingerPrint.visibility = View.GONE

                Log.d("BIOMETRIC_STATUS", "No biometric hardware available on this device")
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                ifBiometricAvailable = false
//                binding.rytEnableFingerPrint.visibility = View.GONE

                Log.d("BIOMETRIC_STATUS", "Biometric hardware is currently unavailable")
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                ifBiometricAvailable = false
//                binding.rytEnableFingerPrint.visibility = View.GONE
                Log.d(
                    "BIOMETRIC_STATUS",
                    "No biometric data enrolled; prompt the user to set up biometrics"
                )
            }
        }

        gpsStatusReceiver = GPSStatusReceiver(this)

        appViewModel!!.isStaffLocations?.observe(this) { response ->
            if (response != null && response.status) {
                val isStaffLocation = response.data
                punchHiddenShow(isStaffLocation)
            }
        }

        appViewModel!!.isStaffAttendanceReport?.observe(this) { response ->
            if (response != null && response.status) {
                val isStaffReport = response.data
                isLoadData(isStaffReport)
            }
        }

        val years = (2025 downTo 2001).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYears.adapter = adapter
        binding.spinnerYears.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedYear = parent.getItemAtPosition(position).toString()
                isLoadMonth(selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun isLoadData(isStaffReport: List<StaffAttendanceReportData>) {
        if (isStaffReport.isNotEmpty()) {
            Constant.executeAfterDelay {

                isStaffAttendanceReportAdapter =
                    StaffAttendanceReportAdapter(
                        isStaffReport,
                        this,
                        this,
                        Constant.isShimmerViewDisable
                    )
                binding.recycleAttendanceReports.adapter = isStaffAttendanceReportAdapter
            }
        } else {
            binding.recycleAttendanceReports.visibility = View.GONE
        }
    }

    fun isLoadMonth(selectedYear: String) {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerMonths.adapter = monthAdapter

        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        binding.spinnerMonths.setSelection(currentMonthIndex)

        binding.spinnerMonths.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedMonthNumber = String.format("%02d", position + 1)
                getStaffAttendanceReport(selectedYear, selectedMonthNumber)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION).apply {
            addAction(Intent.ACTION_PROVIDER_CHANGED) // Optional extra compatibility
        }
        registerReceiver(gpsStatusReceiver, filter)

        Log.d("onResume", "onResume")
        getLocationPermissions()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Constant.isGPSEnabled(this)) {
                    binding.rytGPSRedirect.visibility = View.GONE
                    getCurrentLocation("new")
                } else {
                    binding.rytGPSRedirect.visibility = View.VISIBLE
                    binding.rytPresentlayout.visibility = View.GONE
                    binding.lblErrorMessage.visibility = View.GONE
                }
            } else {
                Toast.makeText(this, R.string.Permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation(type: String) {
       binding.rytProgressBar.visibility = View.VISIBLE
        val locationHelper = LocationHelper(this, this, "current")
        locationHelper.getFreshLocation()
    }


    fun isGetGeoMetricStaffLocations() {
        isAccessToken?.let {
            appViewModel?.getStaffLocations(it, this)
        }
    }

    fun getStaffAttendanceReport(selectedYear: String, selectedMonth: String) {
        binding.recycleAttendanceReports.visibility = View.VISIBLE
        isStaffAttendanceReportAdapter =
            StaffAttendanceReportAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.recycleAttendanceReports.layoutManager = LinearLayoutManager(this)
        binding.recycleAttendanceReports.adapter = isStaffAttendanceReportAdapter

        isAccessToken?.let {
            appViewModel?.getStaffAttendanceReport(it, "$selectedYear-$selectedMonth", this)
        }
    }

    private fun getLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationRequestCode
            )
        } else {
            if (Constant.isGPSEnabled(this)) {
                binding.rytGPSRedirect.setVisibility(View.GONE)
                getCurrentLocation("new")
            } else {
                binding.rytGPSRedirect.setVisibility(View.VISIBLE)
                binding.lblErrorMessage.setVisibility(View.GONE)
                binding.rytPresentlayout.setVisibility(View.GONE)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gpsStatusReceiver)
        Log.d("onPause", "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onDestroy", "onDestroy")
    }

    private fun showFingerPrintDisablepopup() {
        val alertDialog = AlertDialog.Builder(this@MarkYourAttendance)
        alertDialog.setTitle(R.string.Disable_Fingerprint)
        alertDialog.setMessage(R.string.disable_fingerprint_authentication)
        alertDialog.setNegativeButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                dialog.cancel()
                binding.enableSwitch.isChecked = false
//                TeacherUtil_SharedPreference.putBiometricEnabled(
//                    this@PunchStaffAttendanceUsingFinger,
//                    false
//                )
            }
        })
        alertDialog.setPositiveButton(
            "Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.cancel()

//                    val enabled: Boolean =
//                        TeacherUtil_SharedPreference.getBiometricEnabled(this@PunchStaffAttendanceUsingFinger)
//                    enableSwitch.setChecked(enabled)
                }
            })
        val dialog = alertDialog.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun enableLocalFingerPrint() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.biometric_permission_enable_popup, null)
        enableBiometricPopup = PopupWindow(
            layout, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true
        )
        enableBiometricPopup!!.contentView = layout
        binding.rytParent.post(object : Runnable {
            override fun run() {
                enableBiometricPopup!!.showAtLocation(binding.rytParent, Gravity.CENTER, 0, 0)
            }
        })
        val btnAllow = layout.findViewById<View?>(R.id.btnAllow) as TextView
        val btnSkip = layout.findViewById<View?>(R.id.btnSkip) as TextView
        val imgClose = layout.findViewById<View?>(R.id.imgClose) as ImageView
        btnAllow.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
//                TeacherUtil_SharedPreference.putBiometricSkip(
//                    this@PunchStaffAttendanceUsingFinger,
//                    false
//                )
//                TeacherUtil_SharedPreference.putBiometricEnabled(
//                    this@PunchStaffAttendanceUsingFinger,
//                    true
//                )
                binding.enableSwitch.setChecked(true)
                enableBiometricPopup!!.dismiss()
            }
        })

        btnSkip.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
//                TeacherUtil_SharedPreference.putBiometricSkip(
//                    this@PunchStaffAttendanceUsingFinger,
//                    true
//                )
                binding.enableSwitch.setChecked(false)
                enableBiometricPopup!!.dismiss()
            }
        })

        imgClose.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                enableBiometricPopup!!.dismiss()
//                val isEnabled: Boolean =
//                    TeacherUtil_SharedPreference.getBiometricEnabled(this@PunchStaffAttendanceUsingFinger)
//                binding.enableSwitch.setChecked(isEnabled)
            }
        })

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnEnableLocation -> {
                redirectToEnableGPS()
            }

            R.id.btnPresent -> {
                enableBiometric()
            }

            R.id.rytAddLocation -> {

            }

            R.id.btnHistory -> {
                isBackgroundChange(binding.btnHistory)
            }

            R.id.btnCreate -> {
                isBackgroundChange(binding.btnCreate)
            }
        }
    }

    private fun isBackgroundChange(btnClick: TextView) {
        binding.btnCreate.background = null
        binding.btnHistory.background = null

        binding.lnrParent.setBackgroundResource(R.drawable.bg_light_blue)
        btnClick.setBackgroundResource(R.drawable.white_bg_radius)

        if (btnClick == binding.btnCreate) {
            binding.rytMarkAttendanceSceen.visibility = View.VISIBLE
            binding.rytAttendanceHistorySceen.visibility = View.GONE
        }

        if (btnClick == binding.btnHistory) {
            binding.rytMarkAttendanceSceen.visibility = View.GONE
            binding.rytAttendanceHistorySceen.visibility = View.VISIBLE
        }
    }


    private fun enableBiometric() {
        val isEnab = SharedPreference.getBiometricEnabled(this@MarkYourAttendance)
        val isBiometricSkip = SharedPreference.getBiometricSkip(this@MarkYourAttendance)

        if (isBiometricSkip!!) {
            putAttendanceDataAPI(false)
        } else {
            if (!isEnab!! && ifBiometricAvailable) {
                enableLocalFingerPrint()
            } else {
                val isEnabled = SharedPreference.getBiometricEnabled(this@MarkYourAttendance)
                if (isEnabled!!) {
                    authenticatStart()
                } else {
                    putAttendanceDataAPI(false)
                }
            }
        }
    }

    private fun putAttendanceDataAPI(b: Boolean) {

    }
    private fun authenticatStart() {

        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this@MarkYourAttendance,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    //               if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
//                }
                    if (ifBiometricAvailable) {
                        if (authenticatealertpopupWindow != null) {
                            if (authenticatealertpopupWindow!!.isShowing()) {
                                authenticatealertpopupWindow!!.dismiss()
                            }
                        }
                        againAuthenticatePopup()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    putAttendanceDataAPI(true)
                    // Handle successful authentication here
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = PromptInfo.Builder()
            .setTitle(resources.getString(R.string.Biometric_Authentications))
            .setSubtitle(resources.getString(R.string.Mark_attendance_biometric_credential))
            .setNegativeButtonText(resources.getString(R.string.cancel))
            .build()
        biometricPrompt!!.authenticate(promptInfo)

    }

    private fun againAuthenticatePopup() {

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.authenticate_alert_popup, null)

        val authenticateAlertPopupWindow = PopupWindow(
            layout, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true
        )

        authenticateAlertPopupWindow.contentView = layout

        binding.rytParent.post {
            authenticateAlertPopupWindow.showAtLocation(binding.rytParent, Gravity.CENTER, 0, 0)
        }

        val lblAuthenticate = layout.findViewById<TextView>(R.id.lblAuthenticate)
        lblAuthenticate.setOnClickListener {
            authenticateAlertPopupWindow.dismiss()
            authenticatStart()
        }
    }

    private fun redirectToEnableGPS() {
        this@MarkYourAttendance.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    override fun onGPSStatusChanged(isGPSEnabled: Boolean) {
        getLocationPermissions()
        Toast.makeText(this, "GPS Enabled: $isGPSEnabled", Toast.LENGTH_SHORT).show()

    }

    override fun onLocationReturn(latitude: Double, longitude: Double, type: String?) {
        Log.d("isLatitude", latitude.toString())
        Log.d("isLongitude", longitude.toString())
        if (latitude.toString() != "" || longitude.toString() != "") {
            isLatitude = latitude
            isLongitude = longitude
            binding.rytProgressBar.visibility = View.GONE
            isGetGeoMetricStaffLocations()
        }
    }

    private fun punchHiddenShow(isStaffLocation: List<StaffLocationData>) {
        var isDistanceCalculation: LocationDistanceCalculator? = null
        isDistanceCalculation = LocationDistanceCalculator()
        var isStaffNearByLocation: Boolean? = false
        try {
            for (i in isStaffLocation.indices) {
                if (isStaffLocation[i].latitude.toString() != "" && isStaffLocation[i].longitude.toString() != "" && isStaffLocation[i].distance != "") {
                    val isGetDistance: Float = isDistanceCalculation.calculateDistance(
                        isLatitude!!.toDouble(),
                        isLongitude!!.toDouble(),
                        isStaffLocation[i].latitude.toDouble(),
                        isStaffLocation[i].longitude.toDouble()
                    )
                    if (isGetDistance <= isStaffLocation[i].distance.toDouble()) {
                        isStaffNearByLocation = true
                    }
                    break
                }
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            Log.e("MarkYourAttendance", "Error parsing number: ${e.message}")
            // Maybe show an error or skip this step
        }

        if (isStaffNearByLocation!!) {
            binding.lblErrorMessage.visibility = View.GONE
            binding.rytPresentlayout.visibility = View.VISIBLE
        } else {
            binding.lblErrorMessage.visibility = View.VISIBLE
            binding.rytPresentlayout.visibility = View.GONE
        }
    }

    override fun onItemClick(data: StaffAttendanceReportData) {

    }
}