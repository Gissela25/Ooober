package com.ooober.driver.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ooober.driver.models.Driver

class DriverProvider {

    val db = Firebase.firestore.collection("Drivers")

    fun create(driver: Driver): Task<Void>{
        return db.document(driver.id!!).set(driver)
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
        return db.document(driver?.id!!).update(map)
    }
}