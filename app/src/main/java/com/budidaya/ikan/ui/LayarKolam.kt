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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.budidaya.ikan.AppViewModel
import com.budidaya.ikan.Layar
import com.budidaya.ikan.R
import com.budidaya.ikan.data.Kolam
import com.budidaya.ikan.util.Fmt
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarAtas(judul: String, onKembali: () -> Unit, aksi: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(judul) },
        navigationIcon = {
            IconButton(onClick = onKembali) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") }
        },
        actions = { aksi() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

// =====================================================================
// FORM TAMBAH / EDIT KOLAM
// =====================================================================

@Composable
fun LayarFormKolam(vm: AppViewModel, idKolam: String?) {
    val lama = idKolam?.let { vm.kolam(it) }
    val id = rememberSaveable { lama?.id ?: UUID.randomUUID().toString() }

    var nomor by rememberSaveable { mutableStateOf(lama?.nomor ?: "") }
    var nama by rememberSaveable { mutableStateOf(lama?.nama ?: "") }
    var jenisIkan by rememberSaveable { mutableStateOf(lama?.jenisIkan ?: "") }
    var ekor by rememberSaveable { mutableStateOf(lama?.jumlahEkor?.toString() ?: "") }
    var kg by rememberSaveable { mutableStateOf(lama?.let { Fmt.keInput(it.beratTotalKg) } ?: "") }
    var jenisPakan by rememberSaveable { mutableStateOf(lama?.jenisPakan ?: "") }
    var karung by rememberSaveable { mutableStateOf(lama?.let { Fmt.keInput(it.stokPakanKarung) } ?: "") }
    var kgKarung by rememberSaveable { mutableStateOf(lama?.let { Fmt.keInput(it.beratPerKarungKg) } ?: "30") }
    var tglEpoch by rememberSaveable { mutableStateOf((lama?.tanggalPembenihan ?: LocalDate.now()).toEpochDay()) }
    var catatan by rememberSaveable { mutableStateOf(lama?.catatan ?: "") }
    var dicoba by rememberSaveable { mutableStateOf(false) }

    val nEkor = ekor.toIntOrNull()
    val nKg = Fmt.desimal(kg)
    val nKarung = Fmt.desimal(karung)
    val nKgKarung = Fmt.desimal(kgKarung)

    val gNomor = when {
        nomor.isBlank() -> "Nomor kolam wajib diisi"
        vm.nomorDipakai(nomor, id) -> "Nomor kolam ini sudah dipakai kolam lain"
        else -> null
    }
    val gJenis = if (jenisIkan.isBlank()) "Jenis ikan wajib diisi" else null
    val gEkor = if (nEkor == null) "Isi jumlah ekor (angka)" else null
    val gKg = if (nKg == null) "Isi berat total (kg)" else null
    val gKarung = if (nKarung == null) "Isi stok pakan (karung), isi 0 jika habis" else null
    val gKgKarung = if (nKgKarung == null || nKgKarung <= 0) "Isi berat per karung" else null
    val valid = listOf(gNomor, gJenis, gEkor, gKg, gKarung, gKgKarung).all { it == null }

    Scaffold(topBar = { BarAtas(if (lama == null) "Tambah Kolam" else "Edit ${lama.judul}", { vm.kembali() }) }) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KartuBagian("Identitas Kolam", ikon = Icons.Filled.Pool) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldTeks("No. kolam *", nomor, { nomor = it }, Modifier.weight(0.38f),
                        galat = if (dicoba) gNomor else null, kapital = KeyboardCapitalization.Characters)
                    FieldTeks("Nama kolam", nama, { nama = it }, Modifier.weight(0.62f),
                        bantuan = "Opsional, mis. Blok A", kapital = KeyboardCapitalization.Words)
                }
            }

            KartuBagian("Ikan di Kolam", ikonRes = R.drawable.ic_ikan, warna = Hijau) {
                FieldJenisIkan(jenisIkan, { jenisIkan = it }, if (dicoba) gJenis else null)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldAngka("Jumlah ikan *", ekor, { ekor = it }, "ekor", Modifier.weight(1f),
                        desimal = false, galat = if (dicoba) gEkor else null)
                    FieldAngka("Berat total *", kg, { kg = it }, "kg", Modifier.weight(1f),
                        galat = if (dicoba) gKg else null)
                }
                if (nEkor != null && nKg != null && nEkor > 0 && nKg > 0) {
                    val contoh = Kolam(nomor = "", nama = "", jenisIkan = "", jumlahEkor = nEkor, beratTotalKg = nKg,
                        jenisPakan = "", stokPakanKarung = 0.0, beratPerKarungKg = 0.0, tanggalPembenihan = LocalDate.now())
                    Text(
                        "≈ ${Fmt.angka(contoh.beratRataGram, 1)} gram/ekor  •  ${Fmt.angka(contoh.ekorPerKg, 1)} ekor/kg",
                        color = Hijau, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium
                    )
                }
            }

            KartuBagian("Stok Pakan", ikon = Icons.Filled.ShoppingCart, warna = Oranye) {
                FieldTeks("Jenis / merk pakan", jenisPakan, { jenisPakan = it },
                    bantuan = "Mis. Pelet apung PF-1000, 781-2", kapital = KeyboardCapitalization.Words)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldAngka("Stok pakan *", karung, { karung = it }, "karung", Modifier.weight(1f),
                        galat = if (dicoba) gKarung else null)
                    FieldAngka("Isi per karung *", kgKarung, { kgKarung = it }, "kg", Modifier.weight(1f),
                        galat = if (dicoba) gKgKarung else null)
                }
                if (nKarung != null && nKgKarung != null) {
                    Text("Total stok pakan: ${Fmt.angka(nKarung * nKgKarung, 1)} kg",
                        color = Oranye, fontWeight = FontWeight.Medium)
                }
            }

            KartuBagian("Waktu Pembenihan / Tebar", ikon = Icons.Filled.DateRange) {
                val tgl = LocalDate.ofEpochDay(tglEpoch)
                FieldTanggal("Tanggal pembenihan *", tgl) { tglEpoch = it.toEpochDay() }
                BarisInfo("Hari", Fmt.hari(tgl))
                BarisInfo("Tanggal", tgl.dayOfMonth.toString())
                BarisInfo("Bulan", Fmt.bulan(tgl))
                BarisInfo("Tahun", tgl.year.toString())
                BarisInfo("Umur pemeliharaan", Fmt.umur(LocalDate.now().toEpochDay() - tglEpoch))
            }

            KartuBagian("Catatan", ikon = Icons.Filled.Info) {
                FieldTeks("Catatan (opsional)", catatan, { catatan = it }, barisMin = 3,
                    bantuan = "Mis. asal benih, ukuran kolam, kondisi air")
            }

            Button(
                onClick = {
                    dicoba = true
                    if (valid) {
                        vm.simpanKolam(
                            Kolam(
                                id = id,
                                nomor = nomor.trim(),
                                nama = nama.trim(),
                                jenisIkan = jenisIkan.trim(),
                                jumlahEkor = nEkor!!,
                                beratTotalKg = nKg!!,
                                jenisPakan = jenisPakan.trim(),
                                stokPakanKarung = nKarung!!,
                                beratPerKarungKg = nKgKarung!!,
                                tanggalPembenihan = LocalDate.ofEpochDay(tglEpoch),
                                catatan = catatan.trim()
                            )
                        )
                    } else {
                        vm.pesan = "Lengkapi data yang bertanda merah"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("Simpan Data Kolam") }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// =====================================================================
// DETAIL KOLAM
// =====================================================================

@Composable
fun LayarDetailKolam(vm: AppViewModel, idKolam: String) {
    val k = vm.kolam(idKolam)
    if (k == null) {
        LaunchedEffect(idKolam) { vm.ke(Layar.Beranda) }
        return
    }
    var konfirmasiHapus by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BarAtas(k.judul, { vm.kembali() }) {
                IconButton(onClick = { vm.ke(Layar.FormKolam(k.id)) }) { Icon(Icons.Filled.Edit, "Edit") }
                IconButton(onClick = { konfirmasiHapus = true }) { Icon(Icons.Filled.Delete, "Hapus") }
            }
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KartuBagian("Identitas Kolam", ikon = Icons.Filled.Pool) {
                BarisInfo("Nomor kolam", k.nomor, tebal = true)
                BarisInfo("Nama kolam", k.nama.ifBlank { "-" })
            }

            KartuBagian("Ikan di Kolam", ikonRes = R.drawable.ic_ikan, warna = Hijau) {
                BarisInfo("Jenis ikan", k.jenisIkan, tebal = true)
                BarisInfo("Jumlah ikan", "${Fmt.angka(k.jumlahEkor)} ekor", tebal = true)
                BarisInfo("Berat total", "${Fmt.angka(k.beratTotalKg)} kg", tebal = true)
                BarisInfo("Berat rata-rata", "${Fmt.angka(k.beratRataGram, 1)} gram/ekor")
                BarisInfo("Ukuran (size)", "${Fmt.angka(k.ekorPerKg, 1)} ekor/kg")
            }

            KartuBagian("Stok Pakan", ikon = Icons.Filled.ShoppingCart, warna = Oranye) {
                BarisInfo("Jenis / merk pakan", k.jenisPakan.ifBlank { "-" })
                BarisInfo("Stok pakan", "${Fmt.angka(k.stokPakanKarung)} karung", tebal = true)
                BarisInfo("Isi per karung", "${Fmt.angka(k.beratPerKarungKg)} kg")
                BarisInfo("Total stok", "${Fmt.angka(k.totalPakanKg, 1)} kg", tebal = true)
                if (k.beratTotalKg > 0 && k.totalPakanKg > 0) {
                    BarisInfo("Pakan per kg ikan", "${Fmt.angka(k.totalPakanKg / k.beratTotalKg, 2)} kg")
                }
            }

            KartuBagian("Waktu Pembenihan / Tebar", ikon = Icons.Filled.DateRange) {
                val t = k.tanggalPembenihan
                BarisInfo("Lengkap", Fmt.tanggal(t), tebal = true)
                BarisInfo("Hari", Fmt.hari(t))
                BarisInfo("Tanggal", t.dayOfMonth.toString())
                BarisInfo("Bulan", "${Fmt.bulan(t)} (${t.monthValue})")
                BarisInfo("Tahun", t.year.toString())
                BarisInfo("Umur pemeliharaan", Fmt.umur(k.umurHari()))
            }

            if (k.catatan.isNotBlank()) {
                KartuBagian("Catatan", ikon = Icons.Filled.Info) {
                    Text(k.catatan)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Terakhir diperbarui: ${Fmt.waktu(k.diperbarui)}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { vm.ke(Layar.FormKolam(k.id)) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Edit, null)
                Spacer(Modifier.width(8.dp))
                Text("Perbarui Data (timbang / stok pakan)")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (konfirmasiHapus) {
        AlertDialog(
            onDismissRequest = { konfirmasiHapus = false },
            title = { Text("Hapus ${k.judul}?") },
            text = { Text("Data ikan ${k.jenisIkan} dan stok pakan kolam ini akan dihapus.") },
            confirmButton = {
                TextButton(onClick = {
                    konfirmasiHapus = false
                    vm.hapusKolam(k.id)
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { konfirmasiHapus = false }) { Text("Batal") } }
        )
    }
}
