package com.adreal.randomplaces

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.lifecycle.ViewModelProvider
import com.adreal.randomplaces.SharedPreferences.SharedPreferences
import com.adreal.randomplaces.ViewModel.ViewModel
import com.adreal.randomplaces.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, OnRequestPermissionsResultCallback {

    private lateinit var mMap: GoogleMap
    private val binding by lazy {
        ActivityMapsBinding.inflate(layoutInflater)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel by lazy {
        ViewModelProvider(this)[ViewModel::class.java]
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    @RequiresApi(VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        SharedPreferences.init(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.getAllData()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.locationData.observe(this) {
            for (location in it) {
                mMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            location.get("latitude").toString().toDouble(),
                            location.get("longitude").toString().toDouble()
                        )
                    )
                )
            }
        }
    }

    @RequiresApi(VERSION_CODES.M)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markCurrentLocation()
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.setPadding(10, 100, 0, 100)
        enableMyLocation()

        mMap.setOnMapClickListener {
            mMap.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
            viewModel.updateLocation(it)
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        Toast.makeText(this, "Marker Clicked", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onInfoWindowClick(p0: Marker) {
        Toast.makeText(this, "Info Window Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Current Location", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            showRationaleDialog()
        } else {
            // 3. Otherwise, request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @RequiresApi(VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            enableMyLocation()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(VERSION_CODES.M)
    private fun showRationaleDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Required Permission")
            .setMessage("This Permission is required to show your precise location")
            .setPositiveButton("Ok") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        builder.create().show()
    }

    private fun markCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if(location != null){
                        if (this::mMap.isInitialized) {
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        location?.latitude!!,
                                        location.longitude
                                    ), 15f
                                )
                            )
                        }
                    }
                }
        }
    }
}