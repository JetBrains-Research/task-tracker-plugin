package org.jetbrains.research.ml.tasktracker.reporting

import org.apache.commons.codec.binary.Base64
import java.io.InputStream
import java.io.ObjectInputStream
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Provides functionality to decode secret token.
 */
internal object GitHubAccessTokenDecoder {
    private const val initVector = "RandomInitVector"
    private const val key = "GitHubErrorToken"

    fun decrypt(inputStream: InputStream?): String {
        val iv = IvParameterSpec(initVector.toByteArray(StandardCharsets.UTF_8))
        val keySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        return String(cipher.doFinal(Base64.decodeBase64(ObjectInputStream(inputStream).readObject() as String)))
    }
}