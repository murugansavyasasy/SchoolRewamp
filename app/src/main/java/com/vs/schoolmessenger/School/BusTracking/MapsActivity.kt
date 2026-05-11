package com.vs.schoolmessenger.School.BusTracking


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.graphics.Color
import android.os.*
import android.widget.Toast
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.BusTracking.Model.BusStop

import org.json.JSONObject
import org.maplibre.android.MapLibre
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


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: MapLibreMap

    // Demo stops (same as iOS)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)

        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mapLibre: MapLibreMap) {

        map = mapLibre

        map.setStyle(
            Style.Builder().fromUri(
                "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
            )
        ) {

            addStopPins()
            fetchRoute()
        }
    }

    // -----------------------
    // Stop Pins
    // -----------------------

    private fun addStopPins() {

        stops.forEach {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lat, it.lng))
                    .title(it.name)
            )
        }
    }

    // -----------------------
    // OSRM Route Fetch
    // -----------------------

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

    // -----------------------
    // Route Layers
    // -----------------------

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

    // -----------------------
    // Bus Animation
    // -----------------------

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

    // -----------------------
    // Lifecycle
    // -----------------------

    override fun onStart() {
        super.onStart(); mapView.onStart()
    }

    override fun onResume() {
        super.onResume(); mapView.onResume()
    }

    override fun onPause() {
        super.onPause(); mapView.onPause()
    }

    override fun onStop() {
        super.onStop(); mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy(); mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory(); mapView.onLowMemory()
    }
}