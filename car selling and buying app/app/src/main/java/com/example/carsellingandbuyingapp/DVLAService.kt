package com.example.carsellingandbuyingapp

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DVLAService {
    @POST("vehicle-enquiry/v1/vehicles")
    suspend fun getVehicleData(
        @Header("x-api-key") apiKey: String,
        @Body payload: Map<String, String>
    ): VehicleData
}
