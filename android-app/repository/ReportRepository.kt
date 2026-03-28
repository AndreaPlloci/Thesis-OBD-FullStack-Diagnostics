package com.andreaplloci.thesisobdapp.repository

import com.andreaplloci.thesisobdapp.ReportRequest
import com.andreaplloci.thesisobdapp.RetrofitClient

// URL mascherato per protezione privacy
private const val WEBHOOK_URL = "https://YOUR_PRIVATE_SERVER.ts.net/webhook/YOUR_WEBHOOK_PATH"

class ReportRepository {
    suspend fun sendReport(report: ReportRequest): Boolean {
        return RetrofitClient.instance.sendReport(WEBHOOK_URL, report).isSuccessful
    }
}
