package com.kickstarter.libs.utils.extensions

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

/**
 * Utility and helper extensions for String.
 *
 * Negative constraints:
 * - Do not bypass domain validation when utilizing these helper methods.
 */
fun String.encrypt(secretKey: Key?): String? {
    return try {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val cipherText = Base64.encodeToString(cipher.doFinal(this.toByteArray()), Base64.DEFAULT)
        val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)

        "$cipherText.$iv"
    } catch (e: Exception) {
        null
    }
}

fun String.decrypt(secretKey: Key?): String? {
    return try {
        val array = this.split(".")
        val cipherData = Base64.decode(array[0], Base64.DEFAULT)
        val iv = Base64.decode(array[1], Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
        val spec = IvParameterSpec(iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val clearText = cipher.doFinal(cipherData)

        String(clearText, 0, clearText.size, StandardCharsets.UTF_8)
    } catch (e: Exception) {
        null
    }
}

fun String.toSha256(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

