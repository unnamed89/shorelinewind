package com.meacer.shorelinewind.complication

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
import java.util.Calendar

/**
 * Skeleton for complication data source that returns short text.
 */
class MainComplicationService : SuspendingComplicationDataSourceService() {

    // IMPORTANT: Replace these with your actual credentials from tempestwx.com
    private val API_TOKEN = "6bff2f89-84ab-463c-886e-fc0f443da4cf"
    private val DEVICE_ID = "389493"
    private val AVG_WIND_SPEED_KEY = 2
    // Sample url:
    // https://swd.weatherflow.com/swd/rest/observations?callback=jQuery224011556950892581941_1751337590228&api_key=6bff2f89-84ab-463c-886e-fc0f443da4cf&build=156&device_id=389493&bucket=b&time_start=1750905590&time_end=1751337590&_=1751337590229

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) {
            return null
        }
        return createComplicationData("Mon", "Monday")
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val temp = fetchTemperature()
        if (request.complicationType == ComplicationType.SHORT_TEXT) {
            return createComplicationData(temp.toString(), "Temperature")
        }
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> createComplicationData("Sun", "Sunday")
            Calendar.MONDAY -> createComplicationData("aaa", "Monday")
            Calendar.TUESDAY -> createComplicationData("Tue", "Tuesday")
            Calendar.WEDNESDAY -> createComplicationData("Wed", "Wednesday")
            Calendar.THURSDAY -> createComplicationData("Thu", "Thursday")
            Calendar.FRIDAY -> createComplicationData("Fri!", "Friday!")
            Calendar.SATURDAY -> createComplicationData("Sat", "Saturday")
            else -> throw IllegalArgumentException("too many days")
        }
    }

    private fun createComplicationData(text: String, contentDescription: String) =
        ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build()
        ).build()

    /**
     * Parses the JSON string to extract and format the air temperature.
     */
    private fun parseTemperature(jsonString: String): Double {
        val jsonObject = JSONObject(jsonString)
        val observations = jsonObject.getJSONArray("obs")
        if (observations.length() > 0) {
            val latestObservation = observations.getJSONArray(0)

//            val latestObservation = observations.getJSONObject(0)
////            val tempCelsius = latestObservation.getDouble("air_temperature")
//            val tempFahrenheit = tempCelsius * 9 / 5 + 32
//            val tempCelsius = latestObservation.
//            val windSpeed = latestObservation[2]
            val windSpeed = latestObservation.getDouble(AVG_WIND_SPEED_KEY)

            // Format to a whole number and add the degree symbol
//            val df = DecimalFormat("#.#")
//            df.roundingMode = RoundingMode.HALF_UP
//            return "${df.format(windSpeed)}"
            return  windSpeed
        }

        throw IOException("No observation data found in JSON")
    }


    /**
     * Fetches and parses the temperature from the Tempest API.
     */
    private suspend fun fetchTemperature() : String {
        // Use withContext to switch to a background thread for networking
        return withContext(Dispatchers.IO) {
    //            val url = URL("https://swd.weatherflow.com/swd/rest/observations/station/$STATION_ID?token=$API_TOKEN")
//            val url = URL("https://swd.weatherflow.com/swd/rest/observations?api_key=$API_TOKEN&build=156&device_id=$DEVICE_ID&bucket=b&time_start=1750905590&time_end=1751337590&_=1751337590229")
            val url = URL("https://swd.weatherflow.com/swd/rest/observations?api_key=$API_TOKEN&build=156&device_id=$DEVICE_ID&bucket=b")
            println(">> URL: $url")

            var connection: HttpURLConnection? = null
            try {
                connection = url.openConnection() as HttpURLConnection
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
    //                    parseTemperature(jsonString)

                    val windSpeedInMetersPerSecond = parseTemperature(jsonString)
                    val windSpeedInKnots = windSpeedInMetersPerSecond * 1.94384
                    println(">>>>>>> GOT VALUE windspeed knots: $windSpeedInKnots")

                    val df = DecimalFormat("#.#")
                    df.roundingMode = RoundingMode.HALF_UP
                    return@withContext df.format(windSpeedInKnots)
                } else {
    //                    throw IOException("HTTP Error: ${connection.responseCode}")
                    println("Error: ${connection.responseCode}")
                }
            } finally {
                connection?.disconnect()
            }
        }.toString()
    }
}