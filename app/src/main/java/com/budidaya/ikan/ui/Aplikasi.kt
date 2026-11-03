package com.budidaya.ikan.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budidaya.ikan.AppViewModel
import com.budidaya.ikan.Layar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun AplikasiBudidaya(vm: AppViewModel) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        snapshotFlow { vm.pesan }
            .filterNotNull()
            .collectLatest { teks ->
                vm.pesan = null
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(teks)
            }
    }

    val layar = vm.layar
    val data = vm.data
    BackHandler(enabled = layar != Layar.Masuk && layar != Layar.Beranda) { vm.kembali() }

    Box(Modifier.fillMaxSize()) {
        when {
            layar == Layar.Daftar -> LayarDaftar(vm)
            data == null -> LayarMasuk(vm)
            else -> when (layar) {
                Layar.Beranda, Layar.Masuk, Layar.Daftar -> LayarBeranda(vm, data)
                is Layar.FormKolam -> LayarFormKolam(vm, layar.id)
                is Layar.DetailKolam -> LayarDetailKolam(vm, layar.id)
                Layar.Profil -> LayarProfil(vm, data)
            }
        }

        if (vm.sedangSync) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .align(Alignment.TopCenter)
            )
        }
        SnackbarHost(
            snackbar,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
        )
    }
}
