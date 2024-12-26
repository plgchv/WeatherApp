package com.example.weatherapp

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileInputStream

object Database {
    private val logger: Logger = LoggerFactory.getLogger(Database::class.java)
    private val props = Properties()

    init {
        loadProperties()
    }

    private fun loadProperties() {
        try {
            FileInputStream("src/main/resources/com/example/weatherapp/application.properties").use { inputStream ->
                props.load(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to load properties file.")
        }
    }

    fun connect(): Connection? {
        val host: String = props.getProperty("database.host")
        val port: Int = props.getProperty("database.port").toInt()
        val schema: String = props.getProperty("database.schema")
        val username: String = props.getProperty("database.user")
        val password: String = props.getProperty("database.password")
        val jdbcUrl = "jdbc:oracle:thin:@//$host:$port/$schema?charSet=UTF-8"

        return try {
            val connection = DriverManager.getConnection(jdbcUrl, username, password)
            logger.info("Successfully connected!")
            connection
        } catch (e: Exception) {
            logger.error("$e")
            null
        }
    }

    fun executeQuery(connection: Connection?, query: String): ResultSet? {
        val statement: Statement? = connection?.createStatement()
        return try {
            statement?.executeQuery(query)
        } catch (e: Exception) {
            logger.error("$e")
            null
        }
    }

    fun getFirstValue(resultSet: ResultSet?, columnName: String): String? {
        return if (resultSet != null && resultSet.next())
            resultSet.getString(columnName)
        else
            null
    }
}
