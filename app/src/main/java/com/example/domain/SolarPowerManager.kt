package com.example.domain

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PowerSourceMode {
    SOLAR_ONLY,     // High light: 100% solar powered
    DUAL_POWER,     // Moderate indoor light: Solar assisted + Battery backup
    BATTERY_BACKUP  // Dark / covered: Pure battery backup
}

data class SolarState(
    val currentLux: Float = 250f,
    val solarEfficiencyPercent: Int = 85,
    val powerSource: PowerSourceMode = PowerSourceMode.SOLAR_ONLY,
    val isLightSensorAvailable: Boolean = true,
    val isSimulatedTorchActive: Boolean = false,
    val simulatedLightLevel: Float = 300f
)

class SolarPowerManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _solarState = MutableStateFlow(
        SolarState(
            isLightSensorAvailable = lightSensor != null,
            currentLux = if (lightSensor != null) 300f else 400f
        )
    )
    val solarState: StateFlow<SolarState> = _solarState.asStateFlow()

    private var isRegistered = false

    fun startListening() {
        if (!isRegistered && lightSensor != null) {
            sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
            isRegistered = true
        }
    }

    fun stopListening() {
        if (isRegistered) {
            sensorManager?.unregisterListener(this)
            isRegistered = false
        }
    }

    fun toggleSimulatedTorch() {
        val current = _solarState.value
        val newTorch = !current.isSimulatedTorchActive
        val effectiveLux = if (newTorch) 850f else (if (lightSensor != null) current.currentLux else current.simulatedLightLevel)
        updateLux(effectiveLux, newTorch)
    }

    fun setSimulatedLight(lux: Float) {
        updateLux(lux, _solarState.value.isSimulatedTorchActive)
    }

    private fun updateLux(lux: Float, torchActive: Boolean) {
        val effectiveLux = if (torchActive) maxOf(lux, 800f) else lux
        // Solar threshold:
        // >= 120 lx: Solar only (Casio photovoltaic cells generate sufficient ~1.5V)
        // 30..120 lx: Dual power
        // < 30 lx: Battery backup
        val mode = when {
            effectiveLux >= 120f -> PowerSourceMode.SOLAR_ONLY
            effectiveLux >= 30f -> PowerSourceMode.DUAL_POWER
            else -> PowerSourceMode.BATTERY_BACKUP
        }

        val efficiency = when {
            effectiveLux >= 500f -> 100
            effectiveLux <= 0f -> 0
            else -> ((effectiveLux / 500f) * 100).toInt().coerceIn(0, 100)
        }

        _solarState.value = _solarState.value.copy(
            currentLux = effectiveLux,
            solarEfficiencyPercent = efficiency,
            powerSource = mode,
            isSimulatedTorchActive = torchActive,
            simulatedLightLevel = lux
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            if (!_solarState.value.isSimulatedTorchActive) {
                updateLux(lux, false)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
