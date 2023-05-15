package com.ooober.driver.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration
import com.ooober.driver.databinding.ActivityMapBinding
import com.ooober.driver.fragments.ModalButtomSheetBooking
import com.ooober.driver.fragments.ModalButtomSheetMenu
import com.ooober.driver.models.Booking
import com.ooober.driver.models.FCMBody
import com.ooober.driver.models.FCMResponse
import com.ooober.driver.providers.*
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener,SensorEventListener {

    private  var bookingListener: ListenerRegistration? = null
    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationlatLog: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider= GeoProvider()
    private val authProvider = AuthProvider()
    private val driverProvider = DriverProvider()
    private val bookingProvider = BookingProvider()
    private val notificationProvider = NotificationProvider()
    private val modalBooking = ModalButtomSheetBooking()
    private val modalMenu = ModalButtomSheetMenu()

    //SENSOR CAMER
    private var angle = 0
    private val rotationMatrix = FloatArray(16)
    private var sensorManager:SensorManager ?= null
    private var vectSensor:Sensor ?= null
    private var declination = 0.0f
    private var isFistTimeOnResume = false
    private var isFistLocation = false

     val timer = object : CountDownTimer(30000,1000){
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

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        //To get specific location
        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f

        }
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        vectSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        //Instance to get location
        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)
        locationPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        listenerBooking()
        createToken()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnConnect.setOnClickListener{connectDriver()}
        binding.btnDisconnect.setOnClickListener{disconnectDriver()}
        binding.imageViewMenu.setOnClickListener{
            showModalMenu()
        }
    }

    val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.d("LOCALIZACION", "Permiso concdedido")
                       // easyWayLocation?.startLocation()
                        checkIfDriverIsConnected()
                    }
                    permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        Log.d("LOCALIZACION", "Permiso concdedido con limitación")
                  //      easyWayLocation?.startLocation()
                        checkIfDriverIsConnected()
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

    private fun createToken(){
        driverProvider.createToken(authProvider.getId())
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

    private fun showModalMenu(){
        modalMenu.show(supportFragmentManager, ModalButtomSheetMenu.TAG)
    }

    private fun showModalBookin(booking: Booking){
        val bundle = Bundle()
        bundle.putString("booking",booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.isCancelable = false
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


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
       // easyWayLocation?.startLocation()
        startSensor()
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
        binding.btnDisconnect.visibility = View.GONE //ocultando
        binding.btnConnect.visibility = View.VISIBLE //Mostrando
    }

    private fun showButtonDisconnect() {
        binding.btnDisconnect.visibility = View.VISIBLE //ocultando
        binding.btnConnect.visibility = View.GONE //Mostrando
    }

    override fun currentLocation(location: Location) {
        //Lat y log de la posicion
        myLocationlatLog = LatLng(location.latitude, location.longitude)
        val field = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )

        declination = field.declination
       //if(!isFistLocation){
       //    isFistLocation = true
       //    googleMap?.moveCamera(
       //        CameraUpdateFactory.newCameraPosition(
       //            CameraPosition.builder().target(myLocationlatLog!!).zoom(19f).build()
       //        )
       //    )
       //}
        googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(myLocationlatLog!!).zoom(19f).build()
            )
        )
        addDirectionMarker(myLocationlatLog!!,angle)
        saveLocation()
    }

    override fun locationCancelled() {

    }
    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            120,
            120,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, 120, 120)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun updateCamera(bearing:Float){
        val oldPos = googleMap?.cameraPosition
        val pos = CameraPosition.builder(oldPos!!).bearing(bearing).tilt(50f).build()
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
        if(myLocationlatLog != null){
            addDirectionMarker(myLocationlatLog!!, angle )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
        bookingListener?.remove()
        stopSensor()
    }
    private fun addDirectionMarker(latLng: LatLng,angle:Int){
        val circleDrawable = ContextCompat.getDrawable(applicationContext,R.drawable.ic_up_arrow_circle)
        val markerIcon = getMarkerFromDrawable(circleDrawable!!)
        if(markerDriver != null){
                markerDriver?.remove()
        }
        markerDriver = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .anchor(0.5f,0.5f)
                .rotation(angle.toFloat())
                .flat(true)
                .icon(markerIcon)
        )
    }


    override fun onSensorChanged(event: SensorEvent) {
        if(event.sensor.type == Sensor.TYPE_ROTATION_VECTOR)
        {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            if(Math.abs(Math.toDegrees(orientation[0].toDouble()) - angle ) >0.8){
                val bearing = Math.toDegrees(orientation[0].toDouble()).toFloat() + declination
                updateCamera(bearing)

            }
            angle = Math.toDegrees(orientation[0].toDouble()).toInt()
        }
    }

    private fun startSensor(){
        if(sensorManager != null){
            sensorManager?.registerListener(this, vectSensor,SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        }
    }

    private fun stopSensor(){
        sensorManager?.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()
        stopSensor()
    }

    override fun onResume() {
        super.onResume()
        if(!isFistTimeOnResume){
            isFistTimeOnResume = true
        }
        else{
            startSensor()
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }


}