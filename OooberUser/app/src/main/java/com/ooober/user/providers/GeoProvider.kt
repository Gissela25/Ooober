package com.ooober.user.providers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery

class GeoProvider {

    val collection = FirebaseFirestore.getInstance().collection("Locations")
    val collectionWorking = FirebaseFirestore.getInstance().collection("LocationsWorking")
    val geoFirestore = GeoFirestore(collection)
    val geoFirestoreWorking = GeoFirestore(collectionWorking)

    fun saveLocation(idDriver: String, position: LatLng) {
        geoFirestore.setLocation(idDriver, GeoPoint(position.latitude, position.longitude))
    }

    fun getNearbyDrivers(position: LatLng, radius: Double): GeoQuery{
        val query = geoFirestore.queryAtLocation(GeoPoint(position.latitude, position.longitude), radius)
        query.removeAllListeners()
        return query
    }

    fun removeLocation(idDriver: String) {
        collection.document(idDriver).delete()
    }

    fun getLocation(idDriver: String): Task<DocumentSnapshot> {
        return collection.document(idDriver).get().addOnFailureListener { exception ->
            Log.d("FIREBASE", "ERROR: ${exception.toString()}")
        }
    }

    fun getLocationWorking(idDriver: String): DocumentReference {
        return collectionWorking.document(idDriver)
    }

}