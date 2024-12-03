package com.devinci.landminemapper

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.OutputStream

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fab: FloatingActionButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val landmineMap = HashMap<Marker, Landmine>()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchCurrentLocation()
        } else {
            showToast("Location permission denied")
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showToast("Camera permission denied")
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { capturedBitmap ->
            requestCurrentLocationAndSaveImage(capturedBitmap)
        } ?: showToast("Failed to capture photo")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        fab = findViewById(R.id.fab)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fab.setOnClickListener {
            checkPermissionsAndOpenCamera()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setInfoWindowAdapter(LandmineInfoWindowAdapter(this, landmineMap))
        fetchCurrentLocation()
    }

    private fun checkPermissionsAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> openCamera()
                    else -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        takePicture.launch(null)
    }

    private fun requestCurrentLocationAndSaveImage(bitmap: Bitmap) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                showDiscovererInputDialog(bitmap, LatLng(it.latitude, it.longitude))
            } ?: showToast("Unable to get location")
        }.addOnFailureListener {
            showToast("Location retrieval failed")
        }
    }

    private fun showDiscovererInputDialog(bitmap: Bitmap, location: LatLng) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_discoverer_input, null)
        val discovererEditText = dialogView.findViewById<EditText>(R.id.discoverer_edit_text)

        AlertDialog.Builder(this)
            .setTitle("Enter Your Name")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val discovererName = discovererEditText.text.toString().ifEmpty { "Anonymous" }
                saveImageToGallery(bitmap, location, discovererName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveImageToGallery(bitmap: Bitmap, location: LatLng, discoverer: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Landmine_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { imageUri ->
            val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            val landmine = Landmine(
                name = "Landmine ${System.currentTimeMillis()}",
                discoverer = discoverer,
                latitude = location.latitude,
                longitude = location.longitude,
                defused = false,
                imageUri = imageUri.toString()
            )

            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(landmine.name)
            )

            marker?.let {
                landmineMap[it] = landmine
                showToast("Landmine location saved!")
            }
        } ?: showToast("Failed to save image")
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLocation = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d("LandmineMapper", message)
    }

    // Lifecycle methods
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}