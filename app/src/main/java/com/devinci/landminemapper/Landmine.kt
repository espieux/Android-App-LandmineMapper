package com.devinci.landminemapper

data class Landmine(
    val name: String,
    val discoverer: String,
    val latitude: Double,
    val longitude: Double,
    val defused: Boolean,
    val imageUri: String // Stores the URI of the photo
)
