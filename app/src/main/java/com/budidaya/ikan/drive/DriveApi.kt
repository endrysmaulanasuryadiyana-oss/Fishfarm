package com.budidaya.ikan.drive

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Akses Google Drive lewat REST API (v3).
 *
 * Data setiap akun disimpan di Google Drive pengguna:
 *   Folder : "Budidaya Ikan (Aplikasi)"
 *   File   : "budidaya_<username>.json"
 *
 * Jadi satu akun Google bisa menyimpan beberapa username, dan saat ganti HP
 * pengguna cukup login Google + isi username yang sama untuk memulihkan data.
 * Fungsi-fungsi di sini bersifat blocking — panggil dari Dispatchers.IO.
 */
object DriveApi {
    /** Izin hanya untuk file yang dibuat aplikasi ini (paling aman). */
    const val SCOPE = "https://www.googleapis.com/auth/drive.file"

    const val NAMA_FOLDER = "Budidaya Ikan (Aplikasi)"
    private const val MIME_FOLDER = "application/vnd.google-apps.folder"
    private const val API = "https://www.googleapis.com/drive/v3"
    private const val UPLOAD = "https://www.googleapis.com/upload/drive/v3"

    class DriveException(pesan: String, val kode: Int) : Exception(pesan)

    fun namaFile(username: String) = "budidaya_$username.json"

    /** Email akun Google yang sedang dipakai. */
    fun emailAkun(token: String): String {
        val r = request("GET", "$API/about?fields=user(emailAddress,displayName)", token)
        return JSONObject(r).getJSONObject("user").optString("emailAddress", "")
    }

    /** Upload / timpa backup untuk username tertentu. */
    fun unggah(token: String, username: String, json: String) {
        val folder = folderId(token, buatJikaTidakAda = true)!!
        val nama = namaFile(username)
        val lama = cari(token, "name = '${esc(nama)}' and '$folder' in parents and trashed = false")

        // Buat file baru dulu, baru hapus versi lama → backup tidak pernah hilang di tengah jalan.
        val batas = "budidaya_ikan_${System.currentTimeMillis()}"
        val meta = JSONObject()
            .put("name", nama)
            .put("mimeType", "application/json")
            .put("parents", JSONArray().put(folder))
        val body = buildString {
            append("--$batas\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(meta.toString()).append("\r\n")
            append("--$batas\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(json).append("\r\n")
            append("--$batas--\r\n")
        }
        request(
            "POST", "$UPLOAD/files?uploadType=multipart&fields=id", token,
            body.toByteArray(Charsets.UTF_8), "multipart/related; boundary=$batas"
        )
        lama.forEach { id ->
            runCatching { request("DELETE", "$API/files/$id", token) }
        }
    }

    /** Ambil isi backup untuk username; null jika belum pernah di-backup. */
    fun unduh(token: String, username: String): String? {
        val folder = folderId(token, buatJikaTidakAda = false) ?: return null
        val id = cari(
            token,
            "name = '${esc(namaFile(username))}' and '$folder' in parents and trashed = false"
        ).firstOrNull() ?: return null
        return request("GET", "$API/files/$id?alt=media", token)
    }

    // ---------------- internal ----------------

    private fun folderId(token: String, buatJikaTidakAda: Boolean): String? {
        cari(token, "name = '${esc(NAMA_FOLDER)}' and mimeType = '$MIME_FOLDER' and trashed = false")
            .firstOrNull()?.let { return it }
        if (!buatJikaTidakAda) return null
        val meta = JSONObject().put("name", NAMA_FOLDER).put("mimeType", MIME_FOLDER)
        val r = request(
            "POST", "$API/files?fields=id", token,
            meta.toString().toByteArray(Charsets.UTF_8), "application/json; charset=UTF-8"
        )
        return JSONObject(r).getString("id")
    }

    /** Daftar id file yang cocok, terbaru lebih dulu. */
    private fun cari(token: String, query: String): List<String> {
        val url = "$API/files?q=${enc(query)}&spaces=drive&orderBy=modifiedTime%20desc&fields=files(id,name)"
        val arr = JSONObject(request("GET", url, token)).optJSONArray("files") ?: JSONArray()
        return (0 until arr.length()).map { arr.getJSONObject(it).getString("id") }
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8").replace("+", "%20")
    private fun esc(s: String) = s.replace("\\", "\\\\").replace("'", "\\'")

    private fun request(
        method: String,
        url: String,
        token: String,
        body: ByteArray? = null,
        contentType: String? = null
    ): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = method
            c.connectTimeout = 20_000
            c.readTimeout = 40_000
            c.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                c.doOutput = true
                c.setRequestProperty("Content-Type", contentType ?: "application/octet-stream")
                c.setFixedLengthStreamingMode(body.size)
                c.outputStream.use { it.write(body) }
            }
            val kode = c.responseCode
            val stream = if (kode in 200..299) c.inputStream else c.errorStream
            val teks = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            if (kode !in 200..299) {
                val pesan = when (kode) {
                    401 -> "Sesi Google kedaluwarsa, silakan hubungkan ulang"
                    403 -> "Akses Google Drive ditolak (cek Drive API & izin)"
                    else -> "Google Drive error $kode"
                }
                throw DriveException(pesan, kode)
            }
            return teks
        } finally {
            c.disconnect()
        }
    }
}
