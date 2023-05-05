package com.ooober.driver.providers


import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ooober.driver.models.Booking
import com.ooober.driver.models.History

class HistoryProvider {

    val db = Firebase.firestore.collection("Histories")
    val authProvider = AuthProvider()

    fun create(history: History): Task<DocumentReference> {
        return db.add(history).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }
    }

    fun getLastHistory(): Query{
        return  db.whereEqualTo("idDriver",authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idDriver",authProvider.getId())
    }

    fun updateStatus(idCliente:String,status:String): Task<Void> {
        return db.document(idCliente).update("status", status).addOnFailureListener{
            exception -> Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }

}