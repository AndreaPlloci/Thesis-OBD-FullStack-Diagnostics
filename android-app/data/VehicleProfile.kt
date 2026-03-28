package com.andreaplloci.thesisobdapp.data

import com.andreaplloci.thesisobdapp.MaintenanceWork

data class VehicleProfile(
    val lingua: String,
    val marca: String,
    val modello: String,
    val anno: String,
    val motore: String,
    val cambio: String,
    val email: String,
    val storicoLavori: List<MaintenanceWork>
) {
    val displayName: String get() = "$marca $modello ($anno) — $motore"
}
