package com.vs.schoolmessenger.Parent.BusTracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
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
    View.OnClickListener, OnMapReadyCallback {

    private lateinit var map: MapLibreMap
    private lateinit var mapView: MapView

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

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

    var isVendor = false

    private var stops = listOf(
        BusStop("1", "Greenfield Bus Stand", "09:00", 13.0418, 80.2341, true, true),
        BusStop("2", "Anna Nagar",           "09:05", 13.0350, 80.2360),
        BusStop("3", "Koyambedu",            "09:10", 13.0280, 80.2300),
        BusStop("4", "Vadapalani",           "09:15", 13.0109, 80.2120),
        BusStop("5", "Ashok Nagar",          "09:20", 12.9941, 80.1709)
    )

    private var currentStopIndex = -1
    private val roadCoords       = mutableListOf<Point>()
    private val completedCoords  = mutableListOf<Point>()
    private var busMarker: Marker? = null
    private var busIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()


    override fun onCreate(savedInstanceState: Bundle?) {
        MapLibre.getInstance(this)
        super.onCreate(savedInstanceState)
    }

    override fun getViewBinding(): LiveBusTrackingBinding =
        LiveBusTrackingBinding.inflate(layoutInflater)

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION]   == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                proceedAfterPermission()
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                    showPermissionSettingsDialog()
                else
                    showRetryPermissionDialog()
            }
        }

    override fun setupViews() {
        super.setupViews()

        isToolBarPrimaryParent(
            mainViewId      = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        userDetails = SharedPreference.getUserDetails(this)

        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id     = intent.getIntExtra(Constant.msg_id, -1)
            headerId   = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name  = intent.getStringExtra(Constant.menu_name)
            Constant.isSelectedMenuName = menu_name.toString()
            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)
        }

        isAccessToken = if (Constant.isParentChoose)
            SharedPreference.getChildDetails(this)?.access_token
        else
            SharedPreference.getStaffDetails(this)?.access_token

        binding.toolbarLayout.lblStudentName.text = Constant.isSelectedMenuName
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }

        mapView      = binding.mapView
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        setupBottomSheet()
        setupScreen()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.three_twenty)
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state      = BottomSheetBehavior.STATE_COLLAPSED

        renderStopTimeline(-1)


    }


    private fun renderStopTimeline(reachedIndex: Int) {
        val container = binding.routeStopsContainer
        container.removeAllViews()

        stops.forEachIndexed { index, stop ->
            val isReached = index <= reachedIndex
            val isLast    = index == stops.lastIndex

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            if (isReached) {
                row.addView(ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(28.dp, 28.dp)
                    setImageResource(R.drawable.ic_bus_green)
                })
            } else {
                row.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(28.dp, 28.dp)
                    setBackgroundResource(R.drawable.gray_circle)
                })
            }

            row.addView(TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).also { it.marginStart = 12.dp }
                text     = stop.name
                textSize = 15f
                setTextColor(ContextCompat.getColor(this@LiveBusTracking, android.R.color.black))
                setTypeface(
                    typeface,
                    if (isReached) android.graphics.Typeface.BOLD
                    else           android.graphics.Typeface.NORMAL
                )
            })

            if (isReached) {
                row.addView(TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text     = if (index == reachedIndex) "Now" else stop.time
                    textSize = 13f
                    setTextColor(Color.parseColor("#4CAF50"))
                })
            }

            container.addView(row)

            if (!isLast) {
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(3.dp, 24.dp).also {
                        it.marginStart = 13.dp
                    }
                    setBackgroundColor(
                        if (isReached) Color.parseColor("#34C759")
                        else           Color.parseColor("#E0E0E0")
                    )
                })
            }
        }
    }


    private fun setupScreen() {
        if (isVendor) {
            binding.WVLiveBus.visibility           = View.VISIBLE
            binding.mapCoordinatorLayout.visibility = View.GONE
            observeLiveBusResponse()
            checkAndRequestLocation()
        } else {
            binding.WVLiveBus.visibility           = View.GONE
            binding.mapCoordinatorLayout.visibility = View.VISIBLE
            mapView.onCreate(null)
            mapView.getMapAsync(this)
        }
    }


    private fun checkAndRequestLocation() {
        if (!hasLocationPermission()) requestLocationPermission()
        else proceedAfterPermission()
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun proceedAfterPermission() {
        if (!isLocationEnabled()) { showGpsEnableDialog(); return }
        if (!isApiCalled) { isApiCalled = true; isLiveBus() }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showGpsEnableDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Location")
            .setMessage("Location services must be enabled to track the bus.")
            .setCancelable(false)
            .setPositiveButton("Enable") { _, _ ->
                isSettingsOpened = true
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.show()
    }

    private fun showRetryPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Location permission is required for live bus tracking.")
            .setCancelable(false)
            .setPositiveButton("Retry") { _, _ -> requestLocationPermission() }.show()
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Location permission is permanently denied. Allow it in Settings.")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                isSettingsOpened = true
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                )
            }.show()
    }


    private fun observeLiveBusResponse() {
        appViewModel?.isGetLiveBusData?.observe(this) { response ->
            Constant.hideLoading(this)
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.lytList.visibility   = View.GONE
                binding.WVLiveBus.visibility  = View.VISIBLE
                binding.toolbarLayout.lblStudentSection.text = response.data[0].thing_id ?: ""
                setupWebView(response.data[0].tracking_url ?: "")
            } else {
                binding.WVLiveBus.visibility = View.GONE
                binding.lytList.visibility   = View.VISIBLE
                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
            }
        }
    }

    private fun setupWebView(trackingUrl: String) {
        binding.WVLiveBus.apply {
            settings.javaScriptEnabled    = true
            settings.domStorageEnabled    = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort      = true
            settings.builtInZoomControls  = false
            settings.displayZoomControls  = false
            settings.setGeolocationEnabled(true)
        }

        binding.WVLiveBus.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) { callback?.invoke(origin, true, false) }
        }

        binding.WVLiveBus.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Constant.showLoading(this@LiveBusTracking)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                Constant.hideLoading(this@LiveBusTracking)
            }
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Constant.hideLoading(this@LiveBusTracking)
                showWebViewError()
            }
        }

        binding.WVLiveBus.loadUrl(trackingUrl)
    }

    private fun showWebViewError() {
        binding.WVLiveBus.visibility = View.GONE
        binding.lytList.visibility   = View.VISIBLE
        binding.txtNoData.text = getString(R.string.Something_went_wrong_Please_try_again)
    }

    private fun isLiveBus() {
        Constant.showLoading(this)
        appViewModel?.isLiveBus(isAccessToken!!, this)
    }

    override fun onClick(v: View?) {}


    override fun onMapReady(mapLibreMap: MapLibreMap) {
        map = mapLibreMap
        map.setStyle(
            Style.Builder().fromUri(
                "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
            )
        ) {
            addStopPins()
            fetchRoute()
        }
    }


    private fun vectorToBitmap(drawableRes: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(this, drawableRes)
            ?: return createBitmap(width, height)

        drawable.setBounds(0, 0, width, height)
        val bmp    = createBitmap(width, height)
        val canvas = android.graphics.Canvas(bmp)
        drawable.draw(canvas)
        return bmp
    }

    private fun scaledStopIcon(): org.maplibre.android.annotations.Icon {
        val bmp = vectorToBitmap(R.drawable.ic_location, 40.dp, 40.dp)
        return IconFactory.getInstance(this).fromBitmap(bmp)
    }

    private fun scaledBusIcon(): org.maplibre.android.annotations.Icon {
        val bmp = vectorToBitmap(R.drawable.ic_bus_green, 60.dp, 60.dp)
        return IconFactory.getInstance(this).fromBitmap(bmp)
    }

    private fun addStopPins() {
        val stopIcon = scaledStopIcon()
        stops.forEach { stop ->
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(stop.lat, stop.lng))
                    .title(stop.name)
                    .icon(stopIcon)
            )
        }
    }

    private fun fetchRoute() {
        val path = stops.joinToString(";") { "${it.lng},${it.lat}" }
        val url  =
            "https://router.project-osrm.org/route/v1/driving/$path?overview=full&geometries=geojson"

        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 15_000
                connection.readTimeout    = 15_000

                val responseCode = connection.responseCode
                val stream =
                    if (responseCode == HttpURLConnection.HTTP_OK) connection.inputStream
                    else connection.errorStream

                val response = stream.bufferedReader().use { it.readText() }
                Log.d("OSRM", "Code: $responseCode")

                if (responseCode != HttpURLConnection.HTTP_OK) return@thread

                val json = JSONObject(response)
                if (json.getString("code") != "Ok") {
                    Log.e("OSRM", "Route error: ${json.getString("code")}")
                    return@thread
                }

                val coords = json
                    .getJSONArray("routes").getJSONObject(0)
                    .getJSONObject("geometry").getJSONArray("coordinates")

                roadCoords.clear()
                for (i in 0 until coords.length()) {
                    val c = coords.getJSONArray(i)
                    roadCoords.add(Point.fromLngLat(c.getDouble(0), c.getDouble(1)))
                }

                runOnUiThread {
                    setupRouteLayers()
                    fitToRoute()
                    startBusAnimation()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Route error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fitToRoute() {
        if (roadCoords.isEmpty()) return
        val builder = LatLngBounds.Builder()
        stops.forEach { builder.include(LatLng(it.lat, it.lng)) }

        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(builder.build(), 250),
            1500
        )

        map.setMinZoomPreference(12.0)
        map.setMaxZoomPreference(25.0)
    }


    private fun setupRouteLayers() {
        val style = map.style ?: return
        style.addSource(GeoJsonSource("completed-src"))
        style.addSource(GeoJsonSource("remaining-src"))
        style.addLayer(
            LineLayer("completed-layer", "completed-src")
                .withProperties(lineColor(Color.GRAY), lineWidth(6f))
        )
        style.addLayer(
            LineLayer("remaining-layer", "remaining-src")
                .withProperties(lineColor(Color.GREEN), lineWidth(6f))
        )
        updateRemaining()
    }

    private fun updateRemaining() {
        val remain =
            if (busIndex < roadCoords.size) roadCoords.subList(busIndex, roadCoords.size)
            else emptyList()
        map.style?.getSourceAs<GeoJsonSource>("remaining-src")
            ?.setGeoJson(LineString.fromLngLats(remain))
    }

    private fun updateCompleted() {
        map.style?.getSourceAs<GeoJsonSource>("completed-src")
            ?.setGeoJson(LineString.fromLngLats(completedCoords))
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
                checkStopReached(p)

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
                    .icon(scaledBusIcon())
            )
        } else {
            busMarker!!.position = latLng
        }
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 500)
    }


    private fun checkStopReached(busPoint: Point) {
        val nextIndex = currentStopIndex + 1
        if (nextIndex >= stops.size) return

        val nextStop   = stops[nextIndex]
        val busLatLng  = LatLng(busPoint.latitude(), busPoint.longitude())
        val stopLatLng = LatLng(nextStop.lat, nextStop.lng)

        if (busLatLng.distanceTo(stopLatLng) <= 150.0) {
            currentStopIndex = nextIndex
            renderStopTimeline(currentStopIndex)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


    override fun onStart() {
        super.onStart()
        if (!isVendor) mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (isVendor) {
            binding.WVLiveBus.onResume()
            binding.WVLiveBus.resumeTimers()
            if (isSettingsOpened) { isSettingsOpened = false; checkAndRequestLocation() }
        } else {
            mapView.onResume()
        }
    }

    override fun onPause() {
        if (isVendor) {
            binding.WVLiveBus.onPause()
            binding.WVLiveBus.pauseTimers()
        } else {
            mapView.onPause()
        }
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    override fun onStop() {
        if (!isVendor) mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (isVendor) {
            binding.WVLiveBus.apply {
                clearHistory()
                clearCache(true)
                loadUrl("about:blank")
                removeAllViews()
                destroy()
            }
        } else {
            mapView.onDestroy()
        }
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (!isVendor) mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!isVendor) mapView.onSaveInstanceState(outState)
    }
}