package com.the8way.digitaldiary.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

@Suppress("DEPRECATION")
class LocationUtils @Inject constructor(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return withContext(Dispatchers.IO) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider)
                if (l != null && (bestLocation == null || l.accuracy < bestLocation.accuracy)) {
                    bestLocation = l
                }
            }
            bestLocation
        }
    }

    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    "${address.locality}, ${address.countryName}"
                } else {
                    "Unknown location"
                }
            } catch (e: IOException) {
                "Unknown location"
            }
        }
    }
}
