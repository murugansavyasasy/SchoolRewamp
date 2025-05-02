package com.vs.schoolmessenger.School.MarkYourAttendance

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Repository.ResponseKeys
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.LocationHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.LocationHistoryData
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.LocationHistoryClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AddLocationActivityBinding
import java.util.Calendar
import java.util.Locale


class AddLocationActivity : BaseActivity<AddLocationActivityBinding>(), View.OnClickListener,
    LocationHistoryClickListener {

    override fun getViewBinding(): AddLocationActivityBinding {
        return AddLocationActivityBinding.inflate(layoutInflater)
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var isLatitude: Double? = null
    var isLongitude: Double? = null
    var isDistance: String? = null
    var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null
    private var recyleLocations: RecyclerView? = null
    var isLocationHistoryAdapter: LocationHistoryAdapter? = null
    var isDeletedId: Int? = null
    private lateinit var dialog: Dialog
    private lateinit var view: View
    private lateinit var dialogRootView: ConstraintLayout


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.btnAddLocation.setOnClickListener(this)
        binding.btnViewLocations.setOnClickListener(this)
        binding.webViewMap.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel?.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token

        isLoadMeter()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()

        appViewModel!!.isAddLocation?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.hideLoading(this@AddLocationActivity)
                Constant.showTopAlertPopup(response.message, Constant.isGioMetric, this)
            }
        }

        appViewModel!!.isLocationHistory?.observe(this) { response ->
            if (response != null && response.status) {
                val isLocationHistory = response.data
                isLoadLocationHistory(isLocationHistory)
            }
        }

        appViewModel!!.isUpdateLocation?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.hideLoading(this@AddLocationActivity)
                val dialogRootView = view as ViewGroup
                showTopAlertPopup(response.message, dialogRootView, -1, response.status, "isUpdate")
            }
        }


        appViewModel!!.isRemoveLocation?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.hideLoading(this)
                val dialogRootView = view as ViewGroup
                showTopAlertPopup(response.message, dialogRootView, -1, response.status, "isRemove")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showTopAlertPopup(
        message: String,
        rootView: ViewGroup,
        id: Int,
        isStatus: Boolean,
        isDeleteLocation: String
    ) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.success_popup, null)

        val messageText = popupView.findViewById<TextView>(R.id.alertMessage)
        val okButton = popupView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = popupView.findViewById<TextView>(R.id.btnCancel)
        if (id != -1) {
            btnCancel.visibility = View.VISIBLE
            isDeletedId = id
        } else {
            btnCancel.visibility = View.GONE
        }
        messageText.text = message

        // Dim background to cover full screen
        val dimView = View(this).apply {
            setBackgroundColor(Color.parseColor("#80000000")) // semi-transparent black
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        // Create layout params with constraints and margin
        val marginInPx =
            resources.getDimensionPixelSize(R.dimen.fourty_five) // Adjust to your needs
        val popupLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            setMargins(marginInPx, 0, marginInPx, 0)
        }

        rootView.addView(dimView)
        rootView.addView(popupView, popupLayoutParams)

        val closePopup = {
            rootView.removeView(popupView)
            rootView.removeView(dimView)
        }

        if (id == -1 && isStatus && isDeleteLocation == "isRemove") {
            isLocationHistoryAdapter?.removeItemById(isDeletedId!!)
        }

        okButton.setOnClickListener {
            if (id != -1 && isDeleteLocation == "isRemove") {
                val jsonObject = JsonObject()
                jsonObject.addProperty(ResponseKeys.location_id, id)
                Log.d("jsonObject", jsonObject.toString())
                isAccessToken?.let {
                    appViewModel?.removeLocation(it, jsonObject, this)
                }
            } else if (isDeleteLocation == "isUpdate") {
                isShowLocationHistory()
            }
            closePopup()
        }
        btnCancel.setOnClickListener { closePopup() }
        dimView.setOnClickListener { closePopup() }
    }

    private fun getCurrentLocation() {
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
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                isLatitude = latitude
                isLongitude = longitude
                val isLocationName = getAddressFromLocation(isLatitude!!, isLongitude!!)
                binding.lblAddress.text = isLocationName
            } else {
                println("Location is null. Try again later or enable location.")
            }
        }
    }

    private fun openInGoogleMaps(lat: Double, lng: Double, isTitle: String) {
        val encodedTitle = Uri.encode(isTitle)
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($encodedTitle)")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                address.getAddressLine(0) ?: "Address not found"
            } else {
                "No address found"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Geocoder error"
        }
    }

    private fun isLoadMeter() {
        val months = listOf(
            "10", "15", "20", "25", "30", "35",
            "40", "45", "50", "55", "60", "75"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerMetres.adapter = monthAdapter
        binding.spinnerMetres.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedMeter = parent.getItemAtPosition(position).toString()
                isDistance = selectedMeter
                binding.txtMeters.setText(selectedMeter)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnAddLocation -> {
                isCheckValidation()
            }

            R.id.btnViewLocations -> {
                isLocationHistory()
            }

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.webViewMap -> {
                val isLocationName = getAddressFromLocation(isLatitude!!, isLongitude!!)
                binding.lblAddress.text = isLocationName
                openInGoogleMaps(isLatitude!!, isLongitude!!, isLocationName)
            }
        }
    }

    fun isSaveLocation() {
        Constant.showLoading(this@AddLocationActivity)

        val jsonObject = JsonObject()
        jsonObject.addProperty("location", binding.txtLocationName.text.toString())
        jsonObject.addProperty("longitude", isLongitude)
        jsonObject.addProperty("latitude", isLatitude)
        jsonObject.addProperty("distance", isDistance)
        Log.d("jsonObject", jsonObject.toString())
        isAccessToken?.let {
            appViewModel?.addLocation(it, jsonObject, this)
        }
    }


    private fun isCheckValidation() {
        if (isLatitude != null && isLongitude != null) {
            if (isValidLatLng(isLatitude, isLongitude)) {
                if (binding.txtLocationName.text.toString() != "") {
                    if (!isDistance.equals("") || isDistance != null) {
                        isSaveLocation()
                    } else {
                        showInvalidLocationDialog("Alert", "Choose or enter the distance")
                    }
                } else {
                    showInvalidLocationDialog("Alert", "Enter the location name")
                }
            } else {
                showInvalidLocationDialog(
                    "Location Error",
                    "Unable to get your location. Please ensure location is enabled and try again."
                )
            }
        }
    }

    private fun showInvalidLocationDialog(isAlert: String, isErrorContent: String) {
        AlertDialog.Builder(this)
            .setTitle(isAlert)
            .setMessage(isErrorContent)
            .setPositiveButton("OK", null)
            .show()
    }


    fun isValidLatLng(latitude: Double?, longitude: Double?): Boolean {
        return latitude != null && longitude != null &&
                latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    private fun isLocationHistory() {
        dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        view = LayoutInflater.from(this).inflate(R.layout.locations_history, null)
        dialogRootView = view.findViewById(R.id.constParent)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        recyleLocations = view.findViewById<RecyclerView>(R.id.recyleLocations)
        val imgClose = view.findViewById<ImageView>(R.id.imgClose)
        isShowLocationHistory()
        imgClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun isShowLocationHistory() {
        isLocationHistoryAdapter =
            LocationHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
        recyleLocations!!.layoutManager = LinearLayoutManager(this)
        recyleLocations!!.adapter = isLocationHistoryAdapter

        isAccessToken?.let {
            appViewModel?.getLocationHistory(it, this)
        }
    }

    private fun isLoadLocationHistory(isLocationHistory: List<LocationHistoryData>) {
        if (isLocationHistory.isNotEmpty()) {
            recyleLocations!!.visibility = View.VISIBLE
//            Constant.executeAfterDelay {

            isLocationHistoryAdapter = LocationHistoryAdapter(
                isLocationHistory, this, this, Constant.isShimmerViewDisable
            )
            recyleLocations!!.adapter = isLocationHistoryAdapter
//            }
        } else {
            recyleLocations!!.visibility = View.GONE
        }
    }

    private fun showEditLocationPopup(id: Int, rootView: ViewGroup) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.edit_location, null)

        val edtLocationName = popupView.findViewById<EditText>(R.id.edtLocationName)
        val edtDistance = popupView.findViewById<EditText>(R.id.edtDistance)
        val btnCancel = popupView.findViewById<Button>(R.id.btnCancel)
        val btnUpdate = popupView.findViewById<Button>(R.id.btnUpdate)

        val dimView = View(this).apply {
            setBackgroundColor(Color.parseColor("#80000000")) // semi-transparent black
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val marginInPx = resources.getDimensionPixelSize(R.dimen.ten)
        val popupLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            setMargins(marginInPx, 0, marginInPx, 0)
        }
        rootView.addView(dimView)
        rootView.addView(popupView, popupLayoutParams)

        val closePopup = {
            rootView.removeView(popupView)
            rootView.removeView(dimView)
        }

        btnCancel.setOnClickListener { closePopup() }

        btnUpdate.setOnClickListener {
            val locationName = edtLocationName.text.toString().trim()
            val distance = edtDistance.text.toString().trim()

            if (locationName.isNotEmpty() || distance.isNotEmpty()) {
                Constant.showLoading(this@AddLocationActivity)

                val jsonObject = JsonObject().apply {
                    addProperty("id", id)
                    addProperty("location", locationName)
                    addProperty("distance", distance)
                }
                Log.d("jsonObject", jsonObject.toString())

                isAccessToken?.let {
                    appViewModel?.updateLocation(it, jsonObject, this)
                }
                closePopup()
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dimView.setOnClickListener { closePopup() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(data: LocationHistoryData, isType: String) {
        val dialogRootView = view as ViewGroup
        if (isType == "isDelete") {
            showTopAlertPopup(
                "Are you sure want to delete this location?",
                dialogRootView,
                data.id, false, "isRemove"
            )
        } else {
            showEditLocationPopup(data.id, dialogRootView)
        }
    }
}