package com.ooober.driver.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.ooober.driver.R
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration
import com.ooober.driver.databinding.ActivityMapBinding
import com.ooober.driver.databinding.ActivityMapTripBinding
import com.ooober.driver.fragments.ModalButtomSheetBooking
import com.ooober.driver.models.Booking
import com.ooober.driver.providers.AuthProvider
import com.ooober.driver.providers.BookingProvider
import com.ooober.driver.providers.GeoProvider

class MapTripActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    private  var bookingListener: ListenerRegistration? = null
    private lateinit var binding: ActivityMapTripBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationlatLog: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider= GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val modalBooking = ModalButtomSheetBooking()

    private val timer = object : CountDownTimer(30000,1000){
        override fun onTick(counter: Long) {
            Log.d("TIMER","Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER","FINISH")
            modalBooking.dismiss()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.btnLogout.setOnClickListener{
            authProvider.logout()
            val i = Intent(this, HomeActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
            finish()
        }

        //To get specific location
        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f

        }
        //Instance to get location
        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)
        locationPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        listenerBooking()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

       // binding.btnStartTrip.setOnClickListener{connectDriver()}
       // binding.btnFinishTtrip.setOnClickListener{disconnectDriver()}
    }

    val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.d("LOCALIZACION", "Permiso concdedido")
                        easyWayLocation?.startLocation()
                        //checkIfDriverIsConnected()
                    }
                    permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        Log.d("LOCALIZACION", "Permiso concdedido con limitación")
                        easyWayLocation?.startLocation()
                        //checkIfDriverIsConnected()
                    }
                    else -> {
                        Log.d("LOCALIZACION", "Permiso no concedido")
                    }
                }
            }
        }

    // To save and set the drivers positions
    private fun saveLocation(){
        if(myLocationlatLog != null){
            geoProvider.saveLocation(authProvider.getId(),myLocationlatLog!!)
        }
    }

    private fun checkIfDriverIsConnected() {
        geoProvider.getLocation(authProvider.getId()).addOnSuccessListener { document->
            if(document.exists()){
                if(document.contains("l")){
                    connectDriver()
                }
                else{
                    showButtonConnect()
                }
            }
            else{
                showButtonConnect()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
        bookingListener?.remove()
    }

    private fun listenerBooking(){
        bookingListener = bookingProvider.getBooking().addSnapshotListener{snapshot, e ->
            if(e != null){
                Log.d("FIRESTORE","ERROR: ${e.message}")
                return@addSnapshotListener
            }
            if(snapshot != null){
                if(snapshot.documents.size >0)
                {
                    val booking = snapshot.documents[0].toObject(Booking::class.java)
                    if(booking?.status == "create"){
                        showModalBookin(booking!!)
                    }
                }
            }

        }
    }

    private fun showModalBookin(booking: Booking){
        val bundle = Bundle()
        bundle.putString("booking",booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.show(supportFragmentManager,ModalButtomSheetBooking.TAG)
        timer.start()
    }

    private fun addMarker() {
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.uber_car)
        val markerIcon = getMarkerFromDrawable(drawable!!)
        if (markerDriver != null) {
            markerDriver!!.remove() //No redibujar el dibujo
        }

        if (myLocationlatLog != null) {
            markerDriver = googleMap?.addMarker(
                MarkerOptions().position(myLocationlatLog!!)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(markerIcon)
            )
        }
    }

    //Traer imagen
    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            70,
            150,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, 70, 150)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        // easyWayLocation?.startLocation()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap?.isMyLocationEnabled = false
        //Code to change map style
        /*
        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )
            if (!success!!) {
                Log.d("MAPAS", "No se pudo encontrar el estlo")
            }
        } catch (e: Resources.NotFoundException) {
            Log.d("MAPAS", "Error : ${e.toString()} ")
        }
         */
    }

    override fun locationOn() {

    }

    private fun disconnectDriver() {
        easyWayLocation?.endUpdates()
        if (myLocationlatLog != null) {
            geoProvider.removeLocation(authProvider.getId())
            showButtonConnect()
        }
    }

    private fun connectDriver() {
        easyWayLocation?.endUpdates()
        easyWayLocation?.startLocation()
        showButtonDisconnect()
    }

    private fun showButtonConnect() {
        binding.btnFinishTtrip.visibility = View.GONE //ocultando
        binding.btnStartTrip.visibility = View.VISIBLE //Mostrando
    }

    private fun showButtonDisconnect() {
        binding.btnFinishTtrip.visibility = View.VISIBLE //ocultando
        binding.btnStartTrip.visibility = View.GONE //Mostrando
    }

    override fun currentLocation(location: Location) {
        //Lat y log de la posicion
        myLocationlatLog = LatLng(location.latitude, location.longitude)

        googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(myLocationlatLog!!).zoom(15f).build()
            )
        )
        addMarker()
        saveLocation()
    }

    override fun locationCancelled() {

    }


}