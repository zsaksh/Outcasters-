package com.example.backend.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BatteryStateReceiver(private val context: Context) : BroadcastReceiver() {
    
    private val _isLowPowerModeRecommended = MutableStateFlow(false)
    val isLowPowerModeRecommended: StateFlow<Boolean> = _isLowPowerModeRecommended.asStateFlow()
    
    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    fun register() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(this, filter)
    }

    fun unregister() {
        context.unregisterReceiver(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            
            if (level != -1 && scale != -1) {
                val batteryPct = (level * 100) / scale
                _batteryLevel.value = batteryPct
                
                // Recommend low power mode if battery is below 20%
                _isLowPowerModeRecommended.value = batteryPct <= 20
            }
        }
    }
}
