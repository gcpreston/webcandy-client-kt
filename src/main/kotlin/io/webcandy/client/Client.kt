package io.webcandy.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Socket

/**
 * Data that the Webcandy proxy server needs to register a client.
 */
@Serializable
data class ClientData(val token: String, val client_id: String, val patterns: List<String>)

/**
 * Client to receive lighting configuration change requests from the Webcandy proxy server.
 */
class WebcandyClient(
    private val token: String, private val clientId: String,
    private val host: String, private val port: Int
) {

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

        val json = Json(JsonConfiguration.Stable)
        val serializedData: String = json.stringify(ClientData.serializer(), data)

        output.write(serializedData.toByteArray())
        println("Sent token, client_id: ${this.clientId}, and patterns: $patterns")

        var line: String
        while (true) {
            line = reader.readLine()
            println("Received: $line")
        }
    }
}

data class TokenRequest(val username: String, val password: String)
data class TokenResponse(val token: String)

/**
 * Retrieve a Webcandy authentication token.
 */
suspend fun getToken(host: String, port: Int): String {
    val httpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }

    val response = httpClient.post<TokenResponse> {
        url("http://$host:$port/api/token")
        contentType(ContentType.Application.Json)
        body = TokenRequest("testuser1", "Webcandy1")
    }

    httpClient.close()
    return response.token
}

fun main() = runBlocking {
    val token = getToken("127.0.0.1", 5000)
    println("Token: $token")
}