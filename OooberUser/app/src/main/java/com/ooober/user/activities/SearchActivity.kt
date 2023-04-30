package com.ooober.user.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.ooober.user.R
import com.ooober.user.databinding.ActivitySearchBinding
import com.ooober.user.models.Booking
import com.ooober.user.providers.AuthProvider
import com.ooober.user.providers.BookingProvider
import com.ooober.user.providers.GeoProvider
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    private var extraOriginName = ""
    private var extraDestinationName = ""
    private var extraOriginLat = 0.0
    private var extraOriginLng = 0.0
    private var extraDestinationLat = 0.0
    private var extraDestinationLng = 0.0
    private var extraTime = 0.0
    private var extraDistance = 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var geoProvider = GeoProvider()
    private var authProvider = AuthProvider()

    // BUSQUEDA DEL CONDUCTOR
    private var radius = 0.1
    private var idDriver = ""
    //private var driver: Driver? = null
    private var isDriverFound = false
    private var driverLatLng: LatLng? = null
    private var limitRadius = 20

    private val bookingProvider = BookingProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        //Extras
        extraOriginName = intent.getStringExtra("origin")!!
        extraDestinationName = intent.getStringExtra("destination")!!
        extraOriginLat = intent.getDoubleExtra("origin_lat", 0.0)
        extraOriginLng = intent.getDoubleExtra("origin_lng", 0.0)
        extraDestinationLat = intent.getDoubleExtra("destination_lat", 0.0)
        extraDestinationLng = intent.getDoubleExtra("destination_lng", 0.0)
        extraTime = intent.getDoubleExtra("time", 0.0)
        extraDistance = intent.getDoubleExtra("distance", 0.0)

        originLatLng = LatLng(extraOriginLat, extraOriginLng)
        destinationLatLng = LatLng(extraDestinationLat, extraDestinationLng)

        getClosesDriver()
    }

    private fun createBooking(idDriver: String) {

        val booking = Booking(
            idClient = authProvider.getId(),
            idDriver = idDriver,
            status = "create",
            destination = extraDestinationName,
            origin = extraOriginName,
            time = extraTime,
            km = extraDistance,
            originLat = extraOriginLat,
            originLng = extraOriginLng,
            destinationLat = extraDestinationLat,
            destinationLng = extraDestinationLng
        )

        bookingProvider.create(booking).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this@SearchActivity, "Datos del viaje creados", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(this@SearchActivity, "Error al crear los datos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getClosesDriver(){
        geoProvider.getNearbyDrivers(originLatLng!!, radius).addGeoQueryEventListener(object: GeoQueryEventListener {
            override fun onGeoQueryError(exception: Exception) {

            }

            override fun onGeoQueryReady() {
                if(!isDriverFound){
                    radius = radius + 0.1

                    if(radius > limitRadius){
                        binding.textViewSearch.text = "NO SE ENCONTROL NINGUN CONDUCTOR"
                        return
                    }
                    else {
                        getClosesDriver()
                    }
                }
            }

            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                if(!isDriverFound){
                    isDriverFound = true
                    idDriver = documentID
                    Log.d("FIRESTORE","Conductor id: $idDriver")
                    driverLatLng = LatLng(location.latitude, location.longitude)
                    binding.textViewSearch.text = "CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA"
                    createBooking(documentID)
                }
            }

            override fun onKeyExited(documentID: String) {

            }

            override fun onKeyMoved(documentID: String, location: GeoPoint) {

            }

        })
    }
}