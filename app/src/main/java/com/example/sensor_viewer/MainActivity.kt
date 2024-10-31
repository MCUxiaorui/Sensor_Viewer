// Created by: Xiaorui Liu
// Created date: Sep 8 2024

package com.example.sensor_viewer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorTextView: TextView

    private val magnetometer = Sensor.TYPE_MAGNETIC_FIELD
    private val gyroscope = Sensor.TYPE_GYROSCOPE
    private val pressure = Sensor.TYPE_PRESSURE
    private val proximity = Sensor.TYPE_PROXIMITY
    private val luminance = Sensor.TYPE_LIGHT
    private val accelerometer = Sensor.TYPE_ACCELEROMETER

    // Variables to hold sensor data
    private var magnetometerData = ""
    private var gyroscopeData = ""
    private var pressureData = ""
    private var altitudeData = ""
    private var proximityData = ""
    private var luminanceData = ""
    private var accelerometerData = ""

    // Variable for GPS
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationData = ""

    // GPS speed variables
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var previousLocation: Location? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views and services
        sensorTextView = findViewById(R.id.sensorTextView)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize location request using LocationRequest.Builder
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L // Desired interval in milliseconds
        ).setMinUpdateIntervalMillis(500L) // Fastest interval in milliseconds
            .build()

        // Initialize location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateSpeed(location)
                }
            }
        }

        // Check for location permissions
        checkLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        // Register sensors
        registerSensor(magnetometer)
        registerSensor(gyroscope)
        registerSensor(accelerometer)
        registerSensor(pressure)
        registerSensor(proximity)
        registerSensor(luminance)
        // Start location updates
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listener
        sensorManager.unregisterListener(this)
        // Stop location updates
        stopLocationUpdates()
    }

    private fun registerSensor(sensorType: Int) {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                magnetometer -> {
                    magnetometerData = formatSensorData(event)
                }
                gyroscope -> {
                    gyroscopeData = formatSensorData(event)
                }
                accelerometer -> {
                    accelerometerData = formatSensorData(event)
                }
                pressure -> {
                    pressureData = formatSensorData(event)
                    altitudeData = calculateAltitude(event.values[0])
                }
                proximity -> {
                    proximityData = formatSensorData(event)
                }
                luminance -> {
                    luminanceData = formatSensorData(event)
                }
            }
            // Update the TextView with new sensor data
            updateSensorTextView()
        }
    }

    private fun formatSensorData(event: SensorEvent): String {
        val sensorData = StringBuilder()
        val sensor = event.sensor
        val locale = Locale.US

        sensorData.append("Sensor: ${sensor.name}\n")
        sensorData.append("Vendor: ${sensor.vendor}\n")
        sensorData.append("Version: ${sensor.version}\n")
        sensorData.append("ID: ${sensor.id}\n")
        sensorData.append("Power: ${sensor.power}\n")
        sensorData.append("Type: ${sensor.type}\n")
        sensorData.append("Values: ")
        for (value in event.values) {
            sensorData.append(String.format(locale, "%.2f", value) + ", ")
        }
        sensorData.append("\n\n")

        return sensorData.toString()
    }

    private fun updateSensorTextView() {
        val fullSensorData = StringBuilder()
        fullSensorData.append("Magnetometer Data:\n$magnetometerData\n")
        fullSensorData.append("Gyroscope Data:\n$gyroscopeData\n")
        fullSensorData.append("Accelerometer Data:\n$accelerometerData\n")
        fullSensorData.append("Pressure Data:\n$pressureData")
        fullSensorData.append(altitudeData)
        fullSensorData.append("Proximity Sensor Data:\n$proximityData\n")
        fullSensorData.append("Luminance Data:\n$luminanceData\n")
        fullSensorData.append("GPS Data:\n$locationData\n")

        sensorTextView.text = fullSensorData.toString()
    }

    private fun calculateAltitude(pressure: Float): String {
        // Calculate altitude using the simplified formula
        val altitude = (1013.25 - pressure) * 9
        return String.format(Locale.US, "Altitude: %.2f meters\n\n", altitude)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if necessary
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission has already been granted
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                startLocationUpdates()
            } else {
                // Permission denied
                locationData = "Location permission denied.\n"
                updateSensorTextView()
            }
        }
    }

    private fun updateSpeed(currentLocation: Location) {
        val speed: Float

        if (currentLocation.hasSpeed()) {
            // Use the speed provided by the GPS
            speed = currentLocation.speed // speed in m/s
        } else {
            val prevLocation = previousLocation
            if (prevLocation != null) {
                // Calculate speed manually
                val distance = currentLocation.distanceTo(prevLocation)
                val timeDelta = (currentLocation.time - prevLocation.time) / 1000.0 // time in seconds

                speed = if (timeDelta > 0) {
                    (distance / timeDelta).toFloat()
                } else {
                    0f
                }
            } else {
                // Unable to determine speed
                speed = 0f
            }
        }

        // Update previousLocation for the next calculation
        previousLocation = currentLocation

        // Update locationData with speed
        locationData = String.format(
            Locale.US,
            "Latitude: %.6f\nLongitude: %.6f\nSpeed: %.2f m/s\n\n",
            currentLocation.latitude,
            currentLocation.longitude,
            speed
        )
        // Update the TextView with new data
        updateSensorTextView()
    }
}
