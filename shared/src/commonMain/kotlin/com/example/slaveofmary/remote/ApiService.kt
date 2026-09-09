package com.example.slaveofmary.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ApiService(private val client: HttpClient) {
    private val parishBaseUrl = "https://api-parafia.mivs.dev"
    private val privacyBaseUrl = "https://privacypolicy.mivs.dev"

    suspend fun getNewsFeed(): List<NewsResponse>? {
        return try {
            client.get("$parishBaseUrl/news").body<List<NewsResponse>>()
        } catch (e: Exception) {
            println("[CNC_LOG] Błąd pobierania newsów: ${e.message}")
            null
        }
    }

    suspend fun getPrivacyPolicy(serviceName: String): String? {
        return try {
            val response: PrivacyPolicyResponse =
                client.get("$privacyBaseUrl/api/privacy/$serviceName").body()
            response.content
        } catch (e: Exception) {
            println("[CNC_LOG] Błąd pobierania polityki prywatności: ${e.message}")
            null
        }
    }
}