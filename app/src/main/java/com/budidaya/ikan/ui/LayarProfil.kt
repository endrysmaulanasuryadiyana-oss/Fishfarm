package com.budidaya.ikan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.budidaya.ikan.AksiDrive
import com.budidaya.ikan.AppViewModel
import com.budidaya.ikan.data.DataBudidaya
import com.budidaya.ikan.drive.DriveApi
import com.budidaya.ikan.util.Fmt

@Composable
fun LayarProfil(vm: AppViewModel, data: DataBudidaya) {
    val p = data.profil
    var namaUsaha by rememberSaveable(p) { mutableStateOf(p.namaUsaha) }
    var namaPemilik by rememberSaveable(p) { mutableStateOf(p.namaPemilik) }
    var noHp by rememberSaveable(p) { mutableStateOf(p.noHp) }
    var alamat by rememberSaveable(p) { mutableStateOf(p.alamat) }
    var logo by rememberSaveable(p) { mutableStateOf(p.logoBase64) }
    var galatProfil by rememberSaveable { mutableStateOf<String?>(null) }

    var pwLama by rememberSaveable { mutableStateOf("") }
    var pwBaru by rememberSaveable { mutableStateOf("") }
    var pwUlang by rememberSaveable { mutableStateOf("") }
    var galatPw by rememberSaveable { mutableStateOf<String?>(null) }

    var konfirmasiPulihkan by rememberSaveable { mutableStateOf(false) }
    var konfirmasiPutus by rememberSaveable { mutableStateOf(false) }

    val pilihLogo = rememberPilihLogo { hasil ->
        if (hasil != null) logo = hasil else vm.pesan = "Gambar tidak dapat dibaca"
    }

    Scaffold(topBar = { BarAtas("Profil & Google Drive", { vm.kembali() }) }) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------------- Google Drive ----------------
            KartuBagian("Google Drive", ikon = Icons.Filled.Cloud) {
                if (p.googleEmail != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudDone, null, tint = Hijau)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Terhubung", color = Hijau, fontWeight = FontWeight.Bold)
                            Text(p.googleEmail, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    BarisInfo("Username", "@${p.username}")
                    BarisInfo("Backup terakhir", if (p.terakhirSync > 0) Fmt.waktu(p.terakhirSync) else "Belum pernah")
                    BarisInfo("Lokasi di Drive", "${DriveApi.NAMA_FOLDER} / ${DriveApi.namaFile(p.username)}")
                    Text(
                        "Setiap perubahan data otomatis di-backup ke Google Drive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { vm.jalankanDrive(AksiDrive.BACKUP) },
                        enabled = !vm.sedangSync,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (vm.sedangSync) "Menyinkronkan…" else "Backup Sekarang")
                    }
                    OutlinedButton(
                        onClick = { konfirmasiPulihkan = true },
                        enabled = !vm.sedangSync,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CloudDownload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pulihkan dari Google Drive")
                    }
                    TextButton(onClick = { konfirmasiPutus = true }) {
                        Icon(Icons.Filled.CloudOff, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Putuskan Google Drive")
                    }
                } else {
                    Text(
                        "Hubungkan akun Google agar data budidaya \"@${p.username}\" tersimpan aman di Google Drive " +
                            "dan bisa dipulihkan di HP lain dengan username yang sama.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { vm.jalankanDrive(AksiDrive.HUBUNGKAN) },
                        enabled = !vm.sedangSync,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Cloud, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (vm.sedangSync) "Menghubungkan…" else "Hubungkan Google Drive")
                    }
                }
            }

            // ---------------- Profil usaha ----------------
            KartuBagian("Profil Usaha", ikon = Icons.Filled.Store) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoUsaha(logo, 80.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        OutlinedButton(onClick = pilihLogo) {
                            Icon(Icons.Filled.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ganti dari Galeri")
                        }
                        if (logo != null) TextButton(onClick = { logo = null }) { Text("Hapus logo") }
                    }
                }
                FieldTeks("Nama usaha / budidaya *", namaUsaha, { namaUsaha = it }, ikon = Icons.Filled.Store,
                    kapital = KeyboardCapitalization.Words)
                FieldTeks("Nama pemilik *", namaPemilik, { namaPemilik = it }, ikon = Icons.Filled.Person,
                    kapital = KeyboardCapitalization.Words)
                FieldTeks("No. HP / WhatsApp", noHp, { noHp = it }, ikon = Icons.Filled.Phone,
                    keyboard = KeyboardType.Phone)
                FieldTeks("Alamat lengkap *", alamat, { alamat = it }, ikon = Icons.Filled.LocationOn, barisMin = 3,
                    galat = if (alamat.isBlank()) "Alamat wajib diisi" else null)
                galatProfil?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = { galatProfil = vm.perbaruiProfil(namaUsaha, namaPemilik, noHp, alamat, logo) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Simpan Profil") }
            }

            // ---------------- Akun ----------------
            KartuBagian("Akun & Keamanan", ikon = Icons.Filled.Lock) {
                BarisInfo("Username", "@${p.username}", tebal = true)
                BarisInfo("Terdaftar", Fmt.waktu(p.dibuat))
                FieldPassword("Password lama", pwLama, { pwLama = it })
                FieldPassword("Password baru", pwBaru, { pwBaru = it })
                FieldPassword("Ulangi password baru", pwUlang, { pwUlang = it })
                galatPw?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedButton(
                    onClick = {
                        galatPw = vm.gantiPassword(pwLama, pwBaru, pwUlang)
                        if (galatPw == null) {
                            pwLama = ""; pwBaru = ""; pwUlang = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Ganti Password") }
            }

            Button(
                onClick = { vm.keluar() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(Modifier.width(8.dp))
                Text("Keluar")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (konfirmasiPulihkan) {
        AlertDialog(
            onDismissRequest = { konfirmasiPulihkan = false },
            title = { Text("Pulihkan dari Google Drive?") },
            text = { Text("Data di HP ini akan diganti dengan backup terakhir di Google Drive untuk \"@${p.username}\".") },
            confirmButton = {
                TextButton(onClick = {
                    konfirmasiPulihkan = false
                    vm.jalankanDrive(AksiDrive.PULIHKAN)
                }) { Text("Pulihkan") }
            },
            dismissButton = { TextButton(onClick = { konfirmasiPulihkan = false }) { Text("Batal") } }
        )
    }
    if (konfirmasiPutus) {
        AlertDialog(
            onDismissRequest = { konfirmasiPutus = false },
            title = { Text("Putuskan Google Drive?") },
            text = { Text("Backup otomatis berhenti. File yang sudah ada di Google Drive tidak dihapus.") },
            confirmButton = {
                TextButton(onClick = {
                    konfirmasiPutus = false
                    vm.putuskanDrive()
                }) { Text("Putuskan") }
            },
            dismissButton = { TextButton(onClick = { konfirmasiPutus = false }) { Text("Batal") } }
        )
    }
}
