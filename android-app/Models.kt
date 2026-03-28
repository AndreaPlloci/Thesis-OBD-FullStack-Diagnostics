package com.andreaplloci.thesisobdapp

// Questo file deve contenere SOLO le definizioni dei dati
data class ObdData(
    val voltaggioBatteria: String = "0V",
    val codiciErrore: List<String> = emptyList(),
    val tempLiquidoRaffreddamento: String = "0°C",   // ECT
    val massaAria: String = "0.0 g/s",              // MAF
    val caricoMotore: String = "0%",                // Load
    val tempAspirazione: String = "0°C",            // IAT
    val pressioneRail: String = "0 bar"             // Fuel Rail
)

data class MaintenanceWork(
    val km: String = "",
    val descrizione: String = ""
)

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
