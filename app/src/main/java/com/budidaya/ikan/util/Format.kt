package com.budidaya.ikan.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/** Format angka & tanggal dalam Bahasa Indonesia. */
object Fmt {
    val ID: Locale = Locale.forLanguageTag("id-ID")

    private val fLengkap = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", ID)
    private val fHari = DateTimeFormatter.ofPattern("EEEE", ID)
    private val fBulan = DateTimeFormatter.ofPattern("MMMM", ID)

    /** Contoh: "Selasa, 3 November 2026" */
    fun tanggal(d: LocalDate): String = fLengkap.format(d)
    fun hari(d: LocalDate): String = fHari.format(d)
    fun bulan(d: LocalDate): String = fBulan.format(d)

    fun waktu(ms: Long): String = SimpleDateFormat("d MMM yyyy, HH:mm", ID).format(Date(ms))

    fun angka(x: Double, maksDesimal: Int = 2): String {
        val nf = NumberFormat.getNumberInstance(ID)
        nf.maximumFractionDigits = maksDesimal
        nf.minimumFractionDigits = 0
        return nf.format(x)
    }

    fun angka(x: Long): String = NumberFormat.getIntegerInstance(ID).format(x)
    fun angka(x: Int): String = angka(x.toLong())

    /** Terima "12,5" maupun "12.5". */
    fun desimal(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()

    /** Ubah angka kembali ke teks untuk diisi ulang di form (tanpa pemisah ribuan). */
    fun keInput(x: Double): String =
        if (x == Math.floor(x) && !x.isInfinite()) x.toLong().toString() else x.toString().replace('.', ',')

    /** Contoh: "45 hari (6 minggu 3 hari)" */
    fun umur(hari: Long): String = when {
        hari < 0 -> "Dijadwalkan ${-hari} hari lagi"
        hari == 0L -> "Hari ini (hari ke-0)"
        hari < 7 -> "$hari hari"
        else -> {
            val minggu = hari / 7
            val sisa = hari % 7
            val bln = hari / 30
            val detail = if (sisa == 0L) "$minggu minggu" else "$minggu minggu $sisa hari"
            if (bln >= 1) "$hari hari ($detail, ±$bln bulan)" else "$hari hari ($detail)"
        }
    }
}
