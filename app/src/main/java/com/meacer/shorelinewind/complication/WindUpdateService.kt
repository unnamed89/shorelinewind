package com.meacer.shorelinewind.complication

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import android.content.ComponentName
import java.util.Timer
import java.util.TimerTask

class WindUpdateService : Service() {
    private val TAG = "WindUpdateService"
    private var timer: Timer? = null

    private val UPDATE_INTERVAL_MILLIS: Long = 60000

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WindUpdateService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting wind update service")
        startPeriodicUpdates()
        return START_STICKY
    }

    private fun startPeriodicUpdates() {
        timer?.cancel()
        timer = Timer()

        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    Log.d(TAG, "Requesting complication update")

                    val updateRequester = ComplicationDataSourceUpdateRequester.create(
                        context = this@WindUpdateService,
                        complicationDataSourceComponent = ComponentName(
                            this@WindUpdateService,
                            MainComplicationService::class.java
                        )
                    )

                    updateRequester.requestUpdateAll()

                } catch (e: Exception) {
                    Log.e(TAG, "Error updating complication", e)
                }
            }
        }, 0, UPDATE_INTERVAL_MILLIS) // Update immediately, then every 60 seconds
    }

    override fun onDestroy() {
        Log.d(TAG, "Stopping wind update service")
        timer?.cancel()
        timer = null
        super.onDestroy()
    }
}