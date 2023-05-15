package com.ooober.driver.models

class FCMResponse(
    val success:Int? = null,
    val failure:Int? = null,
    val canonical_ids:Int?=null,
    val multicastid:Long? = null,
    val result:ArrayList<Any> = ArrayList<Any>()
) {
}