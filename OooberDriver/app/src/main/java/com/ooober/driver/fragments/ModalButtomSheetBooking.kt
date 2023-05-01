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
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ooober.driver.R
import com.ooober.driver.activities.MapActivity
import com.ooober.driver.activities.MapTripActivity
import com.ooober.driver.models.Booking
import com.ooober.driver.providers.AuthProvider
import com.ooober.driver.providers.BookingProvider
import com.ooober.driver.providers.GeoProvider

class ModalButtomSheetBooking : BottomSheetDialogFragment() {


    private lateinit var txtViewOrigin: TextView
    private lateinit var txtViewDestination: TextView
    private lateinit var txtViewTimeAndDistance: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button
    private lateinit var booking: Booking
    private val bookingProvider = BookingProvider()
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private lateinit var mapActivity: MapActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_booking, container, false)

        txtViewOrigin = view.findViewById(R.id.txtViewOrigin)
        txtViewDestination = view.findViewById(R.id.txtViewDestination)
        txtViewTimeAndDistance = view.findViewById(R.id.txtViewTimeAndDistance)
        btnAccept = view.findViewById(R.id.btnAccept)
        btnCancel = view.findViewById(R.id.btnCancel)

        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!
        Log.d("ARGUMENTS", "Booking: ${booking?.toJson()}")

        txtViewOrigin.text = booking?.origin
        txtViewDestination.text = booking?.destination
        txtViewTimeAndDistance.text =
            "${String.format("%.1f", booking?.time)} Min - ${String.format("%.1f", booking?.km)} Km"

        btnAccept.setOnClickListener { acceptBooking(booking?.idClient!!) }
        btnCancel.setOnClickListener { cancelBooking(booking?.idClient!!) }
        return view
    }

    private fun cancelBooking(idCliente: String) {
        bookingProvider.updateStatus(idCliente, "cancel").addOnCompleteListener {
            (activity as? MapActivity)?.timer?.cancel()
                dismiss()
        }
    }

    private fun acceptBooking(idCliente: String) {
        bookingProvider.updateStatus(idCliente, "accept").addOnCompleteListener {
            (activity as? MapActivity)?.timer?.cancel()
            if (it.isSuccessful) {
                (activity as? MapActivity)?.easyWayLocation?.endUpdates()
                geoProvider.removeLocation(authProvider.getId())
                goToMapTrip()
            } else {
                  //  Toast.makeText(activity, "No se pudo aceptar el viaje", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToMapTrip() {
        val i = Intent(context, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context?.startActivity(i)
    }

    companion object {
        const val TAG = "ModalButtomSheet"
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? MapActivity)?.timer?.cancel()
        //if (booking.idClient != null) {
        //    cancelBooking(booking.idClient!!)
        //}
    }
}