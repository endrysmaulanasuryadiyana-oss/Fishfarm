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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.budidaya.ikan.AksiDrive
import com.budidaya.ikan.AppViewModel
import com.budidaya.ikan.Layar
import com.budidaya.ikan.data.Akun

@Composable
fun LayarMasuk(vm: AppViewModel) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var galat by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            LogoUsaha(null, 96.dp)
            Text("Budidaya Ikan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(
                "Catat kolam, jumlah ikan (kg/ekor), stok pakan & tanggal pembenihan",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            FieldTeks("Username", username, { username = it; galat = null }, ikon = Icons.Filled.Person,
                kapital = KeyboardCapitalization.None)
            FieldPassword("Password", password, { password = it; galat = null })
            galat?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }

            Button(
                onClick = { galat = vm.masuk(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) { Text("Masuk") }

            TextButton(onClick = { vm.ke(Layar.Daftar) }) { Text("Belum punya akun? Daftar di sini") }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ganti HP / install ulang?", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Isi username di atas, lalu pulihkan data dari Google Drive yang terhubung dengan akun tersebut.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(
                        onClick = { vm.jalankanDrive(AksiDrive.PULIHKAN_SAAT_MASUK, username) },
                        enabled = !vm.sedangSync,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CloudDownload, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (vm.sedangSync) "Memulihkan…" else "Pulihkan dari Google Drive")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarDaftar(vm: AppViewModel) {
    var namaUsaha by rememberSaveable { mutableStateOf("") }
    var namaPemilik by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var noHp by rememberSaveable { mutableStateOf("") }
    var alamat by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var ulang by rememberSaveable { mutableStateOf("") }
    var logo by rememberSaveable { mutableStateOf<String?>(null) }
    var dicoba by rememberSaveable { mutableStateOf(false) }
    var galat by rememberSaveable { mutableStateOf<String?>(null) }

    val pilihLogo = rememberPilihLogo { hasil ->
        if (hasil != null) logo = hasil else vm.pesan = "Gambar tidak dapat dibaca"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Akun Budidaya") },
                navigationIcon = {
                    IconButton(onClick = { vm.kembali() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KartuBagian("Logo Budidaya", ikon = Icons.Filled.PhotoLibrary) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoUsaha(logo, 84.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(onClick = pilihLogo) {
                            Icon(Icons.Filled.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (logo == null) "Upload dari Galeri" else "Ganti Logo")
                        }
                        if (logo != null) TextButton(onClick = { logo = null }) { Text("Hapus logo") }
                    }
                }
            }

            KartuBagian("Data Usaha", ikon = Icons.Filled.Store) {
                FieldTeks("Nama usaha / budidaya *", namaUsaha, { namaUsaha = it },
                    ikon = Icons.Filled.Store, kapital = KeyboardCapitalization.Words,
                    galat = if (dicoba && namaUsaha.isBlank()) "Wajib diisi" else null)
                FieldTeks("Nama pemilik *", namaPemilik, { namaPemilik = it },
                    ikon = Icons.Filled.Person, kapital = KeyboardCapitalization.Words,
                    galat = if (dicoba && namaPemilik.isBlank()) "Wajib diisi" else null)
                FieldTeks("No. HP / WhatsApp", noHp, { noHp = it }, ikon = Icons.Filled.Phone,
                    keyboard = KeyboardType.Phone)
                FieldTeks(
                    "Alamat lengkap *", alamat, { alamat = it },
                    ikon = Icons.Filled.LocationOn, barisMin = 3,
                    galat = when {
                        dicoba && alamat.isBlank() -> "Alamat wajib diisi"
                        dicoba && alamat.trim().length < 10 -> "Tulis alamat lengkap (jalan, desa, kecamatan, kabupaten)"
                        else -> null
                    },
                    bantuan = "Jalan / dusun, desa, kecamatan, kabupaten/kota, provinsi"
                )
            }

            KartuBagian("Akun Login", ikon = Icons.Filled.Person) {
                FieldTeks(
                    "Username *", username, { username = it.lowercase().trim() },
                    kapital = KeyboardCapitalization.None,
                    galat = if (dicoba) Akun.cekUsername(username) else null,
                    bantuan = "Dipakai untuk login & nama file backup di Google Drive"
                )
                FieldPassword("Password *", password, { password = it })
                FieldPassword(
                    "Ulangi password *", ulang, { ulang = it },
                    galat = if (dicoba) Akun.cekPassword(password, ulang) else null
                )
            }

            galat?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    dicoba = true
                    galat = vm.daftar(username, password, ulang, namaPemilik, namaUsaha, noHp, alamat, logo)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("Daftar") }
            Spacer(Modifier.height(24.dp))
        }
    }
}
