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
import com.vs.schoolmessenger.Parent.BusTracking.Model.BusList.BusListData
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
    private val completedCoords = mutableListOf<Point>()
    private var busMarker: Marker? = null
    private var busIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    private var currentBusLatLng: LatLng? = null
    private var busAnimator: ValueAnimator? = null

    private var isCameraFollowingBus = true
    private var isFirstFit = true

    private val POLL_INTERVAL_MS = 6_500L

    private var lastStopReachedCount = 0
    private val LAST_STOP_CONFIRM_COUNT = 2
    private val locationPollHandler = Handler(Looper.getMainLooper())
    private val locationPollRunnable = object : Runnable {
        override fun run() {
            isGetLatestGeoLocation()
            locationPollHandler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }
    private var isMapReady = false

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    private val STOP_RADIUS_METERS = 150.0
    private val LAST_STOP_RADIUS_METERS = 200.0
    private var isTripCompleted = false

    private var isFirstLocationReceived = false

    var isFirstLocationSynced = false


    override fun onCreate(savedInstanceState: Bundle?) {
        MapLibre.getInstance(this)
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

        userDetails = SharedPreference.getUserDetails(this)
        isVendor = userDetails?.child_details[0]?.gps_type

        Log.d("LiveBusTracking", "isVendor = $isVendor")

        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)
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
        if (appViewModel?.isFirstLocationSynced == true) return
        appViewModel?.isFirstLocationSynced = true
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
            Log.d("LiveBusTracking",
                "syncInitialStopProgress: bus near stop[$closestIndex] " +
                        "→ marking stops 0..$alreadyCompletedIndex as completed")
            renderStopTimeline(currentStopIndex)
        } else {
            Log.d("LiveBusTracking",
                "syncInitialStopProgress: bus near first stop, nothing to pre-mark")
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
        appViewModel!!.isgetgeolocation(
            isAccessToken!!,
            Constant.getAndroidSecureId(this),
            busData?.vehicle_no.toString(),
            busData?.route_id.toString(),
            this
        )
    }

    private fun startLocationPolling() {
        locationPollHandler.removeCallbacks(locationPollRunnable)
        locationPollHandler.post(locationPollRunnable)
    }

    private fun stopLocationPolling() = locationPollHandler.removeCallbacks(locationPollRunnable)


    private fun setupMapGestureListeners() {
        binding.fabRecenter.setOnClickListener {
            isCameraFollowingBus = true
            isFirstFit                     = false
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
        bottomSheetBehavior.state      = BottomSheetBehavior.STATE_COLLAPSED

        populateBusInfoCard()

        binding.imgRefresh.setOnClickListener {
            binding.imgRefresh.startAnimation(
                android.view.animation.RotateAnimation(
                    0f, 360f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
                ).apply { duration = 600; interpolator = android.view.animation.DecelerateInterpolator() }
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
            val to   = stops.lastOrNull()?.name  ?: ""
            if (from.isNotBlank() && to.isNotBlank()) append("$from  →  $to")
            else append(data.route_name ?: "")
        }

        binding.tvFromStop.text = stops.firstOrNull()?.name ?: "—"
        binding.tvFromTime.text = stops.firstOrNull()?.time ?: ""
        binding.tvToStop.text   = stops.lastOrNull()?.name  ?: "—"
        binding.tvToTime.text   = stops.lastOrNull()?.time  ?: ""
        binding.tvBusNo.text    = data.vehicle_no?.let { "$it" } ?: "—"

        Log.d("BottomSheet", "✅ tvBusNumber=${binding.tvBusNumber.text}")
        Log.d("BottomSheet", "✅ tvFromStop =${binding.tvFromStop.text}")
        Log.d("BottomSheet", "✅ tvToStop   =${binding.tvToStop.text}")
        Log.d("BottomSheet", "✅ tvBusNo    =${binding.tvBusNo.text}")

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
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            (child?.tag as? ValueAnimator)?.cancel()
            if (child is LinearLayout) {
                for (j in 0 until child.childCount) {
                    (child.getChildAt(j)?.tag as? ValueAnimator)?.cancel()
                }
            }
        }

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

            val indicatorSize = 28.dp

            val indicatorView: View = when {
                isCurrent -> ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(indicatorSize, indicatorSize)
                    setImageResource(R.drawable.ic_bus_green)
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
                textSize = if (isCurrent) 15f else 14f
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
                    textSize = 11f
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    background = createNowBadgeBackground()
                    setPadding(0, 4.dp, 0, 4.dp)
                })
            } else {
                row.addView(TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = stop.time
                    textSize = 12f
                    setTextColor(
                        if (isReached) Color.parseColor("#4CAF50")
                        else Color.parseColor("#BDBDBD")
                    )
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
            mapView.onCreate(null)
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
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.setGeolocationEnabled(true)
        }
        binding.WVLiveBus.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
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
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = getString(R.string.Something_went_wrong_Please_try_again)
    }

    private fun isLiveBus() {
        Constant.showLoading(this)
        appViewModel?.isLiveBus(isAccessToken!!, this)
    }

    override fun onClick(v: View?) {}


    override fun onMapReady(mapLibreMap: MapLibreMap) {
        map = mapLibreMap

        map.uiSettings.isLogoEnabled = false
        map.uiSettings.isAttributionEnabled = false
        map.uiSettings.isCompassEnabled = true

        map.addOnCameraMoveStartedListener { reason ->
            if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                isCameraFollowingBus = false
                binding.fabRecenter.visibility = View.VISIBLE
            }
        }

        map.setStyle(
            Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")
        ) { style ->
            Log.d("MAP", "Style Loaded Successfully")
            isMapReady = true
            Log.d("LiveBusTracking", "Map ready — stops.size=${stops.size}")
            addStopPins()
            setupRouteLayers()
            fitToStops()
            startLocationPolling()
        }
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

    private fun scaledStopIcon() = IconFactory.getInstance(this)
        .fromBitmap(vectorToBitmap(R.drawable.ic_location, 40.dp, 40.dp))

    private fun scaledBusIcon() = IconFactory.getInstance(this)
        .fromBitmap(vectorToBitmap(R.drawable.ic_bus_green, 60.dp, 60.dp))


    private fun addStopPins() {
        if (stops.isEmpty()) {
            Log.w("LiveBusTracking", "addStopPins: stops is empty — no pins added")
            return
        }
        Log.d("LiveBusTracking", "Adding ${stops.size} stop pins")

        stops.forEachIndexed { index, stop ->
            val isEndpoint = index == 0 || index == stops.lastIndex

            val icon = IconFactory.getInstance(this).fromBitmap(
                vectorToBitmap(
                    R.drawable.ic_location,
                    if (isEndpoint) 48.dp else 36.dp,
                    if (isEndpoint) 48.dp else 36.dp
                )
            )
            val label = buildString {
                append(stop.name)
                when (index) {
                    0               -> append(" 🚏 (Start)")
                    stops.lastIndex -> append(" 🏁 (End)")
                }
            }

            map.addMarker(
                MarkerOptions()
                    .position(LatLng(stop.lat, stop.lng))
                    .title(label)
                    .snippet("Stop time: ${stop.time}")
                    .icon(icon)
            )
        }

        map.setOnMarkerClickListener { marker ->
            if (marker.isInfoWindowShown) {
                marker.hideInfoWindow()
            } else {
                marker.showInfoWindow(map, binding.mapView)
            }
            true
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
        val url =
            "https://router.project-osrm.org/route/v1/driving/$path?overview=full&geometries=geojson"
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
                if (json.getString("code") != "Ok") {
                    Log.e("LiveBusTracking", "OSRM error: ${json.getString("code")}")
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

        val nextIndex = currentStopIndex + 1
        if (nextIndex < stops.size) {
            val stopLatLng = LatLng(stops[nextIndex].lat, stops[nextIndex].lng)
            if (busLatLng.distanceTo(stopLatLng) <= STOP_RADIUS_METERS) {
                currentStopIndex = nextIndex
                renderStopTimeline(currentStopIndex)
                if (currentStopIndex == 1) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

        val lastStop = stops.lastOrNull() ?: return
        val lastLatLng = LatLng(lastStop.lat, lastStop.lng)
        if (busLatLng.distanceTo(lastLatLng) <= LAST_STOP_RADIUS_METERS) {
            lastStopReachedCount++
            if (lastStopReachedCount >= LAST_STOP_CONFIRM_COUNT) {
                isTripCompleted = true
                currentStopIndex = stops.lastIndex
                renderStopTimeline(currentStopIndex)
                showTripCompletedDialog()
            }
        } else {
            lastStopReachedCount = 0
        }
    }

    private fun showTripCompletedDialog() {
        stopLocationPolling()

        val journeyLabel = when (journeyStatus) {
            "PICKING"  -> "Pick-up"
            "DROPPING" -> "Drop-off"
            else       -> "Bus"
        }

        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("$journeyLabel Trip Completed ✅")
                .setMessage(
                    "The $journeyLabel journey has been completed.\n" +
                            "The bus has reached the final stop: ${stops.lastOrNull()?.name ?: "Destination"}."
                )
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    // Optionally go back: onBackPressed()
                }
                .show()
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