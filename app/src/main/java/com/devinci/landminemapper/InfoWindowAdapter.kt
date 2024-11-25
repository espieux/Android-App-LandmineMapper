package com.devinci.landminemapper

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso // Add Picasso to load images easily

class LandmineInfoWindowAdapter(
    private val activity: AppCompatActivity,
    private val landmineMap: HashMap<Marker, Landmine>
) : GoogleMap.InfoWindowAdapter {

    private val infoWindow: View = LayoutInflater.from(activity).inflate(R.layout.landmine_info_window, null)

    override fun getInfoWindow(marker: Marker): View? {
        val landmine = landmineMap[marker] ?: return null

        val landmineName: TextView = infoWindow.findViewById(R.id.landmine_name)
        val landmineDiscoverer: TextView = infoWindow.findViewById(R.id.landmine_discoverer)
        val landmineCoordinates: TextView = infoWindow.findViewById(R.id.landmine_coordinates)
        val landmineDefused: TextView = infoWindow.findViewById(R.id.landmine_defused)
        val landmineImage: ImageView = infoWindow.findViewById(R.id.landmine_image)

        landmineName.text = "Name: ${landmine.name}"
        landmineDiscoverer.text = "Discoverer: ${landmine.discoverer}"
        landmineCoordinates.text = "Coordinates: ${landmine.latitude}, ${landmine.longitude}"
        landmineDefused.text = if (landmine.defused) "Defused: Yes" else "Defused: No"
        Picasso.get().load(landmine.imageUri).into(landmineImage)

        return infoWindow
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }
}
