package com.ooober.driver.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.system.Os.accept
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ooober.driver.R
import com.ooober.driver.activities.*
import com.ooober.driver.models.Booking
import com.ooober.driver.models.Driver
import com.ooober.driver.providers.AuthProvider
import com.ooober.driver.providers.BookingProvider
import com.ooober.driver.providers.DriverProvider
import com.ooober.driver.providers.GeoProvider

class ModalButtomSheetMenu : BottomSheetDialogFragment() {

    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()
    var textViewUserName: TextView? = null
    var linearLayoutLogout: LinearLayout? = null
    var linearLayoutProfile: LinearLayout? = null
    var linearLayoutHistory: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_menu, container, false)
        textViewUserName = view.findViewById(R.id.textViewUserName)
        linearLayoutLogout = view.findViewById(R.id.linearLayoutLogout)
        linearLayoutProfile = view.findViewById(R.id.linearLayoutProfile)
        linearLayoutHistory = view.findViewById(R.id.linearLayoutHistory)

        getDriver()
        linearLayoutLogout?.setOnClickListener {
            goToMain()
        }
        linearLayoutProfile?.setOnClickListener {
           goToProfile()
        }
        linearLayoutHistory?.setOnClickListener {
            goToHistories()
        }
        return view
    }

    private fun goToProfile(){
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }
    private fun goToHistories(){
        val i = Intent(activity, HistoriesActivity::class.java)
        startActivity(i)
    }

    private fun getDriver(){
            driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
                if(document.exists()){
                    val driver = document.toObject(Driver::class.java)
                    textViewUserName?.text = "${driver?.name} ${(driver?.lastname)?:""}"
                }
            }
    }

    private fun goToMain(){
        authProvider.logout()
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    companion object {
        const val TAG = "ModalButtomSheetMenu"
    }
}