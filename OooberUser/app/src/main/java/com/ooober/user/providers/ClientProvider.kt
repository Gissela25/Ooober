package com.ooober.user.providers

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.ooober.user.models.Client
import java.io.File

class ClientProvider {

    val db = Firebase.firestore.collection("Client")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")

    fun create(client: Client): Task<Void> {
        return db.document(client.id!!).set(client)
    }

    fun getClientById(id: String): Task<DocumentSnapshot> {
        return db.document(id).get()
    }

    fun uploadImage(id: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val ref = storage.child("$id.jpg")
        storage = ref
        val uploadTask = ref.putFile(fromFile)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "ERROR: ${it.message}")
        }
    }

    fun createToken(idClient: String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(it.isSuccessful){
                val token = it.result //TOKEN
                updateToken(idClient, token)
            }
        }
    }


    fun getImageUrl(): Task<Uri> {
        return storage.downloadUrl
    }

   /* fun createToken(idClient: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result // TOKEN DE NOTIFICACIONES
                updateToken(idClient, token)
            }
        }
    }*/

    fun updateToken(idClient: String, token: String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["token"] = token
        return db.document(idClient).update(map)
    }

    fun update(client: Client): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["name"] = client?.name!!
        map["lastname"] = client?.lastname!!
        map["phone"] = client?.phone!!
        map["image"] = client?.image!!
        return db.document(client?.id!!).update(map)
    }

    fun updateWithOutImage(client: Client): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["name"] = client?.name!!
        map["lastname"] = client?.lastname!!
        map["phone"] = client?.phone!!
        return db.document(client?.id!!).update(map)
    }


}