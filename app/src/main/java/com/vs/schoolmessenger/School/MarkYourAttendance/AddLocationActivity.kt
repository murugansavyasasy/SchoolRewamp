package com.vs.schoolmessenger.School.MarkYourAttendance

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
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
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MarkYourAttendance.Adapter.LocationHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.LocationHistoryData
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.LocationHistoryClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.SpinnerLoadingAdapter
import com.vs.schoolmessenger.databinding.AddLocationActivityBinding
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
    private var lblNoRecords: TextView? = null
    var isLocationHistoryAdapter: LocationHistoryAdapter? = null
    var isDeletedId: Int? = null
    private lateinit var view: View
    private var isFirstTime = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
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
            if (response != null) {
                Constant.hideLoading(this@AddLocationActivity)
                showSuccessPopup(response.message,response.status)
//                Constant.showTopAlertPopup(response.message, this)
            }
        }

        appViewModel!!.isLocationHistory?.observe(this) { response ->
            if (response != null) {
                if(response.status) {
                    recyleLocations?.visibility = View.VISIBLE
                    lblNoRecords?.visibility = View.GONE
                    val isLocationHistory = response!!.data
                    isLoadLocationHistory(isLocationHistory, response.message)
                }
                else{
                    recyleLocations?.visibility = View.GONE
                    lblNoRecords?.visibility = View.VISIBLE
                    lblNoRecords?.text = response.message
                }
            }
        }

        appViewModel!!.isUpdateLocation?.observe(this) { response ->
            if (response != null) {
                Constant.hideLoading(this@AddLocationActivity)
                val dialogRootView = view as ViewGroup
                showTopAlertPopup(response.message, dialogRootView, -1, response.status, Constant.isUpdate)
            }
        }

        appViewModel!!.isRemoveLocation?.observe(this) { response ->
            if (response != null && response.status) {
                Constant.hideLoading(this)
                val dialogRootView = view as ViewGroup
                showTopAlertPopup(response.message, dialogRootView, -1, response.status, Constant.isRemove)
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                isLatitude = location.latitude
                isLongitude = location.longitude
                val isLocationName = getAddressFromLocation(isLatitude!!, isLongitude!!)
                binding.lblAddress.text = isLocationName
                binding.lbllatLong.text = "${getString(R.string.Lat)} : ${isLatitude} , ${getString(R.string.Long)} : ${isLongitude}"
            } else {
                println("Location is null. Try again later or enable location.")
            }
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: getString(R.string.address_not_found)
            } else getString(R.string.address_not_found)
        } catch (e: Exception) {
            e.printStackTrace()
            "Geocoder error"
        }
    }

    private fun openInGoogleMaps(lat: Double, lng: Double, isTitle: String) {
        val uri = Uri.parse("${Constant.geo_}$lat${Constant.camma}$lng${Constant.questionQEqual}$lat${Constant.camma}$lng${Constant.leftBracket}${Uri.encode(isTitle)}${Constant.rightBracket}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage(Constant.googleMap)
        startActivity(intent)
    }

    private fun isLoadMeter() {
        val distances =
            listOf("10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "75", "Custom")
        val adapter = SpinnerLoadingAdapter(this, distances)
        binding.spinnerMetres.adapter = adapter

        binding.spinnerMetres.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                adapter.selectedPosition = position
                adapter.notifyDataSetChanged()

                isDistance = parent.getItemAtPosition(position).toString()
                if (!isDistance.equals(Constant.Custom)) {
                    binding.txtMeters.setText(isDistance)
                } else {
                    binding.txtMeters.setText("")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnAddLocation -> isCheckValidation()
            R.id.btnViewLocations -> isLocationHistory()
            R.id.imgBack -> onBackPressed()
            R.id.webViewMap -> {
                val locationName = getAddressFromLocation(isLatitude!!, isLongitude!!)
                binding.lblAddress.text = locationName
                openInGoogleMaps(isLatitude!!, isLongitude!!, locationName)
            }
        }
    }

    private fun isCheckValidation() {
        if (isLatitude != null && isLongitude != null && isValidLatLng(isLatitude, isLongitude)) {
            when {
                binding.txtLocationName.text.toString()
                    .isEmpty() -> showInvalidLocationDialog(
                    getString(R.string.alert),
                    getString(R.string.EnterLocation)
                )

                isDistance.isNullOrEmpty() -> showInvalidLocationDialog(
                    getString(R.string.alert),
                    getString(R.string.Choose_enter_distance)
                )

                else -> isSaveLocation()
            }
        } else {
            showInvalidLocationDialog(
                getString(R.string.Location_Error), getString(R.string.Location_unavailable)
            )
        }
    }

    private fun showInvalidLocationDialog(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton(getString(R.string.permission_ok), null)
            .show()
    }

    private fun isSaveLocation() {
        Constant.showLoading(this)
        val jsonObject = JsonObject().apply {
            addProperty(APIKeyNames.location, binding.txtLocationName.text.toString())
            addProperty(APIKeyNames.longitude, isLongitude.toString())
            addProperty(APIKeyNames.latitude, isLatitude.toString())
            addProperty(APIKeyNames.distance, isDistance.toString())
        }
        Log.d("jsonObject", jsonObject.toString())
        appViewModel?.addLocation(isAccessToken!!, jsonObject, this)
    }

    private fun isLocationHistory() {
        val dialog = Dialog(this)
        view = LayoutInflater.from(this).inflate(R.layout.locations_history, null)

        recyleLocations = view.findViewById(R.id.recyleLocations)
        lblNoRecords = view.findViewById(R.id.lblNoRecords)
        val imgBack = view.findViewById<ImageView>(R.id.imgClose)

        isShowLocationHistory()

        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val width = (resources.displayMetrics.widthPixels * 0.96).toInt()
        val params = WindowManager.LayoutParams()
        params.copyFrom(dialog.window?.attributes)
        params.width = width - (2 * dpToPx(10))
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = params

        imgBack.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun isShowLocationHistory() {
        isLocationHistoryAdapter =
            LocationHistoryAdapter(null, this, this, Constant.isShimmerViewShow)
        recyleLocations?.layoutManager = LinearLayoutManager(this)
        recyleLocations?.adapter = isLocationHistoryAdapter
        appViewModel?.getLocationHistory(isAccessToken!!, this)
    }

    private fun isLoadLocationHistory(data: List<LocationHistoryData>, isMessage: String) {
        if (data.isNotEmpty()) {
            recyleLocations?.visibility = View.VISIBLE
            lblNoRecords?.visibility = View.GONE
            isLocationHistoryAdapter =
                LocationHistoryAdapter(data, this, this, Constant.isShimmerViewDisable)
            recyleLocations?.adapter = isLocationHistoryAdapter
        } else {
            recyleLocations?.visibility = View.GONE
            lblNoRecords?.visibility = View.VISIBLE
            lblNoRecords?.text = isMessage
        }
    }

    private fun showEditLocationPopup(data: LocationHistoryData, rootView: ViewGroup) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.edit_location, null)
        val edtLocationName = popupView.findViewById<EditText>(R.id.edtLocationName)
        val edtDistance = popupView.findViewById<EditText>(R.id.edtDistance)
        val btnCancel = popupView.findViewById<TextView>(R.id.btnCancel)
        val btnUpdate = popupView.findViewById<Button>(R.id.btnUpdate)
        edtLocationName.setText(data.location)
        edtDistance.setText(data.distance)


        val dimView = View(this).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }

        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val marginInPx = resources.getDimensionPixelSize(R.dimen.ten)
            setMargins(marginInPx, marginInPx, marginInPx, marginInPx)

            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        }

        rootView.addView(dimView)
        rootView.addView(popupView, layoutParams)

        val closePopup = {
            rootView.removeView(popupView)
            rootView.removeView(dimView)
        }

        btnCancel.setOnClickListener { closePopup() }
        btnUpdate.setOnClickListener {
            val locationName = edtLocationName.text.toString().trim()
            val distance = edtDistance.text.toString().trim()

            if (locationName.isNotEmpty() && distance.isNotEmpty()) {
                Constant.showLoading(this)
                val jsonObject = JsonObject().apply {
                    addProperty(APIKeyNames.id, data.id.toString())
                    addProperty(APIKeyNames.location, locationName)
                    addProperty(APIKeyNames.distance, distance.toIntOrNull())
                }
                appViewModel?.updateLocation(isAccessToken!!, jsonObject, this)
                closePopup()
            } else {
                Toast.makeText(this, getString(R.string.Please_enter_fields), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        dimView.setOnClickListener { closePopup() }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(data: LocationHistoryData, isType: String) {
        val dialogRootView = view as ViewGroup
        if (isType == Constant.isDelete) {
            showTopAlertPopup(
                getString(R.string.Are_to_location),
                dialogRootView,
                data.id,
                false,
                Constant.isRemove
            )
        } else {
            showEditLocationPopup(data, dialogRootView)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTopAlertPopup(
        message: String, rootView: ViewGroup, id: Int, isStatus: Boolean, isDeleteLocation: String
    ) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.success_popup, null)
        val messageText = popupView.findViewById<TextView>(R.id.alertMessage)
        val alertTitle = popupView.findViewById<TextView>(R.id.alertTitle)
        val okButton = popupView.findViewById<TextView>(R.id.btnOk)
        val btnCancel = popupView.findViewById<TextView>(R.id.btnCancel)

        if (id != -1) {
            btnCancel.visibility = View.VISIBLE
            isDeletedId = id
        } else {
            btnCancel.visibility = View.GONE
        }
        messageText.text = message
        alertTitle.text = "Delete!!"

        val dimView = View(this).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
        }
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val marginInPx = resources.getDimensionPixelSize(R.dimen.fourty_five)
            setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        }
        rootView.addView(dimView)
        rootView.addView(popupView, layoutParams)

        val closePopup = {
            rootView.removeView(popupView)
            rootView.removeView(dimView)
        }

        if (id == -1 && isStatus && isDeleteLocation == Constant.isRemove) {
            isLocationHistoryAdapter?.removeItemById(isDeletedId!!)
        }

        okButton.setOnClickListener {
            if (id != -1 && isDeleteLocation == Constant.isRemove) {
                val jsonObject = JsonObject().apply {
                    addProperty(APIKeyNames.location_id, id)
                }
                appViewModel?.removeLocation(isAccessToken!!, jsonObject, this)
            } else if (isDeleteLocation == Constant.isUpdate) {
                isShowLocationHistory()
            }
            closePopup()
        }

        btnCancel.setOnClickListener { closePopup() }
        dimView.setOnClickListener { closePopup() }
    }

    private fun isValidLatLng(latitude: Double?, longitude: Double?): Boolean {
        return latitude != null && longitude != null && latitude in -90.0..90.0 && longitude in -180.0..180.0
    }
}