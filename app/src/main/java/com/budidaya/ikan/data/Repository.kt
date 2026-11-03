package com.budidaya.ikan.data

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * Penyimpanan lokal: setiap username punya satu file JSON di memori internal aplikasi.
 * File yang sama persis inilah yang di-backup ke Google Drive.
 */
class Repository(context: Context) {

    private val folder = File(context.filesDir, "akun").apply { mkdirs() }
    private val prefs = context.getSharedPreferences("sesi", Context.MODE_PRIVATE)

    /** Username yang sedang login (null = belum login). */
    var sesiUsername: String?
        get() = prefs.getString("username", null)
        set(v) {
            prefs.edit().putString("username", v).apply()
        }

    private fun file(username: String) = File(folder, "${Akun.normalisasi(username)}.json")

    fun ada(username: String): Boolean = file(username).exists()

    fun muat(username: String): DataBudidaya? = runCatching {
        DataBudidaya.fromJson(JSONObject(file(username).readText()))
    }.getOrNull()

    fun simpan(data: DataBudidaya) {
        val target = file(data.profil.username)
        val tmp = File(target.path + ".tmp")
        tmp.writeText(data.toJson().toString())
        if (!tmp.renameTo(target)) {
            target.writeText(tmp.readText())
            tmp.delete()
        }
    }
}
