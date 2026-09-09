package com.example.slaveofmary.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivacyPolicyResponse(
    @SerialName("content") val content: String? = null
)
