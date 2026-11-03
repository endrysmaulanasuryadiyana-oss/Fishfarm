package com.budidaya.ikan.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/** Profil pemilik / usaha budidaya (dibuat saat pendaftaran). */
data class Profil(
    val username: String,
    val passwordHash: String,
    val salt: String,
    val namaPemilik: String,
    val namaUsaha: String,
    val alamat: String,
    val noHp: String = "",
    val logoBase64: String? = null,
    val googleEmail: String? = null,
    val terakhirSync: Long = 0L,
    val dibuat: Long = System.currentTimeMillis()
)

/** Satu kolam beserta ikan dan stok pakannya. */
data class Kolam(
    val id: String = UUID.randomUUID().toString(),
    val nomor: String,
    val nama: String,
    val jenisIkan: String,
    val jumlahEkor: Int,
    val beratTotalKg: Double,
    val jenisPakan: String,
    val stokPakanKarung: Double,
    val beratPerKarungKg: Double,
    val tanggalPembenihan: LocalDate,
    val catatan: String = "",
    val diperbarui: Long = System.currentTimeMillis()
) {
    /** Berat rata-rata per ekor dalam gram. */
    val beratRataGram: Double
        get() = if (jumlahEkor > 0) beratTotalKg * 1000.0 / jumlahEkor else 0.0

    /** Ukuran ikan: berapa ekor per kilogram. */
    val ekorPerKg: Double
        get() = if (beratTotalKg > 0) jumlahEkor / beratTotalKg else 0.0

    /** Total stok pakan dalam kilogram. */
    val totalPakanKg: Double
        get() = stokPakanKarung * beratPerKarungKg

    /** Umur pemeliharaan sejak tanggal pembenihan / tebar. */
    fun umurHari(hariIni: LocalDate = LocalDate.now()): Long =
        ChronoUnit.DAYS.between(tanggalPembenihan, hariIni)

    /** Judul tampilan, mis. "Kolam 1 – Blok A". */
    val judul: String
        get() = if (nama.isBlank()) "Kolam $nomor" else "Kolam $nomor – $nama"
}

/** Seluruh data satu akun (inilah yang disimpan & di-backup ke Google Drive). */
data class DataBudidaya(
    val profil: Profil,
    val kolam: List<Kolam>
) {
    val totalEkor: Long get() = kolam.sumOf { it.jumlahEkor.toLong() }
    val totalKgIkan: Double get() = kolam.sumOf { it.beratTotalKg }
    val totalKarung: Double get() = kolam.sumOf { it.stokPakanKarung }
    val totalPakanKg: Double get() = kolam.sumOf { it.totalPakanKg }

    /** Ringkasan per jenis ikan: jenis -> (jumlah kolam, total ekor, total kg). */
    fun ringkasanJenis(): List<RingkasanJenis> =
        kolam.groupBy { it.jenisIkan.trim().ifBlank { "Lainnya" } }
            .map { (jenis, list) ->
                RingkasanJenis(
                    jenis = jenis,
                    jumlahKolam = list.size,
                    totalEkor = list.sumOf { it.jumlahEkor.toLong() },
                    totalKg = list.sumOf { it.beratTotalKg }
                )
            }
            .sortedByDescending { it.totalKg }

    fun kolamUrut(): List<Kolam> =
        kolam.sortedWith(compareBy<Kolam>({ it.nomor.toIntOrNull() ?: Int.MAX_VALUE }, { it.nomor.lowercase() }))

    fun toJson(): JSONObject = JSONObject().apply {
        put("aplikasi", "BudidayaIkan")
        put("versi", 1)
        put("profil", profil.toJson())
        put("kolam", JSONArray().apply { kolam.forEach { put(it.toJson()) } })
    }

    companion object {
        fun fromJson(o: JSONObject): DataBudidaya {
            val arr = o.optJSONArray("kolam") ?: JSONArray()
            val list = (0 until arr.length()).map { kolamFromJson(arr.getJSONObject(it)) }
            return DataBudidaya(profilFromJson(o.getJSONObject("profil")), list)
        }
    }
}

data class RingkasanJenis(
    val jenis: String,
    val jumlahKolam: Int,
    val totalEkor: Long,
    val totalKg: Double
)

// ---------------- JSON ----------------

private fun JSONObject.teks(k: String): String? = if (has(k) && !isNull(k)) getString(k) else null

fun Profil.toJson(): JSONObject = JSONObject().apply {
    put("username", username)
    put("passwordHash", passwordHash)
    put("salt", salt)
    put("namaPemilik", namaPemilik)
    put("namaUsaha", namaUsaha)
    put("alamat", alamat)
    put("noHp", noHp)
    put("logo", logoBase64 ?: JSONObject.NULL)
    put("googleEmail", googleEmail ?: JSONObject.NULL)
    put("terakhirSync", terakhirSync)
    put("dibuat", dibuat)
}

fun profilFromJson(o: JSONObject) = Profil(
    username = o.getString("username"),
    passwordHash = o.getString("passwordHash"),
    salt = o.getString("salt"),
    namaPemilik = o.teks("namaPemilik") ?: "",
    namaUsaha = o.teks("namaUsaha") ?: "",
    alamat = o.teks("alamat") ?: "",
    noHp = o.teks("noHp") ?: "",
    logoBase64 = o.teks("logo"),
    googleEmail = o.teks("googleEmail"),
    terakhirSync = o.optLong("terakhirSync", 0L),
    dibuat = o.optLong("dibuat", System.currentTimeMillis())
)

fun Kolam.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("nomor", nomor)
    put("nama", nama)
    put("jenisIkan", jenisIkan)
    put("jumlahEkor", jumlahEkor)
    put("beratTotalKg", beratTotalKg)
    put("jenisPakan", jenisPakan)
    put("stokPakanKarung", stokPakanKarung)
    put("beratPerKarungKg", beratPerKarungKg)
    put("tanggalPembenihan", tanggalPembenihan.toString())
    put("catatan", catatan)
    put("diperbarui", diperbarui)
}

fun kolamFromJson(o: JSONObject) = Kolam(
    id = o.teks("id") ?: UUID.randomUUID().toString(),
    nomor = o.teks("nomor") ?: "",
    nama = o.teks("nama") ?: "",
    jenisIkan = o.teks("jenisIkan") ?: "",
    jumlahEkor = o.optInt("jumlahEkor", 0),
    beratTotalKg = o.optDouble("beratTotalKg", 0.0),
    jenisPakan = o.teks("jenisPakan") ?: "",
    stokPakanKarung = o.optDouble("stokPakanKarung", 0.0),
    beratPerKarungKg = o.optDouble("beratPerKarungKg", 0.0),
    tanggalPembenihan = o.teks("tanggalPembenihan")?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: LocalDate.now(),
    catatan = o.teks("catatan") ?: "",
    diperbarui = o.optLong("diperbarui", System.currentTimeMillis())
)
