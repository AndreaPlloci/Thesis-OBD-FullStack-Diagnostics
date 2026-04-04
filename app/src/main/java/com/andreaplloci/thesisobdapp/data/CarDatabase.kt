package com.andreaplloci.thesisobdapp.data

val lingue = listOf("Italiano", "English", "Deutsch")
val marche = listOf("Audi", "Alfa Romeo")
val modelliPerMarca = mapOf(
    "Audi" to listOf("A3"),
    "Alfa Romeo" to listOf("159", "Giulia")
)
val databaseAuto = mapOf(
    "A3" to mapOf(
        "1.6 TDI (105/110cv)" to listOf("Manuale (5/6m)", "S-tronic (7m)"),
        "2.0 TDI (140/150/170/184cv)" to listOf("Manuale (6m)", "S-tronic (6m)"),
        "1.4 TFSI (122/125/150cv)" to listOf("Manuale (6m)", "S-tronic (7m)"),
        "1.8 TFSI (160/180cv)" to listOf("Manuale (6m)", "S-tronic (7m)"),
        "2.0 TFSI (S3 - 200/220/300cv)" to listOf("Manuale", "S-tronic"),
        "RS3 2.5 TFSI" to listOf("S-tronic (7m)")
    ),
    "159" to mapOf(
        "1.9 JTDM 8v (120cv)" to listOf("Manuale (6m)"),
        "1.9 JTDM 16v (150cv)" to listOf("Manuale (6m)", "Q-Tronic (6m)"),
        "2.0 JTDM (170cv)" to listOf("Manuale (6m)"),
        "2.4 JTDM (200/210cv)" to listOf("Manuale (6m)", "Q-Tronic (6m)"),
        "1.750 TBi (200cv)" to listOf("Manuale (6m)"),
        "1.9 JTS (160cv)" to listOf("Manuale (6m)"),
        "2.2 JTS (185cv)" to listOf("Manuale (6m)", "Selespeed"),
        "3.2 V6 JTS (260cv)" to listOf("Manuale (6m)", "Q-Tronic (6m)")
    ),
    "Giulia" to mapOf(
        "2.0 Turbo (200cv)" to listOf("ZF 8HP (8m)"),
        "2.0 Turbo (280cv)" to listOf("ZF 8HP (8m)"),
        "2.2 JTDM (150/160cv)" to listOf("Manuale (6m)", "ZF 8HP (8m)"),
        "2.2 JTDM (180/190cv)" to listOf("Manuale (6m)", "ZF 8HP (8m)"),
        "2.2 JTDM (210cv)" to listOf("ZF 8HP (8m)"),
        "2.9 V6 Quadrifoglio (510cv)" to listOf("Manuale (6m)", "ZF 8HP (8m)")
    )
)
