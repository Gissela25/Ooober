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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.ooober.driver.R
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
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

class MapTripActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

    private var markerDestination: Marker? = null
    private var originLatLgn: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var booking: Booking? = null
    private var markerOrigin: Marker? = null
    private var bookingListener: ListenerRegistration? = null
    private lateinit var binding: ActivityMapTripBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationlatLog: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val modalBooking = ModalButtomSheetBooking()
    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil
    private var isLocationEnabled = false
    private var isCloseToOrigin = false
    private val timer = object : CountDownTimer(30000, 1000) {
        override fun onTick(counter: Long) {
            Log.d("TIMER", "Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER", "FINISH")
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

        binding.btnLogout.setOnClickListener {
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


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnStartTrip.setOnClickListener{updateToStarted()}
        binding.btnFinishTtrip.setOnClickListener{updateToFinish()}
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
    private fun saveLocation() {
        if (myLocationlatLog != null) {
            geoProvider.saveLocationWorking(authProvider.getId(), myLocationlatLog!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun getDistancieBetween(originLatLng: LatLng, destinationLatLng: LatLng):Float{
        var distance = 0.0f
        var originLocation = Location("")
        var destinationLocation = Location("")

        originLocation.latitude = originLatLng.latitude
        originLocation.longitude = originLatLng.longitude

        destinationLocation.latitude = destinationLatLng.latitude
        destinationLocation.longitude = destinationLatLng.longitude

        distance = originLocation.distanceTo(destinationLocation)
        return  distance
    }

    private fun getBooking(){
        bookingProvider.getBooking().get().addOnSuccessListener { query ->
            if(query != null)
            {
                if(query.size() > 0){
                    booking = query.documents[0].toObject(Booking::class.java)
                    originLatLgn = LatLng(booking?.originLat!!, booking?.originLng!!)
                    destinationLatLng = LatLng(booking?.destinationLat!!, booking?.destinationLng!!)
                    easyDrawRoute(originLatLgn!!)
                    addOriginMarker(originLatLgn!!)
                }
            }
        }
    }

    private fun easyDrawRoute(position: LatLng) {
        wayPoints.clear()
        wayPoints.add(myLocationlatLog!!)
        wayPoints.add(position!!)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(myLocationlatLog!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.yellow)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(position!!)
            .build()

        directionUtil.initPath()
    }

    private fun addOriginMarker(position:LatLng) {
        markerOrigin = googleMap?.addMarker(
            MarkerOptions().position(position).title("Recoger aquí")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person))
        )
    }

    private fun addDestinationMarker() {
        if(destinationLatLng != null) {
            markerDestination = googleMap?.addMarker(
                MarkerOptions().position(destinationLatLng!!).title("Lugar de Destino")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin))
            )
        }
    }

    private fun showModalBookin(booking: Booking) {
        val bundle = Bundle()
        bundle.putString("booking", booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.show(supportFragmentManager, ModalButtomSheetBooking.TAG)
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

        }
    }
    private fun showButtonFinish() {
        binding.btnStartTrip.visibility = View.GONE //ocultando
        binding.btnFinishTtrip.visibility = View.VISIBLE //Mostrando
    }

    private fun updateToStarted(){
        if(isCloseToOrigin){
            bookingProvider.updateStatus(booking?.idClient!!, "started").addOnCompleteListener{
                if(it.isSuccessful){
                    if(destinationLatLng != null) {
                        googleMap?.clear()
                        addMarker()
                        easyDrawRoute(destinationLatLng!!)
                        addDestinationMarker()
                        markerOrigin?.remove()
                    }
                    showButtonFinish()
                }
            }
        }
        else{
            Toast.makeText(this, "Debes estar más cerca a la poisción de recogida ",Toast.LENGTH_LONG).show()
        }
    }

    private fun updateToFinish(){
            bookingProvider.updateStatus(booking?.idClient!!, "finished").addOnCompleteListener {
                val intent = Intent(this, MapActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
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

        if(booking != null && myLocationlatLog != null) {
            var distance = getDistancieBetween(myLocationlatLog!!, originLatLgn!!)
            Log.d("LOCATION","Distnacia: ${distance} m")
            if(distance <= 300){
                isCloseToOrigin = true
            }
        }
        if(!isLocationEnabled){
            isLocationEnabled = true
            getBooking()
        }
    }

    override fun locationCancelled() {

    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>,
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)
    }


}