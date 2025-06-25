package com.vs.schoolmessenger.School.MarkYourAttendance

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.AcademicYear
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.PunchHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.StaffAttendanceReportAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.YearLoadingAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchTimingsData
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportData
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffLocationData
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.AttendanceReportClickListener
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.GPSStatusListener
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.LocationLatLongListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.GPSStatusReceiver
import com.vs.schoolmessenger.Utils.LocationDistanceCalculator
import com.vs.schoolmessenger.Utils.LocationHelper
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.MarkYourAttendanceBinding
import java.util.Calendar

class MarkYourAttendance : BaseActivity<MarkYourAttendanceBinding>(), View.OnClickListener,
    GPSStatusListener, LocationLatLongListener, AttendanceReportClickListener {

    override fun getViewBinding(): MarkYourAttendanceBinding {
        return MarkYourAttendanceBinding.inflate(layoutInflater)
    }

    private var isStaffAttendanceReportAdapter: StaffAttendanceReportAdapter? = null
    private var isPunchHistoryAdapter: PunchHistoryAdapter? = null
    private var appViewModel: App? = null
    private lateinit var gpsStatusReceiver: GPSStatusReceiver
    private val locationRequestCode = 1000
    private var biometricPrompt: BiometricPrompt? = null
    private var isStaffDetails: StaffDetails? = null
    private var isAccessToken: String? = null
    private var isLatitude: Double? = null
    private var isLongitude: Double? = null
    private var rcyPunchList: RecyclerView? = null
    private var lblNoRecordsFound: TextView? = null
    private var lblName: TextView? = null
    private var lblDate: TextView? = null
    private var lblSchoolName: TextView? = null
    private var lblDesignation: TextView? = null
    var isAcademicYear: List<AcademicYear>? = null
    var isAcademicYearId = -1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        binding.btnEnableLocation.setOnClickListener(this)
        binding.btnPresent.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)

        binding.toolbarLayout.lblParentToolBar.text = getString(R.string.Geometric_Attendance)
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblSchoolName.visibility = View.VISIBLE
        binding.toolbarLayout.lblSchoolName.text = isStaffDetails!!.school_name

        if (isStaffDetails!!.biometric_enable) {
            binding.rytAddLocation.visibility = View.VISIBLE
        } else {
            binding.rytAddLocation.visibility = View.GONE
        }
        binding.rytAddLocation.setOnClickListener {
            val intent = Intent(this@MarkYourAttendance, AddLocationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        gpsStatusReceiver = GPSStatusReceiver(this)


        isLoadYear(Constant.isAcademicYearList)

        appViewModel!!.isStaffLocations?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rytProgressBar.visibility = View.GONE
                    binding.rytNoLocationList.visibility = View.GONE
                    val isStaffLocation = response.data
                    if (isStaffLocation.isNotEmpty()) {
                        binding.rytErrorMessage.visibility = View.GONE
                        punchHiddenShow(isStaffLocation)
                    } else {
                        binding.rytErrorMessage.visibility = View.VISIBLE
                        binding.rytNoLocationList.visibility = View.GONE
                    }
                } else {
                    binding.rytNoLocationList.visibility = View.VISIBLE
                    binding.lblNoLocation.text = response.message
                }
            } else {
                binding.rytNoLocationList.visibility = View.GONE
                binding.rytErrorMessage.visibility = View.VISIBLE
            }
        }

        appViewModel!!.isPunchAttendance?.observe(this) { response ->
            if (response!!.status) {
                Constant.hideLoading(this)
                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isStaffAttendanceReport?.observe(this) { response ->
            if (response != null && response.status) {
                val isStaffReport = response.data
                if (isStaffReport.isNotEmpty()) {
                    isLoadData(isStaffReport)
                }
            } else {
                binding.recycleAttendanceReports.visibility = View.GONE
                binding.lblNoRecords.visibility = View.VISIBLE
                binding.imgNorecord.visibility = View.VISIBLE
                binding.linearagendalayout.visibility = View.GONE
                binding.lblNoRecords.text = response?.message ?: "No Data Available"
            }
        }

        appViewModel!!.isPunchHistory?.observe(this) { response ->
            if (response != null && response.status) {
                val historyList = response.data
                if (historyList.isNotEmpty()) {
                    val isPunchTiming = historyList.flatMap { it.timings }
                    isLoadPunchHistoryData(isPunchTiming)
                }
            } else {
                rcyPunchList!!.visibility = View.GONE
                lblNoRecordsFound!!.visibility = View.VISIBLE
                lblNoRecordsFound!!.text = response!!.message
            }
        }
    }

    private fun isLoadYear(isAcademicYear: List<AcademicYear>?) {
        val uniqueYears =
            isAcademicYear!!.mapNotNull { it.year.split("-").firstOrNull() }.distinct()
        val currentYear =
            isAcademicYear.find { it.current_academic_year }?.year?.split("-")?.firstOrNull()
        val sortedYears = if (currentYear != null) {
            listOf(currentYear) + uniqueYears.filter { it != currentYear }
        } else {
            uniqueYears
        }

        val adapter = YearLoadingAdapter(this, sortedYears)
        binding.spinnerYears.adapter = adapter

        binding.spinnerYears.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()

                binding.lblNoRecords.visibility = View.GONE
                binding.imgNorecord.visibility = View.GONE
                val selectedYear = parent.getItemAtPosition(position).toString()
                isLoadMonth(selectedYear)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun isLoadData(isStaffReport: List<StaffAttendanceReportData>) {
        if (isStaffReport.isNotEmpty()) {
            Constant.executeAfterDelay {
                binding.recycleAttendanceReports.visibility = View.VISIBLE
                binding.linearagendalayout.visibility = View.VISIBLE
                binding.lblNoRecords.visibility = View.GONE
                binding.imgNorecord.visibility = View.GONE
                isStaffAttendanceReportAdapter = StaffAttendanceReportAdapter(
                    isStaffReport, this, this, Constant.isShimmerViewDisable
                )
                binding.recycleAttendanceReports.adapter = isStaffAttendanceReportAdapter
            }
        }
    }

    private fun isLoadMonth(selectedYear: String) {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val adapter = SpinnerLoadingAdapter(this, months)
        binding.spinnerMonths.adapter = adapter

        // Get the current month (0-based index)
        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        adapter.selectedPosition = currentMonthIndex

        // Set the spinner to the current month
        binding.spinnerMonths.setSelection(currentMonthIndex)

        binding.spinnerMonths.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()

                    val selectedMonthNumber = String.format("%02d", position + 1)
                    binding.lblNoRecords.visibility = View.GONE
                    binding.imgNorecord.visibility = View.GONE

                    getStaffAttendanceReport(selectedYear, selectedMonthNumber)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        // Trigger report call immediately for the default selected month
        val selectedMonthNumber = String.format("%02d", currentMonthIndex + 1)
        getStaffAttendanceReport(selectedYear, selectedMonthNumber)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION).apply {
            addAction(Intent.ACTION_PROVIDER_CHANGED) // Optional extra compatibility
        }
        registerReceiver(gpsStatusReceiver, filter, RECEIVER_NOT_EXPORTED)

        Log.d("onResume", "onResume")
        getLocationPermissions()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Constant.isGPSEnabled(this)) {
                    binding.rytGPSRedirect.visibility = View.GONE
                    getCurrentLocation(Constant.new)
                } else {
                    binding.rytGPSRedirect.visibility = View.VISIBLE
                    binding.rytNoLocationList.visibility = View.GONE
                    binding.rytPresentlayout.visibility = View.GONE
                    binding.rytErrorMessage.visibility = View.GONE
                }
            } else {
                if (Constant.isGPSEnabled(this)) {
                    binding.rytGPSRedirect.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getCurrentLocation(type: String) {
        binding.rytProgressBar.visibility = View.VISIBLE
        val locationHelper = LocationHelper(this, this, Constant.current)
        locationHelper.getFreshLocation()
    }


    fun isGetGeoMetricStaffLocations() {
        isAccessToken?.let {
            appViewModel?.getStaffLocations(it, this)
        }
    }

    fun isPunchAttendance() {

        val jsonObject = JsonObject()
        jsonObject.addProperty(APIKeyNames.staff_or_student, Constant.staff)
        jsonObject.addProperty(APIKeyNames.device_id, Constant.getAndroidSecureId(this))
        jsonObject.addProperty(APIKeyNames.punch_type, 1)
        jsonObject.addProperty(APIKeyNames.device_model, Constant.getDeviceName())

        isAccessToken?.let {
            appViewModel?.punchAttendance(it, jsonObject, this)
        }
    }


    fun getStaffAttendanceReport(selectedYear: String, selectedMonth: String) {
        binding.recycleAttendanceReports.visibility = View.VISIBLE
        binding.linearagendalayout.visibility = View.VISIBLE
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
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), locationRequestCode
            )
        } else {
            if (Constant.isGPSEnabled(this)) {
                binding.rytGPSRedirect.setVisibility(View.GONE)
                getCurrentLocation(Constant.new)
            } else {
                binding.rytGPSRedirect.setVisibility(View.VISIBLE)
                binding.rytNoLocationList.setVisibility(View.GONE)
                binding.rytErrorMessage.setVisibility(View.GONE)
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


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnEnableLocation -> {
                redirectToEnableGPS()
            }

            R.id.btnPresent -> {
                authenticatStart()
            }

            R.id.btnHistory -> {
                binding.rytAddLocation.visibility = View.GONE
                binding.rytProgressBar.visibility = View.GONE
                isBackgroundChange(binding.btnHistory)
            }

            R.id.btnCreate -> {
                if (isStaffDetails!!.biometric_enable) {
                    binding.rytAddLocation.visibility = View.VISIBLE
                } else {
                    binding.rytAddLocation.visibility = View.GONE
                }
                binding.rytProgressBar.visibility = View.VISIBLE
                binding.rytPresentlayout.visibility = View.GONE
                isBackgroundChange(binding.btnCreate)
                val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION).apply {
                    addAction(Intent.ACTION_PROVIDER_CHANGED) // Optional extra compatibility
                }
                registerReceiver(gpsStatusReceiver, filter, RECEIVER_NOT_EXPORTED)
                getLocationPermissions()
            }
        }
    }

    private fun isBackgroundChange(btnClick: TextView) {

        binding.btnCreate.background = null
        binding.btnHistory.background = null
        binding.lblNoRecords.visibility = View.GONE
        binding.imgNorecord.visibility = View.GONE
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

    private fun authenticatStart() {
        val biometricManager = BiometricManager.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val authenticators =
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

            when (biometricManager.canAuthenticate(authenticators)) {
                BiometricManager.BIOMETRIC_SUCCESS -> showBiometricPrompt(authenticators)
                else -> {
                    Log.d("BiometricAuth", "No biometric/PIN/PATTERN available. Proceeding...")
                    isPunchAttendance()
                }
            }
        } else {
            // For Android 8.x (Oreo)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> showLegacyBiometricPrompt()
                else -> {
                    Log.d("BiometricAuth", "Legacy biometric not available. Proceeding...")
                    isPunchAttendance()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showBiometricPrompt(authenticators: Int) {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(
            this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("BiometricAuth", "Authentication succeeded")
                    isPunchAttendance()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.d("BiometricAuth", "Authentication error: $errString")

                    if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS || errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE || errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT) {
                        isPunchAttendance()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d("BiometricAuth", "Authentication failed")
                }
            })

        val promptInfo = PromptInfo.Builder().setTitle("Authenticate")
            .setSubtitle("Use fingerprint, face, PIN, or pattern")
            .setAllowedAuthenticators(authenticators).build()

        biometricPrompt?.authenticate(promptInfo)
    }

    private fun showLegacyBiometricPrompt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            if (keyguardManager.isKeyguardSecure) {
                val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                    "Authentication Required", "Please confirm your screen lock PIN or pattern"
                )
                startActivityForResult(intent, 1001)
            } else {
                isPunchAttendance()
            }
        } else {
            isPunchAttendance()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("BiometricAuth", "PIN/Pattern auth succeeded")
                isPunchAttendance()
            } else {
                Log.d("BiometricAuth", "PIN/Pattern auth cancelled or failed")
            }
        }
    }

    private fun redirectToEnableGPS() {
        this@MarkYourAttendance.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    override fun onGPSStatusChanged(isGPSEnabled: Boolean) {
        Log.d("isEnableLocation", "isEnableLocation")
        getLocationPermissions()
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
                if (isStaffLocation[i].latitude != "" && isStaffLocation[i].longitude != "" && isStaffLocation[i].distance != "") {
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
        }

        if (isStaffNearByLocation!!) {
            binding.rytErrorMessage.visibility = View.GONE
            binding.rytPresentlayout.visibility = View.VISIBLE
        } else {
            binding.rytErrorMessage.visibility = View.VISIBLE
            binding.rytPresentlayout.visibility = View.GONE
        }
    }

    override fun onItemClick(data: StaffAttendanceReportData) {
        isLoadPunchHistory(data)
    }

    private fun isLoadPunchHistory(data: StaffAttendanceReportData) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.punch_history, null)

        lblName = view.findViewById<TextView>(R.id.lblStaffName)
        lblDate = view.findViewById<TextView>(R.id.lblDate)
        lblSchoolName = view.findViewById<TextView>(R.id.lblSchoolName)
        lblDesignation = view.findViewById<TextView>(R.id.lblDesignation)

        lblDate!!.text = Constant.convertDateTimeFormat(data.date)
        lblName!!.text = data.name
        lblSchoolName!!.text = isStaffDetails!!.school_name
        lblDesignation!!.text = data.role


        rcyPunchList = view.findViewById<RecyclerView>(R.id.rcyPunchList)
        lblNoRecordsFound = view.findViewById<TextView>(R.id.lblNoRecordsFound)
        val imgBack = view.findViewById<ImageView>(R.id.imgBack)
        isPunchHistory(data)

        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val width = (resources.displayMetrics.widthPixels * 0.96).toInt()
        val params = WindowManager.LayoutParams()
        params.copyFrom(dialog.window?.attributes)
        params.width = width - (2 * dpToPx(10))
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = params

        imgBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun isPunchHistory(data: StaffAttendanceReportData) {

        isAccessToken?.let {
            appViewModel?.getPunchHistory(it, data.date, data.staff_id, this)
        }
    }

    fun isLoadPunchHistoryData(data: List<PunchTimingsData>) {
        if (data.isNotEmpty()) {
            rcyPunchList!!.visibility = View.VISIBLE
            lblNoRecordsFound!!.visibility = View.GONE
            isPunchHistoryAdapter = PunchHistoryAdapter(
                data, this, Constant.isShimmerViewDisable
            )
            rcyPunchList!!.layoutManager = LinearLayoutManager(this)
            rcyPunchList!!.adapter = isPunchHistoryAdapter
        } else {
            rcyPunchList!!.visibility = View.GONE
            lblNoRecordsFound!!.text = getString(R.string.Punch_History_found)
            lblNoRecordsFound!!.visibility = View.VISIBLE
        }
    }
}