package com.example.weatherapp

import com.google.gson.Gson
import com.google.gson.JsonArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object WeatherAPI {
    private val logger: Logger = LoggerFactory.getLogger(WeatherApp::class.java)

    private const val API_KEY = "10dd414f72209ffbae60d9aa21936976"
    private const val FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast"
    private const val GEOCODING_URL = "http://api.openweathermap.org/geo/1.0/direct"

    private fun <T> getResponseAsJson(url: URL, className: Class<T>): T? {
        val response: String
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        try {
            logger.debug("Getting response from URL: {}", url)
            response = connection.inputStream.bufferedReader().use { it.readText() }
            logger.info("Response: $response")
        } catch (e: Exception) {
            logger.error("$e")
            return null
        }
        connection.disconnect()
        return Gson().fromJson(response, className)
    }

    fun getWeather(query: String?): WeatherResponse? {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = URL("$FORECAST_URL?q=$encodedQuery&cnt=5&appid=$API_KEY&units=metric")
        return getResponseAsJson(url, WeatherResponse::class.java)
    }

    fun getCities(query: String): List<String> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = URL("${GEOCODING_URL}?q=$encodedQuery&limit=5&appid=${API_KEY}")
        val cities = mutableSetOf<String>()

        val jsonResponse = getResponseAsJson(url, JsonArray::class.java)
        jsonResponse?.forEach {
            val cityObject = it.asJsonObject
            val name = cityObject.get("name").asString
            val country = cityObject.get("country").asString
            val stateCode = cityObject.get("state")?.asString
            val cityDisplay = if (stateCode != null)
                "$name, $stateCode, $country"
            else
                "$name, $country"
            cities.add(cityDisplay)
        }
        return cities.take(5).toList()
    }
}