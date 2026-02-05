package com.example.automotive.obdapp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface N8nApiService {
    @POST
    suspend fun sendReport(@Url url: String, @Body request: ReportRequest): Response<Unit>
}
