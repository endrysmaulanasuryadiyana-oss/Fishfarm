package com.budidaya.ikan

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.budidaya.ikan.data.Akun
import com.budidaya.ikan.data.DataBudidaya
import com.budidaya.ikan.data.Kolam
import com.budidaya.ikan.data.Profil
import com.budidaya.ikan.data.Repository
import com.budidaya.ikan.drive.DriveApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed interface Layar {
    data object Masuk : Layar
    data object Daftar : Layar
    data object Beranda : Layar
    data class FormKolam(val id: String?) : Layar
    data class DetailKolam(val id: String) : Layar
    data object Profil : Layar
}

enum class AksiDrive { HUBUNGKAN, BACKUP, BACKUP_OTOMATIS, PULIHKAN, PULIHKAN_SAAT_MASUK }

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository(app)

    var data by mutableStateOf<DataBudidaya?>(null)
        private set
    var layar by mutableStateOf<Layar>(Layar.Masuk)
        private set
    var pesan by mutableStateOf<String?>(null)
    var sedangSync by mutableStateOf(false)
        private set

    /** Diisi oleh MainActivity: meminta izin/token Google Drive. */
    var mintaOtorisasiDrive: ((interaktif: Boolean) -> Unit)? = null

    private var aksiTertunda: AksiDrive? = null
    private var usernamePulihkan: String? = null
    private var adaPerubahanBelumSync = false

    init {
        repo.sesiUsername?.let { u ->
            repo.muat(u)?.let {
                data = it
                layar = Layar.Beranda
            }
        }
    }

    // ---------------- navigasi ----------------

    fun ke(l: Layar) {
        layar = l
    }

    /** @return false jika sudah di layar paling awal (biarkan sistem menutup aplikasi). */
    fun kembali(): Boolean {
        layar = when (val l = layar) {
            is Layar.FormKolam -> if (l.id != null) Layar.DetailKolam(l.id) else Layar.Beranda
            is Layar.DetailKolam, Layar.Profil -> Layar.Beranda
            Layar.Daftar -> Layar.Masuk
            Layar.Masuk, Layar.Beranda -> return false
        }
        return true
    }

    // ---------------- akun ----------------

    fun daftar(
        username: String, password: String, ulang: String,
        namaPemilik: String, namaUsaha: String, noHp: String, alamat: String, logo: String?
    ): String? {
        Akun.cekUsername(username)?.let { return it }
        if (namaUsaha.isBlank()) return "Nama usaha / budidaya wajib diisi"
        if (namaPemilik.isBlank()) return "Nama pemilik wajib diisi"
        if (alamat.isBlank()) return "Alamat wajib diisi"
        if (alamat.trim().length < 10) return "Alamat terlalu singkat, tulis alamat lengkap"
        Akun.cekPassword(password, ulang)?.let { return it }
        val u = Akun.normalisasi(username)
        if (repo.ada(u)) return "Username \"$u\" sudah terdaftar di HP ini"

        val salt = Akun.buatSalt()
        val profil = Profil(
            username = u,
            passwordHash = Akun.hash(password, salt),
            salt = salt,
            namaPemilik = namaPemilik.trim(),
            namaUsaha = namaUsaha.trim(),
            alamat = alamat.trim(),
            noHp = noHp.trim(),
            logoBase64 = logo
        )
        val baru = DataBudidaya(profil, emptyList())
        repo.simpan(baru)
        repo.sesiUsername = u
        data = baru
        layar = Layar.Beranda
        pesan = "Pendaftaran berhasil. Hubungkan Google Drive agar data aman."
        return null
    }

    fun masuk(username: String, password: String): String? {
        val u = Akun.normalisasi(username)
        if (u.isEmpty() || password.isEmpty()) return "Isi username dan password"
        val d = repo.muat(u)
            ?: return "Username belum terdaftar di HP ini. Daftar dulu, atau pulihkan dari Google Drive."
        if (!Akun.cocok(d.profil, password)) return "Password salah"
        repo.sesiUsername = u
        data = d
        layar = Layar.Beranda
        return null
    }

    fun keluar() {
        repo.sesiUsername = null
        data = null
        layar = Layar.Masuk
    }

    fun perbaruiProfil(namaUsaha: String, namaPemilik: String, noHp: String, alamat: String, logo: String?): String? {
        val d = data ?: return "Belum login"
        if (namaUsaha.isBlank()) return "Nama usaha wajib diisi"
        if (namaPemilik.isBlank()) return "Nama pemilik wajib diisi"
        if (alamat.isBlank()) return "Alamat wajib diisi"
        simpanData(
            d.copy(
                profil = d.profil.copy(
                    namaUsaha = namaUsaha.trim(), namaPemilik = namaPemilik.trim(),
                    noHp = noHp.trim(), alamat = alamat.trim(), logoBase64 = logo
                )
            )
        )
        pesan = "Profil disimpan"
        return null
    }

    fun gantiPassword(lama: String, baru: String, ulang: String): String? {
        val d = data ?: return "Belum login"
        if (!Akun.cocok(d.profil, lama)) return "Password lama salah"
        Akun.cekPassword(baru, ulang)?.let { return it }
        val salt = Akun.buatSalt()
        simpanData(d.copy(profil = d.profil.copy(salt = salt, passwordHash = Akun.hash(baru, salt))))
        pesan = "Password diganti"
        return null
    }

    // ---------------- kolam ----------------

    fun kolam(id: String): Kolam? = data?.kolam?.firstOrNull { it.id == id }

    fun nomorDipakai(nomor: String, kecualiId: String?): Boolean =
        data?.kolam?.any { it.id != kecualiId && it.nomor.trim().equals(nomor.trim(), ignoreCase = true) } == true

    fun simpanKolam(k: Kolam) {
        val d = data ?: return
        val kolamBaru = k.copy(diperbarui = System.currentTimeMillis())
        val list = if (d.kolam.any { it.id == k.id }) d.kolam.map { if (it.id == k.id) kolamBaru else it }
        else d.kolam + kolamBaru
        simpanData(d.copy(kolam = list))
        layar = Layar.DetailKolam(k.id)
        pesan = "Data ${kolamBaru.judul} disimpan"
    }

    fun hapusKolam(id: String) {
        val d = data ?: return
        val k = d.kolam.firstOrNull { it.id == id }
        simpanData(d.copy(kolam = d.kolam.filterNot { it.id == id }))
        layar = Layar.Beranda
        pesan = "${k?.judul ?: "Kolam"} dihapus"
    }

    private fun simpanData(d: DataBudidaya, syncOtomatis: Boolean = true) {
        repo.simpan(d)
        data = d
        if (syncOtomatis && d.profil.googleEmail != null) {
            adaPerubahanBelumSync = true
            if (aksiTertunda == null) jalankanDrive(AksiDrive.BACKUP_OTOMATIS)
        }
    }

    // ---------------- Google Drive ----------------

    fun jalankanDrive(aksi: AksiDrive, username: String? = null) {
        if (aksiTertunda != null) {
            if (aksi != AksiDrive.BACKUP_OTOMATIS) pesan = "Sinkronisasi masih berjalan, tunggu sebentar"
            return
        }
        if (aksi == AksiDrive.PULIHKAN_SAAT_MASUK) {
            val err = Akun.cekUsername(username ?: "")
            if (err != null) {
                pesan = "Isi username yang ingin dipulihkan. $err"
                return
            }
            usernamePulihkan = Akun.normalisasi(username!!)
        }
        val minta = mintaOtorisasiDrive
        if (minta == null) {
            pesan = "Google Drive belum siap, coba lagi"
            return
        }
        aksiTertunda = aksi
        sedangSync = true
        minta(aksi != AksiDrive.BACKUP_OTOMATIS)
    }

    fun putuskanDrive() {
        val d = data ?: return
        simpanData(d.copy(profil = d.profil.copy(googleEmail = null)), syncOtomatis = false)
        pesan = "Google Drive diputus dari aplikasi (file di Drive tidak dihapus)"
    }

    fun onDrivePerluLogin() {
        selesaiDrive("Sesi Google Drive perlu disambungkan ulang. Buka Profil → Backup Sekarang.")
    }

    fun onDriveGagal(alasan: String) {
        selesaiDrive(alasan)
    }

    private fun selesaiDrive(info: String?) {
        aksiTertunda = null
        sedangSync = false
        if (info != null) pesan = info
    }

    fun onTokenDrive(token: String) {
        val aksi = aksiTertunda ?: return
        viewModelScope.launch {
            try {
                when (aksi) {
                    AksiDrive.HUBUNGKAN -> {
                        val d = data ?: return@launch selesaiDrive(null)
                        val email = withContext(Dispatchers.IO) { DriveApi.emailAkun(token) }
                        val terhubung = d.copy(profil = d.profil.copy(googleEmail = email))
                        backup(token, terhubung)
                        selesaiDrive("Terhubung ke Google Drive ($email) dan data sudah di-backup")
                    }

                    AksiDrive.BACKUP, AksiDrive.BACKUP_OTOMATIS -> {
                        val d = data ?: return@launch selesaiDrive(null)
                        backup(token, d)
                        selesaiDrive(if (aksi == AksiDrive.BACKUP) "Backup ke Google Drive berhasil" else null)
                        // ada data yang berubah saat upload berlangsung → kirim sekali lagi
                        if (adaPerubahanBelumSync) jalankanDrive(AksiDrive.BACKUP_OTOMATIS)
                    }

                    AksiDrive.PULIHKAN -> {
                        val d = data ?: return@launch selesaiDrive(null)
                        val dariDrive = unduhData(token, d.profil.username)
                        if (dariDrive == null) {
                            selesaiDrive("Belum ada backup untuk \"${d.profil.username}\" di akun Google ini")
                        } else {
                            val email = withContext(Dispatchers.IO) { DriveApi.emailAkun(token) }
                            val hasil = dariDrive.copy(profil = dariDrive.profil.copy(googleEmail = email))
                            simpanData(hasil, syncOtomatis = false)
                            selesaiDrive("Data dipulihkan dari Google Drive (${hasil.kolam.size} kolam)")
                        }
                    }

                    AksiDrive.PULIHKAN_SAAT_MASUK -> {
                        val u = usernamePulihkan ?: return@launch selesaiDrive(null)
                        val dariDrive = unduhData(token, u)
                        if (dariDrive == null) {
                            selesaiDrive("Tidak ada backup untuk username \"$u\" di akun Google ini")
                        } else {
                            val email = withContext(Dispatchers.IO) { DriveApi.emailAkun(token) }
                            repo.simpan(dariDrive.copy(profil = dariDrive.profil.copy(googleEmail = email)))
                            selesaiDrive("Data \"$u\" berhasil dipulihkan. Silakan masuk dengan password Anda.")
                        }
                    }
                }
            } catch (e: DriveApi.DriveException) {
                selesaiDrive(e.message ?: "Gagal mengakses Google Drive")
            } catch (e: Exception) {
                selesaiDrive("Gagal terhubung ke Google Drive. Periksa koneksi internet.")
            }
        }
    }

    private suspend fun backup(token: String, d: DataBudidaya) {
        adaPerubahanBelumSync = false
        val waktu = System.currentTimeMillis()
        val tersinkron = d.copy(profil = d.profil.copy(terakhirSync = waktu))
        withContext(Dispatchers.IO) {
            DriveApi.unggah(token, tersinkron.profil.username, tersinkron.toJson().toString())
        }
        // Jangan timpa perubahan yang terjadi saat upload berlangsung.
        val sekarang = data
        val dasar = if (sekarang != null && sekarang.profil.username == d.profil.username) sekarang else d
        val akhir = dasar.copy(
            profil = dasar.profil.copy(terakhirSync = waktu, googleEmail = d.profil.googleEmail)
        )
        repo.simpan(akhir)
        data = akhir
    }

    private suspend fun unduhData(token: String, username: String): DataBudidaya? {
        val teks = withContext(Dispatchers.IO) { DriveApi.unduh(token, username) } ?: return null
        val d = DataBudidaya.fromJson(JSONObject(teks))
        return if (d.profil.username == username) d else null
    }
}
