package com.budidaya.ikan.data

import java.security.MessageDigest
import java.security.SecureRandom

/** Aturan akun: username, hash password, validasi pendaftaran. */
object Akun {
    private val POLA_USERNAME = Regex("^[a-z0-9_.]{4,20}$")

    fun normalisasi(username: String): String = username.trim().lowercase()

    fun cekUsername(username: String): String? {
        val u = normalisasi(username)
        return when {
            u.isEmpty() -> "Username wajib diisi"
            !POLA_USERNAME.matches(u) -> "Username 4–20 karakter: huruf kecil, angka, titik atau garis bawah"
            else -> null
        }
    }

    fun cekPassword(password: String, ulang: String): String? = when {
        password.length < 6 -> "Password minimal 6 karakter"
        password != ulang -> "Konfirmasi password tidak sama"
        else -> null
    }

    fun buatSalt(): String {
        val b = ByteArray(16)
        SecureRandom().nextBytes(b)
        return b.hex()
    }

    fun hash(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256").digest((salt + password).toByteArray(Charsets.UTF_8)).hex()

    fun cocok(profil: Profil, password: String): Boolean = hash(password, profil.salt) == profil.passwordHash

    private fun ByteArray.hex(): String = joinToString("") { "%02x".format(it) }
}
