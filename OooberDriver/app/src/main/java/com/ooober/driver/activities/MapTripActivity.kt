package com.ooober.driver.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
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
import com.ooober.driver.fragments.ModalButtomSheetTripinfo
import com.ooober.driver.models.*
import com.ooober.driver.providers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MapTripActivity : AppCompatActivity(), OnMapReadyCallback, Listener,
    DirectionUtil.DirectionCallBack, SensorEventListener {

    private var client: Client? = null
    private val configProvider = ConfigProvider()
    private var totalPrices = 0.0
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
    private val historyProvider = HistoryProvider()
    private val bookingProvider = BookingProvider()
    private val notificationProvider = NotificationProvider()
    private val clientProvider = ClientProvider()
    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil
    private var isLocationEnabled = false
    private var isCloseToOrigin = false

    //Distancia
    private var meters = 0.0
    private var km = 0.0
    private var previusLocation = Location("")
    private var currentLocation = Location("")
    private var isStartedTrip = false

    //MODAL
    private var modalTrip = ModalButtomSheetTripinfo()


    //SENSOR CAMER
    private var angle = 0
    private val rotationMatrix = FloatArray(16)
    private var sensorManager:SensorManager ?= null
    private var vectSensor:Sensor ?= null
    private var declination = 0.0f
    private var isFistTimeOnResume = false
    private var isFistLocation = false

    //Temporizador
    private var counter = 0
    private var min = 0
    private var handle = Handler(Looper.myLooper()!!)
    private var runnable = Runnable {
        kotlin.run {
            counter++

            if (min == 0) {
                binding.textViewTimer.text = "$counter Seg"
            } else {
                binding.textViewTimer.text = "$min Min $counter Seg"

            }
            if (counter == 60) {
                min = min + (counter / 60)
                binding.textViewTimer.text = "$min Min $counter Seg"
            }

            startTimer()
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


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnStartTrip.setOnClickListener { updateToStarted() }
        binding.btnFinishTtrip.setOnClickListener { updateToFinish() }
        binding.imageViewInfo.setOnClickListener { showModalInfo() }
    }


    private fun getClientInfo(){
        clientProvider.getClientById(booking?.idClient!!).addOnSuccessListener { document ->
            if(document.exists()){
                client = document.toObject(Client::class.java)
            }
        }
    }


    private  fun sendNotification(status: String){

        val map = HashMap<String,String>()
        map.put("title","ESTADO DEL VIAJE")
        map.put("body",status)
        val body = FCMBody(
            to = client?.token!!,
            priority = "high",
            ttl = "4500s",
            data = map
        )

        notificationProvider.sendNotification(body).enqueue(object : Callback<FCMResponse> {
            override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {
                if(response.body() != null){
                    if(response.body()!!.success ==1){
                        Toast.makeText(this@MapTripActivity, "Se envió la notificación", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this@MapTripActivity, "No se envió la notificación", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this@MapTripActivity, "Hubo un error enviando la notificación", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                Log.d("NOTIFICATION","ERROR: ${t.message}")
            }

        })
    }

    private fun showModalInfo(){
        if(booking != null){
            val bundle = Bundle()
            bundle.putString("booking",booking?.toJson())
            modalTrip.arguments = bundle
            modalTrip.show(supportFragmentManager, ModalButtomSheetTripinfo.TAG)
        }
        else{
            Toast.makeText(this,"No se pudo cargar la información", Toast.LENGTH_SHORT).show()
        }

    }

    private fun startTimer() {
        handle.postDelayed(runnable, 1000)
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

 
    private fun getDistancieBetween(originLatLng: LatLng, destinationLatLng: LatLng): Float {
        var distance = 0.0f
        var originLocation = Location("")
        var destinationLocation = Location("")

        originLocation.latitude = originLatLng.latitude
        originLocation.longitude = originLatLng.longitude

        destinationLocation.latitude = destinationLatLng.latitude
        destinationLocation.longitude = destinationLatLng.longitude

        distance = originLocation.distanceTo(destinationLocation)
        return distance
    }

    private fun getBooking() {
        bookingProvider.getBooking().get().addOnSuccessListener { query ->
            if (query != null) {
                if (query.size() > 0) {
                    booking = query.documents[0].toObject(Booking::class.java)
                    originLatLgn = LatLng(booking?.originLat!!, booking?.originLng!!)
                    destinationLatLng = LatLng(booking?.destinationLat!!, booking?.destinationLng!!)
                    easyDrawRoute(originLatLgn!!)
                    addOriginMarker(originLatLgn!!)
                    getClientInfo()
                }
            }
        }
    }

    private fun easyDrawRoute(position: LatLng) {
        wayPoints.clear()
        wayPoints.add(myLocationlatLog!!)
        wayPoints.add(position)
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

    private fun addOriginMarker(position: LatLng) {
        markerOrigin = googleMap?.addMarker(
            MarkerOptions().position(position).title("Recoger aquí")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person))
        )
    }

    private fun addDestinationMarker() {
        if (destinationLatLng != null) {
            markerDestination = googleMap?.addMarker(
                MarkerOptions().position(destinationLatLng!!).title("Lugar de Destino")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin))
            )
        }
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
        startSensor()
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

    private fun updateToStarted() {
        if (isCloseToOrigin) {
            bookingProvider.updateStatus(booking?.idClient!!, "started").addOnCompleteListener {
                if (it.isSuccessful) {
                    if (destinationLatLng != null) {
                        isStartedTrip = true
                        googleMap?.clear()
                        addDirectionMarker(myLocationlatLog!!,angle)
                        easyDrawRoute(destinationLatLng!!)
                        addDestinationMarker()
                        sendNotification("Viaje iniciado")
                        markerOrigin?.remove()

                        startTimer()
                    }
                    showButtonFinish()
                }
            }
        } else {
            Toast.makeText(
                this,
                "Debes estar más cerca a la poisción de recogida ",
                Toast.LENGTH_LONG
            ).show()
        }
    }

/*    private fun updateToFinish() {
        bookingProvider.updateStatus(booking?.idClient!!, "finished").addOnCompleteListener {
            if (it.isSuccessful) {
                handle.removeCallbacks(runnable)
                isStartedTrip = false
                easyWayLocation?.endUpdates()
                geoProvider.removeLocationWorking(authProvider.getId())
                getPrices(km, min.toDouble())


            }
        }
    }*/

    private fun updateToFinish() {
        handle.removeCallbacks(runnable)
        isStartedTrip = false
        geoProvider.removeLocationWorking((authProvider.getId()))
        if (min == 0) {
            min = 1
        }
        getPrices(km, min.toDouble())
    }

    private fun createHistory() {
        val history = History(
            idDriver = authProvider.getId(),
            idClient = booking?.idClient,
            origin = booking?.origin,
            destination = booking?.destination,
            originLat = booking?.originLat,
            originLng = booking?.originLng,
            destinationLat = booking?.destinationLat,
            destinationLng = booking?.destinationLng,
            time = min,
            km = km,
            price = totalPrices,
            timestamp = Date().time
        )
        historyProvider.create(history).addOnCompleteListener {
            if (it.isSuccessful) {
                bookingProvider.updateStatus(booking?.idClient!!, "finished")
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            sendNotification("Viaje terminado")
                            goToCalificationClient()
                        }
                    }
            }
        }
    }

    private fun getPrices(distance: Double, time: Double) {

        configProvider.getPrices().addOnSuccessListener { document ->
            if (document.exists()) {
                val prices =
                    document.toObject(Prices::class.java) // DOCUMENTO CON LA INFORMACION

                val totalDistance = distance * prices?.km!! // VALOR POR KM
                Log.d("PRICES", "totalDistance: $totalDistance")
                val totalTime = time * prices?.min!! // VALOR POR MIN
                Log.d("PRICES", "totalTime: $totalTime")
                totalPrices = totalDistance + totalTime // TOTAL
                Log.d("PRICES", "total: $totalPrices")

                totalPrices = if (totalPrices < 5.0) prices?.minValue!! else totalPrices
                createHistory()
            }
        }

    }

    private fun goToCalificationClient() {
        val i = Intent(this, CalificationClientActivity::class.java)
        i.putExtra("price", totalPrices)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    override fun currentLocation(location: Location) {
        //Lat y log de la posicion
        myLocationlatLog = LatLng(location.latitude, location.longitude)
        currentLocation = location

        if (isStartedTrip) {
            meters = meters + previusLocation.distanceTo(currentLocation)
            km = meters / 1000
            binding.textViewDistance.text = "${String.format("%.1f", km)} Km"
        }

        previusLocation = location

        //if(!isFistLocation){
        //    isFistLocation = true
        //    googleMap?.moveCamera(
        //        CameraUpdateFactory.newCameraPosition(
        //            CameraPosition.builder().target(myLocationlatLog!!).zoom(19f).build()
        //        )
        //    )
        //}
        //googleMap?.moveCamera(
        //    CameraUpdateFactory.newCameraPosition(
        //        CameraPosition.builder().target(myLocationlatLog!!).build()
        //    )
        //)
        googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(myLocationlatLog!!).zoom(19f).build()
            )
        )
        addDirectionMarker(myLocationlatLog!!,angle)
        saveLocation()

        if (booking != null && myLocationlatLog != null) {
            var distance = getDistancieBetween(myLocationlatLog!!, originLatLgn!!)
            Log.d("LOCATION", "Distnacia: ${distance} m")
            if (distance <= 300) {
                isCloseToOrigin = true
            }
        }
        if (!isLocationEnabled) {
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
            sensorManager?.registerListener(this, vectSensor, SensorManager.SENSOR_STATUS_ACCURACY_LOW)
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
