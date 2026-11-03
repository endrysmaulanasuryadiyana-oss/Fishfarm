package com.budidaya.ikan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.budidaya.ikan.AksiDrive
import com.budidaya.ikan.AppViewModel
import com.budidaya.ikan.Layar
import com.budidaya.ikan.R
import com.budidaya.ikan.data.DataBudidaya
import com.budidaya.ikan.data.Kolam
import com.budidaya.ikan.util.Fmt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarBeranda(vm: AppViewModel, data: DataBudidaya) {
    var cari by rememberSaveable { mutableStateOf("") }
    val p = data.profil
    val semua = data.kolamUrut()
    val daftar = if (cari.isBlank()) semua else semua.filter {
        it.nomor.contains(cari, true) || it.nama.contains(cari, true) ||
            it.jenisIkan.contains(cari, true) || it.jenisPakan.contains(cari, true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LogoUsaha(p.logoBase64, 40.dp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(p.namaUsaha, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("@${p.username}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.ke(Layar.Profil) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Profil & Google Drive")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.ke(Layar.FormKolam(null)) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Tambah Kolam") }
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { KartuAlamatDrive(vm, data) }
            item { GridStat(data) }

            val ringkasan = data.ringkasanJenis()
            if (ringkasan.isNotEmpty()) {
                item {
                    KartuBagian("Ringkasan per Jenis Ikan", ikonRes = R.drawable.ic_ikan) {
                        ringkasan.forEach { r ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(r.jenis, fontWeight = FontWeight.SemiBold)
                                    Text("${r.jumlahKolam} kolam", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${Fmt.angka(r.totalKg)} kg", fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary)
                                    Text("${Fmt.angka(r.totalEkor)} ekor", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Daftar Kolam (${semua.size})", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }
            if (semua.size > 3) {
                item {
                    OutlinedTextField(
                        value = cari, onValueChange = { cari = it },
                        placeholder = { Text("Cari no./nama kolam, jenis ikan, pakan…") },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (semua.isEmpty()) {
                item { KosongKolam { vm.ke(Layar.FormKolam(null)) } }
            } else if (daftar.isEmpty()) {
                item {
                    Text("Tidak ada kolam yang cocok dengan \"$cari\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(daftar, key = { it.id }) { k ->
                KartuKolam(k) { vm.ke(Layar.DetailKolam(k.id)) }
            }
        }
    }
}

@Composable
private fun KartuAlamatDrive(vm: AppViewModel, data: DataBudidaya) {
    val p = data.profil
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(p.namaPemilik, fontWeight = FontWeight.SemiBold)
                    Text(p.alamat, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (p.googleEmail != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CloudDone, null, tint = Hijau, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Google Drive: ${p.googleEmail}" +
                            if (p.terakhirSync > 0) " • ${Fmt.waktu(p.terakhirSync)}" else "",
                        style = MaterialTheme.typography.bodySmall, color = Hijau
                    )
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OranyeMuda)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CloudOff, null, tint = Oranye)
                    Spacer(Modifier.width(8.dp))
                    Text("Belum terhubung Google Drive. Data hanya ada di HP ini.",
                        style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { vm.jalankanDrive(AksiDrive.HUBUNGKAN) }, enabled = !vm.sedangSync,
                        contentPadding = PaddingValues(horizontal = 12.dp)) { Text("Hubungkan") }
                }
            }
        }
    }
}

@Composable
private fun GridStat(data: DataBudidaya) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KartuStat("Jumlah Kolam", Fmt.angka(data.kolam.size), "kolam",
                BiruMuda, Color(0xFF003544), Modifier.weight(1f), ikon = Icons.Filled.Pool)
            KartuStat("Total Ikan", Fmt.angka(data.totalEkor), "ekor",
                HijauMuda, Color(0xFF00382A), Modifier.weight(1f), ikonRes = R.drawable.ic_ikan)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KartuStat("Berat Ikan", Fmt.angka(data.totalKgIkan, 1), "kg",
                HijauMuda, Color(0xFF00382A), Modifier.weight(1f), ikon = Icons.Filled.FitnessCenter)
            KartuStat("Stok Pakan", Fmt.angka(data.totalKarung, 1), "karung • ${Fmt.angka(data.totalPakanKg, 1)} kg",
                OranyeMuda, Color(0xFF4A2800), Modifier.weight(1f), ikon = Icons.Filled.ShoppingCart)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KartuKolam(k: Kolam, onKlik: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onKlik)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                    Text(k.nomor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(k.judul, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Ikan ${k.jenisIkan}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Lencana("${Fmt.angka(k.jumlahEkor)} ekor", HijauMuda, Color(0xFF00382A))
                    Lencana("${Fmt.angka(k.beratTotalKg)} kg", HijauMuda, Color(0xFF00382A))
                    Lencana("${Fmt.angka(k.stokPakanKarung)} karung pakan", OranyeMuda, Color(0xFF4A2800))
                }
                Text(
                    "Tebar: ${Fmt.tanggal(k.tanggalPembenihan)} • ${Fmt.umur(k.umurHari()).substringBefore(" (")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Lencana(teks: String, latar: Color, warna: Color) {
    Text(
        teks,
        style = MaterialTheme.typography.labelMedium,
        color = warna,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(latar)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun KosongKolam(onTambah: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Pool, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Text("Belum ada kolam", fontWeight = FontWeight.Bold)
            Text("Tambahkan kolam pertama: nomor/nama kolam, jenis ikan, jumlah kg & ekor, stok pakan, dan tanggal pembenihan.",
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Button(onClick = onTambah) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("Tambah Kolam")
            }
        }
    }
}
