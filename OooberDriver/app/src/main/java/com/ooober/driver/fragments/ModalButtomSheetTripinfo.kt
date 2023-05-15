package com.ooober.driver.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.system.Os.accept
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ooober.driver.R
import com.ooober.driver.activities.*
import com.ooober.driver.models.Booking
import com.ooober.driver.models.Client
import com.ooober.driver.models.Driver
import com.bumptech.glide.Glide
import com.ooober.driver.providers.*
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.ref.Cleaner

class ModalButtomSheetTripinfo : BottomSheetDialogFragment() {

    private var client:  Client? = null
    private lateinit  var booking: Booking
    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()
    var textViewClientName :TextView? = null
    var textViewOrigin :TextView? = null
    var textViewDestination :TextView? = null
    var imageViewPhone :ImageView? = null
    var circleImageClient :CircleImageView? = null

    val REQUEST_HOME_CALL = 30
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_trip_info, container, false)

        textViewClientName = view.findViewById(R.id.textViewClientName)
        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        imageViewPhone = view.findViewById(R.id.imageViewPhone)
        circleImageClient = view.findViewById(R.id.circleImageClient)
        //getDriver()
        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!

        textViewOrigin?.text = booking.origin
        textViewDestination?.text = booking.destination
        imageViewPhone?.setOnClickListener {
            if(client?.phone !=null) {
                if(ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.CALL_PHONE) !=    PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE),REQUEST_HOME_CALL)
                }
                call(client?.phone!!)
            }
        }
        getClientInfo()
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_HOME_CALL){
            if(client?.phone !=null) {
                call(client?.phone!!)
            }
        }
    }

    private fun call(phone: String){
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:${phone}")

        if(ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.CALL_PHONE) !=    PackageManager.PERMISSION_GRANTED ){
            return
        }

          requireActivity().startActivity(intent)
    }
    private fun getClientInfo(){
            clientProvider.getClientById(booking?.idClient!!).addOnSuccessListener { document ->
                if(document.exists()){
                    client = document.toObject(Client::class.java)
                    textViewClientName?.text = "${client?.name} ${(client?.lastname)?:""}"
                    if(client?.image != null){
                        if(client?.image != ""){
                            Glide.with(requireActivity()).load(client?.image).into(circleImageClient!!)
                        }
                    }
                    //textViewUserName?.text = "${driver?.name} ${(driver?.lastname)?:""}"
                }
            }
    }

    companion object {
        const val TAG = "ModalButtomSheetTripinfo"
    }
}