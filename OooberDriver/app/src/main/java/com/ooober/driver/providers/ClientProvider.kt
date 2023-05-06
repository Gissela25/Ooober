package com.ooober.driver.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ooober.driver.models.Client

class ClientProvider {

    val db = Firebase.firestore.collection("Client")

    fun create(client: Client): Task<Void>{
        return db.document(client.id!!).set(client)
    }

    fun getClientById(id: String): Task<DocumentSnapshot>{
        return db.document(id).get()
    }

}