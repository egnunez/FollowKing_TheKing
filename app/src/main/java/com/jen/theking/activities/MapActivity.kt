package com.jen.theking.activities

//import android.location.LocationRequest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.jen.theking.R
import com.jen.theking.databinding.ActivityMapBinding
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.jen.theking.providers.AuthProvider
import com.jen.theking.providers.GeoProvider


class MapActivity : AppCompatActivity(),OnMapReadyCallback, Listener {

    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private var geoProvider = GeoProvider()
    private var authProvider = AuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0 // Intervalo de actualización de ubicación en milisegundos
            fastestInterval = 0  // Prioridad alta de la solicitud de ubicación
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }
        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)
        locationPermission.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION)
        )
        binding.btnConnect.setOnClickListener{connectDriver()}
        binding.btnDisconnect.setOnClickListener{disconnectDriver()}
//hasta aca revisado EGN 23/5/2023
    }

    val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permission.getOrDefault(
                        android.Manifest.permission.ACCESS_FINE_LOCATION, false
                    ) -> {
                        Log.d("Localizacion", "Permiso concedido");
            //            easyWayLocation?.startLocation()
                          checkIfDriverIsConnected()
                    }

                    permission.getOrDefault(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION, false

                    ) -> {
                        Log.d("Localizacion", "Permiso concedido con limitacion");
             //           easyWayLocation?.startLocation()
                          checkIfDriverIsConnected()
                    }

                else ->  {
                        Log.d("Localizacion", "Permiso no concedido");

                         }
                }
            }
//            googleMap?.isMyLocationEnabled = true
}

    private fun checkIfDriverIsConnected(){
        geoProvider.getLocation(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                if(document.contains("l")){
                    connectDriver()
                }
                else{
                    showButtonDisconnect()
                }
            }
            else{
                showButtonDisconnect()
            }
        }

    }


    private fun saveLocation(){
        if (myLocationLatLng != null){
            geoProvider.savelocation(authProvider.getId(),myLocationLatLng!!)
        }
    }

    private fun disconnectDriver() {
        easyWayLocation?.endUpdates()
        if (myLocationLatLng != null) {
            geoProvider.removeLocation(authProvider.getId())
            showButtonConnect()
        }
        myLocationLatLng = null
    }


    private fun connectDriver(){
        easyWayLocation?.endUpdates()
        easyWayLocation?.startLocation()
            showButtonDisconnect()
    }


    private fun showButtonConnect(){
        binding.btnDisconnect.visibility = View.GONE
        binding.btnConnect.visibility = View.VISIBLE

    }

    private fun showButtonDisconnect(){
        binding.btnDisconnect.visibility = View.VISIBLE
        binding.btnConnect.visibility = View.GONE


    }


    private fun addMarker(){
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.thecrown)
        val markerIcon = getMarkerFromDrawable(drawable!!)

        if(markerDriver != null){
            markerDriver?.remove()
        }

        if(myLocationLatLng != null){
            markerDriver = googleMap?.addMarker(
                MarkerOptions()
                .position(myLocationLatLng!!)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .icon(markerIcon)
            )
        }

    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            100,
            100,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,100,100)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
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
    googleMap?.uiSettings?.setZoomGesturesEnabled(true)

    googleMap?.setPadding(0, 0, 0, 200)

    easyWayLocation?.startLocation();

    if(ActivityCompat.checkSelfPermission(
            this,android.Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED)
    {
      return

     }
        googleMap?.isMyLocationEnabled = false

        googleMap?.setOnMapClickListener {
            // Lógica para el zoom in
            googleMap?.moveCamera(CameraUpdateFactory.zoomIn())
        }

        googleMap?.setOnMapLongClickListener {
            // Lógica para el zoom out
            googleMap?.moveCamera(CameraUpdateFactory.zoomOut())
        }

        googleMap?.setOnCameraMoveListener {
            // Lógica adicional según sea necesario
        }

    }

    override fun locationOn() {
    }

    override fun currentLocation(location: Location?) {
        location?.let {
            myLocationLatLng =
                LatLng(location.latitude, location.longitude)   //Lat y Long de la pos actual

            googleMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()
                )
            )
            addMarker()
            saveLocation()
        }
    }


    override fun locationCancelled() {
    }

}
