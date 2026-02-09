package com.example.automotive.obdapp

// Definizione dei modelli dati per lo scambio con il backend AI
data class ObdData(
    val voltaggioBatteria: String = "0V",
    val codiciErrore: List<String> = emptyList(),
    val tempLiquidoRaffreddamento: String = "0°C",   // ECT (Engine Coolant Temp)
    val massaAria: String = "0.0 g/s",              // MAF (Mass Air Flow)
    val caricoMotore: String = "0%",                // Engine Load
    val tempAspirazione: String = "0°C",            // IAT (Intake Air Temp)
    val pressioneRail: String = "0 bar"             // Fuel Rail Pressure
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
