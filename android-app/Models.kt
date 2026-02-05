package com.example.automotive.obdapp

data class ReportRequest(
    val email: String,
    val lingua: String,
    val marca: String,
    val modello: String,
    val anno: String,
    val motore: String,
    val cambio: String,
    val kmAttuali: String,
    val storicoLavori: List<MaintenanceWork>,
    val datiDallaCentralina: ObdData
)

data class MaintenanceWork(val km: String = "", val descrizione: String = "")
data class ObdData(val voltaggioBatteria: String = "N/D", val codiciErrore: List<String> = listOf("P0000"))
