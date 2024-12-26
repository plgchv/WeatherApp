package com.example.weatherapp

import com.google.gson.annotations.SerializedName
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.text.Text
import javafx.scene.control.TextField
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class WeatherResponse (val list: List<WeatherData>)

data class WeatherData(
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    @SerializedName("dt_txt") val dtTxt: String
)

data class Main (
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
)

data class Wind (
    val speed: Double,
    val deg: Int
)

data class Weather (
    val description: String,
    val icon: String
)

data class Clouds (val all: Int)

class WeatherAppController {
    private val logger: Logger = LoggerFactory.getLogger(WeatherApp::class.java)

    lateinit var searchField: TextField
    lateinit var searchButton: Button
    lateinit var suggestions: ListView<String>

    lateinit var temp: Text
    lateinit var maxTemp: Text
    lateinit var minTemp: Text
    lateinit var pressure: Text
    lateinit var humidity: Text
    lateinit var cloudiness: Text
    lateinit var speed: Text
    lateinit var direction: Text
    lateinit var weatherIcon: ImageView

    lateinit var weatherContainer: HBox

    private val connection = Database.connect()

    fun initialize() {
        logger.info("App started")

        val result = Database.executeQuery(connection, "SELECT * FROM CITY") ?: return
        val lastCity = Database.getFirstValue(result, "CITYNAME") ?: return
        logger.info("Last city: $lastCity")
        searchField.text = lastCity
        displayWeather()
    }

    private fun showAlert(message: String) {
        logger.error("City not found")
        Alert(Alert.AlertType.ERROR, message).showAndWait()
    }

    fun showSuggestions() {
        logger.debug("Showing suggestions...")
        val query = searchField.text
        val cityNames = WeatherAPI.getCities(query)
        if (cityNames.isNotEmpty()) {
            logger.info("Successfully get suggestions: $cityNames")
            suggestions.isVisible = true
            suggestions.items.clear()
            suggestions.items.addAll(cityNames)
        } else
            logger.error("City not found")
    }

    fun chooseSuggestion() {
        val suggestion = suggestions.selectionModel.selectedItem
        if (suggestion != null) {
            searchField.text = suggestion
            logger.info("Suggestion: '$suggestion' choosed")
            suggestions.items.clear()
            suggestions.isVisible = false
        }
    }

    fun displayWeather() {
        val query = searchField.text.replace(", ", ",")
        val weather = WeatherAPI.getWeather(query)
        if (weather != null) {
            val main = weather.list[0].main
            temp.text = "${main.temp.toInt()} °C"
            maxTemp.text = "${main.tempMax.toInt()}°"
            minTemp.text = "${main.tempMin.toInt()}°"
            humidity.text = "Humidity: ${main.humidity}%"
            pressure.text = "Pressure: ${main.pressure} hPa"
            speed.text = "Speed: ${weather.list[0].wind.speed} m/s"
            cloudiness.text = "Cloudiness: ${weather.list[0].clouds.all}%"

            val wind = weather.list[0].wind
            direction.text = when (wind.deg) {
                in 0..22, in 338..360 -> "Direction: North"
                in 23..68 -> "Direction: Northeast"
                in 69..113 -> "Direction: East"
                in 114..158 -> "Direction: Southeast"
                in 159..203 -> "Direction: South"
                in 204..248 -> "Direction: Southwest"
                in 249..293 -> "Direction: West"
                in 294..338 -> "Direction: Northwest"
                else -> "Unknown"
            }

            val iconCode = weather.list[0].weather[0].icon
            val iconUrl = "http://openweathermap.org/img/w/$iconCode.png"
            val image = Image(iconUrl)
            weatherIcon.image = image
        } else {
            showAlert("City not found")
            return
        }
        handleDisplayWeather(weather)
        Database.executeQuery(connection, "DROP TABLE CITY")
        Database.executeQuery(connection, "CREATE TABLE CITY(CITYNAME VARCHAR(255))")
        Database.executeQuery(connection, "INSERT INTO CITY VALUES('$query')")
    }

    private fun handleDisplayWeather(weather: WeatherResponse?) {
        weatherContainer.children.clear()

        if (weather != null && weather.list.isNotEmpty()) {
            weather.list.drop(1).forEach { dailyWeather ->
                val main = dailyWeather.main
                val weatherDescription = dailyWeather.weather[0]

                val vBox = VBox()
                vBox.spacing = 5.0
                vBox.alignment = Pos.CENTER

                vBox.children.add(Text(dailyWeather.dtTxt))
                vBox.children.add(Text(weatherDescription.description))
                val imageView = ImageView(Image("http://openweathermap.org/img/w/${weatherDescription.icon}.png"))
                imageView.fitHeight = 77.0
                imageView.fitWidth = 79.0
                vBox.children.add(imageView)

                val tempHBox = HBox()
                tempHBox.alignment = Pos.CENTER
                tempHBox.spacing = 15.0
                tempHBox.children.add(Text("${main.tempMin.toInt()}°"))
                tempHBox.children.add(Text("${main.temp.toInt()}°"))
                tempHBox.children.add(Text("${main.tempMax.toInt()}°"))

                vBox.children.add(tempHBox)

                weatherContainer.children.add(vBox)
            }
        } else
            logger.error("City not found or no weather data available")
    }
    fun hideSuggestions() {
        suggestions.isVisible = false
    }
}