package com.budidaya.ikan.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.budidaya.ikan.R
import com.budidaya.ikan.util.Fmt
import com.budidaya.ikan.util.Gambar
import java.time.LocalDate

val DAFTAR_IKAN = listOf(
    "Lele", "Nila", "Nila Merah", "Gurame", "Patin", "Ikan Mas", "Bawal", "Mujair",
    "Tawes", "Gabus", "Belut", "Koi", "Bandeng", "Udang Vaname", "Udang Galah", "Lobster Air Tawar"
)

/** Logo usaha (bulat). Jika belum ada logo, tampilkan ikon ikan. */
@Composable
fun LogoUsaha(base64: String?, ukuran: Dp, modifier: Modifier = Modifier) {
    val gambar = remember(base64) { base64?.let { Gambar.dariBase64(it) } }
    Box(
        modifier
            .size(ukuran)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (gambar != null) {
            Image(
                bitmap = gambar,
                contentDescription = "Logo usaha",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                painterResource(R.drawable.ic_ikan),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(ukuran * 0.55f)
            )
        }
    }
}

/** Membuka galeri foto; hasilnya (Base64) dikirim ke [onDipilih]. */
@Composable
fun rememberPilihLogo(onDipilih: (String?) -> Unit): () -> Unit {
    val ctx = LocalContext.current
    val peluncur = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onDipilih(Gambar.keBase64(ctx, uri))
    }
    return {
        peluncur.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

@Composable
fun FieldTeks(
    label: String,
    nilai: String,
    onUbah: (String) -> Unit,
    modifier: Modifier = Modifier,
    galat: String? = null,
    bantuan: String? = null,
    ikon: ImageVector? = null,
    barisMin: Int = 1,
    keyboard: KeyboardType = KeyboardType.Text,
    kapital: KeyboardCapitalization = KeyboardCapitalization.Sentences
) {
    OutlinedTextField(
        value = nilai,
        onValueChange = onUbah,
        label = { Text(label) },
        leadingIcon = ikon?.let { i -> { Icon(i, contentDescription = null) } },
        isError = galat != null,
        supportingText = (galat ?: bantuan)?.let { t -> { Text(t) } },
        singleLine = barisMin == 1,
        minLines = barisMin,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard, capitalization = kapital),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FieldPassword(label: String, nilai: String, onUbah: (String) -> Unit, galat: String? = null) {
    var lihat by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = nilai,
        onValueChange = onUbah,
        label = { Text(label) },
        singleLine = true,
        isError = galat != null,
        supportingText = galat?.let { t -> { Text(t) } },
        visualTransformation = if (lihat) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { lihat = !lihat }) {
                Icon(
                    if (lihat) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (lihat) "Sembunyikan" else "Tampilkan"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/** Input angka; [desimal] = boleh koma (mis. 12,5 kg). */
@Composable
fun FieldAngka(
    label: String,
    nilai: String,
    onUbah: (String) -> Unit,
    satuan: String,
    modifier: Modifier = Modifier,
    desimal: Boolean = true,
    galat: String? = null,
    bantuan: String? = null
) {
    OutlinedTextField(
        value = nilai,
        onValueChange = { s -> onUbah(s.filter { it.isDigit() || (desimal && (it == ',' || it == '.')) }) },
        label = { Text(label) },
        suffix = { Text(satuan) },
        singleLine = true,
        isError = galat != null,
        supportingText = (galat ?: bantuan)?.let { t -> { Text(t) } },
        keyboardOptions = KeyboardOptions(keyboardType = if (desimal) KeyboardType.Decimal else KeyboardType.Number),
        modifier = modifier.fillMaxWidth()
    )
}

/** Field tanggal: ketuk untuk membuka kalender. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldTanggal(label: String, tanggal: LocalDate, onUbah: (LocalDate) -> Unit) {
    var buka by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = Fmt.tanggal(tanggal),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
            supportingText = { Text("Ketuk untuk memilih hari, tanggal, bulan & tahun") },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { buka = true }
        )
    }
    if (buka) {
        val satuHari = 86_400_000L
        val state = rememberDatePickerState(initialSelectedDateMillis = tanggal.toEpochDay() * satuHari)
        DatePickerDialog(
            onDismissRequest = { buka = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onUbah(LocalDate.ofEpochDay(it / satuHari)) }
                    buka = false
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { buka = false }) { Text("Batal") } }
        ) {
            DatePicker(state = state)
        }
    }
}

/** Jenis ikan: boleh ketik bebas, dengan saran pilihan. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldJenisIkan(nilai: String, onUbah: (String) -> Unit, galat: String?) {
    var buka by remember { mutableStateOf(false) }
    val saran = DAFTAR_IKAN.filter { nilai.isBlank() || (it.contains(nilai.trim(), true) && !it.equals(nilai.trim(), true)) }
    val tampil = buka && saran.isNotEmpty()
    ExposedDropdownMenuBox(expanded = tampil, onExpandedChange = { buka = it }) {
        OutlinedTextField(
            value = nilai,
            onValueChange = {
                onUbah(it)
                buka = true
            },
            label = { Text("Jenis ikan *") },
            leadingIcon = { Icon(painterResource(R.drawable.ic_ikan), contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tampil) },
            singleLine = true,
            isError = galat != null,
            supportingText = galat?.let { t -> { Text(t) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = tampil, onDismissRequest = { buka = false }) {
            saran.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s) },
                    onClick = {
                        onUbah(s)
                        buka = false
                    }
                )
            }
        }
    }
}

/** Kartu bagian dengan judul. */
@Composable
fun KartuBagian(
    judul: String,
    ikon: ImageVector? = null,
    ikonRes: Int? = null,
    warna: Color = MaterialTheme.colorScheme.primary,
    isi: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    ikon != null -> Icon(ikon, null, tint = warna, modifier = Modifier.size(22.dp))
                    ikonRes != null -> Icon(painterResource(ikonRes), null, tint = warna, modifier = Modifier.size(22.dp))
                }
                if (ikon != null || ikonRes != null) Spacer(Modifier.width(8.dp))
                Text(judul, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = warna)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            isi()
        }
    }
}

/** Baris label : nilai pada halaman detail. */
@Composable
fun BarisInfo(label: String, nilai: String, tebal: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            nilai,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (tebal) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(0.55f)
        )
    }
}

/** Kartu angka ringkasan di beranda. */
@Composable
fun KartuStat(
    judul: String,
    nilai: String,
    satuan: String,
    latar: Color,
    warna: Color,
    modifier: Modifier = Modifier,
    ikon: ImageVector? = null,
    ikonRes: Int? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = latar),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    ikon != null -> Icon(ikon, null, tint = warna, modifier = Modifier.size(18.dp))
                    ikonRes != null -> Icon(painterResource(ikonRes), null, tint = warna, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(judul, style = MaterialTheme.typography.labelLarge, color = warna, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.size(6.dp))
            Text(nilai, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = warna, maxLines = 1)
            Text(satuan, style = MaterialTheme.typography.bodySmall, color = warna.copy(alpha = 0.8f))
        }
    }
}
