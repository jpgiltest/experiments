package com.example.juiceev

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import org.json.JSONObject
import java.net.URL


class MainActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Places.initialize(applicationContext, "AIzaSyCkGKnsJZjDFlnPZfC-gQa-qca_8nXsOJc\n")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        try {
            val mapId = resources.openRawResource(R.raw.map_style).bufferedReader().use { it.readText() }
            val mapStyleUrl = "https://maps.googleapis.com/maps/api/style?key=AIzaSyCkGKnsJZjDFlnPZfC-gQa-qca_8nXsOJc\n&map_id=$mapId"
            mMap.setMapStyle(MapStyleOptions(mapStyleUrl))
        } catch (e: Resources.NotFoundException) {
            Log.e("MainActivity", "Can't find style. Error: $e")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mMap.isMyLocationEnabled = true
                getDeviceLocation()
            }
        }
    }

    private fun getDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }

            mFusedLocationClient.lastLocation.addOnSuccessListener(this) { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    // Call the searchNearbyEVChargers function to find and display real EV chargers
                    searchNearbyEVChargers(currentLatLng)
                }
            }.addOnFailureListener {
                Log.e("MainActivity", "Failed to get device location: $it")
            }
        } catch (e: SecurityException) {
            Log.e("MainActivity", "SecurityException: $e")
        }
    }


    private fun searchNearbyEVChargers(currentLocation: LatLng) {
        val radius = 10000 // Search radius in meters, you can adjust this value
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=${currentLocation.latitude},${currentLocation.longitude}" +
                "&radius=$radius" +
                "&type=electric_charging_station" +
                "&key=AIzaSyCkGKnsJZjDFlnPZfC-gQa-qca_8nXsOJc"

        Thread {
            try {
                val json = JSONObject(URL(url).readText())
                val results = json.getJSONArray("results")
                runOnUiThread {
                    for (i in 0 until results.length()) {
                        val result = results.getJSONObject(i)
                        val location = result.getJSONObject("geometry").getJSONObject("location")
                        val latLng = LatLng(location.getDouble("lat"), location.getDouble("lng"))
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(result.getString("name"))
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Exception while searching EV chargers: $e")
            }
        }.start()
    }


}