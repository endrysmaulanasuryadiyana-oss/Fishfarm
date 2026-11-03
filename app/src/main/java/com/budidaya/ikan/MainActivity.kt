package com.budidaya.ikan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.budidaya.ikan.drive.DriveApi
import com.budidaya.ikan.ui.AplikasiBudidaya
import com.budidaya.ikan.ui.TemaBudidaya
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    /** Hasil layar pilih akun / izin Google. */
    private val peluncurIzin = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { hasil ->
        val dataIntent = hasil.data
        if (hasil.resultCode != RESULT_OK || dataIntent == null) {
            vm.onDriveGagal("Login Google dibatalkan")
            return@registerForActivityResult
        }
        try {
            val r = Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(dataIntent)
            val token = r.accessToken
            if (token != null) vm.onTokenDrive(token) else vm.onDriveGagal("Token Google tidak diterima")
        } catch (e: ApiException) {
            vm.onDriveGagal("Gagal login Google (kode ${e.statusCode})")
        }
    }

    private val otorisasi: (Boolean) -> Unit = { interaktif -> mintaTokenDrive(interaktif) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        vm.mintaOtorisasiDrive = otorisasi
        setContent {
            TemaBudidaya {
                AplikasiBudidaya(vm)
            }
        }
    }

    override fun onDestroy() {
        if (vm.mintaOtorisasiDrive === otorisasi) vm.mintaOtorisasiDrive = null
        super.onDestroy()
    }

    /**
     * Meminta access token Google Drive (scope drive.file).
     * Jika pengguna belum pernah memberi izin, tampilkan layar pilih akun Google.
     */
    private fun mintaTokenDrive(interaktif: Boolean) {
        val permintaan = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveApi.SCOPE)))
            .build()
        Identity.getAuthorizationClient(this)
            .authorize(permintaan)
            .addOnSuccessListener { r ->
                if (r.hasResolution()) {
                    val pi = r.pendingIntent
                    when {
                        !interaktif -> vm.onDrivePerluLogin()
                        pi != null -> peluncurIzin.launch(IntentSenderRequest.Builder(pi.intentSender).build())
                        else -> vm.onDriveGagal("Tidak dapat membuka login Google")
                    }
                } else {
                    val token = r.accessToken
                    if (token != null) vm.onTokenDrive(token) else vm.onDriveGagal("Token Google kosong")
                }
            }
            .addOnFailureListener { e ->
                vm.onDriveGagal("Gagal terhubung ke Google: ${e.message ?: "periksa pengaturan OAuth"}")
            }
    }
}
