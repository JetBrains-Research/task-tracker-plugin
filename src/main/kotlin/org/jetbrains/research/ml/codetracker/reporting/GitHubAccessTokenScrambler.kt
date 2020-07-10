package org.jetbrains.research.ml.codetracker.reporting

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
internal object GitHubAccessTokenScrambler {
    private const val initVector = "RandomInitVector"
    private const val key = "GitHubErrorToken"

    @Throws(Exception::class)
    fun decrypt(inputStream: InputStream?): String {
        val input: String
        val o = ObjectInputStream(inputStream)
        input = o.readObject() as String
        val iv = IvParameterSpec(initVector.toByteArray(StandardCharsets.UTF_8))
        val keySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val original = cipher.doFinal(Base64.decodeBase64(input))
        return String(original)
    }
}