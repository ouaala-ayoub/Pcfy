package com.example.pc.data.models.local

enum class LoadPolicy(private val policy: Boolean) {
    Cache(false),
    Reload(true)
}

