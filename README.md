# Aplikasi Android – Budidaya Ikan

Aplikasi untuk mencatat kolam, ikan (kg & ekor), stok pakan (karung), dan tanggal pembenihan.
Data tersambung ke **Google Drive** sesuai **username** yang didaftarkan.

## Fitur

| Bagian | Isi |
|---|---|
| **Daftar akun** | Logo usaha (upload dari galeri), nama usaha, nama pemilik, no. HP, **alamat lengkap (wajib)**, username, password |
| **Masuk** | Username + password; atau **pulihkan dari Google Drive** saat ganti HP |
| **Beranda** | Logo & nama usaha, alamat, status Google Drive, ringkasan: jumlah kolam, total ekor, total kg ikan, total karung pakan; ringkasan per jenis ikan; daftar kolam + pencarian |
| **Data kolam** | No. kolam / nama kolam, jenis ikan, jumlah ekor, berat total (kg), otomatis hitung gram/ekor & ekor/kg, jenis/merk pakan, stok pakan (karung) & isi per karung → total kg pakan, tanggal pembenihan (kalender) |
| **Detail tanggal** | Hari, tanggal, bulan, tahun, tanggal lengkap (mis. *Selasa, 3 November 2026*), dan umur pemeliharaan (hari/minggu/bulan) |
| **Google Drive** | Hubungkan akun Google, backup otomatis setiap ada perubahan, backup manual, pulihkan, putuskan |
| **Profil** | Ubah logo, nama usaha, alamat, no. HP, ganti password, keluar |

Backup tersimpan di Google Drive pengguna:
`Budidaya Ikan (Aplikasi)/budidaya_<username>.json` — jadi satu akun Google bisa menyimpan beberapa username.

---

## 1. Build APK

### Cara A – Android Studio (paling mudah)
1. Install **Android Studio** (versi terbaru).
2. *File → Open* → pilih folder `BudidayaIkan`.
3. Tunggu proses *Gradle Sync* selesai (butuh internet).
4. *Build → Build App Bundle(s) / APK(s) → Build APK(s)*.
5. File APK ada di `app/build/outputs/apk/debug/app-debug.apk` → kirim ke HP dan install.

### Cara B – GitHub (tanpa install Android Studio)
1. Buat repository baru di GitHub, upload seluruh isi folder ini.
2. Buka tab **Actions** → workflow **Build APK** berjalan otomatis.
3. Setelah selesai (±5 menit), unduh **BudidayaIkan-APK** di bagian *Artifacts*.

---

## 2. Aktifkan Google Drive (wajib agar tombol "Hubungkan Google Drive" berfungsi)

Lakukan sekali saja di **Google Cloud Console** (https://console.cloud.google.com):

1. **Buat project** baru, mis. `Budidaya Ikan`.
2. Menu **APIs & Services → Library** → cari **Google Drive API** → **Enable**.
3. Menu **APIs & Services → OAuth consent screen** (Google Auth Platform):
   - User type: **External**
   - Isi nama aplikasi, email dukungan, email developer.
   - Scopes: tambahkan `.../auth/drive.file`
   - **Test users**: tambahkan alamat Gmail yang akan dipakai login (selama status masih *Testing*).
4. Menu **Credentials → Create credentials → OAuth client ID**:
   - Application type: **Android**
   - Package name: `com.budidaya.ikan`
   - SHA-1 certificate fingerprint:
     ```
     D9:4E:25:3B:EB:C4:4B:A2:EC:E3:DB:86:67:AB:CE:94:54:90:72:61
     ```
     (ini SHA-1 dari keystore `app/budidaya.keystore` yang sudah disertakan, dipakai untuk build debug & release)
5. Simpan. Tidak perlu memasukkan Client ID ke dalam kode — Google mencocokkan otomatis lewat package name + SHA-1.

> Jika muncul pesan *"Gagal terhubung ke Google"* atau kode error 10, biasanya SHA-1 / package name di Credentials belum sama, atau Gmail belum ditambahkan sebagai *Test user*.

---

## 3. Cara pakai

1. Buka aplikasi → **Daftar** → upload logo dari galeri, isi nama usaha, alamat lengkap, username & password.
2. Di beranda, ketuk **Hubungkan** (Google Drive) → pilih akun Gmail → izinkan.
3. Ketuk **Tambah Kolam** → isi nomor kolam, jenis ikan, jumlah ekor, berat (kg), stok pakan (karung), tanggal pembenihan.
4. Setiap kali data disimpan, backup ke Google Drive berjalan otomatis.
5. **Ganti HP?** Install aplikasi → di layar Masuk isi username → **Pulihkan dari Google Drive** → pilih Gmail yang sama → lalu masuk dengan password lama.

---

## Catatan teknis

- Kotlin + Jetpack Compose (Material 3), minSdk 26 (Android 8.0), targetSdk 35.
- Data lokal: file JSON per username di penyimpanan internal; password disimpan sebagai hash SHA-256 + salt.
- Google Drive: Google Identity `AuthorizationClient` (scope `drive.file`, hanya bisa mengakses file buatan aplikasi ini) + Drive REST API v3.
- Keystore `app/budidaya.keystore` (password `budidaya123`) hanya untuk memudahkan. **Sebelum rilis ke Play Store, buat keystore sendiri yang dirahasiakan**, lalu daftarkan SHA-1 barunya di Google Cloud.
- Struktur kode:
  ```
  app/src/main/java/com/budidaya/ikan/
  ├── MainActivity.kt        – login Google / izin Drive
  ├── AppViewModel.kt        – logika aplikasi & sinkronisasi
  ├── data/                  – model Kolam & Profil, akun, penyimpanan
  ├── drive/DriveApi.kt      – upload / download Google Drive
  ├── util/                  – format tanggal & angka Indonesia, olah logo
  └── ui/                    – layar Masuk, Daftar, Beranda, Kolam, Profil
  ```
