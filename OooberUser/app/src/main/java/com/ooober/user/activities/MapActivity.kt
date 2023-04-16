package com.ooober.user.activities

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.ooober.user.R
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.android.SphericalUtil
import com.ooober.user.databinding.ActivityMapBinding
import com.ooober.user.providers.AuthProvider
import com.ooober.user.providers.GeoProvider

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var myLocationlatLog: LatLng? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()

    //GooglePlaces
    private var places: PlacesClient? = null
    private var autocompleteOrigin: AutocompleteSupportFragment? = null
    private var autocompleteDestination: AutocompleteSupportFragment? = null
    private var originName = ""
    private var destinationName = ""
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var isLocationEnabled = false

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
        //Instance to get location
        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)
        locationPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        startGooglePlaces()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.d("LOCALIZACION", "Permiso concdedido")
                        easyWayLocation?.startLocation()

                    }
                    permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        Log.d("LOCALIZACION", "Permiso concdedido con limitación")
                        easyWayLocation?.startLocation()

                    }
                    else -> {
                        Log.d("LOCALIZACION", "Permiso no concedido")
                    }
                }
            }
        }

    //Somthing use to happend
    private fun onCameraMove() {
        googleMap?.setOnCameraMoveListener {
            try {
                val geocoder = Geocoder(this)
                originLatLng = googleMap?.cameraPosition?.target
                if(originLatLng != null){
                    val addressList = geocoder.getFromLocation(originLatLng?.latitude!!, originLatLng?.longitude!!, 1)
                    if (addressList != null && addressList.isNotEmpty()) {
                        val city = addressList[0].locality
                        val country = addressList[0].countryName
                        val address = addressList[0].getAddressLine(0)
                        originName = "$address $city"
                        autocompleteOrigin?.setText("$address $city")
                    }
                }
            } catch (e: Exception) {
                Log.d("ERROR", "Mensaje error: ${e.message}")
            }
        }
    }

    private fun startGooglePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, resources.getString(R.string.google_maps_key))
        }

        places = Places.createClient(this)
        instanceAutocompleteOrigin()
        instanceAutocompleteDestination()
    }

    private fun limitSearch() {
        val northSide = SphericalUtil.computeOffset(myLocationlatLog, 5000.0, 0.0)
        val southSide = SphericalUtil.computeOffset(myLocationlatLog, 5000.0, 180.0)

        autocompleteOrigin?.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
        autocompleteDestination?.setLocationBias(
            RectangularBounds.newInstance(
                southSide,
                northSide
            )
        )
    }

    private fun instanceAutocompleteOrigin() {
        autocompleteOrigin =
            supportFragmentManager.findFragmentById(R.id.placesAutocompleteOrigin) as AutocompleteSupportFragment
        autocompleteOrigin?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
        )
        autocompleteOrigin?.setHint("Lugar de recogida")
        autocompleteOrigin?.setCountry("SV")
        autocompleteOrigin?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                originName = place.name!!
                originLatLng = place.latLng
                Log.d("PLACES", "Address: $originName")
                Log.d("PLACES", "LAT: ${originLatLng?.latitude}")
                Log.d("PLACES", "LNG: ${originLatLng?.longitude}")
            }

            override fun onError(p0: Status) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun instanceAutocompleteDestination() {
        autocompleteDestination =
            supportFragmentManager.findFragmentById(R.id.placesAutocompleteDestination) as AutocompleteSupportFragment
        autocompleteDestination?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
        )
        autocompleteDestination?.setHint("Destino")
        autocompleteDestination?.setCountry("SV")
        autocompleteDestination?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destinationName = place.name!!
                destinationLatLng = place.latLng
                Log.d("PLACES", "Address: $destinationName")
                Log.d("PLACES", "LAT: ${destinationLatLng?.latitude}")
                Log.d("PLACES", "LNG: ${destinationLatLng?.longitude}")
            }

            override fun onError(p0: Status) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        onCameraMove()
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
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location) {
        //Lat y log de la posicion
        myLocationlatLog = LatLng(location.latitude, location.longitude)

        if (!isLocationEnabled) {
            googleMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder().target(myLocationlatLog!!).zoom(15f).build()
                )
            )
            isLocationEnabled = true
            limitSearch()
        }
    }

    override fun locationCancelled() {

    }


}