package com.vs.schoolmessenger.School.MarkYourAttendance

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.AddLocationActivityBinding
import java.util.Calendar
import java.util.Locale


class AddLocationActivity : BaseActivity<AddLocationActivityBinding>(),
    View.OnClickListener, OnMapReadyCallback {

    override fun getViewBinding(): AddLocationActivityBinding {
        return AddLocationActivityBinding.inflate(layoutInflater)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    var isLatitude: Double? = null
    var isLongitude: Double? = null
    var isDistance: String? = null
    var isAccessToken: String? = null
    private var appViewModel: App? = null
    private var isStaffDetails: StaffDetails? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.btnAddLocation.setOnClickListener(this)
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
                val mapFragment = supportFragmentManager
                    .findFragmentById(binding.mapFragment.id) as SupportMapFragment
                mapFragment.getMapAsync(this)
            } else {
                println("Location is null. Try again later or enable location.")
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (isLatitude != null && isLongitude != null) {
            val location = LatLng(isLatitude!!, isLongitude!!)
            val isLocationName = getAddressFromLocation(isLatitude!!, isLongitude!!)
            googleMap.addMarker(MarkerOptions().position(location).title(isLocationName))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            binding.lblAddress.text = isLocationName
            googleMap.setOnMapClickListener {
                openInGoogleMaps(it.latitude, it.longitude, isLocationName)
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

    fun isLoadMeter() {
        val months = listOf(
            "10", "15", "20", "25", "30", "35",
            "40", "45", "50", "55", "60", "75"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerMetres.adapter = monthAdapter

        val currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH)
        binding.spinnerMetres.setSelection(currentMonthIndex)

        binding.spinnerMetres.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedMeter = parent.getItemAtPosition(position).toString()
                isDistance = selectedMeter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnAddLocation -> {
                isCheckValidation()
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

}