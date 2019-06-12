package io.webcandy.client

import kotlin.collections.List

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

/**
 * Data that the Webcandy proxy server needs to register a client.
 */
data class ClientData(val token: String, val client_id: String, val patterns: List<String>)

/**
 * Client to receive lighting configuration change requests from the Webcandy proxy server.
 */
class WebcandyClient(private val token: String, private val clientId: String,
                     private val host: String, private val port: Int) {

    /**
     * Start the client.
     */
    fun start() {
        val socket = Socket(this.host, this.port)

        val output: OutputStream = socket.getOutputStream()
        val input: InputStream = socket.getInputStream()
        val reader = BufferedReader(InputStreamReader(input))

        // TODO: Get actual patterns
        val patterns: List<String> = listOf("pattern1", "pattern2")
        val data = ClientData(this.token, this.clientId, patterns)
        val serializedData = Gson().toJson(data)

        output.write(serializedData.toByteArray())
        println("Sent token, client_id: ${this.clientId}, and patterns: $patterns")

        var line: String
        while (true) {
            line = reader.readLine()
            println("Received: $line")
        }
    }
}

fun main() {
    // TODO: Make HTTP request to get token
    /*
    val host = "127.0.0.1"
    val appPort = 5000

    val url = URL("http://$host:$appPort/api/token")
    with (url.openConnection() as HttpURLConnection) {
        requestMethod = "POST"
    }
    */

    val token = "fake token"
    val client = WebcandyClient(token, "testclient1", "localhost", 6543)
    client.start()
}