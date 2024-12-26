package com.example.weatherapp

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.fxml.FXMLLoader

class WeatherApp : Application() {
    override fun start(primaryStage: Stage) {
        val fxmlLoader = FXMLLoader(WeatherApp::class.java.getResource("weather-app.fxml"))
        val scene = Scene(fxmlLoader.load())
        primaryStage.title = "Weather App"
        primaryStage.scene = scene
        primaryStage.isResizable = false
        primaryStage.show()
    }
}

fun main() {
    Application.launch(WeatherApp::class.java)
}