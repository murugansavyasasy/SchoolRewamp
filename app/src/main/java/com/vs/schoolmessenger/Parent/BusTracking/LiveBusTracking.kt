package com.vs.schoolmessenger.Parent.BusTracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.BusTracking.Model.BusStop
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LiveBusTrackingBinding
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class LiveBusTracking : BaseActivity<LiveBusTrackingBinding>(),
    View.OnClickListener {

    override fun getViewBinding(): LiveBusTrackingBinding {
        return LiveBusTrackingBinding.inflate(layoutInflater)
    }

    private lateinit var map: MapLibreMap

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    private var isApiCalled = false
    var userDetails: UserDetails? = null
    private var isSettingsOpened = false
    private var isAccessToken: String? = null
    private var appViewModel: App? = null




    // Not

    var isVendor=false

    private val stops = listOf(
        BusStop("1", "Stop 1", "09:00", 13.0418, 80.2341, true, true),
        BusStop("2", "Stop 2", "09:05", 13.0350, 80.2360),
        BusStop("3", "Stop 3", "09:10", 13.0280, 80.2300),
        BusStop("4", "Stop 4", "09:15", 13.0109, 80.2120),
        BusStop("5", "Stop 5", "09:20", 12.9941, 80.1709)
    )

    private val roadCoords = mutableListOf<Point>()
    private val completedCoords = mutableListOf<Point>()

    private var busMarker: Marker? = null
    private var busIndex = 0

    private val handler = Handler(Looper.getMainLooper())

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                proceedAfterPermission()
            } else {
                val permanentlyDenied = !shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (permanentlyDenied) {
                    showPermissionSettingsDialog()
                } else {
                    showRetryPermissionDialog()
                }
            }
        }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
          userDetails= SharedPreference.getUserDetails(this)

        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)
            Constant.isSelectedMenuName=menu_name.toString()
            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )
            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)

        }

        if (Constant.isParentChoose){
            val childDetails = SharedPreference.getChildDetails(this)
            isAccessToken = childDetails?.access_token
        }else{
            val isStaffDetails = SharedPreference.getStaffDetails(this)
            isAccessToken = isStaffDetails?.access_token
        }
        binding.toolbarLayout.lblStudentName.text=Constant.isSelectedMenuName
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }



        if (isVendor){
            binding.WVLiveBus.visibility= View.VISIBLE
            binding.mapView.visibility= View.GONE
            observeLiveBusResponse()
        checkAndRequestLocation()
        }else{
            if (!isVendor) {
                try {
                    MapLibre.getInstance(this)
                } catch (e: Exception) {
                    Log.e("MAP", "Init Error", e)
                }
            }
            binding.WVLiveBus.visibility= View.GONE
            binding.mapView.visibility= View.VISIBLE
//            binding.mapView.getMapAsync(this)
        }
    }

    private fun checkAndRequestLocation() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
        } else {
            proceedAfterPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun proceedAfterPermission() {
        if (!isLocationEnabled()) {
            showGpsEnableDialog()
            return
        }

        if (!isApiCalled) {
            isApiCalled = true
            isLiveBus()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun showGpsEnableDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Location")
            .setMessage("Location services must be enabled to track the bus.\n\nPlease turn on Location now.")
            .setCancelable(false)
            .setPositiveButton("Enable Location") { _, _ ->
                isSettingsOpened = true
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

    private fun showRetryPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Location permission is required for live bus tracking.")
            .setCancelable(false)
            .setPositiveButton("Enable Location") { _, _ ->
                requestLocationPermission()
            }
            .show()
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(
                "Location permission is permanently denied.\n\n" +
                        "Allow Location access."
            )
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                isSettingsOpened = true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .show()
    }


    private fun observeLiveBusResponse() {
        appViewModel?.isGetLiveBusData?.observe(this) { response ->
            Constant.hideLoading(this)

            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.lytList.visibility = View.GONE
                binding.WVLiveBus.visibility = View.VISIBLE
                binding.toolbarLayout.lblStudentSection.text =response.data[0].thing_id ?: ""
                setupWebView(response.data[0].tracking_url ?: "")
            } else {
                binding.WVLiveBus.visibility = View.GONE
                binding.lytList.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
            }
        }
    }

    private fun setupWebView(trackingUrl: String) {
        binding.WVLiveBus.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.setGeolocationEnabled(true)
        }

        binding.WVLiveBus.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                callback?.invoke(origin, true, false)
            }
        }

        binding.WVLiveBus.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Constant.showLoading(this@LiveBusTracking)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Constant.hideLoading(this@LiveBusTracking)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Constant.hideLoading(this@LiveBusTracking)
                showWebViewError()
            }
        }

        binding.WVLiveBus.loadUrl(trackingUrl)
    }

    private fun showWebViewError() {
        binding.WVLiveBus.visibility = View.GONE
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = getString(R.string.Something_went_wrong_Please_try_again)
    }

    private fun isLiveBus() {
        Constant.showLoading(this)
        appViewModel?.isLiveBus(isAccessToken!!, this)
    }


    override fun onResume() {
        super.onResume()

        binding.WVLiveBus.onResume()
        binding.WVLiveBus.resumeTimers()

        if (isSettingsOpened) {
            isSettingsOpened = false
            checkAndRequestLocation()
        }
    }

    override fun onPause() {
        binding.WVLiveBus.onPause()
        binding.WVLiveBus.pauseTimers()
        super.onPause()
    }

    override fun onDestroy() {
        binding.WVLiveBus.apply {
            clearHistory()
            clearCache(true)
            loadUrl("about:blank")
            onPause()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }

    override fun onClick(v: View?) {}


//    override fun onMapReady(mapLibre: MapLibreMap) {
//
//        if (!isVendor){
//            map = mapLibre
//            map.setStyle(
//                Style.Builder().fromUri(
//                    "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
//                )
//            ) {
//
//                addStopPins()
//                fetchRoute()
//            }
//        }
//    }

    private fun addStopPins() {

        stops.forEach {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lat, it.lng))
                    .title(it.name)
            )
        }
    }

    private fun fetchRoute() {

        val path = stops.joinToString(";") {
            "${it.lng},${it.lat}"
        }

        val url =
            "https://router.project-osrm.org/route/v1/driving/$path?overview=full&geometries=geojson"

        thread {
            try {

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 15000
                connection.readTimeout = 15000




                val responseCode = connection.responseCode

                val stream = if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }


                val response = stream.bufferedReader().use { it.readText() }

                Log.d("OSRM", "Code: $responseCode")
                Log.d("OSRM", "Response: $response")
                Log.d("FINAL_URL", connection.url.toString())



                if (responseCode != HttpURLConnection.HTTP_OK) return@thread

                val json = JSONObject(response)

                // 🚨 IMPORTANT CHECK
                if (json.getString("code") != "Ok") {
                    Log.e("OSRM", "Route error: ${json.getString("code")}")
                    return@thread
                }

                val coords = json.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

                roadCoords.clear()

                for (i in 0 until coords.length()) {
                    val c = coords.getJSONArray(i)
                    roadCoords.add(
                        Point.fromLngLat(c.getDouble(0), c.getDouble(1))
                    )
//                    Log.d("Error",roadCoords.toString())
                }

                runOnUiThread {
                    setupRouteLayers()
                    fitToRoute()
                    startBusAnimation()
                    Toast.makeText(this,"Gotach", Toast.LENGTH_SHORT).show()
                    Log.d("Error","SUCCESS")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show()
                Log.d("Error",e.toString())
                Log.d("Error","FAILED")
            }
        }
    }

    private fun setupRouteLayers() {

        val style = map.style ?: return

        style.addSource(GeoJsonSource("completed-src"))
        style.addSource(GeoJsonSource("remaining-src"))

        style.addLayer(
            LineLayer("completed-layer", "completed-src")
                .withProperties(
                    lineColor(Color.GRAY),
                    lineWidth(6f)
                )
        )

        style.addLayer(
            LineLayer("remaining-layer", "remaining-src")
                .withProperties(
                    lineColor(Color.GREEN),
                    lineWidth(6f)
                )
        )

        updateRemaining()
    }

    private fun updateRemaining() {

        val style = map.style ?: return
        val remain = roadCoords.subList(busIndex, roadCoords.size)

        style.getSourceAs<GeoJsonSource>("remaining-src")
            ?.setGeoJson(LineString.fromLngLats(remain))
    }

    private fun updateCompleted() {

        val style = map.style ?: return

        style.getSourceAs<GeoJsonSource>("completed-src")
            ?.setGeoJson(LineString.fromLngLats(completedCoords))
    }

    private fun fitToRoute() {
        if (roadCoords.isEmpty()) return

        // Build bounds from all stops (or roadCoords for precise route fit)
        val builder = LatLngBounds.Builder()
        stops.forEach { stop ->
            builder.include(LatLng(stop.lat, stop.lng))
        }
        // Alternative: Use roadCoords for exact route bounds
        // roadCoords.forEach { builder.include(LatLng(it.latitude(), it.longitude())) }

        val bounds = builder.build()

        // Padding in pixels (adjust for your UI – e.g., for bottom sheet or controls)
        val padding = 120

        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds, padding),
            1500  // Smooth animation duration in ms
        )


        map.setMinZoomPreference(10.0)   // can't zoom out beyond city-level
        map.setMaxZoomPreference(25.0)   // can't zoom in closer than street/building level
    }

    private fun startBusAnimation() {

        handler.postDelayed(object : Runnable {

            override fun run() {

                if (busIndex >= roadCoords.size) return

                val p = roadCoords[busIndex]
                completedCoords.add(p)

                updateCompleted()
                updateRemaining()
                moveBus(p)

                busIndex++

                handler.postDelayed(this, 800)
            }

        }, 800)
    }

    private fun moveBus(p: Point) {

        val latLng = LatLng(p.latitude(), p.longitude())

        if (busMarker == null) {

            busMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Bus")
            )

        } else {
            busMarker!!.position = latLng
        }

        map.animateCamera(
            CameraUpdateFactory.newLatLng(latLng),
            500
        )
    }

}