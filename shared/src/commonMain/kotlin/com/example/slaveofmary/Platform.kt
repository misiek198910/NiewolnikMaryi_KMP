package com.example.slaveofmary

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform