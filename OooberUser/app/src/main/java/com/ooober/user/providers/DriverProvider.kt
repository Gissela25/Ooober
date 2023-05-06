package com.ooober.user.providers

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.ooober.user.models.Driver
import java.io.File

class DriverProvider {

    val db = Firebase.firestore.collection("Drivers")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")
    fun create(driver: Driver): Task<Void>{
        return db.document(driver.id!!).set(driver)
    }

    fun getImageUrl(): Task<Uri> {
        return storage.downloadUrl
    }

    fun uploadImage(id:String,file:File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val ref = storage.child("$id.jpg")
        storage = ref
        val uploadTask = ref.putFile(fromFile)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "Error : ${it.message}")
        }

    }

    fun getDriver(idDriver:String): Task<DocumentSnapshot> {
        return db.document(idDriver).get()
    }

    fun update(driver: Driver): Task<Void> {
        val map:MutableMap<String, Any> = HashMap()
        map["name"] = driver?.name!!
        map["lastname"] = driver?.lastname!!
        map["phone"] = driver?.phone!!
        map["brandCar"] = driver?.brandCar!!
        map["colorCar"] = driver?.colorCar!!
        map["plateNumber"] = driver?.plateNumber!!
        map["image"] = driver?.image!!
        return db.document(driver?.id!!).update(map)
    }
}