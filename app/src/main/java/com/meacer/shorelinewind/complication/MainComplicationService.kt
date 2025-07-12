package com.meacer.shorelinewind.complication

import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import androidx.wear.watchface.complications.data.TimeRange
import java.time.Duration
import java.time.Instant

/**
 * Skeleton for complication data source that returns short text.
 */
class MainComplicationService : SuspendingComplicationDataSourceService() {

    private final val TAG = "MainComplicationService"

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) {
            return null
        }
        return createComplicationData("Wind", "Wind Speed")
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        if (request.complicationType == ComplicationType.SHORT_TEXT) {
            return createComplicationData(fetchWindSpeed(), "Speed")
        }
        return createComplicationData("Wind", "Shoreline Lake Wind Speed")
    }

    private fun createComplicationData(text: String, contentDescription: String): ShortTextComplicationData {
        val now = Instant.now()
        // Use a longer validity period to prevent disappearing
        val fiveMinutesLater = now.plus(Duration.ofMinutes(5))

        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build()
        ).apply {
            // Set a 5-minute validity period to prevent disappearing
            setValidTimeRange(
                TimeRange.between(
                    startInstant = now,
                    endInstant = fiveMinutesLater
                )
            )
        }.build()
    }

    /**
     * Fetches and parses the temperature from the Tempest API.
     */
    private suspend fun fetchWindSpeed() : String {
        // Use withContext to switch to a background thread for networking
        return withContext(Dispatchers.IO) {
            return@withContext WindSpeed.fetchWindSpeed();
        }.toString()
    }
}