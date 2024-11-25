package com.devinci.landminemapper

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import androidx.exifinterface.media.ExifInterface
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.OutputStream

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fab: FloatingActionButton
    private val landmineMap = HashMap<Marker, Landmine>()

    private var currentLocation: LatLng = LatLng(0.0, 0.0) // Default to (0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        fab = findViewById(R.id.fab)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fab.setOnClickListener {
            openCamera()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Set up InfoWindowAdapter
        googleMap.setInfoWindowAdapter(LandmineInfoWindowAdapter(this, landmineMap))

        // Listen for map camera movements to track the current location
        googleMap.setOnCameraIdleListener {
            currentLocation = googleMap.cameraPosition.target
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            saveImageToGallery(bitmap)
        } else {
            Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        takePicture.launch(null)
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Landmine_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            // Extract GPS metadata
            val inputStream = resolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            val latLong = exif?.latLong

            val markerLocation: LatLng = if (latLong != null) {
                LatLng(latLong[0], latLong[1]) // Use GPS coordinates from the photo
            } else {
                currentLocation // Default to map's current center if GPS data is missing
            }

            // Create landmine object
            val landmine = Landmine(
                name = "Landmine ${System.currentTimeMillis()}",
                discoverer = "John Doe", // Placeholder for discoverer name
                latitude = markerLocation.latitude,
                longitude = markerLocation.longitude,
                defused = false,
                imageUri = uri.toString()
            )

            // Add marker and associate it with landmine
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(markerLocation)
                    .title(landmine.name)
            )
            if (marker != null) {
                landmineMap[marker] = landmine
            }

            Toast.makeText(this, "Photo saved to gallery and marker added!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
        }
    }

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
