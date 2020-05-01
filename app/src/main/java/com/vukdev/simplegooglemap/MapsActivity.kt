package com.vukdev.simplegooglemap

import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val belgradeLat = 44.787197
        val belgradeLong = 20.457273
        val belgrade = LatLng(belgradeLat, belgradeLong)
        val zoom = 15f

        // za dodavanje pocetne pozicije
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(belgrade, zoom))

        // za dodavanje markera pocetnoj poziciji
        map.addMarker(MarkerOptions().position(belgrade).title("Beograd je svet"))

        // za dodavanje overlay-a
        val androidOverlay = GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
                                .position(belgrade, 100f)

        map.addGroundOverlay(androidOverlay)

        // za long click akcija
        setMapLongClick(map)

        // za klik na poi
        setPoiClick(map)

        // za promenu stila mape
        setMapStyle(map)

        // za odobrenje pracenja lokacije
        enableMyLocation()
    }

    // postavka menu-ja
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }
    // akcije elemenata menu-ja
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }


    // postavljanje markera na long click
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { coordinate ->

            // info window
            val snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", coordinate.latitude, coordinate.longitude)
            // marker sa naslovom i info window-om
            map.addMarker(MarkerOptions()
                .position(coordinate)
                .title(getString(R.string.lat_long_snippet))
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
        }
    }


    // postavljanje markera kada se klikne na poi
    private  fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            poiMarker.showInfoWindow()
        }
    }


    // stilizovanje mape
    // 1. https://mapstyle.withgoogle.com/
    // 2. create style
    // 3. odaberi temu
    // 4. klikni na More Options
    // 5. u okviru Feature type liste, npr odaberi Road > Fill i onda odaberi neku boju puteva na mapi
    // 6. posle Finish-a iskopiraj JSON iz popup-a
    // 7. u android studiju napravi novi resurs fajl - map_style.json a folder oznaci sa raw
    // 8. nalepi iskopirani json sa google-obog servisa

    private val TAG = MapsActivity::class.java.simpleName
    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Parsiranje stila nije uspelo.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Ne mogu da nadjem stil. Greska: ", e)
        }
    }


    // pracenje lokacije
    private val REQUEST_LOCATION_PERMISSION = 1
    // 1. u Manifestu izvan taga <application> dodaj <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    // 2. proveri da li je data dozvola za pracenje lokacije
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }
    // 3. omoguci pracenje lokacije
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            // Alert
            ActivityCompat.requestPermissions(this, arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }
    // 4. overridovati onRequestPermissionsResult
    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

}
