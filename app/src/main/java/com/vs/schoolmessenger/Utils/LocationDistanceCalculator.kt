package com.vs.schoolmessenger.Utils

import android.location.Location

class LocationDistanceCalculator {

    fun calculateDistance(isStaffCurrentLatitude: Double, isStaffCurrentLongitude: Double, isSchoolLocationLatitude: Double, isSchoolLocationLongitude: Double): Float {
        // Create a Location object for the first location
        val isCurrentLocation = Location("Location 1")
        isCurrentLocation.latitude = isStaffCurrentLatitude
        isCurrentLocation.longitude = isStaffCurrentLongitude

        // Create a Location object for the second location
        val isSchoolLocation = Location("Location 2")
        isSchoolLocation.latitude = isSchoolLocationLatitude
        isSchoolLocation.longitude = isSchoolLocationLongitude

        // Calculate the distance in meters
        return isCurrentLocation.distanceTo(isSchoolLocation)
    }

}