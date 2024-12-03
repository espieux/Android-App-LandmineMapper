package com.devinci.landminemapper

data class Landmine(
    val id: Long = System.currentTimeMillis(), // Unique identifier
    val name: String,
    val discoverer: String,
    val latitude: Double,
    val longitude: Double,
    val defused: Boolean,
    val imageUri: String
)