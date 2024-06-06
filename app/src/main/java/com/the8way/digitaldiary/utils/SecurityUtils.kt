package com.the8way.digitaldiary.utils

import java.security.MessageDigest

object SecurityUtils {

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(inputPin: String, storedHash: String): Boolean {
        return hashPin(inputPin) == storedHash
    }
}
