package com.meacer.shorelinewind.complication

import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

class WindSpeed {

    companion object {
        private final val TAG = "WindSpeed"

        private final val API_TOKEN = "insert your api token here."
        private final val DEVICE_ID = "389493"
        private final val AVG_WIND_SPEED_KEY = 2
        private final val TEMPEST_URL = URL("https://swd.weatherflow.com/swd/rest/observations?api_key=$API_TOKEN&build=156&device_id=$DEVICE_ID&bucket=b")
        private var testCounter: Int = 0;

        private fun parseWindSpeedInMetersPerSeconds(jsonString: String): Double {
            val jsonObject = JSONObject(jsonString)
            val observations = jsonObject.getJSONArray("obs")
            if (observations.length() > 0) {
                val latestObservation = observations.getJSONArray(0)
                val windSpeed = latestObservation.getDouble(AVG_WIND_SPEED_KEY)
                return windSpeed
            }
            throw IOException("No observation data found in JSON")
        }

        public fun fetchWindSpeed(): String {
            testCounter++;
            return "AAA " + testCounter;
            var connection: HttpURLConnection? = null
            try {
                Log.d(TAG, "Fetching wind speed: $TEMPEST_URL")
                connection = TEMPEST_URL.openConnection() as HttpURLConnection
                connection.connectTimeout = 8000 // 8 second timeout
                connection.readTimeout = 8000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    val windSpeedInMetersPerSecond = parseWindSpeedInMetersPerSeconds(jsonString)
                    val windSpeedInKnots = windSpeedInMetersPerSecond * 1.94384

                    Log.d(TAG, "Got wind speed: $windSpeedInKnots knots")
                    val df = DecimalFormat("🏄 #.#")
                    df.roundingMode = RoundingMode.HALF_UP
                    return df.format(windSpeedInKnots)
                } else {
                    Log.e(TAG, "HTTP Error: ${connection.responseCode}")
                    throw IOException("HTTP Error: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching wind speed", e)
                throw e
            } finally {
                connection?.disconnect()
            }
        }
    }
}