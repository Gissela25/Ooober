package com.ooober.user.utils

import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


object CarMoveAnim {
    fun carAnim(
        carMarker: Marker, googleMap: GoogleMap, startPosition: LatLng,
        endPosition: LatLng, duration: Int, callback: CancelableCallback?
    ) {
        var duration = duration
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        if (duration == 0 || duration < 2000) duration = 2000
        valueAnimator.duration = duration.toLong()
        val latLngInterpolator: LatLngInterpolatorNew = LatLngInterpolatorNew.LinearFixed()
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            val v = valueAnimator.animatedFraction
            val lng = v * endPosition.longitude + (1 - v) * startPosition.longitude
            val lat = v * endPosition.latitude + (1 - v) * startPosition.latitude
            val newPos = latLngInterpolator.interpolate(v, startPosition, endPosition)
            carMarker.position = newPos
            carMarker.setAnchor(0.5f, 0.5f)
            carMarker.rotation = bearingBetweenLocations(startPosition, endPosition)
                .toFloat()
            if (callback != null) {
                googleMap.animateCamera(
                    CameraUpdateFactory
                        .newCameraPosition(
                            CameraPosition.Builder()
                                .target(newPos)
                                .bearing(
                                    bearingBetweenLocations(
                                        startPosition,
                                        endPosition
                                    ).toFloat()
                                )
                                .zoom(12f)
                                .build()
                        ), callback
                )
            } else {
                googleMap.animateCamera(
                    CameraUpdateFactory
                        .newCameraPosition(
                            CameraPosition.Builder()
                                .target(newPos)
                                .bearing(
                                    bearingBetweenLocations(
                                        startPosition,
                                        endPosition
                                    ).toFloat()
                                )
                                .zoom(12f)
                                .build()
                        )
                )
            }
        }
        valueAnimator.start()
    }

    fun carAnim(marker: Marker, start: LatLng, end: LatLng) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 2000
        val latLngInterpolator: LatLngInterpolatorNew = LatLngInterpolatorNew.LinearFixed()
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { valueAnimator ->
            val v = valueAnimator.animatedFraction
            val newPos = latLngInterpolator.interpolate(v, start, end)
            Log.d("ENTRO", "Lat: " + newPos.latitude + " Lng " + newPos.longitude)
            marker.position = newPos
            marker.setAnchor(0.5f, 0.5f)
            marker.rotation = bearingBetweenLocations(start, end).toFloat()
        }
        valueAnimator.start()
    }

    private fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Double {
        val PI = 3.14159
        val lat1 = latLng1.latitude * PI / 180
        val long1 = latLng1.longitude * PI / 180
        val lat2 = latLng2.latitude * PI / 180
        val long2 = latLng2.longitude * PI / 180
        val dLon = long2 - long1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - (Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon))
        var brng = Math.atan2(y, x)
        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360
        return brng
    }

    interface LatLngInterpolatorNew {
        fun interpolate(fraction: Float, a: LatLng?, b: LatLng?): LatLng
        class LinearFixed : LatLngInterpolatorNew {

            override fun interpolate(fraction: Float, a: LatLng?, b: LatLng?): LatLng {
                val lat = (b?.latitude!! - a?.latitude!!) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                if (Math.abs(lngDelta) > 180) lngDelta -= Math.signum(lngDelta) * 360
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }
}