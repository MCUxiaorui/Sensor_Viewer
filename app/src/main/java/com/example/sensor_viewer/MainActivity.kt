//Created by: Xiaorui Liu

package com.example.sensor_viewer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import kotlin.math.pow

//import com.example.sensor_viewer.R

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorTextView: TextView
//    private late init var sensors: List<Sensor>
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorTextView = findViewById(R.id.sensorTextView)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

    }


    override fun onResume() {
        super.onResume()
//        // Register all sensors for real-time data updates
//        for (sensor in sensors) {
//            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
//        }
        registerSensor(magnetometer)
        registerSensor(gyroscope)
        registerSensor(accelerometer)
        registerSensor(pressure)
        registerSensor(proximity)
        registerSensor(luminance)
    }

    private fun registerSensor(sensorType: Int) {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listener to save battery when the activity is not in focus
        sensorManager.unregisterListener(this)
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
        sensorData.append("type: ${sensor.type}\n")
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
        fullSensorData.append("Accelerometer Data:\n${accelerometerData}\n")
        fullSensorData.append("Pressure Data:\n$pressureData")
        fullSensorData.append(altitudeData)
        fullSensorData.append("Proximity Sensor Data:\n$proximityData\n")
        fullSensorData.append("Luminance Data:\n$luminanceData\n")

        sensorTextView.text = fullSensorData.toString()
    }

    private fun calculateAltitude(pressure: Float): String {
        // Constants
        val seaLevelPressure = 101325.0 // Standard atmospheric pressure at sea level (Pa)
        val standardTemperature = 288.15 // Standard temperature (K)
        val temperatureLapseRate = 0.0065 // Standard temperature lapse rate (K/m)
        val gasConstant = 8.31447 // Universal gas constant (J/(mol·K))
        val gravity = 9.80665 // Standard gravity (m/s²)

        // Convert pressure to Pa if necessary
        val pressurePa = pressure * 100.0

        // Calculate altitude
        val altitude = (standardTemperature / temperatureLapseRate) *
                ((seaLevelPressure / pressurePa).pow(gasConstant * temperatureLapseRate / gravity) - 1)

        return String.format(Locale.US, "Altitude: %.2f meters\n\n", altitude)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if necessary
    }
}


