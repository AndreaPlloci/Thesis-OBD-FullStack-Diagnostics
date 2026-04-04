package com.andreaplloci.thesisobdapp.repository

import com.andreaplloci.thesisobdapp.ReportRequest
import com.andreaplloci.thesisobdapp.RetrofitClient

private const val WEBHOOK_URL = "https://jarvis.tail34577a.ts.net/webhook/tesi-obd-andrea"

class ReportRepository {
    suspend fun sendReport(report: ReportRequest): Boolean {
        return RetrofitClient.instance.sendReport(WEBHOOK_URL, report).isSuccessful
    }
}
