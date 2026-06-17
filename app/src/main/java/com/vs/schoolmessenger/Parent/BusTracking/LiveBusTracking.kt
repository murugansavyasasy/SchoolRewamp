package com.vs.schoolmessenger.Parent.BusTracking

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.annotation.SuppressLint
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
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
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.BusListData
import com.vs.schoolmessenger.R
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Parent.BusTracking.Model.LiveBus.BusStop
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LiveBusTrackingBinding
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineBlur
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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

    var isVendor: String? = ""

    private var busData: BusListData? = null

    private var journeyStatus: String? = null

    private var stops = listOf<BusStop>()

    private var currentStopIndex = -1
    private val roadCoords = mutableListOf<Point>()
    private var busMarker: Marker? = null
    private var busIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    private var currentBusLatLng: LatLng? = null
    private var busAnimator: ValueAnimator? = null

    private var isCameraFollowingBus = true
    private var isFirstFit = true

    private val POLL_INTERVAL_MS = 3_000L

    private var lastStopReachedCount = 0
    private val LAST_STOP_CONFIRM_COUNT = 1
    private val locationPollHandler = Handler(Looper.getMainLooper())
    private val locationPollRunnable = object : Runnable {
        override fun run() {
            isGetLatestGeoLocation()
            locationPollHandler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }
    private var isMapReady = false

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp: Float get() = (this * resources.displayMetrics.density)

    private val STOP_RADIUS_METERS = 150.0
    private val LAST_STOP_RADIUS_METERS = 200.0
    private var isTripCompleted = false

    // FIX: Single flag for initial stop sync – removed the duplicate guard on ViewModel
    private var isFirstLocationReceived = false

    private var savedInstanceStateRef: Bundle? = null
    var isChildDetails: ChildDetails? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        MapLibre.getInstance(this)
        savedInstanceStateRef = savedInstanceState
        super.onCreate(savedInstanceState)
    }

    override fun getViewBinding(): LiveBusTrackingBinding =
        LiveBusTrackingBinding.inflate(layoutInflater)


    override fun setupViews() {
        super.setupViews()

        busData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("bus_data", BusListData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("bus_data")
        }

        journeyStatus = intent.getStringExtra("status")?.uppercase()

        Log.d("LiveBusTracking", "━━━ onCreate ━━━")
        Log.d("LiveBusTracking", "vehicleId  = ${busData?.vehicle_id}")
        Log.d("LiveBusTracking", "vehicleNo  = ${busData?.vehicle_no}")
        Log.d("LiveBusTracking", "routeId    = ${busData?.route_id}")
        Log.d("LiveBusTracking", "journeyStatus = $journeyStatus")
        Log.d("LiveBusTracking", "stoppingPoints count = ${busData?.stopping_points?.size}")

        buildStopsFromBusData()
        Log.d("LiveBusTracking", "stops.size after build = ${stops.size}")

        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        isChildDetails = SharedPreference.getChildDetails(this)
        isVendor  = isChildDetails?.gps_type

        Log.d("LiveBusTracking", "isVendor = $isVendor")

//        if (fromNotification) {
//            Constant.isParentChoose = true
//            msg_id = intent.getIntExtra(Constant.msg_id, -1)
//            headerId = intent.getStringExtra(Constant.header_id)
//            receiverId = intent.getStringExtra(Constant.receiverid)
//            menu_name = intent.getStringExtra(Constant.menu_name)
//            Constant.isSelectedMenuName = menu_name.toString()
//            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
//            SharedPreference.putChildDetails(this, matchedChild!!)
//        }

        isAccessToken = if (Constant.isParentChoose)
            SharedPreference.getChildDetails(this)?.access_token
        else
            SharedPreference.getStaffDetails(this)?.access_token

        binding.toolbarLayout.lblStudentName.text = Constant.isSelectedMenuName
        binding.toolbarLayout.imgBack.setOnClickListener { onBackPressed() }

        mapView = binding.mapView
        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }

        setupBottomSheet()
        setupMapGestureListeners()
        setupScreen()

        appViewModel?.isgetgeolocation?.observe(this) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val loc = response.data[0]
                val lat = loc.latitude.toDoubleOrNull() ?: return@observe
                val lng = loc.longitude.toDoubleOrNull() ?: return@observe

                Log.d("LiveBusTracking", "Bus update → lat=$lat, lng=$lng, speed=${loc.speed}")

                if (isMapReady) {
                    val newLatLng = LatLng(lat, lng)
                    syncInitialStopProgress(newLatLng)
                    smoothMoveBusToLocation(newLatLng)
                    checkStopReached(Point.fromLngLat(lng, lat))
                }
            } else {
                Log.w("LiveBusTracking", "Geo-location response empty/failed: ${response?.message}")
            }
        }
    }


    private fun syncInitialStopProgress(busLatLng: LatLng) {
        if (isFirstLocationReceived) return
        isFirstLocationReceived = true

        if (stops.isEmpty()) return

        var closestIndex = 0
        var minDist = Double.MAX_VALUE

        stops.forEachIndexed { index, stop ->
            val dist = busLatLng.distanceTo(LatLng(stop.lat, stop.lng))
            if (dist < minDist) {
                minDist = dist
                closestIndex = index
            }
        }

        val alreadyCompletedIndex = (closestIndex - 1).coerceAtLeast(-1)

        if (alreadyCompletedIndex >= 0) {
            currentStopIndex = alreadyCompletedIndex
            Log.d(
                "LiveBusTracking",
                "syncInitialStopProgress: bus near stop[$closestIndex] " +
                        "→ marking stops 0..$alreadyCompletedIndex as completed"
            )
            renderStopTimeline(currentStopIndex)
        } else {
            Log.d(
                "LiveBusTracking",
                "syncInitialStopProgress: bus near first stop, nothing to pre-mark"
            )
        }
    }

    private fun buildStopsFromBusData() {
        val points = busData?.stopping_points
        if (points.isNullOrEmpty()) {
            Log.w("LiveBusTracking", "No stopping_points in BusListData – stops list is empty")
            return
        }

        val matchedPoint = if (!journeyStatus.isNullOrEmpty()) {
            points.firstOrNull { it.journey_type.equals(journeyStatus, ignoreCase = true) }
                ?: run {
                    Log.w(
                        "LiveBusTracking",
                        "No stopping_point matched journeyStatus='$journeyStatus'. " +
                                "Available types: ${points.map { it.journey_type }}. " +
                                "Falling back to first entry."
                    )
                    points.first()
                }
        } else {
            Log.w("LiveBusTracking", "journeyStatus is null/empty – using first stopping_point")
            points.first()
        }

        stops = matchedPoint.stops.mapIndexed { idx, s ->
            BusStop(
                id = s.stop_id,
                name = s.stop_name,
                time = s.stop_time,
                lat = s.latitude.toDoubleOrNull() ?: 0.0,
                lng = s.longitude.toDoubleOrNull() ?: 0.0,
                isFirst = idx == 0,
                isLast = idx == matchedPoint.stops.lastIndex
            )
        }

        Log.d(
            "LiveBusTracking",
            "Built ${stops.size} stops for journey: ${matchedPoint.journey_type}"
        )
        stops.forEachIndexed { i, s ->
            Log.d("LiveBusTracking", "  stop[$i] ${s.name} → (${s.lat}, ${s.lng})")
        }
    }


    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) proceedAfterPermission()
            else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                    showPermissionSettingsDialog()
                else
                    showRetryPermissionDialog()
            }
        }


    private fun isGetLatestGeoLocation() {
        // Production code – uncomment before release:
         appViewModel!!.isgetgeolocation(
             isAccessToken!!,
             Constant.getAndroidSecureId(this),
             busData?.vehicle_reg_no.toString(),
             busData?.route_id.toString(),
             this
         )

//        // Testing:
//        appViewModel!!.isgetgeolocation(
//            isAccessToken!!,
//            "ea973ebc50a1f536",
//            busData?.vehicle_reg_no.toString(),
//            busData?.route_id.toString(),
//            this
//        )
    }

    private fun startLocationPolling() {
        locationPollHandler.removeCallbacks(locationPollRunnable)
        locationPollHandler.post(locationPollRunnable)
    }

    private fun stopLocationPolling() = locationPollHandler.removeCallbacks(locationPollRunnable)


    private fun setupMapGestureListeners() {
        binding.fabRecenter.setOnClickListener {
            isCameraFollowingBus = true
            isFirstFit = false
            binding.fabRecenter.visibility = View.GONE
            currentBusLatLng?.let { latLng ->
                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(latLng)
                            .zoom(15.0)
                            .tilt(30.0)
                            .build()
                    ), 800
                )
            }
        }
    }


    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.peekHeight = resources.getDimensionPixelSize(R.dimen.three_twenty)
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        populateBusInfoCard()

        binding.imgRefresh.setOnClickListener {

            binding.imgRefresh.startAnimation(
                android.view.animation.RotateAnimation(
                    0f, 360f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    duration = 600
                    interpolator = android.view.animation.DecelerateInterpolator()
                }
            )
            isGetLatestGeoLocation()
        }

        renderStopTimeline(-1)
    }




    private fun populateBusInfoCard() {
        Log.d("BottomSheet", "populateBusInfoCard: busData=$busData stops=${stops.size}")
        val data = busData ?: run { Log.e("BottomSheet", "❌ busData NULL"); return }

        binding.tvBusNumber.text = data.vehicle_no?.let { "Bus - $it" } ?: "Bus"
        binding.tvRouteName.text = buildString {
            val from = stops.firstOrNull()?.name ?: data.stop_name ?: ""
            val to = stops.lastOrNull()?.name ?: ""
            if (from.isNotBlank() && to.isNotBlank()) append("$from  →  $to")
            else append(data.route_name ?: "")
        }

        binding.tvFromStop.text = stops.firstOrNull()?.name ?: "—"
        binding.tvFromTime.text = stops.firstOrNull()?.time ?: ""
        binding.tvToStop.text = stops.lastOrNull()?.name ?: "—"
        binding.tvToTime.text = stops.lastOrNull()?.time ?: ""

        Log.d("BottomSheet", "✅ tvBusNumber=${binding.tvBusNumber.text}")
        Log.d("BottomSheet", "✅ tvFromStop =${binding.tvFromStop.text}")
        Log.d("BottomSheet", "✅ tvToStop   =${binding.tvToStop.text}")

        updateNextStopLabel(-1)
    }

    private fun updateNextStopLabel(reachedIndex: Int) {
        val nextIdx = reachedIndex + 1
        binding.tvNextStop.text =
            if (nextIdx < stops.size) stops[nextIdx].name
            else stops.lastOrNull()?.name ?: "—"
    }


    private fun renderStopTimeline(reachedIndex: Int) {
        Log.d("BottomSheet", "━━━ renderStopTimeline(reachedIndex=$reachedIndex) ━━━")
        Log.d("BottomSheet", "stops.size = ${stops.size}")

        val container = binding.routeStopsContainer

        fun cancelAnimatorsInView(view: View) {
            (view.tag as? ValueAnimator)?.cancel()
            if (view is android.view.ViewGroup) {
                for (i in 0 until view.childCount) cancelAnimatorsInView(view.getChildAt(i))
            }
        }
        for (i in 0 until container.childCount) cancelAnimatorsInView(container.getChildAt(i))
        container.removeAllViews()

        if (stops.isEmpty()) {
            Log.e("BottomSheet", "❌ stops is EMPTY — timeline will not render!")
            return
        }

        updateNextStopLabel(reachedIndex)

        stops.forEachIndexed { index, stop ->
            val isReached = index <= reachedIndex
            val isCurrent = index == reachedIndex
            val isLast = index == stops.lastIndex

            Log.d(
                "BottomSheet",
                "  stop[$index] '${stop.name}' isReached=$isReached isCurrent=$isCurrent"
            )

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val indicatorSize = 32.dp

            val indicatorView: View = when {
                isCurrent -> ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(indicatorSize, indicatorSize)
                    setImageResource(R.drawable.bussvg)
                    startPulseAnimation(this)
                }

                isReached -> ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(indicatorSize, indicatorSize)
                    setImageResource(R.drawable.ic_stop_reached)
                }

                else -> View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(indicatorSize, indicatorSize)
                    background = ContextCompat.getDrawable(
                        this@LiveBusTracking, R.drawable.stop_circle_bg
                    )
                }
            }
            row.addView(indicatorView)

            row.addView(TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).also { it.marginStart = 14.dp }
                text = stop.name
                textSize = if (isCurrent) 16f else 15f
                setTextColor(
                    when {
                        isCurrent -> ContextCompat.getColor(
                            this@LiveBusTracking,
                            R.color.PrimaryColor
                        )
                        isReached -> ContextCompat.getColor(
                            this@LiveBusTracking,
                            android.R.color.black
                        )
                        else -> Color.parseColor("#9E9E9E")
                    }
                )
                setTypeface(
                    typeface,
                    if (isCurrent) android.graphics.Typeface.BOLD
                    else android.graphics.Typeface.NORMAL
                )
            })

            if (isCurrent) {
                row.addView(TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = "  Now  "
                    textSize = 12f  // Slightly increased
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    background = createNowBadgeBackground()
                    setPadding(0, 4.dp, 0, 4.dp)
                })
            }

            container.addView(row)

            if (!isLast) {
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(3.dp, 32.dp).also {
                        it.marginStart = ((indicatorSize / 2) - 1).dp
                        it.topMargin = 0
                    }
                    setBackgroundColor(
                        if (isReached) Color.parseColor("#4CAF50")
                        else Color.parseColor("#E0E0E0")
                    )
                })
            }
        }

        Log.d("BottomSheet", "Timeline rendered: ${container.childCount} views added")
    }


    private fun createNowBadgeBackground(): android.graphics.drawable.Drawable =
        android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(Color.parseColor("#4CAF50"))
        }


    private fun startPulseAnimation(view: View) {
        val animator = ValueAnimator.ofFloat(1f, 1.2f, 1f).apply {
            duration = 900
            repeatCount = ValueAnimator.INFINITE
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            addUpdateListener {
                val scale = it.animatedValue as Float
                view.scaleX = scale
                view.scaleY = scale
            }
        }
        animator.start()
        view.tag = animator
    }


    private fun setupScreen() {
        if (isVendor == "dhundhoo") {
            binding.WVLiveBus.visibility = View.VISIBLE
            binding.mapCoordinatorLayout.visibility = View.GONE
            binding.fabRecenter.visibility = View.GONE
            observeLiveBusResponse()
            checkAndRequestLocation()
        } else {
            binding.WVLiveBus.visibility = View.GONE
            binding.mapCoordinatorLayout.visibility = View.VISIBLE
            mapView.onCreate(savedInstanceStateRef)
            mapView.getMapAsync(this)
        }
    }

    private fun checkAndRequestLocation() {
        if (!hasLocationPermission()) requestLocationPermission()
        else proceedAfterPermission()
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

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
            showGpsEnableDialog(); return
        }
        if (!isApiCalled) {
            isApiCalled = true; isLiveBus()
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
                binding.lytList.visibility = View.GONE
                binding.WVLiveBus.visibility = View.VISIBLE
                binding.toolbarLayout.lblStudentSection.text = response.data[0].thing_id ?: ""
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

            settings.apply {
                databaseEnabled=true
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                setGeolocationEnabled(true)
                mediaPlaybackRequiresUserGesture = false
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = true
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/124.0.0.0 Safari/537.36"
            }

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        binding.WVLiveBus.webChromeClient = object : WebChromeClient() {

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, true, false)
            }

            override fun onConsoleMessage(
                consoleMessage: android.webkit.ConsoleMessage?
            ): Boolean {
                Log.d(
                    "WebViewConsole",
                    "[${consoleMessage?.messageLevel()}] " +
                            "${consoleMessage?.message()} " +
                            "@ line ${consoleMessage?.lineNumber()}"
                )
                return true
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message?
            ): Boolean {
                val newWebView = WebView(this@LiveBusTracking).apply {
                    settings.javaScriptEnabled = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        settings.mixedContentMode =
                            android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                }
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        request?.url?.toString()?.let { newUrl ->
                            Log.d("WebViewConsole", "window.open → $newUrl")
                            binding.WVLiveBus.loadUrl(newUrl)
                        }
                        return true
                    }
                }
                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = newWebView
                resultMsg?.sendToTarget()
                return true
            }
        }

        binding.WVLiveBus.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): android.webkit.WebResourceResponse? {
                val url = request?.url?.toString() ?: return null

                if (url.startsWith("http://mt0.google.com") ||
                    url.startsWith("http://mt1.google.com") ||
                    url.startsWith("http://mt2.google.com") ||
                    url.startsWith("http://mt3.google.com") ||
                    url.startsWith("http://maps.googleapis.com") ||
                    url.startsWith("http://")
                ) {
                    val httpsUrl = url.replace("http://", "https://")
                    Log.d("WebViewConsole", "Rewriting HTTP → HTTPS: $httpsUrl")

                    return try {
                        val connection =
                            java.net.URL(httpsUrl).openConnection() as java.net.HttpURLConnection
                        connection.apply {
                            requestMethod = "GET"
                            setRequestProperty(
                                "User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                        "Chrome/124.0.0.0 Safari/537.36"
                            )
                            request.requestHeaders?.forEach { (key, value) ->
                                setRequestProperty(key, value)
                            }
                            connectTimeout = 10_000
                            readTimeout = 10_000
                        }

                        val responseCode = connection.responseCode
                        val mimeType = connection.contentType
                            ?.split(";")?.firstOrNull()?.trim()
                            ?: "image/png"
                        val encoding = connection.contentEncoding ?: "utf-8"

                        if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                            android.webkit.WebResourceResponse(
                                mimeType,
                                encoding,
                                connection.inputStream
                            )
                        } else {
                            Log.w("WebViewConsole", "HTTPS rewrite got $responseCode for $httpsUrl")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("WebViewConsole", "HTTP→HTTPS rewrite failed: ${e.message}")
                        null
                    }
                }

                return null
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                Log.d("WebViewConsole", "shouldOverrideUrlLoading → $url")

                return when {
                    url.startsWith("intent://") -> {
                        try {
                            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            }
                        } catch (e: Exception) {
                            Log.e("WebViewConsole", "Intent parse failed: ${e.message}")
                        }
                        true
                    }
                    url.contains("dhundhoo.com") -> {
                        view?.loadUrl(url)
                        true
                    }
                    url.startsWith("geo:") ||
                            url.contains("maps.google.com") ||
                            url.contains("maps.app.goo.gl") -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        true
                    }
                    url.startsWith("http") || url.startsWith("https") -> {
                        view?.loadUrl(url)
                        true
                    }
                    else -> false
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Log.d("WebViewConsole", "onPageStarted → $url")
                Constant.showLoading(this@LiveBusTracking)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("WebViewConsole", "onPageFinished → $url")
                Constant.hideLoading(this@LiveBusTracking)

                view?.postDelayed({
                    view.evaluateJavascript(
                        """
        (function() {
            try {
                // STEP 1: Log ALL buttons/links so we can see exactly what's there
                var allClickable = document.querySelectorAll('button, a, [role="button"], [onclick], input[type="button"]');
                allClickable.forEach(function(el, i) {
                    console.log('ELEMENT[' + i + '] tag=' + el.tagName 
                        + ' text="' + (el.innerText || el.textContent || '').trim() + '"'
                        + ' href="' + (el.href || '') + '"'
                        + ' onclick="' + (el.getAttribute('onclick') || '') + '"'
                        + ' class="' + (el.className || '') + '"'
                        + ' id="' + (el.id || '') + '"'
                        + ' outerHTML=' + el.outerHTML.substring(0, 200)
                    );
                });

                // STEP 2: Log the full page URL and title
                console.log('PAGE_URL=' + window.location.href);
                console.log('PAGE_TITLE=' + document.title);

                // STEP 3: Intercept window.open globally
                var originalOpen = window.open;
                window.open = function(url, target, features) {
                    console.log('window.open INTERCEPTED url=' + url 
                        + ' target=' + target);
                    // Load inside same WebView instead
                    if (url) window.location.href = url;
                    return null;
                };

                // STEP 4: Intercept window.location changes
                var originalAssign = window.location.assign.bind(window.location);
                var originalReplace = window.location.replace.bind(window.location);
                
                Object.defineProperty(window, 'location', {
                    get: function() { return window._location || location; }
                });

                // STEP 5: Watch for any dynamically added buttons (in case button loads late)
                var observer = new MutationObserver(function(mutations) {
                    mutations.forEach(function(m) {
                        m.addedNodes.forEach(function(node) {
                            if (node.nodeType === 1) {
                                var text = (node.innerText || node.textContent || '').toLowerCase();
                                if (text.includes('track') || text.includes('browser')) {
                                    console.log('DYNAMIC_BUTTON_FOUND: ' + node.outerHTML);
                                }
                            }
                        });
                    });
                });
                observer.observe(document.body, { childList: true, subtree: true });
                console.log('MutationObserver watching for dynamic buttons');

            } catch(e) {
                console.log('inject error: ' + e);
            }
        })();
        """.trimIndent(), null
                    )
                }, 1000)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    Log.e("WebViewConsole",
                        "Main frame error: ${error?.description} for ${request.url}")
                    Constant.hideLoading(this@LiveBusTracking)
                    showWebViewError()
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: android.webkit.WebResourceResponse?
            ) {
                Log.w("WebViewConsole",
                    "HTTP ${errorResponse?.statusCode} for ${request?.url}")
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

    override fun onClick(v: View?) {

    }



    override fun onMapReady(mapLibreMap: MapLibreMap) {
        map = mapLibreMap
        map.uiSettings.isLogoEnabled = false
        map.uiSettings.isAttributionEnabled = false
        map.uiSettings.isCompassEnabled = true

        Log.d("MapLibre", "onMapReady called — loading style")

        map.addOnCameraMoveStartedListener { reason ->
            if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                isCameraFollowingBus = false
                binding.fabRecenter.visibility = View.VISIBLE
            }
        }

        map.setStyle(
            Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"),
            Style.OnStyleLoaded { style ->
                Log.d("MapLibre", "✅ Style loaded successfully")
                isMapReady = true
                addStopPins()
                setupRouteLayers()
                fitToStops()
                isFirstFit = true
                startLocationPolling()
                enableUserLocationDisplay()
            }
        )
    }

    private fun vectorToBitmap(drawableRes: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(this, drawableRes)
            ?: return createBitmap(width, height)
        drawable.setBounds(0, 0, width, height)
        val bmp = createBitmap(width, height)
        val canvas = android.graphics.Canvas(bmp)
        drawable.draw(canvas)
        return bmp
    }

    private fun scaledBusIcon() = IconFactory.getInstance(this)
        .fromBitmap(vectorToBitmap(R.drawable.bussvg, 72.dp, 72.dp))


    private fun addStopPins() {
        if (stops.isEmpty()) return

        stops.forEach { stop ->
            val icon = IconFactory.getInstance(this)
                .fromBitmap(createStopMarkerBitmap(stop.name))

            map.addMarker(
                MarkerOptions()
                    .position(LatLng(stop.lat, stop.lng))
                    .title(stop.name)
                    .snippet("Time : ${stop.time}")
                    .icon(icon)
            )
        }

        map.setOnMarkerClickListener { marker ->
            marker.showInfoWindow(map, mapView)
            true
        }
    }


    private fun createStopMarkerBitmap(stopName: String): Bitmap {
        val density = resources.displayMetrics.density

        val textSizePx = 36f
        val horizontalPadding = (20 * density)
        val verticalPadding = (10 * density)
        val cornerRadius = 14f * density
        val pinWidth = (28 * density).toInt()
        val pinHeight = (34 * density).toInt()
        val bubbleBottomMargin = (4 * density)

        val textPaint = android.graphics.Paint().apply {
            color = Color.parseColor("#212121")
            textSize = textSizePx
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }

        val textWidth = textPaint.measureText(stopName)
        val bubbleWidth = textWidth + horizontalPadding * 2
        val bubbleHeight = textSizePx + verticalPadding * 2

        val totalWidth = bubbleWidth.toInt().coerceAtLeast(pinWidth)
        val totalHeight = (bubbleHeight + bubbleBottomMargin + pinHeight).toInt()

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        val shadowPaint = android.graphics.Paint().apply {
            color = Color.parseColor("#33000000")
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            android.graphics.RectF(
                horizontalPadding / 2,
                verticalPadding / 2 + 3 * density,
                bubbleWidth - horizontalPadding / 2,
                bubbleHeight + 3 * density
            ),
            cornerRadius,
            cornerRadius,
            shadowPaint
        )

        val bgPaint = android.graphics.Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            android.graphics.RectF(0f, 0f, bubbleWidth, bubbleHeight),
            cornerRadius,
            cornerRadius,
            bgPaint
        )

        val borderPaint = android.graphics.Paint().apply {
            color = Color.parseColor("#BDBDBD")
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.5f * density
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            android.graphics.RectF(0f, 0f, bubbleWidth, bubbleHeight),
            cornerRadius,
            cornerRadius,
            borderPaint
        )

        val fontMetrics = textPaint.fontMetrics
        val textY = bubbleHeight / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(stopName, bubbleWidth / 2f, textY, textPaint)

        val pin = ContextCompat.getDrawable(this, R.drawable.ic_location)
        val pinLeft = ((totalWidth - pinWidth) / 2)
        val pinTop = (bubbleHeight + bubbleBottomMargin).toInt()
        pin?.setBounds(pinLeft, pinTop, pinLeft + pinWidth, pinTop + pinHeight)
        pin?.draw(canvas)

        return bitmap
    }


    @SuppressLint("MissingPermission")
    private fun enableUserLocationDisplay() {
        val style = map.style ?: return

        if (!hasLocationPermission()) {
            Log.w("LiveBusTracking", "Location permission not granted — skipping user dot")
            return
        }

        try {
            val options = LocationComponentOptions.builder(this)
                .accuracyAlpha(0.12f)
                .accuracyColor(Color.parseColor("#1976D2"))
                .foregroundTintColor(Color.parseColor("#1976D2"))
                .backgroundTintColor(Color.WHITE)
                .bearingTintColor(Color.parseColor("#1565C0"))
                .elevation(5f)
                .build()

            val activationOptions = LocationComponentActivationOptions
                .builder(this, style)
                .locationComponentOptions(options)
                .useDefaultLocationEngine(true)
                .build()

            map.locationComponent.apply {
                activateLocationComponent(activationOptions)
                isLocationComponentEnabled = true
                cameraMode = CameraMode.NONE
                renderMode = RenderMode.COMPASS
            }

            Log.d("LiveBusTracking", "User location component enabled")

        } catch (e: Exception) {
            Log.w("LiveBusTracking", "Could not enable location component: ${e.message}")
        }
    }


    private fun fitToStops() {
        if (stops.isEmpty()) return
        val builder = LatLngBounds.Builder()
        stops.forEach { builder.include(LatLng(it.lat, it.lng)) }

        if (stops.size == 1) {
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(stops[0].lat, stops[0].lng))
                        .zoom(15.0)
                        .build()
                ), 1800
            )
        } else {
            map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(builder.build(), 250), 1800
            )
        }

        map.setMinZoomPreference(10.0)
        map.setMaxZoomPreference(20.0)
    }

    private fun setupRouteLayers() {
        val style = map.style ?: return

        style.addSource(GeoJsonSource("shadow-src"))
        style.addLayer(
            LineLayer("shadow-layer", "shadow-src").withProperties(
                lineColor("#000000"),
                lineWidth(10f),
                lineOpacity(0.15f),
                lineBlur(4f),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND)
            )
        )

        style.addSource(GeoJsonSource("completed-src"))
        style.addLayer(
            LineLayer("completed-layer", "completed-src").withProperties(
                lineColor("#BDBDBD"),
                lineWidth(6f),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND)
            )
        )

        style.addSource(GeoJsonSource("remaining-src"))
        style.addLayer(
            LineLayer("remaining-layer", "remaining-src").withProperties(
                lineColor("#4CAF50"),
                lineWidth(6f),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND)
            )
        )

        fetchRoute()
    }

    private fun fetchRoute() {
        if (stops.size < 2) {
            Log.w("LiveBusTracking", "fetchRoute: need ≥2 stops, have ${stops.size}")
            return
        }
        val path = stops.joinToString(";") { "${it.lng},${it.lat}" }
//        val url = "http://192.168.6.87:5000/route/v1/driving/$path?overview=full&geometries=geojson"
        val url = "${busData?.map_url_schoolchimes}$path?overview=full&geometries=geojson"
        Log.d("LiveBusTracking", "Fetching OSRM route: $url")

        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000

                val responseCode = connection.responseCode
                val stream = if (responseCode == HttpURLConnection.HTTP_OK)
                    connection.inputStream
                else
                    connection.errorStream
                val response = stream.bufferedReader().use { it.readText() }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("LiveBusTracking", "OSRM HTTP $responseCode: $response")
                    return@thread
                }

                val json = JSONObject(response)
                val code = json.optString("code", "")
                if (code != "Ok") {
                    Log.e("LiveBusTracking", "OSRM error code: $code")
                    return@thread
                }

                val routesArray = json.optJSONArray("routes")
                if (routesArray == null || routesArray.length() == 0) {
                    Log.e("LiveBusTracking", "OSRM returned no routes")
                    return@thread
                }

                val coords = routesArray
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

                roadCoords.clear()
                for (i in 0 until coords.length()) {
                    val c = coords.getJSONArray(i)
                    roadCoords.add(Point.fromLngLat(c.getDouble(0), c.getDouble(1)))
                }
                Log.d("LiveBusTracking", "Route fetched: ${roadCoords.size} road coords")

                runOnUiThread {
                    map.style?.getSourceAs<GeoJsonSource>("shadow-src")
                        ?.setGeoJson(LineString.fromLngLats(roadCoords))
                    map.style?.getSourceAs<GeoJsonSource>("remaining-src")
                        ?.setGeoJson(LineString.fromLngLats(roadCoords))
                }

            } catch (e: Exception) {
                Log.e("LiveBusTracking", "Route fetch error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this, "Route error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun smoothMoveBusToLocation(target: LatLng) {
        val from = currentBusLatLng ?: target
        currentBusLatLng = target

        updateBusIndexForLocation(target)

        busAnimator?.cancel()
        busAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 900
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val f = anim.animatedValue as Float
                val lat = from.latitude + (target.latitude - from.latitude) * f
                val lng = from.longitude + (target.longitude - from.longitude) * f
                val interpolated = LatLng(lat, lng)

                if (busMarker == null) {
                    busMarker = map.addMarker(
                        MarkerOptions()
                            .position(interpolated)
                            .title("🚌 ${busData?.vehicle_no ?: "Bus"}")
                            .snippet("Route: ${busData?.route_name ?: "—"}")
                            .icon(scaledBusIcon())
                    )
                } else {
                    busMarker!!.position = interpolated
                }

                if (isCameraFollowingBus) {
                    map.moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(interpolated)
                                .zoom(15.5)
                                .tilt(30.0)
                                .bearing(bearingBetween(from, target))
                                .build()
                        )
                    )
                }
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    updatePolylineSplit()
                }
            })
        }
        busAnimator!!.start()
    }

    private fun updateBusIndexForLocation(busLatLng: LatLng) {
        if (roadCoords.isEmpty()) return
        var minDist = Double.MAX_VALUE
        var closest = 0
        roadCoords.forEachIndexed { idx, point ->
            val d = LatLng(point.latitude(), point.longitude()).distanceTo(busLatLng)
            if (d < minDist) {
                minDist = d; closest = idx
            }
        }
        busIndex = closest
    }

    private fun updatePolylineSplit() {
        if (roadCoords.isEmpty()) return
        val snapshot = roadCoords.toList()
        val safeIndex = busIndex.coerceIn(0, snapshot.size - 1)
        val completed = snapshot.subList(0, safeIndex + 1)
        val remaining = snapshot.subList(safeIndex, snapshot.size)
        map.style?.getSourceAs<GeoJsonSource>("completed-src")
            ?.setGeoJson(LineString.fromLngLats(completed))
        map.style?.getSourceAs<GeoJsonSource>("remaining-src")
            ?.setGeoJson(LineString.fromLngLats(remaining))
    }



    private fun checkStopReached(busPoint: Point) {
        if (isTripCompleted) return
        val busLatLng = LatLng(busPoint.latitude(), busPoint.longitude())

        if (journeyStatus?.equals("DROPPING", ignoreCase = true) == true) {
            val childStopName = busData?.stop_name?.trim() ?: return

            val nextIndex = currentStopIndex + 1
            if (nextIndex < stops.size) {
                val nextStop = stops[nextIndex]
                val nextLatLng = LatLng(nextStop.lat, nextStop.lng)

                if (busLatLng.distanceTo(nextLatLng) <= STOP_RADIUS_METERS) {
                    currentStopIndex = nextIndex
                    renderStopTimeline(currentStopIndex)

                    Log.d("LiveBusTracking", "DROPPING: reached stop '${nextStop.name}'")

                    if (nextStop.name.trim().equals(childStopName, ignoreCase = true)) {
                        isTripCompleted = true
                        showTripCompletedDialog()
                    }
                }
            }
            return
        }


        val lastStop = stops.lastOrNull() ?: return
        val lastLatLng = LatLng(lastStop.lat, lastStop.lng)

        if (busLatLng.distanceTo(lastLatLng) <= LAST_STOP_RADIUS_METERS) {
            lastStopReachedCount++
            Log.d("LiveBusTracking", "Near last stop: count=$lastStopReachedCount")
            if (lastStopReachedCount >= LAST_STOP_CONFIRM_COUNT) {
                isTripCompleted = true
                currentStopIndex = stops.lastIndex
                renderStopTimeline(currentStopIndex)
                showTripCompletedDialog()
            }
            return
        } else {
            lastStopReachedCount = 0
        }

        val nextIndex = currentStopIndex + 1
        if (nextIndex < stops.lastIndex) {
            val stopLatLng = LatLng(stops[nextIndex].lat, stops[nextIndex].lng)
            if (busLatLng.distanceTo(stopLatLng) <= STOP_RADIUS_METERS) {
                currentStopIndex = nextIndex
                renderStopTimeline(currentStopIndex)
                if (currentStopIndex == 1) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    private fun showTripCompletedDialog() {
        if (isFinishing || isDestroyed) return
        stopLocationPolling()

        runOnUiThread {
            binding.mapCoordinatorLayout.visibility = View.GONE
            val dialogView = layoutInflater.inflate(R.layout.dialog_trip_completed, null)
            val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)
            val btnDone = dialogView.findViewById<Button>(R.id.btnDone)

            tvDescription.text = if (journeyStatus?.equals("DROPPING", ignoreCase = true) == true) {
                "Successfully completed. Thank you for traveling with us! This trip has been completed, and no further tracking updates are available"
            } else {
                "Successfully completed ${stops.size} stops\nThank you for traveling with us!"
            }

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            btnDone.setOnClickListener {
                dialog.dismiss()
                finish()
            }

            dialog.show()
        }
    }

    private fun bearingBetween(from: LatLng, to: LatLng): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }


    override fun onStart() {
        super.onStart()
        if (isVendor != "dhundhoo") mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (isVendor == "dhundhoo") {
            binding.WVLiveBus.onResume()
            binding.WVLiveBus.resumeTimers()
            if (isSettingsOpened) {
                isSettingsOpened = false; checkAndRequestLocation()
            }
        } else {
            mapView.onResume()
            if (isMapReady && !isTripCompleted) startLocationPolling()
        }
    }

    override fun onPause() {
        if (isVendor == "dhundhoo") {
            binding.WVLiveBus.onPause()
            binding.WVLiveBus.pauseTimers()
        } else {
            mapView.onPause()
        }
        stopLocationPolling()
        busAnimator?.cancel()
        super.onPause()
    }

    override fun onStop() {
        if (isVendor != "dhundhoo") mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        stopLocationPolling()
        busAnimator?.cancel()
        if (isVendor == "dhundhoo") {
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
        if (isVendor != "dhundhoo") mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isVendor != "dhundhoo") mapView.onSaveInstanceState(outState)
    }
}